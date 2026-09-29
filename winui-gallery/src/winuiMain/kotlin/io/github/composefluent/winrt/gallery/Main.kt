package io.github.composefluent.winrt.gallery

import io.github.composefluent.winrt.runtime.asWinRT
import microsoft.windows.applifecycle.AppInstance
import microsoft.windows.applifecycle.ExtendedActivationKind
import microsoft.windows.appnotifications.AppNotificationActivatedEventArgs
import windows.applicationmodel.activation.ProtocolActivatedEventArgs
import microsoft.ui.xaml.Application
import microsoft.ui.xaml.LaunchActivatedEventArgs
import microsoft.ui.xaml.controls.XamlControlsResources

import microsoft.ui.windowing.OverlappedPresenter
import microsoft.ui.windowing.OverlappedPresenterState

private var application: GalleryApplication? = null
private var processArguments: Array<String> = emptyArray()

fun main(args: Array<String>) {
    // The JVM launcher passes command-line arguments to this entry point, while
    // WinAppSDK delivers them through LaunchActivatedEventArgs on the first
    // activation.  Preserve both paths so packaged smoke runs can select a
    // sample deterministically and normal protocol/notification activation is
    // unchanged.
    processArguments = args
    println("Kotlin WinUI Gallery: starting application")
    Application.start {
        println("Kotlin WinUI Gallery: creating application")
        application = GalleryApplication()
    }
    application = null
}

// Lifecycle follows .cswinrt/src/Samples/WinUIDesktopSample/App.xaml.cs.
class GalleryApplication : Application() {
    private var window: MainWindow? = null

    private var notifications: GalleryNotifications? = null

    private fun resolveRoute(value: String): String? {
        val route = value.trim('/')
        return GalleryCatalog.pages.firstOrNull {
            it.id.equals(route, ignoreCase = true) || it.title.equals(route, ignoreCase = true)
        }?.id ?: GalleryCatalog.groups.firstOrNull {
            it.id.equals(route, ignoreCase = true) || it.title.equals(route, ignoreCase = true)
        }?.id
    }

    init {
        unhandledException.add { _, args ->
            println("Kotlin WinUI Gallery: ${args.message}")
        }
    }

    override fun onLaunched(args: LaunchActivatedEventArgs) {
        try {
            launchWindow(args.arguments)
        } catch (error: Throwable) {
            println("Kotlin WinUI Gallery: window creation failed\n${error.stackTraceToString()}")
            throw error
        }
    }

    private fun launchWindow(arguments: String) {
        val launchRouteArgument = GalleryXamlValidation.routeArgument(
            arguments.trim().trim('"').ifBlank { processArguments.firstOrNull().orEmpty().trim().trim('"') },
        )
        println("Kotlin WinUI Gallery: loading controls resources")
        resources.mergedDictionaries.add(XamlControlsResources())
        val galleryWindow = MainWindow()
        window = galleryWindow
        val appWindow = checkNotNull(galleryWindow.appWindow)
        fun persistRestoredWindowSize() {
            val presenter = appWindow.presenter?.asWinRT<OverlappedPresenter>() ?: return
            if (presenter.state != OverlappedPresenterState.Restored) return
            val size = appWindow.size
            if (size.width >= 640 && size.height >= 500) GalleryPreferences.putWindowSize(size)
        }
        appWindow.changed.add { _, args -> if (args.didSizeChange) persistRestoredWindowSize() }
        galleryWindow.closed.add { _, _ ->
            persistRestoredWindowSize()
            GalleryWindows.closeAll()
            notifications?.close(); notifications = null
        }
        notifications = GalleryNotifications(checkNotNull(galleryWindow.window), galleryWindow)
        appWindow.resize(GalleryPreferences.windowSize(1280, 900))
        // Register before reading rich activation, following the Windows App SDK
        // notification quickstart. COM launch can deliver its payload later via NotificationInvoked.
        val activation = AppInstance.getCurrent().getActivatedEventArgs()
        println("Kotlin WinUI Gallery: activation kind ${activation.kind}")
        when (activation.kind) {
            ExtendedActivationKind.AppNotification -> notifications?.handle(checkNotNull(activation.data).asWinRT<AppNotificationActivatedEventArgs>())
            ExtendedActivationKind.Protocol -> {
                val uri = checkNotNull(checkNotNull(activation.data).asWinRT<ProtocolActivatedEventArgs>().uri)
                // `kotlin-winui-gallery://ColorPicker` stores the route in the
                // URI host, while `kotlin-winui-gallery:///ColorPicker` stores
                // it in the path.  Accept both forms so protocol activation
                // reaches the same page factory as in-app navigation.
                val route = uri.path.trim('/').ifBlank { uri.host.trim('/') }
                resolveRoute(route)?.let { resolved ->
                    if (uri.schemeName.equals("kotlin-winui-gallery", ignoreCase = true)) GalleryNavigationHost.navigate(resolved)
                }
            }
            else -> {
                val raw = launchRouteArgument
                val route = if (raw.startsWith("kotlin-winui-gallery://", ignoreCase = true)) {
                    val uri = windows.foundation.Uri(raw)
                    uri.path.trim('/').ifBlank { uri.host.trim('/') }
                } else {
                    raw
                }
                resolveRoute(route)?.let { resolved ->
                    println("Kotlin WinUI Gallery: launch route '$resolved'")
                    GalleryNavigationHost.navigate(resolved)
                }
            }
        }
        galleryWindow.activate()
        println("Kotlin WinUI Gallery: window activation requested")
    }
}
