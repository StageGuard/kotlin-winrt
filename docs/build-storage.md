# Build storage ownership

Projection semantics follow `.cswinrt/src/cswinrt/main.cpp` and the resolved-input
ordering of `.cswinrt/nuget/Microsoft.Windows.CsWinRT.targets`. Persistent caches
and IDE preparation are Gradle host adaptations, shared by JVM and mingwX64
generation; they do not alter target ABI behavior.

## Package restoration

With restoration enabled, `restoreWinAppDependencies` owns package acquisition
through WinApp CLI. IDE preparation depends on projection generation and its
restore task. Configuration does not run `nuget install` or populate
`.gradle/kotlin-winrt/prepared-nuget`. A missing task-produced WinApp lock is an
error, not permission to start an independent package restore.

When `restoreNuGetPackages=false`, existing explicitly supplied/default NuGet
package roots can still support read-only configuration-time preparation.
Standalone legacy tasks retain their compatibility paths; normal plugin-wired
restoration uses WinApp. Package/version retention belongs to that package store.

## Projection and model caches

* Each project's `.gradle/kotlin-winrt/prepared-imports` retains its current
  content-keyed projection and two recently used entries. Successful preparation
  refreshes recency and retires older recognized entries under the same store
  lock used by readers. A generation worker can regenerate an evicted entry.
  Failed generation does not evict the previous successful entries. Project
  identity remains part of the key; generated sources are not blindly shared.
* Prepared-source validation and materialization run inside the ValueSource
  boundary. Generated-file existence checks do not become thousands of
  independent configuration inputs. Rechecking the value repairs missing output
  files even when Gradle reuses the configuration cache.
* Parsed metadata models use `GRADLE_USER_HOME/caches/kotlin-winrt/metadata-models`.
  The metadata codec owns the content key and atomic publication. These caches
  are internal acceleration state, not task inputs or task-owned outputs, so a
  different module populating the cache does not invalidate generation.

## Manual maintenance

Use one normal `GRADLE_USER_HOME`; do not create a new repository-local Gradle
home for each experiment. Stop builds using a directory before removing it.

Historical `build/reports/configuration-cache` HTML reports are diagnostics,
separate from `.gradle/configuration-cache`. They can be removed when their
diagnostics are no longer needed. This plugin does not delete Gradle reports.

Old project-local `metadata-models`, `prepared-nuget`, and `nuget-scratch`
directories are no longer written by normal configuration-time preparation.
They are not automatically migrated or removed. Remove them manually after
checking that no explicit local metadata path or custom task uses them; missing
WinApp packages will be restored on the next online build.

`prepared-imports` is reproducible and can be removed between builds. Its new
retention policy takes effect on the next successful static preparation; it does
not sweep projects that are never configured again. Historical experiment
folders, heap dumps, and unused build checkouts require separate review and are
not cache-policy targets. Keep `.git` and the `.cswinrt` reference tree.
