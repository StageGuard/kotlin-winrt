# Kotlin WinUI Gallery

This application is being ported from WinUI Gallery using Kotlin/WinRT projected
classes. UI construction and event handlers are Kotlin code. It does not load
XAML, embed XAML strings, or introduce a markup interpreter.

The application module is `:winui-gallery`, with its JVM build-time navigation
processor under `:winui-gallery:processor`. Application code is shared by the
`winuiJvm` and `mingwX64` targets in `src/winuiMain/kotlin`.
The manifest and image assets live in `src/winuiMain/appxResources` and are
discovered by the Windows toolkit's source-set resource pipeline.

## Navigation and page registration

`GalleryNavigation.kt` owns navigation through `@GalleryGroupEntry` and
`@GalleryPageEntry`. Their `glyph`, title, route, and order are authoritative.
The catalog JSON supplies descriptions, documentation links, tags, and images;
its historical navigation titles, glyphs, and ordering do not override the
annotations. `@GallerySample` binds an existing route to a top-level Kotlin
function returning projected UI content.

The processor uses KSP2, registered separately for `kspWinuiJvm` and
`kspMingwX64`. WinRT authoring and projection generation scan the handwritten
sources first; KSP then processes navigation annotations alongside those
projections. It emits deterministic navigation lists, the flat search index,
and direct calls to the annotated sample functions. It never runs in the app
and has no dependency on Kotlin compiler internals or K1 APIs.
`GalleryCatalog.kt` declares the shared `expect` contract; KSP emits its `actual`
objects in each target so shared UI code can use the generated catalog.
This follows the responsibility split of compose-fluent-ui's
`gallery-processor` without copying its Compose-specific icon or content model.

## Reference ownership

- Runtime and authoring remain in the existing upstream modules. Startup follows
  `.cswinrt/src/Samples/WinUIDesktopSample/App.xaml.cs` through `Application.start`,
  `onLaunched`, native controls resources, and a retained `Window`.
- Navigation, home, page headers, and examples are app-owned ports of
  WinUI Gallery's `MainWindow`, `HomePage`, `PageHeader`, and `ControlExample`.
- WinRT declarations are produced by the existing metadata/projection pipeline.
- Application icons are from the supplied `kt-winrt-Gallery-Assets.zip`.
  See `THIRD-PARTY-NOTICES.md` for the upstream catalog and illustration license.

## Implementation and validation status

The 123-page catalog now has projected-control sample implementations covering
the control, layout, collection, text, media, design, animation, windowing, and
Windows integration families. KSP rejects navigation entries without a sample
factory and generates Symbol lookup from the projected companion properties.
The shell includes persistent settings, favorites and recents, search, native
Frame navigation, fixed page headers, example theme switching, adaptive layouts,
JumpList updates, packaged deep links, and notification activation handling.

Source-code expanders show the sample's complete Kotlin source file. KSP2 reads
the annotated factories' source files and uses Kotlin 2.x's lexer and K2 LightTree syntax
parser to generate text and contiguous UTF-16 highlight ranges. No K1 analysis
API or compiler is loaded by the app. The shared document/palette model feeds
a native selectable TextBlock with Run inlines, created on first expansion.
IDEA Light/Dark editor colors follow the actual app theme; high contrast uses
system foreground/background colors. Copy code preserves the source text.
This is syntax highlighting, not IDE semantic resolution or code execution.
Markup-specific samples demonstrate their behavior using Kotlin-constructed
visual trees, element factories, bindings, and projected dependency properties.
They do not execute markup. ContentIsland uses original procedural geometry in
place of the upstream model excluded for licensing reasons.

The expanded Gallery has passed JVM compilation, KSP2 navigation generation,
Native compilation, and AppX packaging with all 123 page factories. A Windows
host launch reaches window activation and remains running with the projected
navigation shell and theme resources loaded. It selects the reference's
Windows App SDK 2.4.1-experimental, including its experimental controls.

Source-level parity is covered by the catalog, projected control construction,
navigation, settings, activation, and resource paths. Visual and interaction
parity still needs a complete desktop UI pass across all samples.
The generator/runtime fixes preserve declared ABI interfaces, retain collection
references before transferring ownership, query sealed classes' default
interfaces, and expose bindable enumeration for collections passed as `Any`.
Those contracts belong to the shared projection/runtime pipeline, not this app.

The root `PLAN.md` is unchanged; this task's newly requested scope and gaps are
recorded here instead of rewriting the approved repository plan.
