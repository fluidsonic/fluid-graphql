# `extend` definitions take two separate merge paths

How `extend type Foo { … }` and `extend schema { … }` reach a `GSchema`, and why the merged result is visible from some places but not others. Both helpers live in `modules/language/sources/model/TypeSystemExtensionMerging.kt`.

- **Type extensions merge in the `GSchema(document)` factory function, not in the `GSchema` constructor.** The constructor derives interface and union membership from the types handed to it, which would go out of sync with the merged ones (comment at the `mergeTypeExtensions` call in `modules/language/sources/model/GSchema.kt`).
- **A schema extension reaches the schema itself only through its root operation types.** The factory's local `rootTypeRef` reads them from the extensions, the *last* extension winning over earlier ones and over the `schema { … }` definition, with the conventional type names as fallback. `mergeSchemaExtensions` — which also folds in directives — is never called from the factory; its only callers are `printSchemaDefinition` in `modules/language/sources/printing/SchemaPrinter.kt` and the `GSchema.description` property, so merged directives influence printing and nothing else.
- **`GSchema.document` stays verbatim,** holding the unmerged definitions *and* the extension nodes. `GSchema.toString()` shows the merged, filtered view instead — it now routes through `printSchema`, so printing `schema` and printing `schema.document` legitimately disagree in more ways than merging; see schema-printing.md before treating either as the source of truth.
- **An extension with nothing to extend is silently ignored** — an unknown type name, a built-in scalar (the schema's own instances replace any definition of that name), or a kind mismatch such as `extend type` against an input object. Reporting those is SDL validation's job, not schema assembly's.
- **Members merge by name, replacing in place,** so an extended definition keeps its original member order; genuinely new members append.

Related: gschema-quirks.md, schema-printing.md, builtin-scalar-instances.md.
