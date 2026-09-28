package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.metadata.*
import kotlinx.serialization.json.*
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class XamlSemanticExportTest {
    @Test
    fun semantic_pass_exports_private_handler_without_exposing_it_in_winmd() {
        val references = System.getenv("WINRT_TEST_XAMLC_REFERENCES")
        val compiler = System.getenv("WINRT_TEST_XAMLC")
        val genXbf = System.getenv("WINRT_TEST_GENXBF")
        assumeTrue("Windows XamlCompiler, GenXbf and SDK references are required",
            !references.isNullOrBlank() && !compiler.isNullOrBlank() && !genXbf.isNullOrBlank())
        val root = File("build/xaml-semantic-integration").absoluteFile.apply { mkdirs() }
        val xaml = File(root, "MainPage.xaml").apply { writeText("""
            <Page xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
                xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml" x:Class="probe.MainPage">
                <Button x:Name="myButton" Click="onClick" Content="Click" />
            </Page>
        """.trimIndent()) }
        fun item(file: File) = buildJsonObject {
            put("ItemSpec", file.absolutePath); put("FullPath", file.absolutePath); put("IsSystemReference", true)
        }
        val directories = references!!.split(File.pathSeparator).map(::File)
        val compilerInput = buildJsonObject {
            put("ProjectPath", File(root, "probe.proj").absolutePath); put("ProjectName", "probe")
            put("Language", "Kotlin"); put("LanguageSourceExtension", ".kt"); put("IsPass1", true)
            put("OutputPath", File(root, "generated").absolutePath); put("RootNamespace", "probe"); put("OutputType", "WinExe")
            put("TargetPlatformMinVersion", "10.0.19041.0"); put("GenXbfPath", genXbf!!)
            put("SavedStateFile", File(root, "state.xml").absolutePath)
            put("ReferenceAssemblies", JsonArray(directories.flatMap { it.listFiles()!!.filter { it.extension == "winmd" }.sorted() }.map(::item)))
            put("ReferenceAssemblyPaths", JsonArray((directories + File(System.getenv("WINDIR"), "Microsoft.NET/Framework64/v4.0.30319")).map(::item)))
            put("XamlPages", JsonArray(listOf(item(xaml))))
        }
        fun invokeXaml(name: String, input: JsonObject): JsonObject {
            val inputFile = File(root, "$name.input.json").apply { writeText(input.toString()) }
            val outputFile = File(root, "$name.output.json")
            val console = File(root, "$name.console.txt")
            val process = ProcessBuilder(compiler!!, inputFile.absolutePath, outputFile.absolutePath)
                .redirectErrorStream(true).redirectOutput(console).start()
            if (!process.waitFor(45, TimeUnit.SECONDS)) { process.destroyForcibly(); fail("XamlCompiler timed out") }
            assertEquals(outputFile.takeIf { it.exists() }?.readText() ?: console.readText(), 0, process.exitValue())
            return Json.parseToJsonElement(outputFile.readText()).jsonObject
        }
        invokeXaml("declaration", compilerInput)
        val declarations = WinRTXamlDeclarations.readCompilerOutput(File(root, "declaration.output.json").toPath())
        val input = File(root, "declarations.json").apply { writeText(WinRTXamlDeclarations.canonicalText(declarations)) }
        val index = File(root, "metadata.tsv").apply { writeText(
            "Microsoft.UI.Xaml.Controls.Page\tRuntimeClass\t\tSystem.Object\n" +
                "Microsoft.UI.Xaml.RoutedEventArgs\tRuntimeClass\t\tSystem.Object\n") }
        val source = File(root, "MainPage.kt").apply { writeText("""
            package probe
            class MainPage : Microsoft.UI.Xaml.Controls.Page() {
                private fun onClick(sender: Any?, args: Microsoft.UI.Xaml.RoutedEventArgs) {}
            }
        """.trimIndent()) }
        // Tooling fixture only: the actual delegate and base resolve from SDK WinMD in the loader gate.
        val base = File(root, "Page.kt").apply { writeText("package Microsoft.UI.Xaml.Controls; open class Page") }
        val argsType = File(root, "Args.kt").apply { writeText("package Microsoft.UI.Xaml; class RoutedEventArgs") }
        val symbols = File(root, "symbols.json")
        fun compile(): Pair<ExitCode, String> {
            val stdlib = File(Unit::class.java.protectionDomain.codeSource.location.toURI())
            val options = mapOf("metadataIndex" to index.absolutePath, "xamlDeclarations" to input.absolutePath,
                "xamlSemanticOutput" to symbols.absolutePath, "xamlReferences" to references!!)
            val arguments = listOf("-no-stdlib", "-no-reflect", "-jvm-target", "17", "-classpath", stdlib.absolutePath,
                "-Xplugin=${System.getProperty("winrt.test.fullPluginJar")}", "-d", File(root, "semantic-only").absolutePath,
                source.absolutePath, base.absolutePath, argsType.absolutePath) +
                options.flatMap { (key, value) -> listOf("-P", "plugin:io.github.composefluent.winrt.compiler:$key=$value") }
            val diagnostics = ByteArrayOutputStream()
            val result = PrintStream(diagnostics).use { K2JVMCompiler().exec(it, *arguments.toTypedArray()) }
            return result to diagnostics.toString()
        }
        val (result, diagnostics) = compile()
        assertEquals(diagnostics, ExitCode.OK, result)
        val output = Json.parseToJsonElement(symbols.readText()).jsonObject
        assertEquals(WinRTXamlDeclarations.fingerprint(declarations), output.getValue("DeclarationFingerprint").jsonPrimitive.content)
        val handler = output.getValue("Pages").jsonArray.single().jsonObject.getValue("Handlers").jsonArray.single().jsonObject
        assertEquals("onClick", handler.getValue("Name").jsonPrimitive.content)
        assertEquals(listOf("System.Object", "Microsoft.UI.Xaml.RoutedEventArgs"),
            handler.getValue("ParameterTypeNames").jsonArray.map { it.jsonPrimitive.content })
        val page = WinRTMetadataLoader.load(File(root, "KotlinXaml.winmd").toPath()).namespaces.single().types.single()
        assertTrue(page.methods.none { it.name == "onClick" })
        val finalOutput = invokeXaml("final", JsonObject(compilerInput + mapOf(
            "IsPass1" to JsonPrimitive(false), "KotlinSymbols" to output,
            "LocalAssembly" to JsonArray(listOf(item(File(root, "KotlinXaml.winmd")))),
        )))
        assertEquals(WinRTXamlDeclarations.fingerprint(declarations), finalOutput.getValue("KotlinImplementation")
            .jsonObject.getValue("DeclarationFingerprint").jsonPrimitive.content)
        val xbf = finalOutput.getValue("GeneratedXbfFiles").jsonArray.single().jsonPrimitive.content
        assertTrue("XBF is missing or empty", Files.size(Path.of(xbf)) > 16)
        val rewritten = File(finalOutput.getValue("GeneratedXamlFiles").jsonArray.single().jsonPrimitive.content).readText()
        assertTrue(rewritten.contains("ConnectionId"))
        assertFalse(rewritten.contains("Click=\"onClick\""))
        source.writeText(source.readText().replace("private fun onClick", "private suspend fun onClick"))
        assertNotEquals("suspend handlers must fail semantic export", ExitCode.OK, compile().first)
        assertFalse("Failed semantic compilation must not leave a stale sidecar", symbols.exists())
    }
}
