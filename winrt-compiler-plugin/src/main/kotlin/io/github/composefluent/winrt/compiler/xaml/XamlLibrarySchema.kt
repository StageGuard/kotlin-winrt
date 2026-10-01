package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.compiler.KotlinWinRTCommandLineProcessor
import io.github.composefluent.winrt.compiler.authoring.readAuthoringMetadataIndex
import io.github.composefluent.winrt.compiler.authoring.resolveIndexedWinRTTypeByProjectedName
import io.github.composefluent.winrt.metadata.*
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

/** Like CSharpTypeInfoPass2, accessors are compiled in the assembly that owns the model.
 * The schema is private compiler input; none of these classes becomes a WinRT component.
 * IR supplies inferred Kotlin property types, without runtime reflection on either target.
 */
@OptIn(ExperimentalCompilerApi::class)
internal object XamlLibraryOptions {
    private val keys = listOf("xamlLibraryOutput", "xamlLibraryAssembly", "xamlLibraryReferences")
        .associateWith { CompilerConfigurationKey<String>(it) }
    val options = keys.keys.map { CliOption(it, "<value>", "Kotlin XAML library schema $it", false) }

    fun process(name: String, value: String, configuration: CompilerConfiguration): Boolean {
        val key = keys[name] ?: return false
        configuration.put(key, value)
        return true
    }

