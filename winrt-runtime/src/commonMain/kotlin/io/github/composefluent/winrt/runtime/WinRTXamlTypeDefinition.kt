package io.github.composefluent.winrt.runtime

import kotlin.reflect.KClass

/** Generated XAML type information, corresponding to XamlUserType in CSharpTypeInfoPass2.tt. */
class WinRTXamlTypeDefinition(
    val type: KClass<*>,
    val name: String,
    val baseName: String,
    val activate: (() -> Any)? = null,
    val contentProperty: String? = null,
    members: List<WinRTXamlMemberDefinition> = emptyList(),
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
    val get: (Any) -> Any?,
    val set: ((Any, Any?) -> Unit)? = null,
    val isDependencyProperty: Boolean = false,
) {
    init { require(name.isNotBlank() && typeName.isNotBlank()) }
}

fun registerWinRTXamlTypeDefinition(definition: WinRTXamlTypeDefinition) {
    Projections.registerAuthoredRuntimeClassType(definition.type, definition.name, definition.baseName)
    WinUiAuthoredTypeMetadata.registerDefinition(definition)
}
