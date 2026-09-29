package io.github.composefluent.winrt.gallery

import microsoft.ui.xaml.FrameworkElement
import microsoft.ui.xaml.UIElement
import io.github.composefluent.winrt.gallery.basicinput.ButtonPage
import io.github.composefluent.winrt.gallery.validation.validateButtonPage

/** Explicit native integration checks; ordinary launches never synthesize interactions. */
internal object GalleryXamlValidation {
    private var targetRoute: String? = null

    fun routeArgument(value: String): String {
        if (!value.startsWith("--validate-xaml=")) return value
        return value.removePrefix("--validate-xaml=").also { targetRoute = it }
    }

    fun onPageCreated(route: String, element: UIElement) {
        if (!route.equals(targetRoute, ignoreCase = true)) return
        when (route) {
            "Button" -> validateButtonPage(element as ButtonPage)
            else -> error("No native XAML validation registered for '$route'")
        }
    }

    fun onLoaded(route: String, element: FrameworkElement, trigger: () -> Unit, verify: () -> Unit) {
        if (!route.equals(targetRoute, ignoreCase = true)) return
        var started = false
        element.loaded.add { _, _ ->
            if (!started) {
                started = true
                checked(route) {
                    trigger()
                    check(checkNotNull(element.dispatcherQueue).tryEnqueue {
                        checked(route) {
                            verify()
                            println("Gallery XAML validation passed: $route")
                        }
                    })
                }
            }
        }
    }

    private fun checked(route: String, action: () -> Unit) {
        try {
            action()
        } catch (error: Throwable) {
            println("Gallery XAML validation failed: $route\n${error.stackTraceToString()}")
            throw error
        }
    }
}
