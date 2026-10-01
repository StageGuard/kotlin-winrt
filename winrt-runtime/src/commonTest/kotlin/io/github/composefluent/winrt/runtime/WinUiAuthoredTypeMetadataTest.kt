package io.github.composefluent.winrt.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WinUiAuthoredTypeMetadataTest {
    private class DerivedControl

    @Test
    fun dictionary_type_exposes_key_and_item_types_and_inserts_through_the_abi() {
        // XamlCompiler CSharpTypeInfoPass2.tt: XamlUserType.KeyType/ItemType/DictionaryAdd.
        val name = "Windows.Foundation.Collections.IMap`2<String,String>"
        val values = mutableMapOf<String, String>()
        WinUiAuthoredTypeMetadata.registerDefinition(WinRTXamlTypeDefinition(
            DerivedControl::class, "Test.DictionaryOwner", "System.Object",
            members = listOf(WinRTXamlMemberDefinition("Values", name, get = { values },
                dictionary = WinRTXamlDictionaryDefinition(MutableMap::class, "String", String::class,
                    "String", String::class, add = { instance, key, item ->
                        @Suppress("UNCHECKED_CAST")
                        (instance as MutableMap<String, String>)[key as String] = item as String
                    }))),
        ))
        try {
            val pointer = WinUiAuthoredTypeMetadata.tryCreate(name) { PlatformAbi.nullPointer }
            IUnknownReference(pointer.asRawComPtr(), WinUiXamlInterfaceIds.IXamlType).use { type ->
                PlatformAbi.confinedScope().use { scope ->
                    val result = PlatformAbi.allocatePointerSlot(scope)
                    HResult(ComVtableInvoker.invokeArgs(type.pointer, 10, result)).requireSuccess()
                    assertEquals(0, PlatformAbi.readInt8(result).toInt())
                    HResult(ComVtableInvoker.invokeArgs(type.pointer, 12, result)).requireSuccess()
                    assertEquals(1, PlatformAbi.readInt8(result).toInt())
                    for (slot in listOf(15, 16)) {
                        HResult(ComVtableInvoker.invokeArgs(type.pointer, slot, result)).requireSuccess()
                        IUnknownReference(PlatformAbi.readPointer(result).asRawComPtr(), WinUiXamlInterfaceIds.IXamlType).use { element ->
                            HResult(ComVtableInvoker.invokeArgs(element.pointer, 8, result)).requireSuccess()
                            HString.fromHandle(PlatformAbi.readPointer(result), owner = true).use {
                                assertEquals("String", it.toKString())
                            }
                        }
                    }
                    WinRTObjectMarshaller.createMarshaler(values).use { instance ->
                        WinRTObjectMarshaller.createMarshaler("key").use { key ->
                            WinRTObjectMarshaller.createMarshaler("value").use { item ->
                                HResult(ComVtableInvoker.invokeArgs(type.pointer, 23, instance.abi, key.abi, item.abi)).requireSuccess()
                            }
                        }
                    }
                    assertEquals(mapOf("key" to "value"), values)
                }
            }
        } finally {
            WinUiAuthoredTypeMetadata.clearForTests()
        }
    }

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
