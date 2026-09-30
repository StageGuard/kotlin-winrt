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
    private val definitionsByType = ConcurrentCacheMap<KClass<*>, WinRTXamlTypeDefinition>()
    private val collectionDefinitions = ConcurrentCacheMap<String, WinRTXamlCollectionDefinition>()
    private data class EnumType(val type: KClass<*>, val parse: (String) -> Any)
    private val enumTypes = ConcurrentCacheMap<String, EnumType>()

    fun registerEnum(type: KClass<*>, name: String, parse: (String) -> Any) {
        enumTypes.putIfAbsent(name, EnumType(type, parse))
    }

    fun registerDefinition(definition: WinRTXamlTypeDefinition) {
        definitions.putIfAbsent(definition.name, definition)
        definitionsByType.putIfAbsent(definition.type, definition)
        definition.members.values.forEach { member ->
            member.collection?.let { collectionDefinitions.putIfAbsent(member.typeName, it) }
        }
    }

    fun register(type: KClass<*>, name: String, baseName: String) {
        require(name != baseName) { "An authored type cannot derive from itself: $name" }
        types.putIfAbsent(name, Type(type, name, baseName))
    }

    fun clearForTests() {
        types.clear(); definitions.clear(); definitionsByType.clear(); collectionDefinitions.clear(); enumTypes.clear()
    }

    /** Reuses generated accessors for CsWinRT's source-generated ICustomProperty path.
     * No reflection or platform-specific property discovery is required. */
    fun customProperty(source: Any, name: String): microsoft.ui.xaml.data.ICustomProperty? {
        var definition = definitionsByType[source::class]
        while (definition != null) {
            val member = definition.members[name]
            if (member != null) return WinRTBindableCustomProperty(
                canRead = true, canWrite = member.set != null, name = name, type = member.type,
                getValueCallback = { member.get(requireNotNull(it)) },
                setValueCallback = member.set?.let { setter -> { target, value -> setter(requireNotNull(target), value) } },
            )
            definition = definitions[definition.baseName]
        }
        return null
    }

    /** Returns an owned IXamlType pointer, or null when this is not an authored type. */
    fun tryCreate(name: String, resolveType: (String) -> RawAddress): RawAddress {
        enumTypes[name]?.let { return createSystemType(name, it.type, it.parse) }
        // XBF refers to member types by name before asking IXamlMember.Type.
        // Generated XamlTypeInfo registers closed collection types in the same
        // lookup table as authored classes, preserving their typed Add helper.
        collectionDefinitions[name]?.let { return createCollectionType(name, it, resolveType) }
        val type = types[name] ?: return PlatformAbi.nullPointer
        val definition = definitions[name]
        val baseName = definition?.baseName ?: type.baseName
        if (FeatureSwitches.traceCcw) {
            println("winrt-xaml-metadata: authored type=$name definition=${definition != null}")
        }
        fun resolveBase(): RawAddress {
            val authored = tryCreate(baseName, resolveType)
            if (!PlatformAbi.isNull(authored)) return authored
            val sdkType = resolveType(baseName)
            if (!PlatformAbi.isNull(sdkType)) return sdkType
            return definition?.baseType?.let { createSystemType(baseName, it) } ?: PlatformAbi.nullPointer
        }
        // Generated XamlTypeInfo includes system-type entries when the SDK provider omits them.
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
                    // CSharpTypeInfoPass2 XamlUserType owns its BoxedType. A
                    // normal authored reference type is not a box of its SDK base.
                    output { PlatformAbi.writePointer(it, PlatformAbi.nullPointer) }, // BoxedType
                    output { TypeProjection.copyTo(type.type, it) },
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) { args ->
                        PlatformAbi.writePointer(args[0] as RawAddress, PlatformAbi.nullPointer)
                        val activate = definition?.activate
                        if (activate == null) KnownHResults.E_NOTIMPL.value else {
                            if (FeatureSwitches.traceCcw) println("winrt-xaml-metadata: activate $name")
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
                        if (FeatureSwitches.traceCcw) {
                            println("winrt-xaml-metadata: member $name.$memberName found=${member != null}")
                        }
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
                    definition?.initializer?.let { initializer -> WinRTInspectableMethodDefinition(ComMethodSignature.of()) {
                        initializer(); KnownHResults.S_OK.value
                    } } ?: inherited(24, 0), // RunInitializer
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
        fun resolve(name: String, fallbackType: KClass<*>? = null): RawAddress {
            val authored = tryCreate(name, resolveType)
            if (!PlatformAbi.isNull(authored)) return authored
            val sdkType = resolveType(name)
            if (!PlatformAbi.isNull(sdkType)) return sdkType
            return fallbackType?.let { createSystemType(name, it,
                WinRTTypeClassifier.classify(it)?.xamlLiteralParser) } ?: PlatformAbi.nullPointer
        }
        val marshaler = MarshalInspectable.any()
        val host = WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(
                interfaceId = WinUiXamlInterfaceIds.IXamlMember,
                methods = listOf(
                    output { PlatformAbi.writeInt8(it, if (member.isAttachable) 1 else 0) }, // IsAttachable
                    output { PlatformAbi.writeInt8(it, if (member.isDependencyProperty) 1 else 0) },
                    output { PlatformAbi.writeInt8(it, if (member.set == null) 1 else 0) },
                    output { PlatformAbi.writePointer(it, HString.create(member.name).handle) },
                    output { PlatformAbi.writePointer(it, resolve(owner.name)) },
                    output { PlatformAbi.writePointer(it, member.collection?.let { collection ->
                        createCollectionType(member.typeName, collection, resolveType)
                    } ?: resolve(member.typeName, member.type)) },
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
                            if (FeatureSwitches.traceCcw) println("winrt-xaml-metadata: set ${owner.name}.${member.name}")
                            setter(requireNotNull(marshaler.fromAbi(args[0])), marshaler.fromAbi(args[1]))
                            KnownHResults.S_OK.value
                        }
                    },
                ),
            )), defaultInterfaceId = WinUiXamlInterfaceIds.IXamlMember,
        )
        return host.detachReference(WinUiXamlInterfaceIds.IXamlMember)
    }

    private fun createCollectionType(name: String, collection: WinRTXamlCollectionDefinition,
        resolveType: (String) -> RawAddress): RawAddress {
        fun output(write: (RawAddress) -> Unit) = WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) {
            write(it[0] as RawAddress); KnownHResults.S_OK.value
        }
        fun pointer(value: () -> RawAddress) = output { PlatformAbi.writePointer(it, value()) }
        fun boolean(value: Boolean = false) = output { PlatformAbi.writeInt8(it, if (value) 1 else 0) }
        fun unavailable(signature: ComMethodSignature) = WinRTInspectableMethodDefinition(signature) { KnownHResults.E_NOTIMPL.value }
        fun itemType(): RawAddress {
            val authored = tryCreate(collection.itemTypeName, resolveType)
            if (!PlatformAbi.isNull(authored)) return authored
            val projected = resolveType(collection.itemTypeName)
            return if (!PlatformAbi.isNull(projected)) projected else createSystemType(collection.itemTypeName, collection.itemType)
        }
        val marshaler = MarshalInspectable.any()
        val host = WinRTInspectableComObject(interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(
            interfaceId = WinUiXamlInterfaceIds.IXamlType,
            methods = listOf(
                pointer { PlatformAbi.nullPointer }, // BaseType
                pointer { PlatformAbi.nullPointer }, // ContentProperty
                pointer { HString.create(name).handle },
                boolean(), boolean(true), boolean(), boolean(), boolean(), boolean(),
                pointer(::itemType), pointer { PlatformAbi.nullPointer }, pointer { PlatformAbi.nullPointer },
                output { TypeProjection.copyMetadataNameTo(name, it) },
                unavailable(ComMethodSignatures.HResult_Ptr), // ActivateInstance
                unavailable(ComMethodSignatures.HResult_Ptr_Ptr), // CreateFromString
                unavailable(ComMethodSignatures.HResult_Ptr_Ptr), // GetMember
                WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr_Ptr) { args ->
                    collection.add(requireNotNull(marshaler.fromAbi(args[0])), marshaler.fromAbi(args[1]))
                    KnownHResults.S_OK.value
                },
                unavailable(ComMethodSignature.of(ComAbiValueKind.Pointer, ComAbiValueKind.Pointer, ComAbiValueKind.Pointer)),
                WinRTInspectableMethodDefinition(ComMethodSignature.of()) { KnownHResults.S_OK.value },
            ),
        )), defaultInterfaceId = WinUiXamlInterfaceIds.IXamlType)
        return host.detachReference(WinUiXamlInterfaceIds.IXamlType)
    }

    /** XamlCompiler's XamlSystemBaseType for a projected type absent from SDK metadata providers. */
    private fun createSystemType(name: String, type: KClass<*>, parse: ((String) -> Any)? = null): RawAddress {
        fun pointer(value: () -> RawAddress) = WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) {
            PlatformAbi.writePointer(it[0] as RawAddress, value()); KnownHResults.S_OK.value
        }
        fun boolean(value: Boolean = false) = WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) {
            PlatformAbi.writeInt8(it[0] as RawAddress, if (value) 1 else 0); KnownHResults.S_OK.value
        }
        fun unavailable(signature: ComMethodSignature) = WinRTInspectableMethodDefinition(signature) {
            KnownHResults.E_NOTIMPL.value
        }
        val host = WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(
                interfaceId = WinUiXamlInterfaceIds.IXamlType,
                methods = listOf(
                    pointer { PlatformAbi.nullPointer }, // BaseType
                    pointer { PlatformAbi.nullPointer }, // ContentProperty
                    pointer { HString.create(name).handle }, // FullName
                    boolean(), // IsArray
                    boolean(), // IsCollection
                    boolean(), // IsConstructible
                    boolean(), // IsDictionary
                    boolean(), // IsMarkupExtension
                    boolean(), // IsBindable
                    pointer { PlatformAbi.nullPointer }, // ItemType
                    pointer { PlatformAbi.nullPointer }, // KeyType
                    pointer { PlatformAbi.nullPointer }, // BoxedType
                    WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr) {
                        TypeProjection.copyTo(type, it[0] as RawAddress); KnownHResults.S_OK.value
                    }, // UnderlyingType
                    unavailable(ComMethodSignatures.HResult_Ptr), // ActivateInstance
                    parse?.let { parser -> WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Ptr_Ptr) { args ->
                        val input = HString.fromHandle(args[0] as RawAddress, owner = false).use { it.toKString() }
                        PlatformAbi.writePointer(args[1] as RawAddress, WinRTObjectMarshaller.fromManaged(parser(input)))
                        KnownHResults.S_OK.value
                    } } ?: unavailable(ComMethodSignatures.HResult_Ptr_Ptr), // CreateFromString
                    unavailable(ComMethodSignatures.HResult_Ptr_Ptr), // GetMember
                    unavailable(ComMethodSignatures.HResult_Ptr_Ptr), // AddToVector
                    unavailable(ComMethodSignature.of(ComAbiValueKind.Pointer, ComAbiValueKind.Pointer, ComAbiValueKind.Pointer)), // AddToMap
                    unavailable(ComMethodSignature.of()), // RunInitializer
                ),
            )),
            defaultInterfaceId = WinUiXamlInterfaceIds.IXamlType,
        )
        return host.detachReference(WinUiXamlInterfaceIds.IXamlType)
    }
}
