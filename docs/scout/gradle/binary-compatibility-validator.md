# binary-compatibility-validator: committed API dumps that exclude @InternalGraphqlApi

version: 0.18.1 (as of this pass); how the published API surface is frozen, and the one annotation habit it forces on every new nested class.

Wired in the root `build.gradle.kts`: pulled in through a `buildscript` block with `mavenCentral()` and then `apply(plugin = "binary-compatibility-validator")`, with a comment there stating that the Gradle Plugin Portal is unreachable from this environment — while the `plugins` block does resolve three plugins from the portal. See plugin-resolution.md.

The committed dumps are `api/fluid-graphql.api` plus `modules/*/api/*.api`; the umbrella one is empty by design (its only source is `Dummy.kt`). Changing any public declaration fails `check` until `./gradlew apiDump` is re-run and the dump is committed.

The configured `nonPublicMarkers += "io.fluidsonic.graphql.InternalGraphqlApi"` keeps opt-in-only declarations out of the dump — **but it matches per class file and does not propagate from an outer class to its nested classes.** Marking `Visitor` alone therefore leaks `Visitor$Typed`, `Visitor$Hierarchical`, `Visitor$Companion` and friends into the dump. That is why `modules/language/sources/visitors/Visitor.kt` repeats `@InternalGraphqlApi` on every nested class and companion even though Kotlin already requires the opt-in through the outer class. Replicate that when adding one. The custom `checkApiDumpExcludesInternalApi` task in the root build script guards the regression: it discovers `@InternalGraphqlApi` declarations in the sources by regex rather than naming them, compares each dump entry on its *outermost* class segment, and fails if the discovery matches nothing — so a new internal type needs no task edit.

`@Deprecated(level = HIDDEN)` members still appear in the dump, marked `synthetic` — they are not excluded.

Related: fluid-gradle-plugin.md, ../language/visitor-api-surface.md.
