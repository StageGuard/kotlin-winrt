package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.compiler.KotlinWinRTCommandLineProcessor
import io.github.composefluent.winrt.compiler.authoring.readAuthoringMetadataIndex
import io.github.composefluent.winrt.compiler.authoring.resolveIndexedWinRTTypeByProjectedName
import io.github.composefluent.winrt.metadata.*
import kotlinx.serialization.json.*
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** Private Kotlin handlers stay in this compile-time sidecar, never in the public WinMD ABI. */
@OptIn(ExperimentalCompilerApi::class)
internal object XamlSemanticOptions {
    private val keys = listOf("xamlDeclarations", "xamlSemanticOutput", "xamlReferences", "xamlReferencesFile", "xamlImplementation")
        .associateWith { CompilerConfigurationKey<String>(it) }
    val options = keys.keys.map { CliOption(it, "<path>", "Kotlin XAML semantic compilation $it", false) }

    fun process(name: String, value: String, configuration: CompilerConfiguration): Boolean {
        val key = keys[name] ?: return false
        configuration.put(key, value)
        return true
    }

    fun compilation(configuration: CompilerConfiguration): XamlCompilation? {
        fun value(name: String) = configuration.get(keys.getValue(name))
        if (keys.keys.all { value(it) == null }) return null
        val declarationPath = Path.of(requireNotNull(value("xamlDeclarations")))
        val declarations = WinRTXamlDeclarations.parse(Files.readString(declarationPath))
        val finalPath = value("xamlImplementation")
        if (finalPath != null) {
            require(value("xamlSemanticOutput") == null) { "XAML final and semantic modes are mutually exclusive" }
            val actual = WinRTXamlDeclarations.readCompilerOutput(Path.of(finalPath))
            val plan = Json.parseToJsonElement(Files.readString(Path.of(finalPath))).jsonObject
                .getValue("KotlinImplementation").jsonObject
            require(plan.getValue("SchemaVersion").jsonPrimitive.int == 1 &&
                plan.getValue("DeclarationFingerprint").jsonPrimitive.content == WinRTXamlDeclarations.fingerprint(declarations) &&
                WinRTXamlDeclarations.canonicalText(actual) == WinRTXamlDeclarations.canonicalText(declarations)) {
                "XAML final plan does not match declaration input"
            }
            return XamlCompilation(declarations, null)
        }
        val output = Path.of(requireNotNull(value("xamlSemanticOutput")))
        // Invalidate before frontend analysis too: syntax/type errors never reach IR export.
        Files.deleteIfExists(output)
        Files.deleteIfExists(output.resolveSibling("KotlinXaml.winmd"))
        require(value("xamlReferences") == null || value("xamlReferencesFile") == null) {
            "Specify either xamlReferences or xamlReferencesFile"
        }
        val references = value("xamlReferencesFile")?.let { Files.readAllLines(Path.of(it)).filter(String::isNotBlank) }
            ?: requireNotNull(value("xamlReferences")).split(File.pathSeparator)
        val metadataIndex = Path.of(requireNotNull(configuration.get(KotlinWinRTCommandLineProcessor.METADATA_INDEX_KEY)))
        return XamlCompilation(declarations, XamlSemanticExport(declarationPath, output, references.map(Path::of), metadataIndex))
    }
}

internal data class XamlCompilation(val declarations: WinRTXamlDeclarationIndex, val semanticExport: IrGenerationExtension?)

