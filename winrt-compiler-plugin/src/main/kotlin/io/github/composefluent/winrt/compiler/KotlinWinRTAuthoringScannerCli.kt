package io.github.composefluent.winrt.compiler

import io.github.composefluent.winrt.compiler.authoring.IndexedWinRTType
import io.github.composefluent.winrt.compiler.authoring.KotlinImports
import io.github.composefluent.winrt.compiler.authoring.KotlinWinRTAuthoredRuntimeClassAnnotation
import io.github.composefluent.winrt.compiler.authoring.KotlinWinRTAuthoredTypeCandidate
import io.github.composefluent.winrt.compiler.authoring.KotlinWinRTAuthoringCandidateFile
import io.github.composefluent.winrt.compiler.authoring.PROJECTION_PACKAGE_PREFIX
import io.github.composefluent.winrt.compiler.authoring.WINRT_AUTHORED_RUNTIME_CLASS_ANNOTATION
import io.github.composefluent.winrt.compiler.authoring.inheritedOverridableInterfaceNames
import io.github.composefluent.winrt.compiler.authoring.projectionPackageToMetadataName
import io.github.composefluent.winrt.compiler.authoring.readAuthoringMetadataIndex
import io.github.composefluent.winrt.compiler.authoring.resolveIndexedWinRTType
import io.github.composefluent.winrt.compiler.authoring.resolveIndexedWinRTTypeByProjectedName
import io.github.composefluent.winrt.metadata.WinRTXamlDeclarations
import io.github.composefluent.winrt.metadata.WinRTAuthoredRuntimeClassDescriptor
import io.github.composefluent.winrt.metadata.WinRTPortableExecutableMetadataWriter
import io.github.composefluent.winrt.metadata.WinRTMetadataLoader
import io.github.composefluent.winrt.metadata.WinRTTypeKind
import io.github.composefluent.winrt.metadata.WinRTTypeRef
import io.github.composefluent.winrt.metadata.WinRTXamlApplicationProperty
import io.github.composefluent.winrt.metadata.WinRTXamlApplicationTypeMembers
import io.github.composefluent.winrt.metadata.winRTFundamentalTypeForName
import io.github.composefluent.winrt.metadata.toKotlinProjectionTypeName
import io.github.composefluent.winrt.metadata.isWinRTValueType
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironmentMode
import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.parsing.KotlinLightParser
import org.jetbrains.kotlin.util.getChildren
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.streams.asSequence

object KotlinWinRTAuthoringScannerCli {
    @JvmStatic
    fun main(args: Array<String>) {
        val options = CliOptions.parse(args)
        val index = readAuthoringMetadataIndex(options.metadataIndex)
        if (options.xamlHeader) {
            writeXamlHeader(options, index)
            return
        }
        val scanned = scan(options.sourceRoots, index)
        val pages = options.xamlDeclarations?.let { WinRTXamlDeclarations.parse(it.readText()).pages }.orEmpty()
        val byName = scanned.associateBy { it.sourceTypeName }
        val connector = "Microsoft.UI.Xaml.Markup.IComponentConnector"
        if (pages.isNotEmpty()) {
            require(index[connector]?.kind == "Interface") { "XAML authoring requires $connector in the metadata index." }
        }
        pages.forEach { page ->
            val candidate = requireNotNull(byName[page.className]) {
                "XAML class ${page.className} must resolve to an authored Kotlin class."
            }
            require(candidate.winRTBaseClassName == page.baseTypeName) {
                "XAML class ${page.className} requires base ${page.baseTypeName}, found ${candidate.winRTBaseClassName}."
            }
        }
        val pageNames = pages.map { it.className }.toSet()
        val candidates = scanned.map { candidate ->
            if (candidate.sourceTypeName in pageNames) candidate.copy(
                winRTInterfaceNames = (candidate.winRTInterfaceNames + connector).distinct().sorted(),
            ) else candidate
        }
        KotlinWinRTAuthoringCandidateFile.write(options.output, candidates)
    }

    /** Source declarations precede XamlCompiler pass 1; Kotlin IR checks them in the semantic pass. */
    private fun writeXamlHeader(options: CliOptions, index: Map<String, IndexedWinRTType>) {
        val sources = options.sourceRoots.flatMap(::kotlinSourceFiles).distinct().sorted()
            .filter { source -> Files.isRegularFile(source.resolveSibling("${source.name.removeSuffix(".kt")}.xaml")) }
        val candidates = scan(options.sourceRoots, index).associateBy { it.sourceTypeName }
        val pages = sources.map { path ->
            val source = parseSource(path)
            val simpleName = path.name.removeSuffix(".kt")
            val klass = source.classes().singleOrNull { source.className(it) == simpleName }
                ?: error("XAML $path requires exactly one top-level Kotlin class named $simpleName")
            val name = listOf(source.packageName(), simpleName).filter(String::isNotBlank).joinToString(".")
            val candidate = requireNotNull(candidates[name]) {
                "XAML $path requires an authorable Kotlin class $name with a WinRT base and an accessible zero-argument constructor"
            }
            require(source.hasPublicDefaultActivationConstructor(klass)) {
                "XAML $name requires an accessible zero-argument constructor"
            }
            Triple(source, klass, candidate)
        }
        val applicationNames = pages.mapTo(mutableSetOf()) { it.third.sourceTypeName }
        val schemas = pages.associate { (source, klass, candidate) ->
            candidate.sourceTypeName to source.xamlApplicationMembers(klass, applicationNames, index)
        }
        val members = schemas.mapValues { it.value.metadata }
        val descriptors = pages.map { (_, _, candidate) ->
            WinRTAuthoredRuntimeClassDescriptor(candidate.sourceTypeName,
                requireNotNull(candidate.winRTBaseClassName),
                listOf("Microsoft.UI.Xaml.Markup.IComponentConnector"), isActivatable = true)
        }
        WinRTPortableExecutableMetadataWriter.writeXamlApplicationWinmd(
            "KotlinXaml", descriptors, members, options.output,
            WinRTMetadataLoader.loadTypeAssemblyNames(options.references),
            index.values.filter { it.kind == WinRTTypeKind.Enum.name || it.kind == WinRTTypeKind.Struct.name }
                .mapTo(mutableSetOf()) { it.qualifiedName },
        )
        options.xamlHeaderSources?.let { writeXamlRegistrationSources(it, pages, schemas) }
    }

