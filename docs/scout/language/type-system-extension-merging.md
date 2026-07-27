# `extend` definitions take two separate merge paths

How `extend type Foo { … }` and `extend schema { … }` reach a `GSchema`, and why the merged result is visible from some places but not others. All helpers live in `modules/language/sources/model/TypeSystemExtensionMerging.kt`.

- **Type extensions merge in the `GSchema(document)` factory function, not in the `GSchema` constructor.** The constructor derives interface and union membership from the types handed to it, which would go out of sync with the merged ones (comment at the `mergeTypeExtensions` call in `modules/language/sources/model/GSchema.kt`).
- **A schema extension reaches the schema itself only through its root operation types.** The factory's local `rootTypeRef` reads them from the extensions, the *last* extension winning over earlier ones and over the `schema { … }` definition, with the conventional type names as fallback. Directives on a schema extension are merged by `mergeSchemaExtensions`, which is reached **only** from `GSchema.toString()` — never from the factory.
- **`GSchema.document` stays verbatim,** holding the unmerged definitions *and* the extension nodes. Only `toString()` shows the merged view, built by `mergedTypeSystemDocument`, which additionally drops executable definitions and never emits built-in scalars. Printing `schema` and printing `schema.document` therefore legitimately disagree; pick per intent.
- **An extension with nothing to extend is silently ignored** — an unknown type name, a built-in scalar (those are `object`s of a sealed class and cannot be extended), or a kind mismatch such as `extend type` against an input object. Reporting those is SDL validation's job, not schema assembly's.
- **Members merge by name, replacing in place,** so an extended definition keeps its original member order; genuinely new members append.

Related: gschema-quirks.md, printer-lossiness.md.
