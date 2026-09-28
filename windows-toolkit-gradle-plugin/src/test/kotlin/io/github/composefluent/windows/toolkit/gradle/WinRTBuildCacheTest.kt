package io.github.composefluent.windows.toolkit.gradle

import io.github.composefluent.winrt.metadata.WinRTMetadataModel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Gradle cache policy around the cswinrt/main.cpp input-cache and generator boundary. */
class WinRTBuildCacheTest {
    @get:Rule val temporary = TemporaryFolder()

    private fun entry(store: Path, id: Int): Path {
        val path = store.resolve(id.toString(16).padStart(64, '0'))
        Files.createDirectories(path.resolve("sources"))
        Files.writeString(path.resolve("sources/sample.kt"), "class Sample$id")
        writePreparedStaticManifest(path.resolve("manifest.tsv"), emptyList(), WinRTMetadataModel(emptyList()))
        Files.setLastModifiedTime(path, FileTime.fromMillis(id.toLong()))
        return path
    }

    @Test fun retains_current_and_two_recent_entries_without_touching_unknown_directories() {
        val store = temporary.newFolder("store").toPath()
        val entries = (1..6).map { entry(store, it) }
        val unknown = Files.createDirectories(store.resolve("user-files"))
        Files.writeString(unknown.resolve("notes.txt"), "keep")
        val incomplete = Files.createDirectories(store.resolve("f".repeat(64)))
        // Old configuration reactivated: access recency, not key or creation order, decides retention.
        withPreparedSourceStoreLock(store) { retainRecentPreparedSources(store, entries.first()) }
        assertTrue(Files.isDirectory(entries.first()))
        entries.subList(1, 4).forEach { assertFalse(Files.exists(it)) }
        entries.takeLast(2).forEach { assertTrue(Files.isDirectory(it)) }
        assertEquals("keep", Files.readString(unknown.resolve("notes.txt")))
        assertTrue(Files.isDirectory(incomplete))
        assertFalse(materializeCachedPreparedSources(entries[1].resolve("sources"), store.resolve("output")))
        assertFalse(Files.exists(store.resolve("output")))
    }

    @Test fun cached_copy_repairs_outputs_and_corruption_is_a_miss() {
        val store = temporary.newFolder("store").toPath()
        val cached = entry(store, 1)
        val output = temporary.newFolder("output").toPath()
        assertTrue(materializeCachedPreparedSources(cached.resolve("sources"), output))
        Files.delete(output.resolve("sample.kt"))
        assertTrue(materializeCachedPreparedSources(cached.resolve("sources"), output))
        assertEquals("class Sample1", Files.readString(output.resolve("sample.kt")))
        Files.writeString(cached.resolve("sources/sample.kt"), "corrupt")
        assertFalse(materializeCachedPreparedSources(cached.resolve("sources"), output))
        assertEquals("class Sample1", Files.readString(output.resolve("sample.kt")))
    }

    @Test fun reader_and_eviction_share_the_same_lock() {
        val store = temporary.newFolder("store").toPath()
        val cached = entry(store, 1)
        val readerEntered = CountDownLatch(1)
        val releaseReader = CountDownLatch(1)
        val writerStarted = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val reader = executor.submit {
                withPreparedSourceStoreLock(store) {
                    readerEntered.countDown()
                    check(releaseReader.await(10, TimeUnit.SECONDS))
                    assertTrue(isPreparedStaticSourceValid(cached.resolve("sources")))
                }
            }
            assertTrue(readerEntered.await(10, TimeUnit.SECONDS))
            val writer = executor.submit {
                writerStarted.countDown()
                withPreparedSourceStoreLock(store) { retainRecentPreparedSources(store, entry(store, 2), 1) }
            }
            assertTrue(writerStarted.await(10, TimeUnit.SECONDS))
            assertFalse(writer.isDone)
            releaseReader.countDown()
            reader.get(10, TimeUnit.SECONDS)
            writer.get(10, TimeUnit.SECONDS)
            assertFalse(Files.exists(cached))
        } finally {
            releaseReader.countDown()
            executor.shutdownNow()
        }
    }

    @Test fun projects_share_model_caches_but_not_generated_sources() {
        val gradleHome = temporary.newFolder("gradle-home")
        val first = ProjectBuilder.builder().withProjectDir(temporary.newFolder("first"))
            .withGradleUserHomeDir(gradleHome).build()
        val second = ProjectBuilder.builder().withProjectDir(temporary.newFolder("second"))
            .withGradleUserHomeDir(gradleHome).build()
        for (name in listOf("metadata-models")) {
            assertEquals(sharedWinRTCacheDirectory(first, name), sharedWinRTCacheDirectory(second, name))
            assertTrue(sharedWinRTCacheDirectory(first, name).startsWith(gradleHome.toPath()))
        }
        assertNotEquals(first.projectDir, second.projectDir)
    }
}
