plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("io.github.compose-fluent.windows-toolkit")
}

kotlin {
    jvmToolchain(25)
    jvm("winuiJvm")
    mingwX64()
}

windows {
    packageReferences {
        windowsSdk("10.0.26100.0", includeExtensions = false, generateProjection = false)
        nugetPackage("Microsoft.WindowsAppSDK", "2.5.1")
        // XAML resource dictionaries have no Kotlin page class. The compiler's
        // shared conversion helper is the only generated business source.
        type("Microsoft.UI.Xaml.Markup.XamlBindingHelper")
    }
}
