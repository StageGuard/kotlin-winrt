package io.github.composefluent.winrt.runtime

import kotlin.reflect.KClass

/**
 * Type identity for code-created authored controls. CsWinRT supplies the CCW identity;
 * the Windows XAML compiler normally supplies IXamlType's FullName/UnderlyingType/BaseType.
 * Without that second contract WinUI MetadataAPI::GetClassInfoFromObject_Helper falls
 * back to the internal Control type for derived WinUI controls such as SelectorBarItem.
 * Generated definitions optionally supply activation and custom member accessors.
 * Metadata generation and markup interpretation remain compiler responsibilities.
 */
internal object WinUiAuthoredTypeMetadata {
    private data class Type(val type: KClass<*>, val name: String, val baseName: String)
    private val types = ConcurrentCacheMap<String, Type>()
    private val definitions = ConcurrentCacheMap<String, WinRTXamlTypeDefinition>()

    fun registerDefinition(definition: WinRTXamlTypeDefinition) {
        definitions.putIfAbsent(definition.name, definition)
    }

    fun register(type: KClass<*>, name: String, baseName: String) {
        require(name != baseName) { "An authored type cannot derive from itself: $name" }
        types.putIfAbsent(name, Type(type, name, baseName))
    }

    fun clearForTests() { types.clear(); definitions.clear() }

    /** Returns an owned IXamlType pointer, or null when this is not an authored type. */
    fun tryCreate(name: String, resolveType: (String) -> RawAddress): RawAddress {
        val type = types[name] ?: return PlatformAbi.nullPointer
        val definition = definitions[name]
        fun resolveBase(): RawAddress {
            val authored = tryCreate(type.baseName, resolveType)
            return if (PlatformAbi.isNull(authored)) resolveType(type.baseName) else authored
        }
        // Do not advertise a type whose native base cannot be resolved by the SDK provider.
        val base = resolveBase()
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
            val pointer = resolveBase()
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
                    output { PlatformAbi.writePointer(it, resolveBase()) }, // BaseType
                    definition?.contentProperty?.let { member -> output {
                        PlatformAbi.writePointer(it, createMember(definition, definition.members.getValue(member), resolveType))
                    } } ?: inherited(7, 1), // ContentProperty
                    output { PlatformAbi.writePointer(it, HString.create(type.name).handle) },
                    inherited(9, 1), // IsArray
                    inherited(10, 1), // IsCollection
                    output { PlatformAbi.writeInt8(it, if (definition?.activate != null) 1 else 0) },
                    inherited(12, 1), // IsDictionary
                    inherited(13, 1), // IsMarkupExtension
                    output { PlatformAbi.writeInt8(it, 0) }, // No generated binding members
                    inherited(15, 1), // ItemType
                    inherited(16, 1), // KeyType
                    inherited(17, 1), // BoxedType
                    output { TypeProjection.copyTo(type.type, it) },
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) { args ->
                        PlatformAbi.writePointer(args[0] as RawAddress, PlatformAbi.nullPointer)
                        val activate = definition?.activate
                        if (activate == null) KnownHResults.E_NOTIMPL.value else {
                            val instance = activate()
                            if (instance is WinRTXamlComponent) initializeWinRTXamlComponent(instance)
                            PlatformAbi.writePointer(args[0] as RawAddress, WinRTObjectMarshaller.fromManaged(instance))
                            KnownHResults.S_OK.value
                        }
                    }, // Never substitute native base activation for an authored constructor.
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr_Ptr) { args ->
                        PlatformAbi.writePointer(args[1] as RawAddress, PlatformAbi.nullPointer)
                        KnownHResults.E_NOTIMPL.value
                    }, // CreateFromString
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr_Ptr) { args ->
                        val memberName = HString.fromHandle(args[0] as RawAddress, owner = false).use { it.toKString() }
                        val member = definition?.members?.get(memberName)
                        if (definition != null && member != null) {
                            PlatformAbi.writePointer(args[1] as RawAddress, createMember(definition, member, resolveType))
                            KnownHResults.S_OK.value
                        } else {
                            val pointer = resolveBase()
                            if (PlatformAbi.isNull(pointer)) {
                                PlatformAbi.writePointer(args[1] as RawAddress, PlatformAbi.nullPointer)
                                KnownHResults.E_NOINTERFACE.value
                            } else IUnknownReference(pointer.asRawComPtr(), WinUiXamlInterfaceIds.IXamlType).use {
                                ComVtableInvoker.invokeArgs(it.pointer, 21, args[0] as RawAddress, args[1] as RawAddress)
                            }
                        }
                    },
                    inherited(22, 2), // AddToVector
                    inherited(23, 3), // AddToMap
                    inherited(24, 0), // RunInitializer
                ),
            )),
            defaultInterfaceId = WinUiXamlInterfaceIds.IXamlType,
        )
        return host.detachReference(WinUiXamlInterfaceIds.IXamlType)
    }

    private fun createMember(owner: WinRTXamlTypeDefinition, member: WinRTXamlMemberDefinition,
        resolveType: (String) -> RawAddress): RawAddress {
        fun output(write: (RawAddress) -> Unit) = WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) {
            write(it[0] as RawAddress); KnownHResults.S_OK.value
        }
        fun resolve(name: String): RawAddress {
            val authored = tryCreate(name, resolveType)
            return if (PlatformAbi.isNull(authored)) resolveType(name) else authored
        }
        val marshaler = MarshalInspectable.any()
        val host = WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(
                interfaceId = WinUiXamlInterfaceIds.IXamlMember,
                methods = listOf(
                    output { PlatformAbi.writeInt8(it, 0) }, // IsAttachable
                    output { PlatformAbi.writeInt8(it, if (member.isDependencyProperty) 1 else 0) },
                    output { PlatformAbi.writeInt8(it, if (member.set == null) 1 else 0) },
                    output { PlatformAbi.writePointer(it, HString.create(member.name).handle) },
                    output { PlatformAbi.writePointer(it, resolve(owner.name)) },
                    output { PlatformAbi.writePointer(it, resolve(member.typeName)) },
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr_Ptr) { args ->
                        val output = args[1] as RawAddress
                        PlatformAbi.writePointer(output, PlatformAbi.nullPointer)
                        val instance = requireNotNull(marshaler.fromAbi(args[0]))
                        marshaler.copyManaged(member.get(instance), output)
                        KnownHResults.S_OK.value
                    },
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr_Ptr) { args ->
                        val setter = member.set
                        if (setter == null) KnownHResults.E_NOTIMPL.value else {
                            setter(requireNotNull(marshaler.fromAbi(args[0])), marshaler.fromAbi(args[1]))
                            KnownHResults.S_OK.value
                        }
                    },
                ),
            )), defaultInterfaceId = WinUiXamlInterfaceIds.IXamlMember,
        )
        return host.detachReference(WinUiXamlInterfaceIds.IXamlMember)
    }
}
