# GSchema construction and value validation quirks

Silent failure modes in `modules/language/sources/model/GSchema.kt` and directive modeling.

**@deprecated is never deduplicated (bug).** The top-level `GSchema(document, supportOptional)` factory appends each default directive only when no same-named directive exists — except the `@deprecated` check compares `it.name` against `GLanguage.defaultDeprecatedDirective.description` (not `.name`). No name ever equals the description, so the built-in definition is always appended; a document defining its own `directive @deprecated` ends up with two definitions, and `directiveDefinition(name)` returns whichever comes first (the document's own).

**`validateValue` can "pass" without checking anything.** It returns an empty error list when the value is a `GVariableRef` (comment: variables not supported here yet) and when the type reference cannot be resolved in the schema (comment: "We don't check types - only values"). A typo'd type name in an SDL argument definition silently disables value validation for that position. Output composite types always pass. Per single-value list coercion, a non-list scalar can validate against a list type.

**`GDirectiveDefinition` drops unknown locations.** Its constructor resolves location names via `mapNotNullTo` against the `GDirectiveLocation` enum; unrecognized names are silently omitted from `locations` while remaining in `locationNodes` — printed SDL and resolved semantics diverge with no error.

**Deprecation accessors exist where the directive is not permitted.** `GArgumentDefinition` mixes in `WithOptionalDeprecation`, but `GLanguage.defaultDeprecatedDirective` declares only ENUM_VALUE and FIELD_DEFINITION locations (the October 2021 spec also permits argument/input-field locations).
