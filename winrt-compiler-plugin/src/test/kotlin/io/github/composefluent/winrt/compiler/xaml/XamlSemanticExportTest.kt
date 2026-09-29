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
import java.net.URLClassLoader
import io.github.composefluent.winrt.runtime.WinRTXamlLoadState

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
        val referenceFile = File(root, "references.txt").apply {
            writeText(directories.flatMap { it.listFiles()!!.filter { file -> file.extension == "winmd" } }
                .map { it.absolutePath }.sorted().joinToString("\n"))
        }
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
            class MainPage(initializeNow: Boolean = false) : microsoft.ui.xaml.controls.Page() {
                init { if (initializeNow) initializeComponent() }
                private fun onClick(sender: Any?, args: microsoft.ui.xaml.RoutedEventArgs) { myButton.text += "clicked" }
            }
            fun exercise(): String {
                val first = MainPage()
                try { first.myButton; error("missing initialization must fail") } catch (_: IllegalArgumentException) {}
                first.initializeComponent()
                first.myButton.raise()
                first.initializeComponent()
                first.myButton.raise()
                val second = MainPage()
                second.initializeComponent()
                second.myButton.raise()
                check(first.getBindingConnector(0, null) == null)
                val inConstructor = MainPage(true)
                inConstructor.myButton.raise()
                check(inConstructor.myButton.text == "clicked")
                return first.myButton.text + ":" + second.myButton.text
            }
        """.trimIndent()) }
        // Tooling fixture only: the actual delegate and base resolve from SDK WinMD in the loader gate.
        val base = File(root, "Page.kt").apply { writeText("""
            package microsoft.ui.xaml.controls
            open class Page
            class Button : microsoft.ui.xaml.controls.primitives.ButtonBase() { var text = "" }
        """.trimIndent()) }
        val button = File(root, "ButtonBase.kt").apply { writeText("""
            package microsoft.ui.xaml.controls.primitives
            open class ButtonBase {
                private val handlers = mutableListOf<microsoft.ui.xaml.RoutedEventHandler>()
                fun addClick(handler: microsoft.ui.xaml.RoutedEventHandler) { handlers.add(handler) }
                fun raise() { handlers.forEach { it.invoke(this, microsoft.ui.xaml.RoutedEventArgs()) } }
            }
        """.trimIndent()) }
        val buttonId = declarations.pages.single().connections.single { it.fieldName == "myButton" }.id
        val argsType = File(root, "Args.kt").apply { writeText("""
            package microsoft.ui.xaml
            class RoutedEventArgs
            fun interface RoutedEventHandler { fun invoke(sender: Any?, args: RoutedEventArgs) }
            class Application { companion object Metadata {
                fun loadComponent(page: Any, uri: windows.foundation.Uri) {
                    check(uri.value == "ms-appx:///MainPage.xaml")
                    (page as microsoft.ui.xaml.markup.IComponentConnector).connect($buttonId, microsoft.ui.xaml.controls.Button())
                }
            } }
        """.trimIndent()) }
        val uri = File(root, "Uri.kt").apply { writeText("package windows.foundation; class Uri(val value: String)") }
        val connector = File(root, "Connector.kt").apply { writeText("""
            package microsoft.ui.xaml.markup
            interface IComponentConnector {
                fun connect(connectionId: Int, target: Any?)
                fun getBindingConnector(connectionId: Int, target: Any?): IComponentConnector?
            }
        """.trimIndent()) }
        val symbols = File(root, "symbols.json")
        fun compile(final: Boolean = false): Pair<ExitCode, String> {
            val classpath = listOf(Unit::class.java, WinRTXamlLoadState::class.java).joinToString(File.pathSeparator) {
                File(it.protectionDomain.codeSource.location.toURI()).absolutePath
            }
            val options = mapOf("xamlDeclarations" to input.absolutePath) + if (final)
                mapOf("xamlImplementation" to File(root, "final.output.json").absolutePath)
            else mapOf("metadataIndex" to index.absolutePath, "xamlSemanticOutput" to symbols.absolutePath, "xamlReferencesFile" to referenceFile.absolutePath)
            val arguments = listOf("-no-stdlib", "-no-reflect", "-jvm-target", "17", "-classpath", classpath,
                "-Xplugin=${System.getProperty("winrt.test.fullPluginJar")}", "-d", File(root, if (final) "final" else "semantic-only").absolutePath,
                source.absolutePath, base.absolutePath, argsType.absolutePath, button.absolutePath, connector.absolutePath, uri.absolutePath) +
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
        val (finalResult, finalDiagnostics) = compile(final = true)
        assertEquals(finalDiagnostics, ExitCode.OK, finalResult)
        URLClassLoader(arrayOf(File(root, "final").toURI().toURL()), javaClass.classLoader).use { loader ->
            assertEquals("clickedclicked:clicked", loader.loadClass("probe.MainPageKt").getMethod("exercise").invoke(null))
            val pageClass = loader.loadClass("probe.MainPage")
            assertTrue(java.lang.reflect.Modifier.isPrivate(pageClass.getDeclaredMethod("onClick",
                Any::class.java, loader.loadClass("microsoft.ui.xaml.RoutedEventArgs")).modifiers))
            assertFalse(java.lang.reflect.Modifier.isStatic(pageClass.getDeclaredField("myButton").modifiers))
            assertTrue(pageClass.methods.none { it.name == "setMyButton" })
        }
        val validSource = source.readText()
        source.writeText(validSource.replace("args: microsoft.ui.xaml.RoutedEventArgs", "args: kotlin.String"))
        val staleFinal = compile(final = true)
        assertNotEquals("A stale sidecar must not allow a changed handler signature", ExitCode.OK, staleFinal.first)
        assertTrue(staleFinal.second, staleFinal.second.contains("no longer matches"))
        source.writeText(validSource + "\nfun invalid( =\n")
        assertNotEquals("Frontend errors must fail semantic compilation", ExitCode.OK, compile().first)
        assertFalse("Frontend failure must invalidate a previous sidecar", symbols.exists())
        source.writeText(validSource.replace("private fun onClick", "private suspend fun onClick"))
        assertNotEquals("suspend handlers must fail semantic export", ExitCode.OK, compile().first)
        assertFalse("Failed semantic compilation must not leave a stale sidecar", symbols.exists())
    }
}
