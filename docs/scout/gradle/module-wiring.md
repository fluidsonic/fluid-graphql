# Module wiring: auto-discovery and the umbrella Dummy.kt

How Gradle modules are included and named, and the umbrella source file that must not be "cleaned up".

`settings.gradle.kts` contains no module list. It iterates every directory under `modules/`, includes it, and renames the project to `fluid-graphql-<dirname>` with `projectDir` pointed at the directory. Consequences: the Gradle path for `modules/language` is `:fluid-graphql-language`, never `:language`; any new directory dropped under `modules/` silently becomes an included (and publishable) module with no settings edit, except that names starting with a dot are filtered out — `listFiles()` returns hidden entries, and a stray `modules/.claude` once registered as `:fluid-graphql-.claude` and broke every build; removing a module is deleting its directory. Adding a new module needs no other wiring — do not edit settings.

Dokka cross-module link resolution is handled automatically by fluid-gradle since 4.0.0. An earlier manual `dependencies { "dokka"(project(...)) }` block in the root `build.gradle.kts` (added in commit b050f1f to suppress ~297 "Couldn't resolve link" warnings) was removed once 4.0.0 wired this itself — do not reintroduce it.

The root project is itself the published umbrella artifact `fluid-graphql`, which only `api`-depends on dsl and execution. Its sole source file, `sources/Dummy.kt`, contains just the comment "Publishing breaks if this umbrella project doesn't contain any sources." Deleting it as dead code breaks publishing, and the failure only appears during a release, not in CI builds.

Related: fluid-gradle-plugin.md, release-versioning.md.
