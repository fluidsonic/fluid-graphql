# fluid-gradle plugin owns all build configuration, including versions you cannot grep for

How the `io.fluidsonic.gradle` plugin (version: 4.0.0, as of this pass) shapes every build file; matters whenever touching dependencies, Kotlin versions, or compiler flags.

No build file declares a Kotlin version, `kotlin {}` block, or toolchain. Everything derives from the plugin version in the root `build.gradle.kts` (the `plugins` block): fluid-gradle 4.0.0 implies Kotlin 2.4.10 (language/API baseline 2.4), Gradle wrapper 9.6.1, Dokka 2.2.0, and JDK 21+ (per the plugin's 4.0.0 changelog). Bumping Kotlin means bumping the plugin. The plugin — not the README badge or `CHANGELOG.md` prose — is the source of truth; both can lag, and as of this pass still show 2.3.20 (the prior 3.0.0 migration, `CHANGELOG.md` section 0.16.0), not updated for this bump.

The plugin's DSL is Kotlin-Multiplatform-shaped even though the library is JVM-only: module dependencies go inside `targets { common { dependencies { ... } } }` followed by a bare `jvm()` (see `modules/execution/build.gradle.kts`, the `fluidLibraryModule` block); test-only dependencies go in `testDependencies { }` inside common. `modules/language/build.gradle.kts` has no common block at all — just `jvm()`. New dependencies belong in the common block, not inside `jvm()` and not in a standard `dependencies {}` block. The `kotlinx("coroutines-core", "1.10.2")` calls are a fluid-gradle helper for kotlinx artifact coordinates.

The `@InternalGraphqlApi` opt-in is wired centrally in the root `build.gradle.kts` via `allModules { language { withExperimentalApi(...) } }` inside `fluidLibrary` — not per-module and not on the annotation declaration alone.

Related: module-wiring.md.
