package io.github.composefluent.windows.toolkit.gradle

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class WindowsAppSdkComponentPackagesTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    // The Windows App SDK metapackage references every component package. An application that
    // references components instead (NuGet's way to carry part of the runtime) decides for its
    // dependencies too, which declared the metapackage only to ask for the runtime.
    @Test
    fun a_component_package_of_the_project_replaces_the_metapackage_of_a_dependency() {
        val library = identityFile("library", "Microsoft.WindowsAppSDK@2.2.0", "Some.Other.Package@1.0.0")

        val specs = nuGetPackageSpecsWithDependencies(
            declared = listOf("Microsoft.WindowsAppSDK.WinUI@2.2.1", "Microsoft.WindowsAppSDK.Runtime@2.2.0"),
            dependencyIdentityFiles = listOf(library),
        )

        assertEquals(
            listOf(
                "Microsoft.WindowsAppSDK.WinUI@2.2.1",
                "Microsoft.WindowsAppSDK.Runtime@2.2.0",
                "Some.Other.Package@1.0.0",
            ),
            specs,
        )
    }

    @Test
    fun the_metapackage_of_a_dependency_stays_when_the_project_declares_no_component() {
        val library = identityFile("library", "Microsoft.WindowsAppSDK@2.2.0")

        assertEquals(
            listOf("Microsoft.WindowsAppSDK@2.2.0", "Microsoft.WindowsAppSDK@2.2.0"),
            nuGetPackageSpecsWithDependencies(listOf("Microsoft.WindowsAppSDK@2.2.0"), listOf(library)),
        )
        assertEquals(
            listOf("Microsoft.WindowsAppSDK@2.2.0"),
            nuGetPackageSpecsWithDependencies(emptyList(), listOf(library)),
        )
    }

    @Test
    fun a_component_package_of_a_dependency_is_kept() {
        val library = identityFile("library", "Microsoft.WindowsAppSDK.DWrite@2.1.0", "Microsoft.WindowsAppSDK@2.2.0")

        assertEquals(
            listOf("Microsoft.WindowsAppSDK.WinUI@2.2.1", "Microsoft.WindowsAppSDK.DWrite@2.1.0"),
            nuGetPackageSpecsWithDependencies(listOf("Microsoft.WindowsAppSDK.WinUI@2.2.1"), listOf(library)),
        )
    }

    private fun identityFile(name: String, vararg packages: String): File =
        temporaryFolder.newFile("$name.identity.json").apply {
            writeText(
                """{"nugetPackages": [${packages.joinToString(", ") { "\"$it\"" }}]}""",
            )
        }
}
