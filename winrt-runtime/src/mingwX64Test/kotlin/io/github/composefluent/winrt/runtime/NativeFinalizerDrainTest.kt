@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.concurrent.atomics.ExperimentalAtomicApi::class,
)

package io.github.composefluent.winrt.runtime

import kotlin.concurrent.atomics.AtomicInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import platform.windows.GetCurrentThreadId

class NativeFinalizerDrainTest {
    // CsWinRT's finalizer thread releases an agile object directly and relies on the CLR, which
    // serves the COM calls of an apartment that waits for finalizers. GC.collect() of
    // Kotlin/Native blocks without serving them, so the cleaner must leave the release to the
    // thread that drains: a last release that calls into that thread would never return.
    @Test
    fun drain_releases_an_abandoned_reference_on_the_draining_thread() = memScoped {
        releaseThread.store(0)
        val vtable = allocArray<COpaquePointerVar>(3)
        vtable[2] = staticCFunction(::recordReleaseThread)
        val instance = alloc<COpaquePointerVar>()
        instance.value = vtable
        abandonReference(RawAddress(instance.ptr.rawValue.toLong()))

        repeat(20) {
            PlatformFinalization.drain()
            if (releaseThread.load() != 0) return@repeat
            val pressure = List(128) { ByteArray(1024) }
            assertEquals(128, pressure.size)
        }

        assertEquals(GetCurrentThreadId().toInt(), releaseThread.load())
    }

    @Test
    fun releases_are_deferred_only_while_a_thread_waits() {
        val events = mutableListOf<String>()

        assertEquals(false, WaitedFinalizerReleases.defer { events += "outside" })
        WaitedFinalizerReleases.runAfter {
            assertEquals(true, WaitedFinalizerReleases.defer { events += "deferred" })
            events += "collected"
        }

        assertEquals(listOf("collected", "deferred"), events)
        assertEquals(false, WaitedFinalizerReleases.defer { events += "after" })
    }

    // Not inlined: the reference must not stay in a frame of the test.
    private fun abandonReference(pointer: RawAddress) {
        IInspectableReference(pointer.asRawComPtr(), IID.IInspectable)
    }
}

private val releaseThread = AtomicInt(0)

private fun recordReleaseThread(@Suppress("UNUSED_PARAMETER") instance: COpaquePointer?): UInt {
    releaseThread.store(GetCurrentThreadId().toInt())
    return 0u
}
