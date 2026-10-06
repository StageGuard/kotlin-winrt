package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.metadata.*
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

/** CSharpTypeInfoPass2 creates typed activators for referenced WinMD classes too.
 * Non-bindable library classes may be absent from the library's native type table.
 * These entries supplement that provider; they do not export Kotlin components.
 */
internal fun writeXamlProjectedTypeRegistrationSource(
    root: Path,
    references: List<Path>,
    names: Set<String>,
    assemblyName: String?,
): String? {
    if (names.isEmpty()) return null
    val model = WinRTMetadataLoader.loadSources(references.map(WinRTMetadataSource::path))
    val semantics = model.semanticHelpers()
    val types = model.namespaces.flatMap { it.types }.filter { type ->
        type.qualifiedName in names && type.kind == WinRTTypeKind.RuntimeClass &&
            // The projection emits the parameterless ActivationFactory constructor
            // from WinMD activation metadata, rather than a physical .ctor method.
            !type.isStaticType && type.genericParameterCount == 0 && type.activation.isActivatable &&
            type.customAttributes.none { it.typeName.substringAfterLast('.') == "BindableAttribute" }
    }.sortedBy { it.qualifiedName }
    if (types.isEmpty()) return null
    val suffix = assemblyName.orEmpty().replace(Regex("[^A-Za-z0-9_]"), "_")
    val register = "registerKotlinWinRTXamlProjectedTypes_$suffix"
    val packageName = "io.github.composefluent.winrt.generated.xaml"
    val file = root.resolve("${packageName.replace('.', '/')}/KotlinXamlProjectedTypes_$suffix.kt")
    Files.createDirectories(file.parent)
    fun literal(value: String) = JsonPrimitive(value).toString()
    fun sourceType(ref: WinRTTypeRef): String {
        val reference = model.specialTypeResolver().resolveType(ref, "") as? WinRTReferenceTypeDescriptor
        if (reference?.kind == WinRTReferenceInterfaceKind.Reference)
            return sourceType(ref.typeArguments.single()).removeSuffix("?") + "?"
        val name = ref.qualifiedName ?: ref.typeName
        val source = xamlTypeClassId(name).asSingleFqName().asString()
        val args = ref.typeArguments.takeIf { it.isNotEmpty() }?.joinToString(", ", "<", ">", transform = ::sourceType).orEmpty()
        val value = winRTFundamentalTypeForName(name) != null || isWinRTGuidTypeName(name) ||
            semantics.resolveType(ref, "")?.kind in setOf(WinRTTypeKind.Enum, WinRTTypeKind.Struct)
        return source + args + if (value) "" else "?"
    }
    file.writeText(buildString {
        appendLine("@file:Suppress(\"UNCHECKED_CAST\", \"DEPRECATION\")")
        appendLine("@file:OptIn(kotlin.ExperimentalUnsignedTypes::class)")
        appendLine("package $packageName")
        appendLine("internal fun $register() {")
        for (type in types) {
            val name = type.qualifiedName
            val owner = xamlTypeClassId(name).asSingleFqName().asString()
            val baseName = type.baseTypeName ?: "System.Object"
            val base = xamlTypeClassId(baseName).asSingleFqName().asString()
            appendLine("  io.github.composefluent.winrt.runtime.registerWinRTXamlProjectedTypeDefinition(io.github.composefluent.winrt.runtime.WinRTXamlTypeDefinition(")
            appendLine("    type = $owner::class, name = ${literal(name)},")
            appendLine("    baseName = ${literal(baseName)}, baseType = $base::class, activate = { $owner() },")
            appendLine("    isBindable = false,")
            appendLine("    members = listOf(")
            val convert: (String, String) -> String = { value, target -> "kotlinWinRTXamlMemberValue<$target>($value)" }
            for (member in semantics.classMemberMergeDescriptor(type).mergedProperties.filter { it.isPublic && !it.isPrivate && it.getterTarget != null }) {
                val ref = WinRTTypeRef.fromDisplayName(member.propertyTypeName)
                val property = WinRTXamlApplicationProperty(member.propertyName, ref, isReadOnly = member.setterTarget == null)
                appendLine(xamlPropertyRegistrationSource(owner, property, sourceType(ref), convert,
                    accessorName = member.propertyName.replaceFirstChar(Char::lowercase)).prependIndent("      ") + ",")
            }
            appendLine("    )," )
            appendLine("  ))")
        }
        appendLine("}")
    })
    return "$packageName.$register"
}
