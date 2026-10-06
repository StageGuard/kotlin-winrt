package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.metadata.WinRTMetadataLoader
import java.nio.file.Path

/** Keep component TypeRefs in their declaring assembly, as in CsWinRT's metadata lookup.
 * A Kotlin library's compiler-only XAML schema can also describe its authored classes;
 * it does not replace their component identity. Plain Kotlin models still use the schema.
 */
internal fun loadXamlReferenceTypeAssemblyNames(references: List<Path>): Map<String, String> {
    val (schemas, components) = references.partition {
        it.fileName.toString().endsWith(".KotlinXaml.winmd")
    }
    val schemaAssemblies = WinRTMetadataLoader.loadTypeAssemblyNames(schemas)
    val componentAssemblies = WinRTMetadataLoader.loadTypeAssemblyNames(components)
    schemaAssemblies.forEach { (type, schemaAssembly) ->
        val componentAssembly = componentAssemblies[type] ?: return@forEach
        require(schemaAssembly == componentAssembly || schemaAssembly == "$componentAssembly.KotlinXaml") {
            "Ambiguous declaring assembly for $type: $schemaAssembly and $componentAssembly"
        }
    }
    return schemaAssemblies + componentAssemblies
}
