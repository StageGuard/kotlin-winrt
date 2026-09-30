package io.github.composefluent.winrt.runtime

/**
 * Per-instance tracking owned by generated compiled bindings. XamlCompiler's
 * CSharpPagePass2 Initialize/Update/StopTracking owns the same lifecycle;
 * CsWinRT EventSource owns the actual projected delegate and token mechanics.
 * The generated code supplies typed accessors and subscriptions, never paths.
 */
class WinRTXamlBindingState {
    private var active = false
    private var updating = false
    private val subscriptions = mutableListOf<() -> Unit>()

    fun initialize(update: (Boolean) -> Unit) {
        if (active) return
        active = true
        refresh(true, update)
    }

    fun update(update: (Boolean) -> Unit) {
        if (active) refresh(false, update)
    }

    /** Explicit Bindings.Update refreshes OneTime bindings too, as in CSharpPagePass2. */
    fun updateAll(update: (Boolean) -> Unit) {
        active = true
        refresh(true, update)
    }

    /** Target changes must not write back values while a source update is running. */
    fun changeTarget(change: () -> Unit, update: (Boolean) -> Unit) {
        if (!active || updating) return
        updating = true
        try { change() } finally { updating = false }
        refresh(false, update)
    }

    private fun refresh(initial: Boolean, update: (Boolean) -> Unit) {
        if (updating) return
        updating = true
        try {
            disconnect()
            update(initial)
        } catch (error: Throwable) {
            active = false
            try { disconnect() } catch (cleanupError: Throwable) { error.addSuppressed(cleanupError) }
            throw error
        } finally {
            updating = false
        }
    }

    fun stopTracking() {
        active = false
        disconnect()
    }

    private fun disconnect() {
        val pending = subscriptions.toList()
        subscriptions.clear()
        var failure: Throwable? = null
        pending.asReversed().forEach { remove ->
            try { remove() } catch (error: Throwable) {
                if (failure == null) failure = error else failure!!.addSuppressed(error)
            }
        }
        failure?.let { throw it }
    }

    private fun track(remove: () -> Unit) {
        if (active) subscriptions += remove else remove()
    }

    fun <Source : Any, Property : Any> trackProperty(
        source: Source,
        property: Property,
        token: Long,
        remove: (Source, Property, Long) -> Unit,
    ) = track { remove(source, property, token) }

    fun <Source : Any, Handler : Any> trackEvent(
        source: Source,
        handler: Handler,
        remove: (Source, Handler) -> Unit,
    ) = track { remove(source, handler) }
}

/** Mirrors the weak binding receiver used by XamlCompiler's generated tracking callbacks. */
fun <Target : Any, A, B> weakXamlBindingCallback(
    target: Target,
    invoke: (Target, A, B) -> Unit,
): (A, B) -> Unit {
    val reference = WeakReference(target)
    return { first, second -> reference.tryGetTarget()?.let { invoke(it, first, second) } }
}
