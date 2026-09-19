package io.github.composefluent.winrt.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertSame
import kotlin.test.assertFailsWith

class InspectableReferenceTest {
    @Test
    fun owned_inspectable_cast_queries_the_declared_vtable_and_transfers_ownership() {
        // CsWinRT write_class initializes _inner through objRef.As(default IID).
        if (!PlatformRuntime.isWindows) return
        RuntimeScope.initializeSingleThreaded().use {
            ActivationFactory.get("Windows.Data.Json.JsonObject").use { factory ->
                val original = factory.activateInstance().use { it.asInspectable() }
                val typed = castOwnedInspectableReference(original, IID.IStringable)
                typed.use {
                    assertTrue(original.isDisposed)
                    assertEquals(IID.IStringable, typed.interfaceId)
                    assertEquals("{}", WinRTProjectionIntrinsic.getString(typed, 6))
                    assertSame(typed, castOwnedInspectableReference(typed, IID.IStringable))
                    assertFalse(typed.isDisposed)
                }
            }
        }
    }

    @Test
    fun owned_inspectable_cast_releases_input_when_query_fails() {
        if (!PlatformRuntime.isWindows) return
        RuntimeScope.initializeSingleThreaded().use {
            ActivationFactory.get("Windows.Data.Json.JsonObject").use { factory ->
                val original = factory.activateInstance()
                assertFailsWith<WinRTUnsupportedOperationException> {
                    castOwnedInspectableReference(original, Guid("00000000-0000-0000-0000-00000000DEAD"))
                }
                assertTrue(original.isDisposed)
            }
        }
    }

    @Test
    fun can_read_runtime_class_name_from_activation_factory_result() {
        if (!PlatformRuntime.isWindows) {
            return
        }

        RuntimeScope.initializeSingleThreaded().use {
            val factory = ActivationFactory.get("Windows.Data.Json.JsonObject")
            try {
                val inspectable = factory.activateInstance()
                inspectable.use { reference ->
                    val runtimeClass = reference.getRuntimeClassName()
                    assertEquals("Windows.Data.Json.JsonObject", runtimeClass)
                }
            } finally {
                factory.close()
            }
        }
    }

    @Test
    fun inspectable_reference_can_compare_identity_across_iunknown_query() {
        if (!PlatformRuntime.isWindows) {
            return
        }

        RuntimeScope.initializeSingleThreaded().use {
            val factory = ActivationFactory.get("Windows.Data.Json.JsonObject")
            try {
                val inspectable = factory.activateInstance()
                inspectable.use { reference ->
                    reference.queryInterface(IID.IUnknown).getOrThrow().use { unknown ->
                        assertTrue(reference.sameIdentity(unknown))
                        assertTrue(unknown.sameIdentity(reference))
                    }
                }
            } finally {
                factory.close()
            }
        }
    }

    @Test
    fun inspectable_typed_view_matches_legacy_runtime_class_name_lookup() {
        if (!PlatformRuntime.isWindows) {
            return
        }

        RuntimeScope.initializeSingleThreaded().use {
            val factory = ActivationFactory.get("Windows.Data.Json.JsonObject")
            try {
                val inspectable = factory.activateInstance()
                inspectable.use { reference ->
                    val typedView = reference.asTypedView()
                    assertEquals("Windows.Data.Json.JsonObject", typedView.getRuntimeClassName())
                    assertEquals(reference.getRuntimeClassName(), typedView.getRuntimeClassName())
                }
            } finally {
                factory.close()
            }
        }
    }

    @Test
    fun inspectable_reference_returns_null_for_missing_interface_query() {
        if (!PlatformRuntime.isWindows) {
            return
        }

        RuntimeScope.initializeSingleThreaded().use {
            val factory = ActivationFactory.get("Windows.Data.Json.JsonObject")
            try {
                val inspectable = factory.activateInstance()
                inspectable.use { reference ->
                    val missing = reference.tryQueryInterface(Guid("00000000-0000-0000-0000-00000000DEAD"))
                    assertEquals(null, missing)
                }
            } finally {
                factory.close()
            }
        }
    }

    @Test
    fun inspectable_reference_is_idempotently_disposable_and_rejects_late_calls() {
        if (!PlatformRuntime.isWindows) {
            return
        }

        RuntimeScope.initializeSingleThreaded().use {
            val factory = ActivationFactory.get("Windows.Data.Json.JsonObject")
            try {
                val inspectable = factory.activateInstance()
                assertFalse(inspectable.isDisposed)
                inspectable.close()
                inspectable.close()
                assertTrue(inspectable.isDisposed)

                try {
                    inspectable.tryGetRuntimeClassName()
                    throw AssertionError("Expected disposed reference to reject calls")
                } catch (error: WinRTObjectDisposedException) {
                    assertTrue(error.message!!.contains("disposed"))
                }
            } finally {
                factory.close()
            }
        }
    }
}
