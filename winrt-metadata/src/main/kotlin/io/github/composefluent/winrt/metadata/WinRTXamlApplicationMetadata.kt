package io.github.composefluent.winrt.metadata

/** Compile-time application schema only; these properties are not exported WinRT ABI members. */
data class WinRTXamlApplicationProperty(
    val name: String,
    val type: WinRTTypeRef,
    val isReadOnly: Boolean = false,
) {
    init { require(name.isNotBlank()) }
}

data class WinRTXamlApplicationTypeMembers(
    val properties: List<WinRTXamlApplicationProperty> = emptyList(),
    val contentProperty: String? = null,
) {
    init {
        require(properties.map { it.name }.distinct().size == properties.size)
        require(contentProperty == null || properties.any { it.name == contentProperty })
    }
}
