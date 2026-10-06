package io.github.composefluent.windows.toolkit.gradle

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** Returns true for the Windows App SDK package and its split runtime/WinUI packages. */
internal fun isWindowsAppSdkPackageId(packageId: String): Boolean {
    val normalized = packageId.trim()
    return normalized.equals("Microsoft.WindowsAppSDK", ignoreCase = true) ||
        normalized.startsWith("Microsoft.WindowsAppSDK.", ignoreCase = true)
}

internal fun containsWindowsAppSdkPackage(packageSpecs: Iterable<String>): Boolean =
    packageSpecs.any { spec ->
        isWindowsAppSdkPackageId(parseNuGetPackageIdentity(spec).normalizedPackageId)
    }

private const val WINDOWS_APP_SDK_METAPACKAGE_ID = "Microsoft.WindowsAppSDK"

/** True for a split package of the Windows App SDK (`Microsoft.WindowsAppSDK.WinUI`, `.Runtime`, ...). */
internal fun isWindowsAppSdkComponentPackageId(packageId: String): Boolean =
    packageId.trim().startsWith("$WINDOWS_APP_SDK_METAPACKAGE_ID.", ignoreCase = true)

/**
 * The NuGet packages of a project: its own, then those that its dependencies declared.
 *
 * `Microsoft.WindowsAppSDK` is a metapackage that depends on every component of the SDK (WinUI,
 * Foundation, AI, ML, Widgets, ...), and a self-contained application carries the runtime of each
 * one. NuGet's way to carry less is to reference the component packages instead, as the Windows App
 * SDK documents. The project that ships the runtime makes that choice: when it declares a component
 * package, the metapackage that a dependency declared is left out, and the components it needs
 * come from the project. A dependency that declared a component package is kept as it is.
 */
internal fun nuGetPackageSpecsWithDependencies(
    declared: Iterable<String>,
    dependencyIdentityFiles: Iterable<File>,
): List<String> {
    val own = declared.toList()
    val dependencies = dependencyIdentityFiles.flatMap(::readNuGetPackages)
    val selectsComponents = own.any { spec ->
        isWindowsAppSdkComponentPackageId(parseNuGetPackageIdentity(spec).normalizedPackageId)
    }
    if (!selectsComponents) return own + dependencies
    return own + dependencies.filterNot { spec ->
        parseNuGetPackageIdentity(spec).normalizedPackageId
            .equals(WINDOWS_APP_SDK_METAPACKAGE_ID, ignoreCase = true)
    }
}

/**
 * Resolves the public Gradle deployment setting to the concrete mode consumed by application
 * tasks and generated hosts. Auto deliberately has no runtime representation.
 */
internal fun resolveWindowsAppSdkDeployment(
    requested: WindowsAppSdkDeployment,
    packageSpecs: Iterable<String>,
    frameworkDependentAvailable: Boolean,
): WindowsAppSdkDeployment = when {
    requested != WindowsAppSdkDeployment.Auto -> requested
    !containsWindowsAppSdkPackage(packageSpecs) -> WindowsAppSdkDeployment.None
    frameworkDependentAvailable -> WindowsAppSdkDeployment.FrameworkDependent
    else -> WindowsAppSdkDeployment.SelfContained
}

/**
 * Framework-dependent hosting needs a Bootstrap DLL supplied by WinApp restore or an explicit
 * runtime asset. When restore is enabled the restore task is the producer of that asset, so the
 * mode can be selected before the task executes; disabled restore requires an existing asset.
 */
internal fun frameworkDependentDeploymentAvailable(
    restoreEnabled: Boolean,
    explicitRuntimeAssets: Iterable<Path> = emptyList(),
): Boolean {
    if (restoreEnabled) {
        return true
    }
    // With downloads disabled, configuration cannot assume a verified restore exists. Explicit
    // assets allow Auto to select FrameworkDependent without inspecting stale .winapp output.
    return explicitRuntimeAssets.any(::containsWindowsAppSdkBootstrap)
}

private fun containsWindowsAppSdkBootstrap(root: Path): Boolean {
    if (Files.isRegularFile(root)) {
        return root.fileName.toString().equals("Microsoft.WindowsAppRuntime.Bootstrap.dll", ignoreCase = true)
    }
    if (!Files.isDirectory(root)) {
        return false
    }
    return Files.walk(root).use { paths ->
        paths.anyMatch { path ->
            Files.isRegularFile(path) &&
                path.fileName.toString().equals("Microsoft.WindowsAppRuntime.Bootstrap.dll", ignoreCase = true)
        }
    }
}
