package io.github.composefluent.winrt.runtime

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class WinRTComposableObjectReference internal constructor(
    val instance: IInspectableReference,
    val inner: IInspectableReference?,
    private val composed: IInspectableReference?,
    val outer: ComObjectReference,
    val isAggregatedReferenceTrackerObject: Boolean,
    private val outerHost: WinRTInspectableComObject,
    private val cleanup: () -> Unit,
) : AutoCloseable {
    private val closed = AtomicInt(0)

    init {
        ActiveComposableObjectReferences.register(this)
    }

    internal fun tryCreateStaticCallLease(
        interfaceId: Guid,
        identityVerifiedManagedValue: Any,
    ): WinRTProjectionMarshaler? {
        val abi = outerHost.tryAcquireReference(interfaceId, identityVerifiedManagedValue) ?: return null
        return WinRTProjectionMarshaler.managed(abi, outerHost)
    }

    /**
     * Gets an owned reference for an interface implemented by the composed native
     * identity.  Derived projected classes must use this path for public base
     * interfaces; the managed outer host only exposes the authored override
     * interfaces and is not a valid substitute for those interfaces.
     */
    internal fun tryCreateNativeCallMarshaler(interfaceId: Guid): WinRTProjectionMarshaler? {
        // CsWinRT exposes the factory's inner object as NativeObject for an
        // aggregated composable.  Query that identity first when a projected
        // value crosses an ABI boundary.
        val source = instance
        val result = source.queryInterface(interfaceId)
        return result.getOrNull()?.let(WinRTProjectionMarshaler::owned)
    }

    /**
     * Gets an owned reference for a public interface implemented by the composed
     * native identity.  This is the reference counterpart of
     * [tryCreateNativeCallMarshaler] for APIs that need to return a COM object
     * reference rather than a call marshaler.
     */
    internal fun tryCreateNativeCallReference(interfaceId: Guid): ComObjectReference? {
        // Public interfaces on an aggregated composable are obtained from the
        // factory's inner/native identity, matching CsWinRT NativeObject.
        val source = instance
        val result = source.queryInterface(interfaceId)
        return result.getOrNull()
    }

    @PublishedApi
    internal fun tryBorrowStaticCallAbi(interfaceId: Guid): RawAddress =
        // The host's cached table contains the authored override interfaces. For
        // inherited public interfaces (for example Layout on a custom
        // VirtualizingLayout), that table entry is the managed outer CCW and is
        // not the native composable instance that WinUI expects. Let the normal
        // unwrap/QI path resolve those interfaces from `instance` instead.
        if (interfaceId == outer.interfaceId) {
            outerHost.tryBorrowCachedInterfacePointer(interfaceId)
        } else {
            RawAddress.Null
        }

    override fun close() {
        if (!closed.compareAndSet(0, 1)) {
            return
        }
        try {
            instance.close()
        } finally {
            try {
                if (!isAggregatedReferenceTrackerObject && inner !== instance) {
                    inner?.close()
                }
            } finally {
                try {
                    composed?.close()
                } finally {
                    try {
                        outer.close()
                    } finally {
                        try {
                            cleanup()
                        } finally {
                            ActiveComposableObjectReferences.unregister(this)
                        }
                    }
                }
            }
        }
    }

    internal companion object {
        fun closeRuntimeReferences() {
            ActiveComposableObjectReferences.closeAll()
        }
    }
}

interface WinRTComposableObject {
    val winRTComposableObjectReference: WinRTComposableObjectReference?
}

private object ActiveComposableObjectReferences {
    private val lock = PlatformLock()
    private val references = mutableSetOf<WinRTComposableObjectReference>()

    fun register(reference: WinRTComposableObjectReference) {
        lock.withLock {
            references += reference
        }
    }

    fun unregister(reference: WinRTComposableObjectReference) {
        lock.withLock {
            references -= reference
        }
    }

    fun closeAll() {
        val snapshot = lock.withLock {
            references.toList()
        }
        closeAllAutoCloseables(snapshot.asReversed())
    }
}

/** Closes every resource while preserving the first failure and later failures as suppressed. */
internal fun closeAllAutoCloseables(resources: Iterable<AutoCloseable>) {
    var failure: Throwable? = null
    resources.forEach { resource ->
        try {
            resource.close()
        } catch (error: Throwable) {
            failure?.addSuppressed(error) ?: run { failure = error }
        }
    }
    failure?.let { throw it }
}
