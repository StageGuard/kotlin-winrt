package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.runtime.WinRTXamlBindingScope
import io.github.composefluent.winrt.metadata.*
import kotlinx.serialization.json.*
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader

class XamlTemplateConnectorTest {
    @Test
    fun generated_connectors_wait_for_template_children_and_use_the_sdk_data_root() {
        // CSharpPagePass2.GetBindingConnector sets the source; Connect initializes
        // ControlTemplate only after its children, and ProcessBindings initializes DataTemplate.
        val root = File("build/xaml-template-connector").absoluteFile.apply { mkdirs() }
        writeXamlBindingSupportSource(root.toPath())
        File(root, "UI.kt").writeText("""
            package microsoft.ui.xaml
            class DependencyProperty
            fun interface PropertyChangedCallback { fun invoke(sender: DependencyObject, property: DependencyProperty) }
            open class DependencyObject {
                private val callbacks = mutableMapOf<Long, Pair<DependencyProperty, PropertyChangedCallback>>()
                private var next = 0L
                fun registerPropertyChangedCallback(property: DependencyProperty, callback: PropertyChangedCallback): Long {
                    callbacks[++next] = property to callback; return next
                }
                fun unregisterPropertyChangedCallback(property: DependencyProperty, token: Long) { check(callbacks.remove(token)?.first === property) }
                protected fun changed(property: DependencyProperty) { callbacks.values.toList().filter { it.first === property }.forEach { it.second.invoke(this, property) } }
            }
            open class UIElement : DependencyObject()
            class RoutedEventArgs
            fun interface RoutedEventHandler { fun invoke(sender: Any?, args: RoutedEventArgs) }
            class Window
            open class FrameworkElement : UIElement() {
                var dataContext: Any? = null
                val dataContextChanged = ContextEvent()
                fun addLoading(handler: RoutedEventHandler) { }
                fun addUnloaded(handler: RoutedEventHandler) { }
                fun findName(name: String): Any? {
                    check(this === TemplateProbeData.grid && name == "DeferredTemplateText")
                    TemplateProbeData.lookups++
                    val text = microsoft.ui.xaml.controls.TextBlock()
                    TemplateProbeData.connector.connect(4, text)
                    return text
                }
                fun setContext(value: Any?) {
                    dataContext = value
                    dataContextChanged.raise(this, DataContextChangedEventArgs(value))
                }
            }
            class DataContextChangedEventArgs(val newValue: Any?)
            class ContextEvent {
                private val handlers = mutableListOf<(FrameworkElement, DataContextChangedEventArgs) -> Unit>()
                val count get() = handlers.size
                fun add(handler: (FrameworkElement, DataContextChangedEventArgs) -> Unit) { handlers += handler }
                fun remove(handler: (FrameworkElement, DataContextChangedEventArgs) -> Unit) { check(handlers.remove(handler)) }
                fun raise(sender: FrameworkElement, args: DataContextChangedEventArgs) { handlers.toList().forEach { it(sender, args) } }
            }
            object DataTemplate { fun setExtensionInstance(root: FrameworkElement, value: Any) { } }
            object TemplateProbeData {
                val parent = microsoft.ui.xaml.controls.Button()
                val grid = microsoft.ui.xaml.controls.Grid()
                lateinit var connector: microsoft.ui.xaml.markup.IComponentConnector
                var lookups = 0
                var unloads = 0
            }
            class Application { companion object Metadata {
                fun loadComponent(page: Any, uri: windows.foundation.Uri) {
                    check(uri.value == "ms-appx:///MainPage.xaml")
                    val binding = checkNotNull((page as microsoft.ui.xaml.markup.IComponentConnector).getBindingConnector(2, TemplateProbeData.parent))
                    TemplateProbeData.connector = binding
                    binding.connect(3, TemplateProbeData.grid)
                    check(TemplateProbeData.lookups == 0)
                    binding.connect(2, Any())
                }
            } }
        """.trimIndent())
        File(root, "Markup.kt").writeText("""
            package microsoft.ui.xaml.markup
            import io.github.composefluent.winrt.runtime.WinRTOut
            import microsoft.ui.xaml.*
            import microsoft.ui.xaml.controls.ContainerContentChangingEventArgs
            interface IComponentConnector {
                fun connect(connectionId: Int, target: Any?)
                fun getBindingConnector(connectionId: Int, target: Any?): IComponentConnector?
            }
            interface IDataTemplateComponent {
                fun processBindings(item: Any?, itemIndex: Int, phase: Int, nextPhase: WinRTOut<Int>)
                fun recycle()
            }
            interface IDataTemplateExtension {
                fun processBindings(arg: ContainerContentChangingEventArgs): Int
                fun processBinding(phase: UInt): Boolean
                fun resetTemplate()
            }
            object XamlBindingHelper {
                fun setDataTemplateComponent(root: FrameworkElement, value: IDataTemplateComponent) { }
                fun suspendRendering(target: UIElement) { }
                fun resumeRendering(target: UIElement) { }
            }
            class XamlMarkupHelper { companion object Metadata {
                fun unloadObject(target: DependencyObject) { check(target is microsoft.ui.xaml.controls.TextBlock); TemplateProbeData.unloads++ }
            } }
        """.trimIndent())
        File(root, "Controls.kt").writeText("""
            package microsoft.ui.xaml.controls
            open class Page : microsoft.ui.xaml.FrameworkElement()
            class Grid : microsoft.ui.xaml.FrameworkElement()
            class TextBlock : microsoft.ui.xaml.FrameworkElement()
            class ControlTemplate
            class Button : microsoft.ui.xaml.FrameworkElement() {
                var isEnabled = true
                    set(value) { field = value; changed(isEnabledProperty) }
                companion object Metadata { val isEnabledProperty = microsoft.ui.xaml.DependencyProperty() }
            }
            class ContainerContentChangingEventArgs(val item: Any?, val itemIndex: Int, val phase: UInt)
        """.trimIndent())
        File(root, "Probe.kt").writeText("""
            package probe
            import io.github.composefluent.winrt.runtime.*
            import io.github.composefluent.winrt.generated.xaml.KotlinXamlBindingScopeConnector
            import microsoft.ui.xaml.FrameworkElement
            class Owner(val root: FrameworkElement, val child: FrameworkElement, var source: Any) : WinRTXamlBindingScopeOwner {
                var updates = 0
                override fun _kotlinXamlUpdateScope(scope: WinRTXamlBindingScope, initial: Boolean) {
                    check(scope.dataRoot === source)
                    check(scope.target(1) === root)
                    check(scope.target(2) === child)
                    updates++
                }
                override fun _kotlinXamlConnectScope(scope: WinRTXamlBindingScope, connectionId: Int, target: Any?) { }
                override fun _kotlinXamlWriteBackScope(scope: WinRTXamlBindingScope, bindingId: Int) { }
                override fun _kotlinXamlCreateScopeConnector(connectionId: Int, target: Any?): Any? = null
            }
            fun exercise(): String {
                val parent = FrameworkElement()
                val child = FrameworkElement()
                val controlOwner = Owner(parent, child, parent)
                val control = KotlinXamlBindingScopeConnector(controlOwner, 1, parent, true)
                check(controlOwner.updates == 0)
                control.connect(2, child)
                check(controlOwner.updates == 0)
                control.connect(1, Any())
                check(controlOwner.updates == 1)
                control.connect(1, Any())
                check(controlOwner.updates == 1)
                control.recycle()
                control.connect(1, Any())
                check(controlOwner.updates == 2)
                val item = Any()
                val root = FrameworkElement().apply { dataContext = item }
                val dataOwner = Owner(root, child, item)
                val data = KotlinXamlBindingScopeConnector(dataOwner, 1, root, false)
                check(dataOwner.updates == 0)
                data.connect(2, child)
                check(dataOwner.updates == 0)
                root.setContext(item)
                check(dataOwner.updates == 1 && root.dataContextChanged.count == 1)
                val next = WinRTOut<Int>()
                data.processBindings(item, 0, 0, next)
                check(dataOwner.updates == 2 && root.dataContextChanged.count == 0 && next.value == -1)
                data.recycle()
                dataOwner.source = Any()
                data.processBindings(dataOwner.source, 0, 0, next)
                check(dataOwner.updates == 3)
                return "control:${'$'}{controlOwner.updates};template:${'$'}{dataOwner.updates}"
            }
            class MainPage : microsoft.ui.xaml.controls.Page()
            fun exerciseGenerated(): String {
                val page = MainPage()
                val data = microsoft.ui.xaml.TemplateProbeData
                check(data.lookups == 1 && data.unloads == 0)
                data.parent.isEnabled = false
                check(data.unloads == 1)
                data.parent.isEnabled = true
                check(data.lookups == 2)
                (data.connector as microsoft.ui.xaml.markup.IDataTemplateComponent).recycle()
                data.parent.isEnabled = false
                check(data.unloads == 1)
                check(page.getBindingConnector(0, null) == null)
                return "lookups:${'$'}{data.lookups};unloads:${'$'}{data.unloads}"
            }
            fun main() {
                check(exercise() == "control:2;template:3")
                check(exerciseGenerated() == "lookups:2;unloads:1")
                println("Native template connector lifecycle and generated ControlTemplate passed")
            }
        """.trimIndent())
        File(root, "Uri.kt").writeText("package windows.foundation; class Uri(val value: String)")
        File(root, "Definitions.kt").writeText("""
            package io.github.composefluent.winrt.generated.xaml
            object KotlinXamlApplicationDefinitionsProbe { fun registerAll() { } }
        """.trimIndent())
        val location = WinRTXamlSourceLocation(1, 1)
        // The Controls namespace and scope shape are exported by XamlCompiler's
        // actual ControlTemplate harvester; the data root is TargetType (Button).
        val index = WinRTXamlDeclarationIndex(3, listOf(WinRTXamlPageDeclaration(
            "probe.MainPage", "MainPage.xaml", "Microsoft.UI.Xaml.Controls.Page", false,
            listOf("templates", "compiled-bindings", "deferred-elements"), listOf(
                WinRTXamlConnectionDeclaration(1, "Microsoft.UI.Xaml.Controls.Page", null, location, emptyList()),
                WinRTXamlConnectionDeclaration(2, "Microsoft.UI.Xaml.Controls.ControlTemplate", null, location, emptyList(),
                    scopeId = 2, isScopeRoot = true, isTemplateChild = true,
                    dataTypeName = "Microsoft.UI.Xaml.Controls.Button", children = listOf(3, 4)),
                WinRTXamlConnectionDeclaration(3, "Microsoft.UI.Xaml.Controls.Grid", null, location, emptyList(),
                    scopeId = 2, isTemplateChild = true),
                WinRTXamlConnectionDeclaration(4, "Microsoft.UI.Xaml.Controls.TextBlock", null, location, emptyList(),
                    elementName = "DeferredTemplateText", scopeId = 2, isTemplateChild = true,
                    canBeInstantiatedLater = true, isUnloadableRoot = true, bindings = listOf(
                        WinRTXamlBindingDeclaration("Load", "Microsoft.UI.Xaml.Controls.TextBlock", "System.Boolean", "OneWay",
                            WinRTXamlBindingExpression("member", "IsEnabled", receiver = WinRTXamlBindingExpression("root")),
                            location, isLoad = true))),
            ))), emptyList())
        val declarations = File(root, "declarations.json").apply { writeText(WinRTXamlDeclarations.canonicalText(index)) }
        val implementation = File(root, "implementation.json").apply { writeText(buildJsonObject {
            put("KotlinDeclarations", Json.parseToJsonElement(declarations.readText()))
            put("KotlinImplementation", buildJsonObject {
                put("SchemaVersion", 3); put("DeclarationFingerprint", WinRTXamlDeclarations.fingerprint(index))
            })
        }.toString()) }
        val output = File(root, "classes")
        val classpath = listOf(Unit::class.java, WinRTXamlBindingScope::class.java).joinToString(File.pathSeparator) {
            File(it.protectionDomain.codeSource.location.toURI()).absolutePath
        }
        val sources = root.walkTopDown().filter { it.isFile && it.extension == "kt" }.map { it.absolutePath }.toList()
        val diagnostics = ByteArrayOutputStream()
        val arguments = listOf("-no-stdlib", "-no-reflect", "-jvm-target", Runtime.version().feature().toString(), "-classpath", classpath,
            "-d", output.absolutePath, "-Xplugin=${System.getProperty("winrt.test.fullPluginJar")}",
            "-P", "plugin:io.github.composefluent.winrt.compiler:xamlDeclarations=${declarations.absolutePath}",
            "-P", "plugin:io.github.composefluent.winrt.compiler:xamlImplementation=${implementation.absolutePath}") + sources
        val result = PrintStream(diagnostics).use { K2JVMCompiler().exec(it, *arguments.toTypedArray()) }
        assertEquals(diagnostics.toString(), ExitCode.OK, result)
        URLClassLoader(arrayOf(output.toURI().toURL()), javaClass.classLoader).use { loader ->
            assertEquals("control:2;template:3", loader.loadClass("probe.ProbeKt").getMethod("exercise").invoke(null))
            assertEquals("lookups:2;unloads:1", loader.loadClass("probe.ProbeKt").getMethod("exerciseGenerated").invoke(null))
        }
    }
}
