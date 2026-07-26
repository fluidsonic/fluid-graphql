# binary-compatibility-validator: committed API dumps that exclude @InternalGraphqlApi

version: 0.18.1 (as of this pass); how the published API surface is frozen, and the one annotation habit it forces on every new nested class.

Wired in the root `build.gradle.kts`: pulled in through a `buildscript` block with `mavenCentral()` and then `apply(plugin = "binary-compatibility-validator")`, because the Gradle Plugin Portal is unreachable from this environment (stated in the comment there). Any further plugin has to arrive the same way.

The committed dumps are `api/fluid-graphql.api` plus `modules/*/api/*.api`; the umbrella one is empty by design (its only source is `Dummy.kt`). Changing any public declaration fails `check` until `./gradlew apiDump` is re-run and the dump is committed.

The configured `nonPublicMarkers += "io.fluidsonic.graphql.InternalGraphqlApi"` keeps opt-in-only declarations out of the dump — **but it matches per class file and does not propagate from an outer class to its nested classes.** Marking `Visitor` alone therefore leaks `Visitor$Typed`, `Visitor$Hierarchical`, `Visitor$Companion` and friends into the dump. That is why `modules/language/sources/visitors/Visitor.kt` repeats `@InternalGraphqlApi` on every nested class and companion even though Kotlin already requires the opt-in through the outer class. Replicate that when adding one. The custom `checkApiDumpExcludesInternalApi` task in the root build script guards the regression, but only for a hardcoded list of type names (`Visitor`, `VisitCoordinator`, `NodeWalker`, `VisitorContext`, `Visit`) — a newly added internal type is not covered until it is added there.

`@Deprecated(level = HIDDEN)` members still appear in the dump, marked `synthetic` — they are not excluded.

Related: fluid-gradle-plugin.md, ../language/visitor-api-surface.md.
