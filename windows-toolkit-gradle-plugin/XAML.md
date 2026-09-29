# Kotlin XAML compilation

The Windows toolkit discovers XAML beside Kotlin source files. A class-bearing
`views/MainPage.xaml` must have a sibling `views/MainPage.kt`; `x:Class` names the
Kotlin class, and its direct projected base must match the XAML root. No page
annotation or per-file Gradle registration is required.

```kotlin
class MainPage : Page() {
    init { initializeComponent() }
    private fun onClick(sender: Any?, args: RoutedEventArgs) {
        output.text = "Clicked"
    }
}
```

Named elements are generated read-only instance properties. Access before
connection fails explicitly. `initializeComponent()` guards repeated and
reentrant calls; a failed load cannot be retried on the same instance. Ordinary
private event handlers are checked against the actual WinRT delegate signature.

The build first analyzes declarations, then compiles isolated Kotlin semantic
symbols, generates XBF, and finally compiles application classes. Semantic
classes are never placed on the application's runtime classpath. XBF enters the
existing PRI staging pipeline with its relative resource path preserved.

The plugin pins the Windows x64 compiler release `0.1.0-preview.2` and its SHA-256
from [the maintained fork](https://github.com/compose-fluent/microsoft-ui-xaml/releases/tag/kotlin-xamlc-v0.1.0-preview.2).
The first build downloads it; subsequent offline builds use a checksum-verified
Gradle user-home cache. GenXbf comes from the resolved WinUI NuGet package.
Applications do not need a compiler checkout or a stock XamlCompiler installed.

For fork development, `-PkotlinWinRT.xaml.compilerDirectory=<directory>` selects
an unpacked compiler and `-PkotlinWinRT.xaml.genXbfDirectory=<tools-directory>`
overrides GenXbf discovery. A custom release requires both
`kotlinWinRT.xaml.version` and `kotlinWinRT.xaml.sha256`. The `windows.xaml` DSL
also exposes these settings and `archiveUrl` for a custom distribution endpoint.

This integration currently targets JVM compilations. Native and IDE declaration
visibility require their own integration and validation. The preview backend
rejects unsupported templates, deferred loading, and compiled bindings at build
time; it never falls back to parsing XAML strings at runtime. Gallery migration
and real UI acceptance are tracked separately in `winui-gallery/XAML_MIGRATION.md`.
