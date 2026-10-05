package io.github.composefluent.winrt.runtime

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Native-only counterpart of CLR's aggregated NativeObjectWrapper. Borrowed
 * interfaces share the factory's nondelegating inner reference without AddRef
 * on their own managed outer. Cleaner state never owns a page, a ComPtr, or a
 * ReferenceTrackerSource's managed graph.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class WinRTNativeObjectLifetime private constructor(
    private val owner: RawComObjectReferenceSupport,
) : AutoCloseable {
    private val references = AtomicInt(1)

    fun retain(): WinRTNativeObjectLifetime {
        while (true) {
            val current = references.load()
            if (current <= 0) throw WinRTObjectDisposedException("Native composable lifetime is closed.")
            check(current < Int.MAX_VALUE) { "Native composable lifetime reference count overflowed." }
            if (references.compareAndSet(current, current + 1)) return this
        }
    }

    fun initializeReferenceTracker(sourceOwner: ComPtr): Boolean =
        owner.tryInitializeReferenceTracker(
            trackerSourceOwner = sourceOwner,
            addRefFromTrackerSource = false,
            // CsWinRT Init balances the aggregated tracker QI immediately.
            // The nondelegating inner keeps this borrowed tracker alive.
            ownsTrackerPointer = false,
            retainTrackerPointer = {},
            addRefFromTrackerSourceCallback = { tracker ->
                ComVtableInvoker.invoke(tracker, ReferenceTrackerVftblSlots.AddRefFromTrackerSource)
            },
        )

    fun release(fromFinalizer: Boolean) {
        val remaining = references.addAndFetch(-1)
        check(remaining >= 0) { "Native composable lifetime was released more than once." }
        if (remaining != 0) return
        if (fromFinalizer) closeComPtrSupportFromFinalizer(owner) else closeComPtrSupport(owner)
    }

    override fun close() = release(fromFinalizer = false)

    companion object {
        /** Consumes the factory's owned inner reference, including failure. */
        fun create(inner: RawComPtr, afterRelease: () -> Unit): WinRTNativeObjectLifetime {
            var owner: RawComObjectReferenceSupport? = null
            return try {
                val support = RawComObjectReferenceSupport(
                    pointer = inner,
                    interfaceIdLowBits = IID.IInspectable.abiLowBits,
                    interfaceIdHighBits = IID.IInspectable.abiHighBits,
                    knownInterfaceId = IID.IInspectable,
                    afterRelease = afterRelease,
                )
                owner = support
                WinRTNativeObjectLifetime(support)
            } catch (failure: Throwable) {
                val support = owner
                if (support == null) {
                    WinRTPlatformApi.releaseRaw(inner.asRawAddress())
                    afterRelease()
                } else {
                    closeComPtrSupport(support)
                }
                throw failure
            }
        }
    }
}
