package io.github.composefluent.winrt.runtime

/** Typed bodies are generated in the page; template instances own their connections and tracking. */
interface WinRTXamlBindingScopeOwner {
    fun _kotlinXamlUpdateScope(scope: WinRTXamlBindingScope, initial: Boolean)
    fun _kotlinXamlConnectScope(scope: WinRTXamlBindingScope, connectionId: Int, target: Any?)
    fun _kotlinXamlWriteBackScope(scope: WinRTXamlBindingScope, bindingId: Int)
    fun _kotlinXamlCreateScopeConnector(connectionId: Int, target: Any?): Any?
}

fun weakXamlBindingScopeWriteBack(scope: WinRTXamlBindingScope, bindingId: Int): (Any?, Any?) -> Unit {
    val reference = WeakReference(scope)
    return { _, _ -> reference.tryGetTarget()?.writeBack(bindingId) }
}

/** CSharpPagePass2 creates one binding object per DataTemplate/ControlTemplate instance. */
class WinRTXamlBindingScope(owner: WinRTXamlBindingScopeOwner, val scopeId: Int) {
    private val owner = WeakReference(owner)
    private val targets = mutableMapOf<Int, Any>()
    val state: WinRTXamlBindingState = WinRTXamlBindingState()
    var dataRoot: Any? = null
        private set

    fun target(connectionId: Int): Any = requireNotNull(targets[connectionId]) {
        "XAML template scope $scopeId has not connected element $connectionId"
    }

    fun connect(connectionId: Int, target: Any?) {
        requireNotNull(target) { "XAML template connection $connectionId has a null target" }
        targets[connectionId] = target
        owner.tryGetTarget()?._kotlinXamlConnectScope(this, connectionId, target)
    }

    fun createConnector(connectionId: Int, target: Any?): Any? =
        owner.tryGetTarget()?._kotlinXamlCreateScopeConnector(connectionId, target)

    fun initialize(data: Any?) {
        if (dataRoot !== data) {
            state.stopTracking()
            dataRoot = data
        }
        if (data != null) state.initialize(::update)
    }

    private fun update(initial: Boolean) {
        if (dataRoot != null) owner.tryGetTarget()?._kotlinXamlUpdateScope(this, initial)
    }

    fun changed(sender: Any?, args: Any?) = state.update(::update)
    fun writeBack(bindingId: Int) = state.changeTarget(
        { owner.tryGetTarget()?._kotlinXamlWriteBackScope(this, bindingId) }, ::update)

    fun recycle() {
        state.stopTracking()
        dataRoot = null
    }
}