    private data class XamlSourceProperty(val metadata: WinRTXamlApplicationProperty, val kotlinType: String)
    private data class XamlSourceMembers(
        val metadata: WinRTXamlApplicationTypeMembers,
        val properties: List<XamlSourceProperty>,
    )

    private fun String.kotlinLiteral(): String = buildString {
        append('"')
        for (character in this@kotlinLiteral) when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            else -> append(character)
        }
        append('"')
    }

    private fun writeXamlRegistrationSources(
        root: Path,
        pages: List<Triple<KotlinLightSource, LighterASTNode, KotlinWinRTAuthoredTypeCandidate>>,
        schemas: Map<String, XamlSourceMembers>,
    ) {
        Files.createDirectories(root)
        val registrations = pages.map { (source, klass, candidate) ->
            val schema = schemas.getValue(candidate.sourceTypeName)
            val registerName = "registerKotlinWinRTXaml${candidate.className}"
            val file = root.resolve(candidate.packageName.replace('.', '/'))
                .resolve("KotlinWinRTXaml${candidate.className}.kt")
            Files.createDirectories(file.parent)
            val code = buildString {
                if (candidate.packageName.isNotBlank()) appendLine("package ${candidate.packageName}")
                source.imports().forEach { appendLine("import $it") }
                appendLine()
                appendLine("internal fun $registerName() {")
                appendLine("  io.github.composefluent.winrt.runtime.registerWinRTXamlTypeDefinition(")
                appendLine("    io.github.composefluent.winrt.runtime.WinRTXamlTypeDefinition(")
                appendLine("      type = ${candidate.className}::class,")
                appendLine("      name = ${candidate.sourceTypeName.kotlinLiteral()},")
                appendLine("      baseName = ${requireNotNull(candidate.winRTBaseClassName).kotlinLiteral()},")
                appendLine("      baseType = ${source.superTypeNames(klass).first()}::class,")
                appendLine("      activate = { ${candidate.className}() },")
                schema.metadata.contentProperty?.let { appendLine("      contentProperty = ${it.kotlinLiteral()},") }
                appendLine("      members = listOf(")
                schema.properties.forEach { property ->
                    appendLine("        io.github.composefluent.winrt.runtime.WinRTXamlMemberDefinition(")
                    appendLine("          name = ${property.metadata.name.kotlinLiteral()},")
                    appendLine("          typeName = ${property.metadata.type.typeName.kotlinLiteral()},")
                    appendLine("          type = ${property.kotlinType.removeSuffix("?").substringBefore('<')}::class,")
                    appendLine("          get = { (it as ${candidate.className}).${property.metadata.name} },")
                    if (!property.metadata.isReadOnly) {
                        appendLine("          set = { instance, value -> (instance as ${candidate.className}).${property.metadata.name} = value as ${property.kotlinType} },")
                    }
                    appendLine("        ),")
                }
                appendLine("      ),")
                appendLine("    ),")
                appendLine("  )")
                appendLine("}")
            }
            file.writeText(code)
            "${candidate.packageName}.$registerName".removePrefix(".")
        }
        val registry = root.resolve("io/github/composefluent/winrt/generated/xaml/KotlinXamlApplicationDefinitions.kt")
        Files.createDirectories(registry.parent)
        registry.writeText(buildString {
            appendLine("package io.github.composefluent.winrt.generated.xaml")
            appendLine("object KotlinXamlApplicationDefinitions {")
            appendLine("  private val registration: Unit = run {")
            registrations.forEach { appendLine("    $it()") }
            appendLine("  }")
            appendLine("  fun registerAll() { registration }")
            appendLine("}")
        })
    }

    private fun scan(
        sourceRoots: Iterable<Path>,
        winRTTypes: Map<String, IndexedWinRTType>,
    ): List<KotlinWinRTAuthoredTypeCandidate> {
        val sourceFiles = sourceRoots
            .onEach { root ->
                require(Files.exists(root)) {
                    "kotlin-winrt authoring scanner source root $root does not exist."
                }
            }
            .flatMap(::kotlinSourceFiles)
            .distinct()
            .sorted()
        val sources = sourceFiles.map(::parseSource)
        val sourceClasses = sources.flatMap { source ->
            val packageName = source.packageName()
            val imports = parseImports(source)
            source.classes().mapNotNull { klass ->
                val className = source.className(klass) ?: return@mapNotNull null
                val sourceTypeName = if (packageName.isBlank()) className else "$packageName.$className"
                SourceClass(source, klass, packageName, imports, className, sourceTypeName)
            }
        }
        val sourceClassIndex = sourceClasses.associateBy(SourceClass::sourceTypeName)
        val sourceSubtypedNames = sourceClasses
            .flatMap { sourceClass ->
                sourceClass.source.superTypeNames(sourceClass.klass)
                    .mapNotNull { superType ->
                        resolveSourceTypeName(superType, sourceClass.packageName, sourceClass.imports, sourceClassIndex)
                    }
            }
            .toSet()
        val candidates = sourceClasses
            .mapNotNull { sourceClass -> scanSourceClass(sourceClass, sourceClassIndex, sourceSubtypedNames, winRTTypes) }
        val duplicateTypeNames = candidates
            .groupBy(KotlinWinRTAuthoredTypeCandidate::sourceTypeName)
            .filterValues { matches -> matches.size > 1 }
            .keys
            .sorted()
        require(duplicateTypeNames.isEmpty()) {
            "kotlin-winrt authoring scanner found duplicate authored type candidates: " +
                duplicateTypeNames.joinToString()
        }
        return candidates.sortedBy(KotlinWinRTAuthoredTypeCandidate::sourceTypeName)
    }

    private fun scanSourceClass(
        sourceClass: SourceClass,
        sourceClassIndex: Map<String, SourceClass>,
        sourceSubtypedNames: Set<String>,
        winRTTypes: Map<String, IndexedWinRTType>,
    ): KotlinWinRTAuthoredTypeCandidate? {
        val source = sourceClass.source
        val klass = sourceClass.klass
        val packageName = sourceClass.packageName
        val imports = sourceClass.imports
        val className = sourceClass.className
        val sourceTypeName = sourceClass.sourceTypeName
        if (!source.isEffectivelyAuthorableClass(klass)) {
            return null
        }
        if (sourceTypeName in sourceSubtypedNames && source.isUnsealedAuthoredClass(klass)) {
            return null
        }
        val projectedMetadataName = projectionPackageToMetadataName(sourceTypeName)
        if (sourceTypeName.startsWith(PROJECTION_PACKAGE_PREFIX) ||
            (projectedMetadataName != sourceTypeName && projectedMetadataName in winRTTypes)
        ) {
            return null
        }
        val annotation = source.authoredRuntimeClassAnnotation(klass, packageName, imports)
        val inheritedWinRTTypes = inheritedWinRTTypes(sourceClass, sourceClassIndex, winRTTypes)
        val annotatedBase = annotation.baseClassName
            ?.let { typeName ->
                resolveAnnotatedWinRTType(typeName, winRTTypes, sourceTypeName).also { type ->
                    require(type.kind == "RuntimeClass") {
                        "WinRT authored type $sourceTypeName annotation baseClassName must reference a WinRT runtime class: $typeName."
                    }
                }
            }
        val annotatedInterfaces = annotation.interfaceNames
            .map { typeName ->
                resolveAnnotatedWinRTType(typeName, winRTTypes, sourceTypeName).also { type ->
                    require(type.kind == "Interface") {
                        "WinRT authored type $sourceTypeName annotation interfaceNames must reference WinRT interfaces: $typeName."
                    }
                }
            }
        val annotatedOverridableInterfaces = annotation.overridableInterfaceNames
            .map { typeName ->
                resolveAnnotatedWinRTType(typeName, winRTTypes, sourceTypeName).also { type ->
                    require(type.kind == "Interface") {
                        "WinRT authored type $sourceTypeName annotation overridableInterfaceNames must reference WinRT interfaces: $typeName."
                    }
                }
            }
            .map(IndexedWinRTType::qualifiedName)
        val annotatedActivatableFactoryInterface = annotation.activatableFactoryInterfaceName
            ?.let { typeName ->
                resolveAnnotatedWinRTType(typeName, winRTTypes, sourceTypeName).also { type ->
                    require(type.kind == "Interface") {
                        "WinRT authored type $sourceTypeName annotation activatableFactoryInterfaceName must reference a WinRT interface: $typeName."
                    }
                }.qualifiedName
            }
        val annotatedStaticFactoryInterfaces = annotation.staticFactoryInterfaceNames
            .map { typeName ->
                resolveAnnotatedWinRTType(typeName, winRTTypes, sourceTypeName).also { type ->
                    require(type.kind == "Interface") {
                        "WinRT authored type $sourceTypeName annotation staticFactoryInterfaceNames must reference WinRT interfaces: $typeName."
                    }
                }
            }
            .map(IndexedWinRTType::qualifiedName)
        val resolvedWinRTTypes = listOfNotNull(annotatedBase) + annotatedInterfaces + inheritedWinRTTypes
        if (resolvedWinRTTypes.isEmpty()) {
            return null
        }
        require(source.isRuntimeClassDeclaration(klass)) {
            "WinRT authored type $sourceTypeName must be a concrete Kotlin class."
        }
        require(!source.isValueClass(klass)) {
            "WinRT authored type $sourceTypeName must not be a Kotlin value class."
        }
        require(!source.isEffectivelyPublicClass(klass) || source.hasPublicDefaultActivationConstructor(klass)) {
            "Public WinRT authored type $sourceTypeName must declare an accessible zero-argument constructor for default activation."
        }
        require(!source.hasTypeParameters(klass)) {
            "WinRT authored type $sourceTypeName must not be generic."
        }
        require(!source.isUnsealedAuthoredClass(klass)) {
            "WinRT authored class $sourceTypeName must be final."
        }
        require(!source.isNestedClass(klass)) {
            "WinRT authored type $sourceTypeName must be a top-level Kotlin type; " +
                "nested authored runtime classes are not supported."
        }
        val winRTBase = resolvedWinRTTypes.firstOrNull { type -> type.kind == "RuntimeClass" }
        val directInterfaces = resolvedWinRTTypes
            .filter { type -> type.kind == "Interface" }
            .map { type -> type.qualifiedName }
        val overridableInterfaces = (annotatedOverridableInterfaces + inheritedOverridableInterfaceNames(winRTBase, winRTTypes))
            .distinct()
            .sorted()
        return KotlinWinRTAuthoredTypeCandidate(
            packageName = packageName,
            className = className,
            sourceTypeName = sourceTypeName,
            winRTBaseClassName = winRTBase?.qualifiedName,
            winRTInterfaceNames = (directInterfaces + overridableInterfaces).distinct().sorted(),
            overridableInterfaceNames = overridableInterfaces,
            isPublic = source.isEffectivelyPublicClass(klass),
            activatableFactoryInterfaceName = annotatedActivatableFactoryInterface,
            staticFactoryInterfaceNames = annotatedStaticFactoryInterfaces.distinct().sorted(),
        )
    }

    private fun inheritedWinRTTypes(
        sourceClass: SourceClass,
        sourceClassIndex: Map<String, SourceClass>,
        winRTTypes: Map<String, IndexedWinRTType>,
        visitedSourceTypes: MutableSet<String> = mutableSetOf(),
    ): List<IndexedWinRTType> =
        sourceClass.source.superTypeNames(sourceClass.klass).flatMap { superType ->
            resolveIndexedWinRTType(superType, sourceClass.packageName, sourceClass.imports, winRTTypes)
                ?.let { return@flatMap listOf(it) }
            val resolvedSourceTypeName = resolveSourceTypeName(
                superType,
                sourceClass.packageName,
                sourceClass.imports,
                sourceClassIndex,
            ) ?: return@flatMap emptyList()
            if (!visitedSourceTypes.add(resolvedSourceTypeName)) {
                return@flatMap emptyList()
            }
            val resolvedSourceClass = sourceClassIndex[resolvedSourceTypeName] ?: return@flatMap emptyList()
            inheritedWinRTTypes(resolvedSourceClass, sourceClassIndex, winRTTypes, visitedSourceTypes)
        }

    private fun resolveSourceTypeName(
        typeName: String,
        packageName: String,
        imports: KotlinImports,
        sourceClassIndex: Map<String, SourceClass>,
    ): String? {
        val candidates = buildList {
            add(typeName)
            imports.explicit[typeName]?.let(::add)
            imports.wildcards.forEach { wildcard -> add("$wildcard.$typeName") }
            if (packageName.isNotBlank()) {
                add("$packageName.$typeName")
            }
        }
        return candidates.firstOrNull(sourceClassIndex::containsKey)
    }

    private data class SourceClass(
        val source: KotlinLightSource,
        val klass: LighterASTNode,
        val packageName: String,
        val imports: KotlinImports,
        val className: String,
        val sourceTypeName: String,
    )

    private fun parseSource(source: Path): KotlinLightSource {
        ensureKotlinApplicationEnvironment()
        val text = source.readText()
        val tree = KotlinLightParser.buildLightTree(
            text,
            InMemoryKtSourceFile(source.name, source.toAbsolutePath().toString(), text),
        ) { _: Int, _: Int, _: String? -> }
        return KotlinLightSource(text, tree)
    }

    private fun resolveAnnotatedWinRTType(
        typeName: String,
        winRTTypes: Map<String, IndexedWinRTType>,
        sourceTypeName: String,
    ): IndexedWinRTType =
        requireNotNull(resolveIndexedWinRTTypeByProjectedName(typeName, winRTTypes)) {
            "WinRT authored type $sourceTypeName annotation references unknown WinRT metadata type $typeName."
        }

    private fun ensureKotlinApplicationEnvironment() {
        if (ApplicationManager.getApplication() == null) {
            kotlinApplicationEnvironment
        }
    }

    private val kotlinApplicationEnvironment by lazy {
        ensureIntellijHomePath()
        KotlinCoreApplicationEnvironment.create(
            Disposer.newDisposable("kotlin-winrt-authoring-light-tree"),
            KotlinCoreApplicationEnvironmentMode.UnitTest,
        )
    }

    private fun ensureIntellijHomePath() {
        if (System.getProperty("idea.home.path") != null) return
        val home = Files.createTempDirectory("kotlin-winrt-intellij-home-")
        home.resolve("product-info.json").writeText("""{"name":"kotlin-winrt","version":"0"}""")
        System.setProperty("idea.home.path", home.toString())
    }

    private fun kotlinSourceFiles(root: Path): List<Path> {
        if (Files.isRegularFile(root)) {
            return if (root.extension == "kt") listOf(root) else emptyList()
        }
        if (!Files.isDirectory(root)) {
            return emptyList()
        }
        return Files.walk(root).use { stream ->
            stream.asSequence()
                .filter(Files::isRegularFile)
                .filter { path -> path.extension == "kt" }
                .toList()
        }
    }

    private fun parseImports(file: KotlinLightSource): KotlinImports {
        val explicit = linkedMapOf<String, String>()
        val wildcards = mutableListOf<String>()
        file.imports().forEach { imported ->
            val path = imported.substringBefore(" as ").trim()
            if (path.endsWith(".*")) {
                wildcards += path.removeSuffix(".*")
            } else if (path.isNotBlank()) {
                val alias = imported.substringAfter(" as ", missingDelimiterValue = "")
                    .trim()
                    .takeIf(String::isNotBlank)
                explicit[alias ?: path.substringAfterLast('.')] = path
            }
        }
        return KotlinImports(explicit, wildcards)
    }

    private data class CliOptions(
        val metadataIndex: Path,
        val output: Path,
        val sourceRoots: List<Path>,
        val xamlDeclarations: Path?,
        val xamlHeader: Boolean,
        val references: List<Path>,
        val xamlHeaderSources: Path?,
    ) {
        companion object {
            fun parse(args: Array<String>): CliOptions {
                var metadataIndex: Path? = null
                var output: Path? = null
                var xamlDeclarations: Path? = null
                var xamlHeader = false
                val references = mutableListOf<Path>()
                var xamlHeaderSources: Path? = null
                val sourceRoots = mutableListOf<Path>()
                var index = 0
                while (index < args.size) {
                    when (args[index]) {
                        "--metadata-index" -> {
                            metadataIndex = Path.of(argumentValue(args, index))
                            index += 2
                        }
                        "--output" -> {
                            output = Path.of(argumentValue(args, index))
                            index += 2
                        }
                        "--source-root" -> {
                            sourceRoots.add(Path.of(argumentValue(args, index)))
                            index += 2
                        }
                        "--xaml-declarations" -> {
                            xamlDeclarations = Path.of(argumentValue(args, index))
                            index += 2
                        }
                        "--xaml-header" -> { xamlHeader = true; index += 1 }
                        "--reference" -> { references.add(Path.of(argumentValue(args, index))); index += 2 }
                        "--xaml-header-sources" -> { xamlHeaderSources = Path.of(argumentValue(args, index)); index += 2 }
                        else -> error("Unknown kotlin-winrt authoring scanner argument: ${args[index]}")
                    }
                }
                return CliOptions(
                    metadataIndex = requireNotNull(metadataIndex) { "--metadata-index is required" },
                    output = requireNotNull(output) { "--output is required" },
                    sourceRoots = sourceRoots,
                    xamlDeclarations = xamlDeclarations,
                    xamlHeader = xamlHeader,
                    references = references,
                    xamlHeaderSources = xamlHeaderSources,
                )
            }

            private fun argumentValue(args: Array<String>, index: Int): String =
                args.getOrNull(index + 1)
                    ?: throw IllegalArgumentException(
                        "kotlin-winrt authoring scanner argument ${args[index]} requires a path value.",
                    )
        }
    }

    private data class KotlinLightSource(
        val text: String,
        val tree: FlyweightCapableTreeStructure<LighterASTNode>,
    ) {
        fun packageName(): String =
            tree.root.descendantsOfType(KtNodeTypes.PACKAGE_DIRECTIVE)
                .firstOrNull()
                ?.let(::nodeText)
                ?.substringAfter("package", missingDelimiterValue = "")
                ?.trim()
                .orEmpty()

        fun imports(): List<String> =
            Regex("""(?m)^\s*import\s+([^\r\n]+)""")
                .findAll(text)
                .map { match -> match.groupValues[1].trim() }
                .toList()

        fun classes(): List<LighterASTNode> =
            tree.root.descendantsOfType(KtNodeTypes.CLASS)

        fun xamlApplicationMembers(
            classNode: LighterASTNode,
            applicationNames: Set<String>,
            indexedTypes: Map<String, IndexedWinRTType>,
        ): XamlSourceMembers {
            val packageName = packageName()
            val imports = parseImports(this)
            fun resolve(raw: String): WinRTTypeRef {
                val nullable = raw.trim().endsWith('?')
                val parsed = WinRTTypeRef.fromDisplayName(raw)
                val arguments = parsed.typeArguments.map { resolve(it.typeName) }
                val name = requireNotNull(parsed.qualifiedName) { "XAML property requires a named type: $raw" }
                if (name == "Array" || name == "kotlin.Array") {
                    require(arguments.size == 1) { "XAML property Array requires one element type: $raw" }
                    return WinRTTypeRef.array(arguments.single())
                }
                val primitive = winRTFundamentalTypeForName(name.removePrefix("kotlin."))
                val local = buildList {
                    add(name)
                    imports.explicit[name]?.let(::add)
                    imports.wildcards.forEach { add("$it.$name") }
                    if (packageName.isNotBlank()) add("$packageName.$name")
                }.firstOrNull(applicationNames::contains)
                val indexed = resolveIndexedWinRTType(name, packageName, imports, indexedTypes)
                val metadataName = when {
                    primitive != null -> primitive.toKotlinProjectionTypeName()
                    name == "Any" || name == "kotlin.Any" -> "System.Object"
                    local != null -> local
                    indexed != null -> indexed.qualifiedName.substringBefore('`') +
                        if (arguments.isEmpty()) "" else "`${arguments.size}"
                    else -> error("XAML property type $raw has no WinRT metadata projection")
                }
                val result = WinRTTypeRef.named(metadataName, arguments)
                val isValue = primitive?.isWinRTValueType == true ||
                    indexed?.kind in setOf(WinRTTypeKind.Enum.name, WinRTTypeKind.Struct.name)
                return if (nullable && isValue)
                    WinRTTypeRef.named("Windows.Foundation.IReference`1", listOf(result)) else result
            }
            val body = classNode.children().firstOrNull { it.tokenType == KtNodeTypes.CLASS_BODY }
            val properties = body?.children().orEmpty()
                .filter { it.tokenType == KtNodeTypes.PROPERTY }
                .filterNot { property -> hasModifier(property, KtTokens.PRIVATE_KEYWORD, KtTokens.PROTECTED_KEYWORD) }
                .mapNotNull { property ->
                    val typeNode = requireNotNull(property.children().firstOrNull {
                        it.tokenType == KtNodeTypes.TYPE_REFERENCE
                    }) {
                        "Public Kotlin XAML property in ${className(classNode)} requires an explicit type " +
                            "so XamlCompiler pass 1 can resolve it before Kotlin semantic compilation"
                    }
                    val name = property.descendants().dropWhile {
                        it.tokenType != KtTokens.VAL_KEYWORD && it.tokenType != KtTokens.VAR_KEYWORD
                    }.drop(1).firstOrNull { it.tokenType == KtTokens.IDENTIFIER }?.let(::nodeText)
                        ?: error("XAML property declaration is missing a name")
                    val setter = property.children().firstOrNull { accessor ->
                        accessor.tokenType == KtNodeTypes.PROPERTY_ACCESSOR &&
                            accessor.descendants().any { it.tokenType == KtTokens.SET_KEYWORD }
                    }
                    val mutable = property.descendants().any { it.tokenType == KtTokens.VAR_KEYWORD }
                    val rawType = nodeText(typeNode).trim()
                    XamlSourceProperty(WinRTXamlApplicationProperty(name, resolve(rawType),
                        isReadOnly = !mutable || setter?.let {
                            hasModifier(it, KtTokens.PRIVATE_KEYWORD, KtTokens.PROTECTED_KEYWORD)
                        } == true), rawType)
                }
            val modifier = classNode.children().firstOrNull { it.tokenType == KtNodeTypes.MODIFIER_LIST }
                ?.let(::nodeText).orEmpty()
            val contentProperty = Regex("""@(?:[\w.]+\.)?WinRTXamlContentProperty\s*\(\s*"([\w]+)"\s*\)""")
                .find(modifier)?.groupValues?.get(1)
            return XamlSourceMembers(WinRTXamlApplicationTypeMembers(
                properties.map(XamlSourceProperty::metadata), contentProperty), properties)
        }

        fun isEffectivelyAuthorableClass(classNode: LighterASTNode): Boolean =
            isPublicOrInternalClass(classNode) &&
                classes()
                    .filter { candidate -> candidate !== classNode }
                    .filter { candidate -> candidate.startOffset < classNode.startOffset && candidate.endOffset > classNode.endOffset }
                    .all(::isPublicOrInternalClass)

        fun isEffectivelyPublicClass(classNode: LighterASTNode): Boolean =
            isPublicClass(classNode) &&
                classes()
                    .filter { candidate -> candidate !== classNode }
                    .filter { candidate -> candidate.startOffset < classNode.startOffset && candidate.endOffset > classNode.endOffset }
                    .all(::isPublicClass)

        fun isNestedClass(classNode: LighterASTNode): Boolean =
            classes()
                .filter { candidate -> candidate !== classNode }
                .any { candidate -> candidate.startOffset < classNode.startOffset && candidate.endOffset > classNode.endOffset }

        fun hasTypeParameters(classNode: LighterASTNode): Boolean =
            classNode.children().any { child -> child.tokenType == KtNodeTypes.TYPE_PARAMETER_LIST }

        fun isRuntimeClassDeclaration(classNode: LighterASTNode): Boolean =
            classDeclarationKeyword(classNode) == KtTokens.CLASS_KEYWORD

        fun isValueClass(classNode: LighterASTNode): Boolean =
            hasModifier(classNode, KtTokens.VALUE_KEYWORD, KtTokens.INLINE_KEYWORD)

        fun hasPublicDefaultActivationConstructor(classNode: LighterASTNode): Boolean {
            val constructors = classNode.descendantsOfType(KtNodeTypes.PRIMARY_CONSTRUCTOR) +
                classNode.descendantsOfType(KtNodeTypes.SECONDARY_CONSTRUCTOR)
            if (constructors.isEmpty()) {
                return true
            }
            return constructors.any { constructor ->
                isPublicConstructor(constructor) && !constructor.hasValueParameters()
            }
        }

        fun isUnsealedAuthoredClass(classNode: LighterASTNode): Boolean =
            classDeclarationKeyword(classNode) == KtTokens.CLASS_KEYWORD &&
                hasModifier(classNode, KtTokens.OPEN_KEYWORD, KtTokens.ABSTRACT_KEYWORD, KtTokens.SEALED_KEYWORD)

        private fun isPublicConstructor(constructorNode: LighterASTNode): Boolean =
            !hasModifier(constructorNode, KtTokens.PRIVATE_KEYWORD, KtTokens.INTERNAL_KEYWORD, KtTokens.PROTECTED_KEYWORD)

        private fun LighterASTNode.hasValueParameters(): Boolean =
            children()
                .firstOrNull { child -> child.tokenType == KtNodeTypes.VALUE_PARAMETER_LIST }
                ?.children()
                .orEmpty()
                .any { child -> child.tokenType == KtNodeTypes.VALUE_PARAMETER }

        fun className(classNode: LighterASTNode): String? {
            var seenDeclarationKeyword = false
            return classNode.descendants().firstNotNullOfOrNull { node ->
                when (node.tokenType) {
                    KtTokens.CLASS_KEYWORD,
                    KtTokens.INTERFACE_KEYWORD,
                    KtTokens.OBJECT_KEYWORD,
                    -> {
                        seenDeclarationKeyword = true
                        null
                    }
                    KtTokens.IDENTIFIER -> if (seenDeclarationKeyword) nodeText(node) else null
                    else -> null
                }
            }
        }

        fun superTypeNames(classNode: LighterASTNode): List<String> =
            classNode.children()
                .firstOrNull { child -> child.tokenType == KtNodeTypes.SUPER_TYPE_LIST }
                ?.children()
                .orEmpty()
                .filter { child ->
                    child.tokenType == KtNodeTypes.SUPER_TYPE_ENTRY ||
                        child.tokenType == KtNodeTypes.SUPER_TYPE_CALL_ENTRY ||
                        child.tokenType == KtNodeTypes.DELEGATED_SUPER_TYPE_ENTRY
                }
                .mapNotNull { entry ->
                    entry.descendantsOfType(KtNodeTypes.USER_TYPE)
                        .firstOrNull()
                        ?.let(::nodeText)
                        ?.substringBefore('<')
                        ?.trim()
                        ?.takeIf(String::isNotBlank)
                }

        fun authoredRuntimeClassAnnotation(
            classNode: LighterASTNode,
            packageName: String,
            imports: KotlinImports,
        ): KotlinWinRTAuthoredRuntimeClassAnnotation {
            val leadingDeclarationText = text.substring(0, classNode.startOffset)
            val modifierText = classNode.children()
                .firstOrNull { child -> child.tokenType == KtNodeTypes.MODIFIER_LIST }
                ?.let(::nodeText)
                .orEmpty()
            val modifierAnnotationText = authoredRuntimeClassAnnotationText(modifierText, packageName, imports)
            val annotationText = modifierAnnotationText
                ?.takeIf { annotation -> annotation.substringAfter('@').contains('(') }
                ?: authoredRuntimeClassAnnotationText(leadingDeclarationText, packageName, imports)
                ?: modifierAnnotationText
                ?: return KotlinWinRTAuthoredRuntimeClassAnnotation()
            val positionalArguments = annotationPositionalArguments(annotationText)
            return KotlinWinRTAuthoredRuntimeClassAnnotation(
                baseClassName = (
                    annotationStringArgument(annotationText, "baseClassName")
                        .takeIf(String::isNotBlank)
                        ?: positionalArguments.getOrNull(0).stringLiteralArgument()
                    ).takeIf(String::isNotBlank),
                interfaceNames = annotationStringArrayArgument(annotationText, "interfaceNames")
                    .ifEmpty { positionalArguments.getOrNull(1).stringArrayArgument() },
                overridableInterfaceNames = annotationStringArrayArgument(annotationText, "overridableInterfaceNames")
                    .ifEmpty { positionalArguments.getOrNull(2).stringArrayArgument() },
                activatableFactoryInterfaceName = annotationStringArgument(annotationText, "activatableFactoryInterfaceName")
                    .takeIf(String::isNotBlank)
                    ?: positionalArguments.getOrNull(3).stringLiteralArgument().takeIf(String::isNotBlank),
                staticFactoryInterfaceNames = annotationStringArrayArgument(annotationText, "staticFactoryInterfaceNames")
                    .ifEmpty { positionalArguments.getOrNull(4).stringArrayArgument() },
            )
        }

        private fun LighterASTNode.children(): List<LighterASTNode> = getChildren(tree)

        private fun isPublicClass(classNode: LighterASTNode): Boolean {
            val modifierList = classNode.children().firstOrNull { child -> child.tokenType == KtNodeTypes.MODIFIER_LIST }
                ?: return true
            return modifierList.descendants().none { node ->
                node.tokenType == KtTokens.PRIVATE_KEYWORD ||
                    node.tokenType == KtTokens.INTERNAL_KEYWORD ||
                    node.tokenType == KtTokens.PROTECTED_KEYWORD
            }
        }

        private fun isPublicOrInternalClass(classNode: LighterASTNode): Boolean {
            val modifierList = classNode.children().firstOrNull { child -> child.tokenType == KtNodeTypes.MODIFIER_LIST }
                ?: return true
            return modifierList.descendants().none { node ->
                node.tokenType == KtTokens.PRIVATE_KEYWORD ||
                    node.tokenType == KtTokens.PROTECTED_KEYWORD
            }
        }

        private fun hasModifier(classNode: LighterASTNode, vararg modifiers: IElementType): Boolean {
            val modifierTypes = modifiers.toSet()
            val modifierList = classNode.children().firstOrNull { child -> child.tokenType == KtNodeTypes.MODIFIER_LIST }
                ?: return false
            return modifierList.descendants().any { node -> node.tokenType in modifierTypes }
        }

        private fun classDeclarationKeyword(classNode: LighterASTNode): IElementType? =
            classNode.descendants()
                .firstOrNull { node ->
                    node.tokenType == KtTokens.CLASS_KEYWORD ||
                        node.tokenType == KtTokens.INTERFACE_KEYWORD ||
                        node.tokenType == KtTokens.OBJECT_KEYWORD
                }
                ?.tokenType

        private fun LighterASTNode.descendants(): Sequence<LighterASTNode> =
            sequence {
                yield(this@descendants)
                children().forEach { child -> yieldAll(child.descendants()) }
            }

        private fun LighterASTNode.descendantsOfType(type: IElementType): List<LighterASTNode> =
            descendants().filter { node -> node.tokenType == type }.toList()

        private fun nodeText(node: LighterASTNode): String =
            text.substring(node.startOffset, node.endOffset)

        private fun authoredRuntimeClassAnnotationText(
            modifierText: String,
            packageName: String,
            imports: KotlinImports,
        ): String? {
            val acceptedNames = linkedSetOf(
                WINRT_AUTHORED_RUNTIME_CLASS_ANNOTATION,
            )
            imports.explicit
                .filterValues { it == WINRT_AUTHORED_RUNTIME_CLASS_ANNOTATION }
                .keys
                .forEach(acceptedNames::add)
            if (WINRT_AUTHORED_RUNTIME_CLASS_ANNOTATION.substringBeforeLast('.') in imports.wildcards) {
                acceptedNames += "WinRTAuthoredRuntimeClass"
            }
            if (packageName == WINRT_AUTHORED_RUNTIME_CLASS_ANNOTATION.substringBeforeLast('.')) {
                acceptedNames += "WinRTAuthoredRuntimeClass"
            }
            return acceptedNames.firstNotNullOfOrNull { name ->
                annotationTextForName(modifierText, name)
            }
        }

        private fun annotationTextForName(modifierText: String, name: String): String? {
            val match = Regex("""@${Regex.escape(name)}\b""").findAll(modifierText).lastOrNull() ?: return null
            var index = match.range.last + 1
            while (index < modifierText.length && modifierText[index].isWhitespace()) {
                index += 1
            }
            if (modifierText.getOrNull(index) != '(') {
                return modifierText.substring(match.range.first, index)
            }
            var depth = 0
            var inString = false
            var escaped = false
            while (index < modifierText.length) {
                val char = modifierText[index]
                when {
                    escaped -> escaped = false
                    char == '\\' && inString -> escaped = true
                    char == '"' -> inString = !inString
                    !inString && char == '(' -> depth += 1
                    !inString && char == ')' -> {
                        depth -= 1
                        if (depth == 0) {
                            return modifierText.substring(match.range.first, index + 1)
                        }
                    }
                }
                index += 1
            }
            return modifierText.substring(match.range.first)
        }

        private fun annotationStringArgument(annotationText: String, name: String): String =
            Regex("\\b${Regex.escape(name)}\\s*=\\s*\"([^\"]*)\"")
                .find(annotationText)
                ?.groupValues
                ?.get(1)
                .orEmpty()

        private fun annotationStringArrayArgument(annotationText: String, name: String): List<String> {
            val body = Regex("\\b${Regex.escape(name)}\\s*=\\s*\\[([^\\]]*)]")
                .find(annotationText)
                ?.groupValues
                ?.get(1)
                ?: return emptyList()
            return Regex("\"([^\"]*)\"")
                .findAll(body)
                .map { match -> match.groupValues[1] }
                .toList()
        }

        private fun annotationPositionalArguments(annotationText: String): List<String> {
            val body = annotationText.substringAfter('(', missingDelimiterValue = "")
                .substringBeforeLast(')', missingDelimiterValue = "")
                .takeIf(String::isNotBlank)
                ?: return emptyList()
            return splitTopLevelArguments(body)
                .filterNot(::hasTopLevelEquals)
        }

        private fun splitTopLevelArguments(body: String): List<String> {
            val arguments = mutableListOf<String>()
            var start = 0
            var bracketDepth = 0
            var inString = false
            var escaped = false
            body.forEachIndexed { index, char ->
                when {
                    escaped -> escaped = false
                    char == '\\' && inString -> escaped = true
                    char == '"' -> inString = !inString
                    !inString && char == '[' -> bracketDepth += 1
                    !inString && char == ']' -> bracketDepth -= 1
                    !inString && char == ',' && bracketDepth == 0 -> {
                        arguments += body.substring(start, index).trim()
                        start = index + 1
                    }
                }
            }
            arguments += body.substring(start).trim()
            return arguments.filter(String::isNotBlank)
        }

        private fun hasTopLevelEquals(argument: String): Boolean {
            var inString = false
            var escaped = false
            argument.forEach { char ->
                when {
                    escaped -> escaped = false
                    char == '\\' && inString -> escaped = true
                    char == '"' -> inString = !inString
                    !inString && char == '=' -> return true
                }
            }
            return false
        }

        private fun String?.stringLiteralArgument(): String =
            this
                ?.let { Regex("^\\s*\"([^\"]*)\"\\s*$").find(it) }
                ?.groupValues
                ?.get(1)
                .orEmpty()

        private fun String?.stringArrayArgument(): List<String> {
            val body = this
                ?.let { Regex("^\\s*\\[([^\\]]*)]\\s*$").find(it) }
                ?.groupValues
                ?.get(1)
                ?: return emptyList()
            return Regex("\"([^\"]*)\"")
                .findAll(body)
                .map { match -> match.groupValues[1] }
                .toList()
        }
    }

    private class InMemoryKtSourceFile(
        override val name: String,
        override val path: String?,
        private val contents: String,
    ) : KtSourceFile {
        override fun getContentsAsStream(): InputStream =
            ByteArrayInputStream(contents.toByteArray())

        override fun equals(other: Any?): Boolean =
            this === other || other is InMemoryKtSourceFile &&
                name == other.name &&
                path == other.path &&
                contents == other.contents

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + (path?.hashCode() ?: 0)
            result = 31 * result + contents.hashCode()
            return result
        }
    }
}
