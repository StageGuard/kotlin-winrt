plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin { jvmToolchain(25) }
kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")
sourceSets.main { resources.srcDir("../catalog") }
sourceSets.main { kotlin.srcDir("../src/commonMain/kotlin/io/github/composefluent/winrt/gallery/code") }
dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.10")
    implementation(libs.kotlinx.serialization.json)
    // K2 LightTree syntax parsing; no K1 analysis or binding context.
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${libs.versions.kotlin.get()}")
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    dependsOn(tasks.jar)
    systemProperty("gallery.highlighting.plugin", tasks.jar.get().archiveFile.get().asFile.absolutePath)
}
