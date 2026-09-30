package io.github.composefluent.winrt.compiler.xaml

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

/** The three projected interfaces match CSharpPagePass2's generated template binding class. */
internal fun writeXamlBindingSupportSource(root: Path) {
    val file = root.resolve("io/github/composefluent/winrt/generated/xaml/KotlinXamlBindingScopeConnector.kt")
    Files.createDirectories(file.parent)
    file.writeText("""
        package io.github.composefluent.winrt.generated.xaml

        import io.github.composefluent.winrt.runtime.*
        import microsoft.ui.xaml.*
        import microsoft.ui.xaml.controls.ContainerContentChangingEventArgs
        import microsoft.ui.xaml.markup.*

        internal class KotlinXamlBindingScopeConnector(
            owner: WinRTXamlBindingScopeOwner,
            scopeId: Int,
            target: Any?,
            controlTemplate: Boolean,
        ) : IComponentConnector, IDataTemplateComponent, IDataTemplateExtension {
            private val scope = WinRTXamlBindingScope(owner, scopeId)
            private val root = requireNotNull(target).asWinRT<FrameworkElement>()
            private val isControlTemplate = controlTemplate
            private var dataContextHandlerRemoved = false

            init {
                scope.connect(scopeId, target)
                if (!controlTemplate) {
                    root.dataContextChanged.add(::dataContextChanged)
                    DataTemplate.setExtensionInstance(root, this)
                }
                XamlBindingHelper.setDataTemplateComponent(root, this)
                root.loading.add { _, _ -> scope.initialize(if (isControlTemplate) target else root.dataContext) }
                root.unloaded.add { _, _ -> scope.recycle() }
            }

            private fun dataContextChanged(sender: FrameworkElement, args: DataContextChangedEventArgs) {
                scope.initialize(args.newValue)
            }

            override fun connect(connectionId: Int, target: Any?) = scope.connect(connectionId, target)
            override fun getBindingConnector(connectionId: Int, target: Any?): IComponentConnector? =
                if (connectionId == scope.scopeId) this else
                    scope.createConnector(connectionId, target)?.asWinRT<IComponentConnector>()

            override fun processBindings(item: Any?, itemIndex: Int, phase: Int, nextPhase: WinRTOut<Int>) {
                nextPhase.value = -1
                if (!dataContextHandlerRemoved && !isControlTemplate) {
                    root.dataContextChanged.remove(::dataContextChanged)
                    dataContextHandlerRemoved = true
                }
                scope.initialize(item)
            }

            override fun processBindings(arg: ContainerContentChangingEventArgs): Int {
                val next = WinRTOut<Int>()
                processBindings(arg.item, arg.itemIndex, arg.phase.toInt(), next)
                return next.value
            }

            override fun processBinding(phase: UInt): Boolean =
                throw UnsupportedOperationException("Use IDataTemplateComponent.ProcessBindings")
            override fun resetTemplate() = recycle()
            override fun recycle() = scope.recycle()
        }
    """.trimIndent() + "\n")
}
