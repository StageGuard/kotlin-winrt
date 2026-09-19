package io.github.composefluent.winrt.runtime

import kotlin.reflect.KClass

/**
 * Type identity for code-created authored controls. CsWinRT supplies the CCW identity;
 * the Windows XAML compiler normally supplies IXamlType's FullName/UnderlyingType/BaseType.
 * Without that second contract WinUI MetadataAPI::GetClassInfoFromObject_Helper falls
 * back to the internal Control type for derived WinUI controls such as SelectorBarItem.
 * This does not parse markup or provide markup activation/custom member generation.
 */
internal object WinUiAuthoredTypeMetadata {
    private data class Type(val type: KClass<*>, val name: String, val baseName: String)
    private val types = ConcurrentCacheMap<String, Type>()

    fun register(type: KClass<*>, name: String, baseName: String) {
        require(name != baseName) { "An authored type cannot derive from itself: $name" }
        types.putIfAbsent(name, Type(type, name, baseName))
    }

    fun clearForTests() = types.clear()

    /** Returns an owned IXamlType pointer, or null when this is not an authored type. */
    fun tryCreate(name: String, resolveType: (String) -> RawAddress): RawAddress {
        val type = types[name] ?: return PlatformAbi.nullPointer
        // Do not advertise a type whose native base cannot be resolved by the SDK provider.
        val base = resolveType(type.baseName)
        if (PlatformAbi.isNull(base)) return PlatformAbi.nullPointer
        WinRTPlatformApi.releaseRaw(base)

        fun output(write: (RawAddress) -> Unit) =
            WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) { args ->
                write(args[0] as RawAddress)
                KnownHResults.S_OK.value
            }

        fun inherited(slot: Int, arguments: Int) = WinRTInspectableMethodDefinition(
            ComMethodSignature.of(*Array(arguments) { ComAbiValueKind.Pointer }),
        ) { args ->
            val pointer = resolveType(type.baseName)
            if (PlatformAbi.isNull(pointer)) {
                KnownHResults.E_NOINTERFACE.value
            } else {
                IUnknownReference(pointer.asRawComPtr(), WinUiXamlInterfaceIds.IXamlType).use { reference ->
                    when (arguments) {
                        0 -> ComVtableInvoker.invoke(reference.pointer, slot)
                        1 -> ComVtableInvoker.invokeArgs(reference.pointer, slot, args[0] as RawAddress)
                        2 -> ComVtableInvoker.invokeArgs(reference.pointer, slot, args[0] as RawAddress, args[1] as RawAddress)
                        else -> ComVtableInvoker.invokeArgs(reference.pointer, slot, args[0] as RawAddress, args[1] as RawAddress, args[2] as RawAddress)
                    }
                }
            }
        }

        val host = WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(
                interfaceId = WinUiXamlInterfaceIds.IXamlType,
                methods = listOf(
                    output { PlatformAbi.writePointer(it, resolveType(type.baseName)) }, // BaseType
                    inherited(7, 1), // ContentProperty
                    output { PlatformAbi.writePointer(it, HString.create(type.name).handle) },
                    inherited(9, 1), // IsArray
                    inherited(10, 1), // IsCollection
                    output { PlatformAbi.writeInt8(it, 0) }, // IsConstructible: code-created only
                    inherited(12, 1), // IsDictionary
                    inherited(13, 1), // IsMarkupExtension
                    output { PlatformAbi.writeInt8(it, 0) }, // No generated binding members
                    inherited(15, 1), // ItemType
                    inherited(16, 1), // KeyType
                    inherited(17, 1), // BoxedType
                    output { TypeProjection.copyTo(type.type, it) },
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) { args ->
                        PlatformAbi.writePointer(args[0] as RawAddress, PlatformAbi.nullPointer)
                        KnownHResults.E_NOTIMPL.value
                    }, // ActivateInstance must never activate the base instead of the derived type
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr_Ptr) { args ->
                        PlatformAbi.writePointer(args[1] as RawAddress, PlatformAbi.nullPointer)
                        KnownHResults.E_NOTIMPL.value
                    }, // CreateFromString
                    inherited(21, 2), // GetMember
                    inherited(22, 2), // AddToVector
                    inherited(23, 3), // AddToMap
                    inherited(24, 0), // RunInitializer
                ),
            )),
            defaultInterfaceId = WinUiXamlInterfaceIds.IXamlType,
        )
        return host.detachReference(WinUiXamlInterfaceIds.IXamlType)
    }
}
