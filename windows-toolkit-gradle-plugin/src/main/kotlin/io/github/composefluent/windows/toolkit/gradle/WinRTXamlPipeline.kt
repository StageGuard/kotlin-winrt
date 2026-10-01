package io.github.composefluent.windows.toolkit.gradle

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinApiPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinJvmFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File

internal fun isXamlSemanticTask(name: String) = name.startsWith("compileKotlinWinRTXamlSemantic") ||
    name.startsWith("compileWinRTXamlSemantic")

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
    val xamlSourceRoots = project.provider {
        (sourceRoots.get() + winRTMainSourceSets(project).flatMap { sourceSet ->
            sourceSet.resources.srcDirs + project.file("src/${sourceSet.name}/appxResources")
        } + if (project.extensions.findByType(KotlinMultiplatformExtension::class.java) == null)
            project.extensions.findByType(SourceSetContainer::class.java)?.findByName("main")?.resources?.srcDirs.orEmpty() +
                project.file("src/main/appxResources") else emptyList()).distinctBy { it.toPath().toAbsolutePath().normalize() }
    }
    val hasXaml = project.provider {
        xamlSourceRoots.get().any { root -> root.isDirectory && root.walkTopDown().any { it.isFile && it.extension.equals("xaml", true) } }
    }
    val localCompilerDirectory = extension.xaml.compilerDirectory
    val sourceRootOwners = project.provider { winRTSourceRootOwners(project) }
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
        task.sourceRoots.from(xamlSourceRoots)
        task.preparedMetadataManifest.set(metadataManifest)
        task.compilerDirectory.set(extension.xaml.compilerDirectory.orElse(resolveCompiler.flatMap { it.outputDirectory }))
        // DirectoryProperty.orElse can lose producer inference through an absent override.
        // The resolver skips itself for local overrides, but must precede input validation.
        task.dependsOn(resolveCompiler)
        task.genXbfDirectory.set(extension.xaml.genXbfDirectory)
        task.minimumWindowsVersion.set(extension.xaml.minimumWindowsVersion)
        task.projectName.set(project.name)
        task.windowsAppSdkVersion.set(project.provider {
            extension.packageReferences.nugetPackages.firstOrNull {
                it.packageId.equals("Microsoft.WindowsAppSDK", true)
            }?.version?.orNull
        })
        task.onlyIf { hasXaml.get() }
    }
    val applicationHeader = project.tasks.register("generateWinRTXamlApplicationHeader",
        GenerateWinRTXamlApplicationHeaderTask::class.java) { task ->
        task.group = "kotlin-winrt"
        task.description = "Exports adjacent Kotlin XAML type declarations for XamlCompiler pass 1."
        task.sourceRoots.from(xamlSourceRoots)
        task.sourceRootOwners.set(sourceRootOwners.map { owners -> owners.mapValues { (_, owner) -> if (owner == "commonMain") "winuiMain" else owner } })
        task.metadataIndex.set(metadataIndex)
        task.preparedMetadataManifest.set(metadataManifest)
        task.scannerClasspath.from(compilerPluginClasspath)
        task.scannerClasspath.from(kotlinWinRTAuthoringScannerRuntimeClasspath(project))
        task.scannerJvmArgs.set(listOf("-Xmx512m", "-Xss512k", "-XX:+UseSerialGC", "-XX:ReservedCodeCacheSize=32m"))
        task.outputFile.set(project.layout.buildDirectory.file("generated/kotlin-winrt/xaml/application/KotlinXaml.winmd"))
        task.sourceOutputDirectory.set(project.layout.buildDirectory.dir("generated/kotlin-winrt/xaml/application/src"))
        task.onlyIf { hasXaml.get() }
    }
    val analyzeAll = project.tasks.register("analyzeWinRTXaml") { it.group = "kotlin-winrt" }
    project.tasks.withType(GenerateWinRTProjectionsTask::class.java).matching { it.name == "generateWinRTProjections" }.configureEach { task ->
        task.xamlRegistrars.set(hasXaml.flatMap { enabled ->
            if (enabled) applicationHeader.flatMap { it.sourceOutputDirectory.file("registrars.tsv") } else project.providers.provider { null }
        })
    }
    fun compilationInputs(suffix: String, roots: Provider<List<File>>): WinRTXamlCompilationInputs {
        val header = project.tasks.register("generateWinRTXamlApplicationHeader$suffix", GenerateWinRTXamlApplicationHeaderTask::class.java) { task ->
            task.sourceRoots.from(roots)
            task.sourceRootOwners.set(sourceRootOwners)
            task.metadataIndex.set(metadataIndex)
            task.preparedMetadataManifest.set(metadataManifest)
            task.scannerClasspath.from(compilerPluginClasspath, kotlinWinRTAuthoringScannerRuntimeClasspath(project))
            task.scannerJvmArgs.set(listOf("-Xmx512m", "-XX:+UseSerialGC"))
            task.outputFile.set(project.layout.buildDirectory.file("generated/kotlin-winrt/xaml/$suffix/application/KotlinXaml.winmd"))
            task.sourceOutputDirectory.set(project.layout.buildDirectory.dir("generated/kotlin-winrt/xaml/$suffix/application/src"))
            task.emitSources.set(false)
        }
        val declarations = project.tasks.register("analyzeWinRTXaml$suffix", CompileWinRTXamlTask::class.java) { task ->
            configure(task)
            task.sourceRoots.setFrom(roots)
            task.applicationHeaderWinmd.set(header.flatMap { it.outputFile })
            task.outputDirectory.set(project.layout.buildDirectory.dir("generated/kotlin-winrt/xaml/$suffix/declarations"))
        }
        analyzeAll.configure { it.dependsOn(declarations) }
        candidates.configure { it.compilationXamlDeclarations.from(declarations.flatMap { it.declarationsFile }) }
        return WinRTXamlCompilationInputs(header, declarations)
    }
    candidates.configure { task ->
        task.xamlSupportSources.from(hasXaml.flatMap { enabled ->
            if (enabled) applicationHeader.flatMap { it.sourceOutputDirectory } else project.providers.provider { null }
        })
        task.sourceRootOwners.putAll(sourceRootOwners.flatMap { owners -> applicationHeader.flatMap { it.sourceOutputDirectory }.map { output ->
            owners.values.distinct().associate { owner -> output.dir("sourceSets/$owner").asFile.absolutePath to owner } +
                (output.dir("shared").asFile.absolutePath to "winuiMain")
        } })
    }
    // This callback is registered after standalone projection compilation. Its libraries are
    // already attached to each business compilation, so no final application output is needed.
    project.afterEvaluate {
        if (!hasXaml.get()) return@afterEvaluate
        val kmp = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
        if (kmp != null) {
            winRTMainSourceSets(project).forEach { sourceSet ->
                sourceSet.kotlin.srcDir(applicationHeader.flatMap { it.sourceOutputDirectory.dir("sourceSets/${sourceSet.name}") })
            }
            kmp.sourceSets.getByName("winuiMain").kotlin.srcDir(applicationHeader.flatMap { it.sourceOutputDirectory.dir("shared") })
        }
        val kotlinApi = project.plugins.getPlugin(KotlinApiPlugin::class.java) as KotlinJvmFactory
        val businessTasks = project.tasks.withType(KotlinJvmCompile::class.java).toList().filter {
            !it.name.contains("Test", true) && !it.name.startsWith("compileKotlinWinRT")
        }
        businessTasks.forEach { business ->
            val suffix = business.name.removePrefix("compileKotlin")
            val compilation = kmp?.targets?.withType(KotlinJvmTarget::class.java)?.flatMap { it.compilations }
                ?.singleOrNull { it.compileTaskProvider.name == business.name }
            val roots = if (compilation != null) project.provider { winRTXamlCompilationSourceRoots(project, compilation) } else xamlSourceRoots
            if (roots.get().none { root -> root.isDirectory && root.walkTopDown().any { it.isFile && it.extension.equals("xaml", true) } }) return@forEach
            val (header, declarations) = compilationInputs(suffix, roots)
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
                        "xamlApplicationHeader=${header.get().outputFile.get().asFile.absolutePath}",
                        "xamlReferencesFile=${declarations.get().outputDirectory.file("references.txt").get().asFile.absolutePath}")
                        .flatMap { listOf("-P", "plugin:io.github.composefluent.winrt.compiler:$it") }
                })
            }
            val semantic = kotlinApi.registerKotlinJvmCompileTask("compileKotlinWinRTXamlSemantic$suffix", semanticOptions)
            semantic.configure { task ->
                task.description = "Compiles isolated XAML semantic symbols; output classes are never packaged."
                task.group = "kotlin-winrt"
                task.source(business.sources)
                if (kmp == null) task.source(applicationHeader.flatMap { it.sourceOutputDirectory })
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
                task.inputs.file(metadataManifest)
                task.outputs.file(symbols)
                task.outputs.file(semanticRoot.map { it.file("KotlinXaml.winmd") })
                task.dependsOn(declarations)
            }
            val implementation = project.tasks.register("compileWinRTXaml$suffix", CompileWinRTXamlTask::class.java) { task ->
                configure(task)
                task.sourceRoots.setFrom(roots)
                task.semanticSymbols.set(symbols)
                task.semanticWinmd.set(semanticRoot.map { it.file("KotlinXaml.winmd") })
                task.outputDirectory.set(project.layout.buildDirectory.dir("generated/kotlin-winrt/xaml/$suffix/final"))
                task.dependsOn(semantic)
            }
            // KSP contributes source directories to this compilation. Preserve
            // their producer edge for both XAML passes, including targeted runs
            // that reuse an already completed semantic compilation.
            val sourceGeneration = project.tasks.matching {
                it.name == "ksp" + business.name.removePrefix("compile")
            }
            semantic.configure { it.dependsOn(sourceGeneration) }
            implementation.configure { it.dependsOn(sourceGeneration) }
            if (kmp == null) business.source(applicationHeader.flatMap { it.sourceOutputDirectory })
            business.inputs.file(implementation.flatMap { it.implementationFile })
            business.compilerOptions.freeCompilerArgs.addAll(project.provider {
                listOf("xamlDeclarations=${declarations.get().declarationsFile.get().asFile.absolutePath}",
                    "xamlImplementation=${implementation.get().implementationFile.get().asFile.absolutePath}")
                    .flatMap { listOf("-P", "plugin:io.github.composefluent.winrt.compiler:$it") }
            })
            business.dependsOn(implementation)
            configureWinRTXamlPackageResources(project, roots, implementation, business.name, WinAppVariantKind.Jvm,
                compilation?.defaultSourceSet?.name)
        }
        configureWinRTXamlNativePipeline(project, metadataManifest, metadataIndex, compilerPluginClasspath, ::configure, ::compilationInputs)
    }
}

internal data class WinRTXamlCompilationInputs(
    val header: TaskProvider<GenerateWinRTXamlApplicationHeaderTask>,
    val declarations: TaskProvider<CompileWinRTXamlTask>,
)
