package io.github.composefluent.winrt.runtime

import kotlin.reflect.KClass

/** Generated XAML type information, corresponding to XamlUserType in CSharpTypeInfoPass2.tt. */
class WinRTXamlTypeDefinition(
    val type: KClass<*>,
    val name: String,
    val baseName: String,
    val baseType: KClass<*>? = null,
    val activate: (() -> Any)? = null,
    val contentProperty: String? = null,
    members: List<WinRTXamlMemberDefinition> = emptyList(),
    val initializer: (() -> Unit)? = null,
) {
    val members: Map<String, WinRTXamlMemberDefinition> = members.associateBy { it.name }

    init {
        require(name.isNotBlank() && baseName.isNotBlank() && name != baseName)
        require(this.members.size == members.size) { "Duplicate XAML member on $name" }
        require(contentProperty == null || contentProperty in this.members) {
            "Unknown XAML content property $name.$contentProperty"
        }
    }
}

/** Accessors are generated Kotlin calls; the runtime owns only the IXamlMember ABI boundary. */
class WinRTXamlMemberDefinition(
    val name: String,
    val typeName: String,
    val type: KClass<*>? = null,
    val get: (Any) -> Any?,
    val set: ((Any, Any?) -> Unit)? = null,
    val isDependencyProperty: Boolean = false,
    val collection: WinRTXamlCollectionDefinition? = null,
    val isAttachable: Boolean = false,
) {
    init { require(name.isNotBlank() && typeName.isNotBlank()) }
}

/** CSharpTypeInfoPass2's ItemType and CollectionAdd, with generated typed Add calls. */
class WinRTXamlCollectionDefinition(
    val type: KClass<*>,
    val itemTypeName: String,
    val itemType: KClass<*>,
    val add: (Any, Any?) -> Unit,
)

fun registerWinRTXamlTypeDefinition(definition: WinRTXamlTypeDefinition) {
    Projections.registerAuthoredRuntimeClassType(definition.type, definition.name, definition.baseName)
    WinUiAuthoredTypeMetadata.registerDefinition(definition)
}

/** Application enums use Kotlin declaration ordinals as their Int32 values.
 * Mirrors XamlUserType's enum table; component ABI export is a separate contract. */
fun <T : Enum<T>> registerWinRTXamlEnumType(type: KClass<T>, name: String, entries: Array<T>) {
    Projections.registerEnumType(type, name, "enum($name;i4)", { it.ordinal }, entries)
    val byName = entries.associateBy { it.name }
    WinUiAuthoredTypeMetadata.registerEnum(type, name) { input ->
        val value = input.split(',').fold(0) { result, part ->
            val token = part.trim()
            val entry = byName[token] ?: byName.entries.firstOrNull { it.key.equals(token, true) }?.value
            result or (entry?.ordinal ?: token.toInt())
        }
        requireNotNull(entries.firstOrNull { it.ordinal == value }) { "Unknown $name value: $input" }
    }
}
