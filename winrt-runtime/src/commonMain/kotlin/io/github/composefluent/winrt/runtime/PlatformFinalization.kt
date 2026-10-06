package io.github.composefluent.winrt.runtime

internal expect object PlatformFinalization {
    fun drain()

    /** Collect managed objects without draining apartment-bound COM releases. */
    fun collectForReferenceTracking()
}

/**
 * COM releases that finalizers hand to a thread that waits for them.
 *
 * The CLR pumps COM calls while a single-threaded apartment waits in
 * `GC.WaitForPendingFinalizers`, so the finalizer thread of CsWinRT can release an object whose
 * last release calls into that apartment (`ObjectReference.Dispose` just releases). Kotlin/Native
 * has no such wait: `GC.collect()` blocks its caller until the cleaners have run. A cleaner that
 * released such an object would wait for the apartment, and the apartment for the cleaner. The
 * thread that waits takes the releases instead and runs them when the collection has returned,
 * as [ReferenceTrackerManager] does for the collection of a reference tracking pass.
 */
internal object WaitedFinalizerReleases {
    private val lock = PlatformLock()
    private var waiters = 0
    private val releases = mutableListOf<() -> Unit>()

    /** Queues [release] when a thread waits for the finalizers; `false` when none does. */
    fun defer(release: () -> Unit): Boolean = lock.withLock {
        if (waiters == 0) return@withLock false
        releases += release
        true
    }

    /** Runs [collect], which waits for the finalizers, and then the releases they deferred. */
    fun runAfter(collect: () -> Unit) {
        lock.withLock { waiters++ }
        try {
            collect()
        } finally {
            val deferred = lock.withLock {
                waiters--
                // Another waiting thread may be in an apartment that the releases must not enter
                // from here either; whoever is last runs them.
                if (waiters == 0) releases.toList().also { releases.clear() } else emptyList()
            }
            var failure: Throwable? = null
            deferred.forEach { release ->
                // A failed release must not strand the others.
                try {
                    release()
                } catch (error: Throwable) {
                    failure?.addSuppressed(error) ?: run { failure = error }
                }
            }
            failure?.let { throw it }
        }
    }
}
