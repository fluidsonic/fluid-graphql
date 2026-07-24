# Schema DSL gotchas: no validation, root-type dual effect, on() replaces, deprecated() forms

Behaviors of `GraphQL.schema { }` (`modules/dsl/sources/DefaultSchemaBuilder.kt`) that differ from what the surface suggests.

**No validation.** The document DSL validates eagerly (name validity via `GLanguage.isValidName` and friends, duplicate checks, non-empty selections), but the schema builder validates nothing — `DefaultSchemaBuilder.build()` is marked `// FIXME validate?` and accepts any names, duplicates, and empty definitions. `GraphQL.schema { }` can silently produce invalid schemas.

**`Query(type) { }` does two things.** Root-type declarations with a configure block record the root reference AND register a full Object type definition — also calling `Object(type) { }` for the same name yields a duplicate definition. The one-argument form `Query(type)` records only the reference. `build()` synthesizes a `GSchemaDefinition` at index 0 of the definitions, but only when at least one root type was set; otherwise no schema-definition node exists and `GSchema` falls back to default root-name resolution.

**`on(...)` replaces, not accumulates.** In `Directive(name) { }`, a later `on(...)` call overwrites earlier locations. Combine locations in a single call with `or`.

**`deprecated()` three forms diverge.** `deprecated("msg")` prints that reason; `deprecated()` materializes the default reason "No longer supported"; `deprecated(reason = null)` keeps null and prints `reason: null`. Omitting the argument is not the same as passing null.

Related: builder-generations.md, ../language/printer-lossiness.md.
