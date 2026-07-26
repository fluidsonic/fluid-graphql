# Release version string is duplicated across four unlinked files

What must be edited together when bumping the library version; matters for every release.

The authoritative version lives in the root `build.gradle.kts` (`fluidLibrary(name = "graphql", version = ...)`). But the same string is hardcoded with no templating in:

- `README.md` — the installation snippets (four Maven coordinate strings: the umbrella artifact plus the three modules),
- `CLAUDE.md` — the Project Overview section,
- `CHANGELOG.md` — a new dated section is added per release.

A version bump that only touches `build.gradle.kts` silently leaves the README telling users to install the previous version. Nothing links these occurrences; the full set is only discoverable by grepping for the version string.

Release tags carry no `v` prefix (`0.17.0`, not `v0.17.0`) — this is also stated in `CLAUDE.md`.

The committed API dumps under `api/` and `modules/*/api/` are not version-stamped, but a release whose public API changed needs `./gradlew apiDump` re-run and committed or `check` fails — see binary-compatibility-validator.md.

Related: module-wiring.md, fluid-gradle-plugin.md.
