@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class)

package io.github.composefluent.winrt.runtime

import kotlin.native.runtime.GC

internal actual object PlatformFinalization {
    actual fun collectForReferenceTracking() {
        // Native's collect waits for cleaners too. ReferenceTrackerManager defers their
        // COM releases until ReferenceTrackingCompleted has unlocked the XAML graph.
        GC.collect()
    }

    actual fun drain() {
        // collect() waits for the cleaners, without serving the COM calls of this apartment: the
        // cleaners leave their releases to this thread.
        WaitedFinalizerReleases.runAfter { GC.collect() }
        drainDeferredComReleasesForCurrentContext()
    }
}
