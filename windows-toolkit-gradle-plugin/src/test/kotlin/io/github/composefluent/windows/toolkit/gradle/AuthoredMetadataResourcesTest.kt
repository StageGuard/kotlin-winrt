package io.github.composefluent.windows.toolkit.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthoredMetadataResourcesTest {
    // A library ships its authored metadata as a resource. An application stages it into its
    // package, and processResources copies that package to the same resource directory.
    @Test
    fun jvm_application_takes_authored_metadata_from_its_staged_package_only() {
        val project = ProjectBuilder.builder().withName("authored").build()
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply(KotlinWindowsToolkitPlugin::class.java)

        assertTrue(shipsAuthoredMetadataAsResource(project))

        project.extensions.getByType(WindowsExtension::class.java).application { it.mainClass.set("sample.MainKt") }

        assertFalse(shipsAuthoredMetadataAsResource(project))
    }
}
