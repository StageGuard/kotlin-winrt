package io.github.composefluent.winrt.gallery

import microsoft.ui.xaml.RoutedEventArgs
import microsoft.ui.xaml.controls.Page
import microsoft.ui.xaml.automation.peers.ButtonAutomationPeer
import microsoft.ui.xaml.markup.IComponentConnector

@GalleryPage(route = "XamlSupport", title = "XAML + Kotlin", group = "BasicInput", order = 100)
internal fun xamlSupportPage(): XamlSupportPage {
    val first = XamlSupportPage()
    val second = XamlSupportPage()
    check(!first.output.nativeObject.sameIdentity(second.output.nativeObject))
    second.output.text = "Independent second instance"
    check(first.output.text == "Clicked 0 times")
    return first
}

internal class XamlSupportPage : Page() {
    private var clicks = 0
    private var verified = false

    init {
        initializeComponent()
        check(output.text == "Clicked 0 times")
        val originalOutput = output
        initializeComponent()
        check(originalOutput.nativeObject.sameIdentity(output.nativeObject))
        // CsWinRT ComWrappersHelper.Init retains the nondelegating inner as NativeObject.
        // Authored interfaces belong to the controlling outer used at the ABI boundary.
        val outer = checkNotNull(winRTComposableObjectReference).outer
        outer.queryInterface(IComponentConnector.IID).getOrThrow().use { connector ->
            check(outer.sameIdentity(connector))
        }
        loaded.add { _, _ ->
            if (!verified) {
                verified = true
                try {
                    // Exercise WinUI's real Invoke path, including the native event delegate.
                    println("Kotlin XAML validation: create native ButtonAutomationPeer")
                    val peer = ButtonAutomationPeer(increment)
                    println("Kotlin XAML validation: invoke native button")
                    peer.invoke()
                    peer.invoke()
                    println("Kotlin XAML validation: queue callback assertions")
                    check(checkNotNull(dispatcherQueue).tryEnqueue {
                        check(clicks == 2 && output.text == "Clicked 2 times") {
                            "XAML private callback or repeat-initialization check failed: $clicks"
                        }
                        println("Kotlin XAML native validation passed: XBF, connector identity, instance fields, private events, repeat initialization")
                    })
                } catch (error: Throwable) {
                    println("Kotlin XAML native validation failed:\n${error.stackTraceToString()}")
                    throw error
                }
            }
        }
    }

    private fun onIncrement(sender: Any?, args: RoutedEventArgs) {
        clicks += 1
        output.text = "Clicked $clicks times"
    }
}