    fun export(configuration: CompilerConfiguration): IrGenerationExtension? {
        val output = configuration.get(keys.getValue("xamlLibraryOutput")) ?: return null
        val root = Path.of(output)
        // Frontend errors must not leave a usable schema from an earlier build.
        Files.deleteIfExists(root.resolve("KotlinXaml.winmd"))
        Files.deleteIfExists(root.resolve("kotlin-winrt-support/compiler-support.tsv"))
        return XamlLibrarySchema(root,
            requireNotNull(configuration.get(keys.getValue("xamlLibraryAssembly"))),
            Path.of(requireNotNull(configuration.get(KotlinWinRTCommandLineProcessor.METADATA_INDEX_KEY))),
            configuration.get(keys.getValue("xamlLibraryReferences"))?.let { file ->
                Files.readAllLines(Path.of(file)).filter(String::isNotBlank).map(Path::of)
            }.orEmpty())
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private class XamlLibrarySchema(private val root: Path, private val assembly: String,
    private val metadataIndex: Path, private val references: List<Path>) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val types = readAuthoringMetadataIndex(metadataIndex)
        fun publicClasses(declarations: List<IrDeclaration>): List<IrClass> = declarations.filterIsInstance<IrClass>()
            .filter { it.visibility == DescriptorVisibilities.PUBLIC }
            .flatMap { listOf(it) + publicClasses(it.declarations) }
        val classes = moduleFragment.files.flatMap { publicClasses(it.declarations) }
            .filter { it.origin == IrDeclarationOrigin.DEFINED && it.typeParameters.isEmpty() && !it.isInner &&
                it.kind in setOf(ClassKind.CLASS, ClassKind.OBJECT, ClassKind.ENUM_CLASS) && !it.isCompanion &&
                it.fqNameWhenAvailable != null && !it.fqNameWhenAvailable!!.asString().startsWith("io.github.composefluent.winrt.generated.") &&
                resolveIndexedWinRTTypeByProjectedName(it.fqNameWhenAvailable!!.asString(), types) == null }
            .sortedBy { it.fqNameWhenAvailable!!.asString() }
        val names = classes.mapTo(mutableSetOf()) { it.fqNameWhenAvailable!!.asString() } + references
            .filter { it.fileName.toString().endsWith(".KotlinXaml.winmd") }
            .flatMap { WinRTMetadataLoader.load(it).namespaces.flatMap { namespace -> namespace.types }.map { type -> type.qualifiedName } }
        val members = classes.associate { klass ->
            klass.fqNameWhenAvailable!!.asString() to xamlApplicationProperties(klass, types, names,
                strictPublicProperties = false).let { schema -> schema.copy(
                properties = schema.properties.filter { property -> property.isPublic &&
                    klass.declarations.filterIsInstance<IrProperty>().any { it.name.asString() == property.name &&
                        it.getter?.visibility == DescriptorVisibilities.PUBLIC } },
                methods = schema.methods.filter { method -> method.isPublic &&
                    (klass.declarations + klass.companionObject()?.declarations.orEmpty()).filterIsInstance<IrSimpleFunction>()
                        .any { it.name.asString() == method.name && it.visibility == DescriptorVisibilities.PUBLIC } },
            ) }
        }
        val descriptors = classes.map { klass ->
            val base = klass.superTypes.firstNotNullOfOrNull { type ->
                val name = type.classFqName?.asString() ?: return@firstNotNullOfOrNull null
                if (name in names) name else resolveIndexedWinRTTypeByProjectedName(name, types)
                    ?.takeIf { it.kind == WinRTTypeKind.RuntimeClass.name }?.qualifiedName
            }
            WinRTXamlApplicationTypeDescriptor(klass.fqNameWhenAvailable!!.asString(), base,
                klass.superTypes.mapNotNull { type -> resolveIndexedWinRTTypeByProjectedName(type.classFqName?.asString().orEmpty(), types)
                    ?.takeIf { it.kind == WinRTTypeKind.Interface.name }?.qualifiedName },
                isActivatable = defaultConstructor(klass) != null, isSealed = klass.modality == Modality.FINAL,
                enumEntries = if (klass.kind == ClassKind.ENUM_CLASS) klass.declarations.filterIsInstance<IrEnumEntry>()
                    .map { it.name.asString() } else null)
        }
        Files.createDirectories(root)
        WinRTPortableExecutableMetadataWriter.writeXamlSchemaWinmd("$assembly.KotlinXaml", descriptors, members,
            root.resolve("KotlinXaml.winmd"), WinRTMetadataLoader.loadTypeAssemblyNames(references), types.values
                .filter { it.kind in setOf(WinRTTypeKind.Enum.name, WinRTTypeKind.Struct.name) }.mapTo(mutableSetOf()) { it.qualifiedName })
        val registrar = "KotlinXamlLibraryDefinitions_" + assembly.replace(Regex("[^A-Za-z0-9_]"), "_")
        val source = root.resolve("src/io/github/composefluent/winrt/generated/xaml/$registrar.kt")
        Files.createDirectories(source.parent)
        source.writeText(buildString {
            appendLine("@file:Suppress(\"UNCHECKED_CAST\")")
            appendLine("package io.github.composefluent.winrt.generated.xaml")
            appendLine("object $registrar {")
            appendLine("  private val registration: Unit = run {")
            for ((klass, descriptor) in classes.zip(descriptors)) {
                val name = descriptor.runtimeClassName
                if (klass.kind == ClassKind.ENUM_CLASS) {
                    appendLine("    io.github.composefluent.winrt.runtime.registerWinRTXamlEnumType($name::class, ${literal(name)}, $name.entries.toTypedArray())")
                    continue
                }
                appendLine("    io.github.composefluent.winrt.runtime.registerWinRTXamlTypeDefinition(io.github.composefluent.winrt.runtime.WinRTXamlTypeDefinition(")
                appendLine("      type = $name::class, name = ${literal(name)},")
                val baseType = klass.superTypes.firstOrNull { type -> type.classFqName?.asString() == descriptor.baseRuntimeClassName ||
                    resolveIndexedWinRTTypeByProjectedName(type.classFqName?.asString().orEmpty(), types)?.qualifiedName == descriptor.baseRuntimeClassName }
                appendLine("      baseName = ${literal(descriptor.baseRuntimeClassName ?: "System.Object")}, baseType = ${baseType?.sourceType()?.substringBefore('<') ?: "kotlin.Any"}::class,")
                if (descriptor.isActivatable) appendLine("      activate = { $name() },")
                members.getValue(name).contentProperty?.let { content ->
                    if (members.getValue(name).properties.any { it.name == content }) appendLine("      contentProperty = ${literal(content)},")
                }
                appendLine("      members = listOf(")
                for (member in members.getValue(name).properties.filterNot { it.isStatic }) {
                    val property = klass.declarations.filterIsInstance<IrProperty>().single { it.name.asString() == member.name }
                    val propertyType = property.getter!!.returnType
                    val kotlinType = propertyType.sourceType()
                    appendLine("        io.github.composefluent.winrt.runtime.WinRTXamlMemberDefinition(")
                    appendLine("          name = ${literal(member.name)}, typeName = ${literal(member.type.typeName)}, type = ${propertyType.classFqName!!.asString()}::class,")
                    appendLine("          isDependencyProperty = ${member.isDependencyProperty}, get = { (it as $name).`${member.name}` },")
                    if (!member.isReadOnly && property.setter?.visibility == DescriptorVisibilities.PUBLIC)
                        appendLine("          set = { instance, value -> (instance as $name).`${member.name}` = value as $kotlinType },")
                    val args = (propertyType as? IrSimpleType)?.arguments.orEmpty().mapNotNull { it.typeOrNull }
                    when (winRTCollectionKindForAbiName(member.type.qualifiedName.orEmpty())) {
                        WinRTCollectionInterfaceKind.Vector -> {
                            val item = args.single()
                            appendLine("          collection = io.github.composefluent.winrt.runtime.WinRTXamlCollectionDefinition(kotlin.collections.MutableList::class,")
                            appendLine("            ${literal(member.type.typeArguments.single().typeName)}, ${item.classFqName!!.asString()}::class,")
                            appendLine("            { instance, value -> (instance as $kotlinType).add(value as ${item.sourceType()}); Unit }),")
                        }
                        WinRTCollectionInterfaceKind.Map -> {
                            val (key, item) = args
                            appendLine("          dictionary = io.github.composefluent.winrt.runtime.WinRTXamlDictionaryDefinition(kotlin.collections.MutableMap::class,")
                            appendLine("            ${literal(member.type.typeArguments[0].typeName)}, ${key.classFqName!!.asString()}::class,")
                            appendLine("            ${literal(member.type.typeArguments[1].typeName)}, ${item.classFqName!!.asString()}::class,")
                            appendLine("            { instance, key, value -> (instance as $kotlinType)[key as ${key.sourceType()}] = value as ${item.sourceType()} }),")
                        }
                        else -> Unit
                    }
                    appendLine("        ),")
                }
                appendLine("      )," )
                appendLine("    ))")
            }
            appendLine("  }")
            appendLine("  fun registerAll() { registration }")
            appendLine("}")
        })
        val support = root.resolve("kotlin-winrt-support")
        Files.createDirectories(support)
        support.resolve("xaml-type-registrars.tsv").writeText("className\nio.github.composefluent.winrt.generated.xaml.$registrar\n")
        support.resolve("compiler-support.tsv").writeText("kind\tclassName\tsourceFile\tentries\towner\n" +
            "xaml-type-registrar\tio.github.composefluent.winrt.generated.xaml.$registrar\txaml-type-registrars.tsv\t1\t\n")
    }

    private fun defaultConstructor(klass: IrClass): IrConstructor? = if (klass.kind != ClassKind.CLASS || klass.modality == Modality.ABSTRACT) null
        else klass.constructors.firstOrNull { it.visibility == DescriptorVisibilities.PUBLIC &&
            it.parameters.filter { it.kind == IrParameterKind.Regular }.all { it.defaultValue != null } }

    private fun IrType.sourceType(): String {
        val name = requireNotNull(classFqName?.asString())
        val args = (this as? IrSimpleType)?.arguments.orEmpty().map { requireNotNull(it.typeOrNull).sourceType() }
        return name + (if (args.isEmpty()) "" else args.joinToString(", ", "<", ">")) + if (isNullable()) "?" else ""
    }
    private fun literal(value: String): String = kotlinx.serialization.json.JsonPrimitive(value).toString()
}
