package io.github.composefluent.winrt.metadata

/** Compile-time application schema only; these properties are not exported WinRT ABI members. */
data class WinRTXamlApplicationProperty(
    val name: String,
    val type: WinRTTypeRef,
    val isReadOnly: Boolean = false,
    val isPublic: Boolean = true,
) {
    init { require(name.isNotBlank()) }
}

/** A projected add/remove handler pair consumed by the generated Kotlin connector. */
data class WinRTXamlApplicationEvent(val name: String, val handlerType: WinRTTypeRef) {
    init { require(name.isNotBlank()) }
}

data class WinRTXamlApplicationTypeMembers(
    val properties: List<WinRTXamlApplicationProperty> = emptyList(),
    val contentProperty: String? = null,
    val events: List<WinRTXamlApplicationEvent> = emptyList(),
) {
    init {
        require(properties.map { it.name }.distinct().size == properties.size)
        require(events.map { it.name }.distinct().size == events.size)
        require(contentProperty == null || properties.any { it.name == contentProperty })
    }
}
