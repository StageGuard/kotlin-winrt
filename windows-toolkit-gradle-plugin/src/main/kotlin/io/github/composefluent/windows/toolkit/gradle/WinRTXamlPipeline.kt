package io.github.composefluent.windows.toolkit.gradle

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinApiPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinJvmFactory
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File

internal fun isXamlSemanticTask(name: String) = name.startsWith("compileKotlinWinRTXamlSemantic")

// Kotlin 2.4.0 exposes no public factory for cloning KMP fragments into a standalone
// compilation. Keep this version-coupled adaptation here, never in runtime contracts.
@OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
internal fun configureWinRTXamlPipeline(
    project: Project,
    extension: WindowsExtension,
    sourceRoots: Provider<List<File>>,
    metadataManifest: Provider<RegularFile>,
    metadataIndex: Provider<RegularFile>,
    candidates: TaskProvider<GenerateWinRTAuthoringCandidatesTask>,
    compilerPluginClasspath: FileCollection,
) {
    val hasXaml = project.provider {
        sourceRoots.get().any { root -> root.isDirectory && root.walkTopDown().any { it.isFile && it.extension.equals("xaml", true) } }
    }
    val localCompilerDirectory = extension.xaml.compilerDirectory
    val resolveCompiler = project.tasks.register("resolveWinRTXamlCompiler", ResolveWinRTXamlCompilerTask::class.java) { task ->
        task.compilerVersion.set(extension.xaml.compilerVersion)
        task.archiveUrl.set(extension.xaml.archiveUrl)
        task.archiveSha256.set(extension.xaml.archiveSha256)
        task.offline.set(project.gradle.startParameter.isOffline)
        task.cacheDirectory.set(project.gradle.gradleUserHomeDir.resolve("caches/kotlin-winrt/xamlc"))
        task.outputDirectory.set(project.layout.buildDirectory.dir("tools/kotlin-xamlc"))
        task.onlyIf { hasXaml.get() && !localCompilerDirectory.isPresent }
    }
    fun configure(task: CompileWinRTXamlTask) {
        task.group = "kotlin-winrt"
        task.sourceRoots.from(sourceRoots)
        task.preparedMetadataManifest.set(metadataManifest)
        task.compilerDirectory.set(extension.xaml.compilerDirectory.orElse(resolveCompiler.flatMap { it.outputDirectory }))
        // DirectoryProperty.orElse can lose producer inference through an absent override.
        // The resolver skips itself for local overrides, but must precede input validation.
        task.dependsOn(resolveCompiler)
        task.genXbfDirectory.set(extension.xaml.genXbfDirectory.orElse(project.layout.dir(metadataManifest.map { manifest ->
            val winui = readPreparedMetadataCache(manifest.asFile.toPath()).files
                .singleOrNull { it.fileName.toString().equals("Microsoft.UI.Xaml.winmd", true) }
                ?: error("Kotlin XAML requires one resolved Microsoft.UI.Xaml.winmd reference.")
            winui.parent.parent.resolve("tools").toFile().also { tools ->
                require(File(tools, "x64/GenXbf.dll").isFile) {
                    "The selected WinUI package has no x64 GenXbf.dll at $tools; configure windows.xaml.genXbfDirectory."
                }
            }
        })))
        task.minimumWindowsVersion.set(extension.xaml.minimumWindowsVersion)
        task.projectName.set(project.name)
        task.onlyIf { hasXaml.get() }
    }
    val applicationHeader = project.tasks.register("generateWinRTXamlApplicationHeader",
        GenerateWinRTXamlApplicationHeaderTask::class.java) { task ->
        task.group = "kotlin-winrt"
        task.description = "Exports adjacent Kotlin XAML type declarations for XamlCompiler pass 1."
        task.sourceRoots.from(sourceRoots)
        task.metadataIndex.set(metadataIndex)
        task.preparedMetadataManifest.set(metadataManifest)
        task.scannerClasspath.from(compilerPluginClasspath)
        task.scannerClasspath.from(kotlinWinRTAuthoringScannerRuntimeClasspath(project))
        task.scannerJvmArgs.set(listOf("-Xmx512m", "-Xss512k", "-XX:+UseSerialGC", "-XX:ReservedCodeCacheSize=32m"))
        task.outputFile.set(project.layout.buildDirectory.file("generated/kotlin-winrt/xaml/application/KotlinXaml.winmd"))
        task.sourceOutputDirectory.set(project.layout.buildDirectory.dir("generated/kotlin-winrt/xaml/application/src"))
        task.onlyIf { hasXaml.get() }
    }
    val declarations = project.tasks.register("analyzeWinRTXaml", CompileWinRTXamlTask::class.java) { task ->
        configure(task)
        task.applicationHeaderWinmd.set(applicationHeader.flatMap { it.outputFile })
        task.description = "Analyzes XAML declarations before Kotlin authoring and semantic compilation."
        task.outputDirectory.set(project.layout.buildDirectory.dir("generated/kotlin-winrt/xaml/declarations"))
    }
    candidates.configure { task ->
        task.xamlDeclarations.set(hasXaml.flatMap { enabled ->
            if (enabled) declarations.flatMap { it.declarationsFile } else project.providers.provider { null }
        })
    }
    // This callback is registered after standalone projection compilation. Its libraries are
    // already attached to each business compilation, so no final application output is needed.
    project.afterEvaluate {
        if (!hasXaml.get()) return@afterEvaluate
        val kotlinApi = project.plugins.getPlugin(KotlinApiPlugin::class.java) as KotlinJvmFactory
        val businessTasks = project.tasks.withType(KotlinJvmCompile::class.java).toList().filter {
            !it.name.contains("Test", true) && !it.name.startsWith("compileKotlinWinRT")
        }
        businessTasks.forEach { business ->
            val suffix = business.name.removePrefix("compileKotlin")
            val semanticRoot = project.layout.buildDirectory.dir("intermediates/kotlin-winrt/xaml/$suffix/semantic")
            val symbols = semanticRoot.map { it.file("symbols.json") }
            val semanticOptions = kotlinApi.createCompilerJvmOptions().apply {
                apiVersion.set(business.compilerOptions.apiVersion)
                languageVersion.set(business.compilerOptions.languageVersion)
                optIn.set(business.compilerOptions.optIn)
                jvmTarget.set(business.compilerOptions.jvmTarget)
                jvmDefault.set(business.compilerOptions.jvmDefault)
                moduleName.set("${project.name}-xaml-semantic")
                freeCompilerArgs.set(business.compilerOptions.freeCompilerArgs.map(::withoutKotlinWinRTCompilerPluginOptions))
                freeCompilerArgs.addAll(project.provider {
                    listOf("xamlDeclarations=${declarations.get().declarationsFile.get().asFile.absolutePath}",
                        "metadataIndex=${metadataIndex.get().asFile.absolutePath}",
                        "xamlSemanticOutput=${symbols.get().asFile.absolutePath}",
                        "xamlReferencesFile=${declarations.get().outputDirectory.file("references.txt").get().asFile.absolutePath}")
                        .flatMap { listOf("-P", "plugin:io.github.composefluent.winrt.compiler:$it") }
                })
            }
            val semantic = kotlinApi.registerKotlinJvmCompileTask("compileKotlinWinRTXamlSemantic$suffix", semanticOptions)
            semantic.configure { task ->
                task.description = "Compiles isolated XAML semantic symbols; output classes are never packaged."
                task.group = "kotlin-winrt"
                task.source(business.sources)
                task.source(applicationHeader.flatMap { it.sourceOutputDirectory })
                task.libraries.from(business.libraries)
                task.friendPaths.from(business.friendPaths)
                task.pluginClasspath.from(compilerPluginClasspath)
                task.multiPlatformEnabled.set(business.multiPlatformEnabled)
                val targetStructure = (task as org.jetbrains.kotlin.gradle.tasks.KotlinCompile).multiplatformStructure
                val sourceStructure = (business as org.jetbrains.kotlin.gradle.tasks.KotlinCompile).multiplatformStructure
                targetStructure.fragments.set(sourceStructure.fragments)
                targetStructure.refinesEdges.set(sourceStructure.refinesEdges)
                targetStructure.defaultFragmentName.set(sourceStructure.defaultFragmentName)
                task.destinationDirectory.set(semanticRoot.map { it.dir("classes") })
                (task as org.jetbrains.kotlin.gradle.tasks.KotlinCompile).incremental = false
                task.inputs.file(declarations.flatMap { it.declarationsFile })
                task.inputs.files(declarations.map { it.referenceFiles })
                task.outputs.file(symbols)
                task.outputs.file(semanticRoot.map { it.file("KotlinXaml.winmd") })
                task.dependsOn(declarations)
            }
            val implementation = project.tasks.register("compileWinRTXaml$suffix", CompileWinRTXamlTask::class.java) { task ->
                configure(task)
                task.semanticSymbols.set(symbols)
                task.semanticWinmd.set(semanticRoot.map { it.file("KotlinXaml.winmd") })
                task.outputDirectory.set(project.layout.buildDirectory.dir("generated/kotlin-winrt/xaml/$suffix/final"))
                task.dependsOn(semantic)
            }
            business.source(applicationHeader.flatMap { it.sourceOutputDirectory })
            business.inputs.file(implementation.flatMap { it.implementationFile })
            business.compilerOptions.freeCompilerArgs.addAll(project.provider {
                listOf("xamlDeclarations=${declarations.get().declarationsFile.get().asFile.absolutePath}",
                    "xamlImplementation=${implementation.get().implementationFile.get().asFile.absolutePath}")
                    .flatMap { listOf("-P", "plugin:io.github.composefluent.winrt.compiler:$it") }
            })
            business.dependsOn(implementation)
            val applicationVariants = discoverWinAppVariants(project).filter {
                it.kind == WinAppVariantKind.Jvm && business.name in it.compilationTaskNames(project)
            }.map { it.id }.toSet()
            project.tasks.withType(StageWinAppPackageTask::class.java).configureEach { task ->
                if (task.applicationVariant.get() in applicationVariants) {
                    task.projectPriLayoutFiles.from(implementation.flatMap { it.outputDirectory.dir("compiled") })
                    task.projectPriTargetPaths.putAll(implementation.flatMap { it.outputDirectory.dir("compiled") }
                        .map { mapOf(it.asFile.absolutePath to "") })
                    // Source-set XAML is compiled at its source-root-relative resource URI.
                    // Do not also package the original under src/<sourceSet>/kotlin/...
                    task.projectPriExcludedFromBuildPaths.addAll(sourceRoots.map { roots ->
                        roots.filter(File::isDirectory).flatMap { root ->
                            root.walkTopDown().filter { it.isFile && it.extension.equals("xaml", true) }
                                .map { it.absolutePath }.toList()
                        }
                    })
                    task.dependsOn(implementation)
                }
            }
        }
    }
}