@OptIn(UnsafeDuringIrConstructionAPI::class)
private class XamlSemanticExport(
    private val declarationPath: Path,
    private val output: Path,
    private val references: List<Path>,
    private val metadataIndex: Path,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        Files.deleteIfExists(output)
        Files.deleteIfExists(output.resolveSibling("KotlinXaml.winmd"))
        val declarations = WinRTXamlDeclarations.parse(Files.readString(declarationPath))
        val types = readAuthoringMetadataIndex(metadataIndex)
        val classes = moduleFragment.files.flatMap { file ->
            file.declarations.filterIsInstance<IrClass>().map { it to file }
        }
        val authored = mutableListOf<WinRTAuthoredRuntimeClassDescriptor>()
        val applicationMembers = mutableMapOf<String, WinRTXamlApplicationTypeMembers>()
        val applicationTypes = declarations.pages.mapTo(mutableSetOf()) { it.className }
        val pages = declarations.pages.sortedBy { it.className }.map { page ->
            val (klass, file) = requireNotNull(classes.singleOrNull { it.first.fqNameWhenAvailable?.asString() == page.className }) {
                "XAML ${page.resourcePath}: missing top-level Kotlin class ${page.className}"
            }
            val source = Path.of(file.fileEntry.name)
            require(source.fileName.toString() == page.className.substringAfterLast('.') + ".kt" &&
                Files.isRegularFile(source.resolveSibling(source.fileName.toString().removeSuffix(".kt") + ".xaml"))) {
                "XAML ${page.className}: requires same-directory, same-basename .kt and .xaml files"
            }
            require(klass.superTypes.any { type -> type.classFqName?.asString()?.let {
                resolveIndexedWinRTTypeByProjectedName(it, types)?.qualifiedName
            } == page.baseTypeName }) { "XAML ${page.className}: expected direct base ${page.baseTypeName}" }
            authored += WinRTAuthoredRuntimeClassDescriptor(page.className, page.baseTypeName,
                listOf("Microsoft.UI.Xaml.Markup.IComponentConnector"), isActivatable = false)
            applicationMembers[page.className] = xamlApplicationProperties(klass, types, applicationTypes)
            val handlers = page.connections.flatMap { it.events }.map { it.handlerName }.distinct().sorted().map { name ->
                val handler = requireNotNull(klass.declarations.filterIsInstance<IrSimpleFunction>().singleOrNull { it.name.asString() == name }) {
                    "XAML ${page.className}: missing or overloaded handler $name"
                }
                require(!handler.isSuspend && handler.typeParameters.isEmpty() &&
                    handler.parameters.none { it.kind == IrParameterKind.ExtensionReceiver || it.kind == IrParameterKind.Context } &&
                    handler.dispatchReceiverParameter != null) { "XAML ${page.className}.$name: expected ordinary instance method" }
                require(handler.returnType.classFqName?.asString() == "kotlin.Unit") {
                    "XAML ${page.className}.$name: handler must return Unit"
                }
                val parameters = handler.parameters.filter { it.kind == IrParameterKind.Regular }.map { parameter ->
                    require(parameter.varargElementType == null) { "XAML handler parameters cannot be vararg" }
                    val kotlinName = requireNotNull(parameter.type.classFqName?.asString()) { "Unresolved XAML handler parameter" }
                    if (kotlinName == "kotlin.Any") "System.Object" else
                        requireNotNull(resolveIndexedWinRTTypeByProjectedName(kotlinName, types)?.qualifiedName) {
                            "XAML ${page.className}.$name: unsupported parameter type $kotlinName"
                        }
                }
                buildJsonObject {
                    put("Name", name); put("ReturnTypeName", "System.Void")
                    put("ParameterTypeNames", JsonArray(parameters.map(::JsonPrimitive)))
                }
            }
            buildJsonObject { put("ClassName", page.className); put("Handlers", JsonArray(handlers)) }
        }
        val symbols = buildJsonObject {
            put("SchemaVersion", 1)
            put("DeclarationFingerprint", WinRTXamlDeclarations.fingerprint(declarations))
            put("Declarations", Json.parseToJsonElement(WinRTXamlDeclarations.canonicalText(declarations)))
            put("Pages", JsonArray(pages))
        }
        Files.createDirectories(output.toAbsolutePath().parent)
        WinRTPortableExecutableMetadataWriter.writeXamlApplicationWinmd(
            "KotlinXaml", authored, applicationMembers, output.resolveSibling("KotlinXaml.winmd"),
            WinRTMetadataLoader.loadTypeAssemblyNames(references),
            types.values.filter { it.kind == WinRTTypeKind.Enum.name || it.kind == WinRTTypeKind.Struct.name }
                .mapTo(mutableSetOf()) { it.qualifiedName },
        )
        Files.writeString(output, symbols.toString())
    }
}
