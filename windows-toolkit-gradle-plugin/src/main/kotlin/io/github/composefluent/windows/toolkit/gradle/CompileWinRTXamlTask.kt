package io.github.composefluent.windows.toolkit.gradle

import io.github.composefluent.winrt.metadata.WinRTXamlDeclarations
import kotlinx.serialization.json.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

/** Both passes use the fork's DOM/harvester; Gradle never interprets XAML syntax. */
@DisableCachingByDefault(because = "The XamlCompiler protocol contains absolute diagnostic and output paths")
abstract class CompileWinRTXamlTask @Inject constructor(
    private val exec: ExecOperations,
    private val fileSystem: FileSystemOperations,
) : DefaultTask() {
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceRoots: ConfigurableFileCollection
    @get:InputFile @get:PathSensitive(PathSensitivity.NONE)
    abstract val preparedMetadataManifest: RegularFileProperty
    @get:InputFiles @get:PathSensitive(PathSensitivity.NONE)
    abstract val referenceFiles: ConfigurableFileCollection
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val compilerDirectory: DirectoryProperty
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val genXbfDirectory: DirectoryProperty
    @get:Input abstract val projectName: Property<String>
    @get:Input abstract val minimumWindowsVersion: Property<String>
    @get:InputFile @get:Optional @get:PathSensitive(PathSensitivity.NONE)
    abstract val semanticSymbols: RegularFileProperty
    @get:InputFile @get:Optional @get:PathSensitive(PathSensitivity.NONE)
    abstract val semanticWinmd: RegularFileProperty
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty
    @get:Internal val declarationsFile get() = outputDirectory.file("declarations.json")
    @get:Internal val implementationFile get() = outputDirectory.file("output.json")

    init {
        minimumWindowsVersion.convention("10.0.19041.0")
        referenceFiles.from(preparedMetadataManifest.map { readPreparedMetadataCache(it.asFile.toPath()).files })
    }

    @TaskAction fun compile() {
        val output = outputDirectory.get().asFile
        output.mkdirs()
        // Invalid invocations must never leave a consumable previous plan.
        declarationsFile.get().asFile.delete()
        implementationFile.get().asFile.delete()
        fileSystem.delete { it.delete(File(output, "compiled")) }
        File(output, "state.xml").delete()
        val roots = sourceRoots.files.filter { it.isDirectory }.sortedBy { it.absolutePath }
        val sources = linkedMapOf<String, File>()
        roots.forEach { root -> root.walkTopDown().filter { it.isFile && it.extension.equals("xaml", true) }.forEach { file ->
            val relative = file.relativeTo(root).invariantSeparatorsPath
            val existing = sources.keys.firstOrNull { it.equals(relative, true) }
            require(existing == null || sources[existing] == file) { "Duplicate XAML resource path: $relative" }
            sources[relative] = file
        } }
        val refs = referenceFiles.files.sortedBy { it.absolutePath }
        GradleFileOperations.writeStringIfChanged(File(output, "references.txt").toPath(),
            refs.map { it.absolutePath }.sorted().joinToString("\n"))
        fun item(file: File, link: String? = null) = buildJsonObject {
            put("ItemSpec", file.absolutePath); put("FullPath", file.absolutePath)
            put("IsSystemReference", true)
            if (link != null) { put("MSBuild_Link", link); put("MSBuild_TargetPath", link) }
        }
        val finalPass = semanticSymbols.isPresent
        require(finalPass == semanticWinmd.isPresent) { "Final XAML pass requires both semantic symbols and WinMD." }
        val input = buildJsonObject {
            put("ProjectPath", File(output, "${projectName.get()}.proj").absolutePath)
            put("ProjectName", projectName.get()); put("RootNamespace", projectName.get().replace('-', '_'))
            put("Language", "Kotlin"); put("LanguageSourceExtension", ".kt"); put("OutputType", "WinExe")
            put("IsPass1", !finalPass); put("OutputPath", File(output, "compiled").absolutePath)
            put("TargetPlatformMinVersion", minimumWindowsVersion.get())
            put("GenXbfPath", genXbfDirectory.get().asFile.absolutePath)
            put("SavedStateFile", File(output, "state.xml").absolutePath)
            put("ReferenceAssemblies", JsonArray(refs.map { item(it) }))
            put("ReferenceAssemblyPaths", JsonArray((refs.map { it.parentFile } +
                File(System.getenv("WINDIR"), "Microsoft.NET/Framework64/v4.0.30319")).distinct().map { item(it) }))
            put("XamlPages", JsonArray(sources.toSortedMap().map { (path, file) -> item(file, path) }))
            if (finalPass) {
                put("KotlinSymbols", Json.parseToJsonElement(semanticSymbols.get().asFile.readText()))
                put("LocalAssembly", JsonArray(listOf(item(semanticWinmd.get().asFile))))
            }
        }
        val inputFile = File(output, "input.json").apply { writeText(input.toString()) }
        val result = exec.exec { spec ->
            spec.workingDir(output)
            spec.commandLine(File(compilerDirectory.get().asFile, "XamlCompiler.exe").absolutePath,
                inputFile.absolutePath, implementationFile.get().asFile.absolutePath)
            spec.isIgnoreExitValue = true
        }
        val plan = runCatching { WinRTXamlDeclarations.readCompilerOutput(implementationFile.get().asFile.toPath()) }
            .getOrElse { error("XamlCompiler failed (exit ${result.exitValue}): ${it.message}") }
        check(result.exitValue == 0) { "XamlCompiler failed with exit ${result.exitValue}." }
        plan.pages.forEach { page ->
            val xaml = requireNotNull(sources[page.resourcePath]) { "Unknown XAML resource ${page.resourcePath}." }
            require(File(xaml.parentFile, "${xaml.nameWithoutExtension}.kt").isFile) {
                "${page.resourcePath} declares ${page.className} but has no same-directory ${xaml.nameWithoutExtension}.kt."
            }
        }
        GradleFileOperations.writeStringIfChanged(declarationsFile.get().asFile.toPath(), WinRTXamlDeclarations.canonicalText(plan))
    }
}
