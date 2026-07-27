# Gradle plugins resolve from the Plugin Portal only; one arrives through `buildscript`

How plugins reach this build, and what it takes to try an unpublished fluid-gradle build locally.

`settings.gradle.kts` declares no `pluginManagement` block, so every entry of the root `build.gradle.kts` `plugins` block — `io.fluidsonic.gradle`, `org.jlleitschuh.gradle.ktlint`, `dev.detekt` — resolves from the Gradle Plugin Portal, and `mavenLocal()` is not on the plugin resolution path at all.

Two consequences:

- binary-compatibility-validator is deliberately not in the `plugins` block. It comes in through the `buildscript` block with `mavenCentral()` plus `apply(plugin = "binary-compatibility-validator")`. The comment there states the Plugin Portal is unreachable from this environment, yet the three `plugins {}` entries do resolve through the portal — so read that comment as scoped to that one plugin, not as a general rule. See binary-compatibility-validator.md.
- Testing a locally built fluid-gradle from `~/.m2` requires adding `pluginManagement { repositories { mavenLocal(); gradlePluginPortal() } }` to `settings.gradle.kts`, and Gradle requires `pluginManagement` to be the **first** block in that file. Keep `gradlePluginPortal()` in the list or ktlint and detekt stop resolving. Nothing in the repository does this permanently — revert it before committing.

Related: fluid-gradle-plugin.md, binary-compatibility-validator.md.
