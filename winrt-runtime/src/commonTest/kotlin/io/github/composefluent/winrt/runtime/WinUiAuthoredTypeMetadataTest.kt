package io.github.composefluent.winrt.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WinUiAuthoredTypeMetadataTest {
    private class DerivedControl

    @Test
    fun authored_control_exposes_its_identity_and_native_base_without_activating_the_base() {
        // CsWinRT CCW identity + WinUI IXamlType contract used by MetadataAPI's style checks.
        val base = WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(
                interfaceId = WinUiXamlInterfaceIds.IXamlType,
                methods = emptyList(),
            )),
            defaultInterfaceId = WinUiXamlInterfaceIds.IXamlType,
        )
        Projections.registerAuthoredRuntimeClassType(DerivedControl::class, "Test.DerivedControl", "Test.NativeControl")
        try {
            val pointer = WinUiAuthoredTypeMetadata.tryCreate("Test.DerivedControl") { name ->
                assertEquals("Test.NativeControl", name)
                base.acquireReference(WinUiXamlInterfaceIds.IXamlType)
            }
            IUnknownReference(pointer.asRawComPtr(), WinUiXamlInterfaceIds.IXamlType).use { type ->
                PlatformAbi.confinedScope().use { scope ->
                    val result = PlatformAbi.allocatePointerSlot(scope)
                    HResult(ComVtableInvoker.invokeArgs(type.pointer, 8, result)).requireSuccess()
                    HString.fromHandle(PlatformAbi.readPointer(result), owner = true).use {
                        assertEquals("Test.DerivedControl", it.toKString())
                    }
                    HResult(ComVtableInvoker.invokeArgs(type.pointer, 6, result)).requireSuccess()
                    IUnknownReference(PlatformAbi.readPointer(result).asRawComPtr()).use { actualBase ->
                        base.createReference(WinUiXamlInterfaceIds.IXamlType).use { expectedBase ->
                            assertEquals(expectedBase.pointer, actualBase.pointer)
                        }
                    }
                    HResult(ComVtableInvoker.invokeArgs(type.pointer, 11, result)).requireSuccess()
                    assertEquals(0, PlatformAbi.readInt8(result).toInt())
                    assertEquals(KnownHResults.E_NOTIMPL.value, ComVtableInvoker.invokeArgs(type.pointer, 19, result))
                    assertTrue(PlatformAbi.isNull(PlatformAbi.readPointer(result)))
                }
            }
            assertTrue(PlatformAbi.isNull(WinUiAuthoredTypeMetadata.tryCreate("Test.Unregistered") {
                error("Unknown types must be left to the SDK provider")
            }))
            assertTrue(PlatformAbi.isNull(WinUiAuthoredTypeMetadata.tryCreate("Test.DerivedControl") {
                PlatformAbi.nullPointer
            }))
        } finally {
            base.releaseManagedReference()
            WinUiAuthoredTypeMetadata.clearForTests()
        }
    }
}
