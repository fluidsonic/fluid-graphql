# fluid-gradle plugin owns all build configuration, including versions you cannot grep for

How the `io.fluidsonic.gradle` plugin (version: 4.1.0, as of this pass) shapes every build file; matters whenever touching dependencies, Kotlin versions, or compiler flags.

No build file declares a Kotlin version, `kotlin {}` block, or toolchain. Everything derives from the plugin version in the root `build.gradle.kts` (the `plugins` block): fluid-gradle 4.1.0 implies Kotlin 2.4.10 (language/API baseline 2.4), Gradle wrapper 9.6.1, Dokka 2.2.0, and JDK 21+. The 4.0.0 → 4.1.0 bump changed none of those, only test-output logging (see test-output-logging.md). Bumping Kotlin means bumping the plugin. The plugin — not the README badge — is the source of truth; the badge lags and as of this pass still shows Kotlin 2.3.20, from before the fluid-gradle 4.0.0 migration that `CHANGELOG.md` records under 0.17.0.

The plugin's DSL is Kotlin-Multiplatform-shaped even though the library is JVM-only: module dependencies go inside `targets { common { dependencies { ... } } }` followed by a bare `jvm()` (see `modules/execution/build.gradle.kts`, the `fluidLibraryModule` block); test-only dependencies go in `testDependencies { }` inside common. `modules/language/build.gradle.kts` has no common block at all — just `jvm()`. New dependencies belong in the common block, not inside `jvm()` and not in a standard `dependencies {}` block. The `kotlinx("coroutines-core", "1.10.2")` calls are a fluid-gradle helper for kotlinx artifact coordinates.

The `@InternalGraphqlApi` opt-in is wired centrally in the root `build.gradle.kts` via `allModules { language { withExperimentalApi(...) } }` inside `fluidLibrary` — not per-module and not on the annotation declaration alone.

Related: module-wiring.md, binary-compatibility-validator.md, plugin-resolution.md, test-output-logging.md.
