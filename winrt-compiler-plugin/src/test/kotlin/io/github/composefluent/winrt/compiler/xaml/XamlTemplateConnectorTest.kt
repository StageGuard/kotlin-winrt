package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.runtime.WinRTXamlBindingScope
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
            open class UIElement
            class FrameworkElement : UIElement() {
                var dataContext: Any? = null
                val dataContextChanged = ContextEvent()
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
        """.trimIndent())
        File(root, "Controls.kt").writeText("""
            package microsoft.ui.xaml.controls
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
            fun main() { check(exercise() == "control:2;template:3"); println("Native template connector lifecycle passed") }
        """.trimIndent())
        val output = File(root, "classes")
        val classpath = listOf(Unit::class.java, WinRTXamlBindingScope::class.java).joinToString(File.pathSeparator) {
            File(it.protectionDomain.codeSource.location.toURI()).absolutePath
        }
        val sources = root.walkTopDown().filter { it.isFile && it.extension == "kt" }.map { it.absolutePath }.toList()
        val diagnostics = ByteArrayOutputStream()
        val arguments = listOf("-no-stdlib", "-no-reflect", "-jvm-target", Runtime.version().feature().toString(), "-classpath", classpath,
            "-d", output.absolutePath) + sources
        val result = PrintStream(diagnostics).use { K2JVMCompiler().exec(it, *arguments.toTypedArray()) }
        assertEquals(diagnostics.toString(), ExitCode.OK, result)
        URLClassLoader(arrayOf(output.toURI().toURL()), javaClass.classLoader).use { loader ->
            assertEquals("control:2;template:3", loader.loadClass("probe.ProbeKt").getMethod("exercise").invoke(null))
        }
    }
}
