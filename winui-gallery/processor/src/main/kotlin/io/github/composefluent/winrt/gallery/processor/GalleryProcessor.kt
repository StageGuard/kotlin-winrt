package io.github.composefluent.winrt.gallery.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class GalleryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        GalleryProcessor(
            environment.codeGenerator,
            environment.logger,
            requireNotNull(environment.options["gallery.repositoryRoot"]) { "Gallery KSP requires gallery.repositoryRoot" },
        )
}

private class GalleryProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val repositoryRoot: String,
) : SymbolProcessor {
    private val entries = linkedMapOf<String, Entry>()
    private val sources = linkedSetOf<KSFile>()
    private val symbolNames = sortedSetOf<String>()
    private var failed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val deferred = mutableListOf<KSAnnotated>()
        // Symbol is a projected value class, not a Kotlin enum. Resolve its
        // generated companion through KSP so JVM and Native need no reflection.
        val symbolType = "microsoft.ui.xaml.controls.Symbol"
        resolver.getClassDeclarationByName(resolver.getKSNameFromString(symbolType))?.let { symbol ->
            symbol.containingFile?.let(sources::add)
            symbol.declarations.filterIsInstance<KSClassDeclaration>().filter { it.isCompanionObject }.forEach { companion ->
                companion.declarations.filterIsInstance<KSPropertyDeclaration>().filter {
                    it.type.resolve().declaration.qualifiedName?.asString() == symbolType
                }.forEach { symbolNames += it.simpleName.asString() }
            }
        }
        for (kind in listOf("GalleryGroupEntry", "GalleryPage", "GallerySample")) {
            val annotationName = "$galleryPackage.$kind"
            for (symbol in resolver.getSymbolsWithAnnotation(annotationName)) {
                val declaration = symbol as? KSDeclaration
                if (declaration == null) {
                    logger.error("@$kind requires a declaration", symbol)
                    failed = true
                    continue
                }
                val annotation = declaration.annotations.firstOrNull {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
                }
                if (annotation == null) {
                    deferred += symbol
                    continue
                }
                val arguments = annotation.arguments.associate { it.name!!.asString() to it.value.toString() }
                val homePage = kind == "GalleryPage" && arguments["route"] == "Home"
                if (declaration.parentDeclaration != null && !homePage) {
                    logger.error("@$kind requires a top-level declaration", symbol)
                    failed = true
                    continue
                }
                if (kind in setOf("GalleryPage", "GallerySample") && !homePage && (declaration !is KSFunctionDeclaration ||
                        (kind == "GalleryPage" && declaration.parameters.isNotEmpty()) || declaration.extensionReceiver != null ||
                        declaration.typeParameters.isNotEmpty() || Modifier.SUSPEND in declaration.modifiers ||
                        Modifier.PRIVATE in declaration.modifiers)) {
                    logger.error("@$kind requires an accessible, non-generic, non-suspend top-level function; pages must be parameterless", symbol)
                    failed = true
                    continue
                }
                val name = declaration.qualifiedName?.asString() ?: continue
                val file = declaration.containingFile
                entries["$kind:$name"] = Entry(
                    kind, arguments, name, file?.fileName.orEmpty(),
                    if (kind == "GalleryPage") repositoryRelativePath(checkNotNull(file).filePath, repositoryRoot) else "",
                )
                file?.let(sources::add)
            }
        }
        return deferred
    }

    override fun finish() {
        if (failed || entries.isEmpty()) return
        val descriptions = checkNotNull(javaClass.getResourceAsStream("/ControlInfoData.json"))
            .bufferedReader().use { Json.parseToJsonElement(it.readText()).jsonObject }
        val output = try {
            generate(entries.values.toList(), descriptions)
        } catch (exception: IllegalArgumentException) {
            logger.error(exception.message.orEmpty())
            return
        }
        codeGenerator.createNewFile(
            Dependencies(aggregating = true, *sources.toTypedArray()), galleryPackage, "GeneratedGallery",
        ).bufferedWriter().use { it.write(output) }
        generateSourcePreviews()
        if (symbolNames.isEmpty()) {
            logger.error("The generated WinUI Symbol projection must be available to Gallery KSP processing")
            return
        }
        codeGenerator.createNewFile(
            Dependencies(aggregating = true, *sources.toTypedArray()), galleryPackage, "GeneratedGallerySymbols",
        ).bufferedWriter().use { writer ->
            writer.appendLine("// Generated from WinUI Symbol companion properties by KSP.")
            writer.appendLine("package $galleryPackage")
            writer.appendLine("internal actual object GallerySymbols {")
            writer.appendLine("  actual fun find(name: String): microsoft.ui.xaml.controls.Symbol? = when (name) {")
            symbolNames.forEach { name -> writer.appendLine("    \"$name\" -> microsoft.ui.xaml.controls.Symbol.`$name`") }
            writer.appendLine("    else -> null")
            writer.appendLine("  }")
            writer.appendLine("}")
        }
    }

    private fun generateSourcePreviews() {
        val pages = entries.values.filter { it.kind == "GalleryPage" && it.route != "Home" }.sortedBy { it.route }
        val samples = entries.values.filter { it.kind == "GallerySample" }
        val sampleByTitle = samples.associateBy { it.route to it.value("title") }
        require(sampleByTitle.size == samples.size) { "Duplicate @GallerySample route/title" }
        require(samples.all { sample -> pages.any { it.route == sample.route } }) { "@GallerySample references an unknown page" }
        val texts = (pages + samples).map { it.source }.distinct().associateWith { fileName ->
            val file = sources.single { it.fileName == fileName }
            java.io.File(file.filePath).readText().replace("\r\n", "\n").replace('\r', '\n')
        }
        val extractor = KotlinExampleExtractor()
        val sampleExtractor = KotlinSampleSourceExtractor()
        val parser = KotlinSourceParser()
        data class RoutePreviews(val titleIndices: Map<String, Int>, val count: Int)
        val examples = pages.mapIndexed { routeIndex, page ->
            val origins = mutableMapOf<String, KotlinCodeOriginData>()
            fun document(name: String, fileName: String, fragment: KotlinSourceFragment): Pair<String, io.github.composefluent.winrt.gallery.code.KotlinCodeDocument> {
                origins[name] = KotlinCodeOriginData(
                    repositoryRelativePath(sources.single { it.fileName == fileName }.filePath, repositoryRoot), fragment)
                return name to parser.parse(fileName, fragment.source, isScript = true)
            }
            val snippets = extractor.extract(texts.getValue(page.source), page.symbol.substringAfterLast('.'))
            require(snippets.isNotEmpty()) { "Could not locate source for ${page.symbol} in ${page.source}" }
            val objects = snippets.mapIndexed { exampleIndex, snippet ->
                val sample = snippet.titles.firstNotNullOfOrNull { sampleByTitle[page.route to it] }
                val fragment = sample?.let {
                    sampleExtractor.extractFragment(texts.getValue(it.source), it.symbol.substringAfterLast('.'))
                } ?: snippet.fragment
                document("GalleryCode${routeIndex}_$exampleIndex", sample?.source ?: page.source, fragment)
            }.toMutableList()
            val titleIndices = snippets.flatMapIndexed { index, snippet -> snippet.titles.map { it to index } }
                .toMap().toMutableMap()
            // Some pages compose examples through a helper rather than calling
            // example() directly. Their annotated sample factories still own the
            // CodeView source and are indexed by their declared titles.
            samples.filter { it.route == page.route && it.value("title") !in titleIndices }.forEach { sample ->
                val index = objects.size
                val body = sampleExtractor.extractFragment(texts.getValue(sample.source), sample.symbol.substringAfterLast('.'))
                objects += document("GalleryCode${routeIndex}_$index", sample.source, body)
                titleIndices[sample.value("title")] = index
            }
            codeGenerator.createNewFile(
                Dependencies(true, *sources.toTypedArray()), galleryPackage, "GalleryCode$routeIndex",
            ).bufferedWriter().use { it.write(generateCodeDocuments(objects, origins)) }
            RoutePreviews(titleIndices, objects.size)
        }
        codeGenerator.createNewFile(
            Dependencies(true, *sources.toTypedArray()), galleryPackage, "GeneratedGalleryCode",
        ).bufferedWriter().use { writer ->
            writer.appendLine("package $galleryPackage")
            writer.appendLine("internal actual object GalleryCodeCatalog {")
            writer.appendLine("  actual fun document(route: String, title: String, index: Int): $galleryPackage.code.KotlinCodeDocument? = when (route) {")
            pages.forEachIndexed { routeIndex, page ->
                val previews = examples[routeIndex]
                writer.appendLine("    ${kotlinLiteral(page.route)} -> when (title) {")
                previews.titleIndices.forEach { (title, exampleIndex) ->
                    writer.appendLine("      ${kotlinLiteral(title)} -> GalleryCode${routeIndex}_$exampleIndex.create()")
                }
                writer.appendLine("      else -> when (index) {")
                (0 until previews.count).forEach { exampleIndex ->
                    writer.appendLine("        $exampleIndex -> GalleryCode${routeIndex}_$exampleIndex.create()")
                }
                writer.appendLine("        else -> GalleryCode${routeIndex}_${previews.count - 1}.create()")
                writer.appendLine("      }")
                writer.appendLine("    }")
            }
            writer.appendLine("    else -> null\n  }\n}")
        }
    }
}
