package io.github.composefluent.winrt.metadata

internal fun WinRTMethodDefinition.withNullableReturnContract(ownerTypeName: String): WinRTMethodDefinition {
    // WinUI controls/dev/Repeater/Layout.h returns nullptr from this virtual's
    // default implementation. CsWinRT MarshalInterface<T>.FromAbi preserves it.
    // Apply the contract before planning so outgoing calls and authored overrides
    // share the same Kotlin return type, including inherited base-call bridges.
    if (ownerTypeName !in setOf(
            "Microsoft.UI.Xaml.Controls.Layout",
            "Microsoft.UI.Xaml.Controls.ILayoutOverrides",
        ) || name != "CreateDefaultItemTransitionProvider") return this
    // Nullability is a projection contract, not part of a WinMD type signature.
    return copy(returnTypeName = returnTypeName.removeSuffix("?") + "?")
}

fun WinRTPropertyDefinition.projectedPropertyTypeName(
    ownerTypeName: String,
    typesByQualifiedName: Map<String, WinRTTypeDefinition> = emptyMap(),
): String {
    if (!isNullablePropertyProjection(ownerTypeName, typesByQualifiedName)) {
        return typeName
    }
    return typeName.trim().let { trimmed ->
        if (trimmed.endsWith("?")) trimmed else "$trimmed?"
    }
}

fun WinRTPropertyDefinition.isNullablePropertyProjection(
    ownerTypeName: String,
    typesByQualifiedName: Map<String, WinRTTypeDefinition> = emptyMap(),
): Boolean {
    val normalizedOwnerTypeName = ownerTypeName
        .substringBefore('<')
        .removeSuffix("?")
    val currentNamespace = normalizedOwnerTypeName.substringBeforeLast('.', "")
    return type.isNullableWinRTPropertyReference(currentNamespace, typesByQualifiedName)
}

private fun WinRTTypeRef.isNullableWinRTPropertyReference(
    currentNamespace: String,
    typesByQualifiedName: Map<String, WinRTTypeDefinition>,
): Boolean {
    val normalized = normalized()
    if (normalized.kind != WinRTTypeRefKind.Named || normalized.typeArguments.isNotEmpty()) {
        return false
    }
    val rawTypeName = normalized.qualifiedName ?: normalized.typeName
    if (rawTypeName.isNonNullableXamlPropertyRuntimeClassTypeName()) {
        return false
    }
    if (isWinRTObjectTypeName(rawTypeName)) {
        return true
    }
    val resolvedType = resolveTypeReference(normalized, currentNamespace, typesByQualifiedName).definitionType
    return resolvedType?.kind in setOf(
        WinRTTypeKind.Interface,
        WinRTTypeKind.Delegate,
        WinRTTypeKind.RuntimeClass,
    )
}

private fun String.isXamlDependencyPropertyTypeName(): Boolean =
    this == "Microsoft.UI.Xaml.DependencyProperty" ||
        this == "Windows.UI.Xaml.DependencyProperty"

private fun String.isNonNullableXamlPropertyRuntimeClassTypeName(): Boolean =
    isXamlDependencyPropertyTypeName() || isXamlCollectionRuntimeClassTypeName()

private fun String.isXamlCollectionRuntimeClassTypeName(): Boolean {
    if (!startsWith("Microsoft.UI.Xaml.") && !startsWith("Windows.UI.Xaml.")) {
        return false
    }
    val simpleName = substringAfterLast('.')
    return simpleName.endsWith("Collection") || simpleName == "ResourceDictionary"
}
