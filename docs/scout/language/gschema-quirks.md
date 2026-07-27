# GSchema construction and value validation quirks

Silent failure modes in `modules/language/sources/model/GSchema.kt` and directive modeling.

**`validateValue` can "pass" without checking anything.** It returns an empty error list when the value is a `GVariableRef` (comment: variables not supported here yet) and when the type reference cannot be resolved in the schema (comment: "We don't check types - only values"). A typo'd type name in an SDL argument definition silently disables value validation for that position. Output composite types always pass. Per single-value list coercion, a non-list scalar can validate against a list type.

**The `@oneOf` check here is one of four enforcement sites**, deliberately duplicated because syntactic counting cannot see variable values, and its wording is shared from `modules/language/sources/GLanguage.kt` — read ../execution/oneof-enforcement.md before changing either.

**`GDirectiveDefinition` drops unknown locations.** Its constructor resolves location names via `mapNotNullTo` against the `GDirectiveLocation` enum (`modules/language/sources/model/nodes/GNode.kt`); unrecognized names are silently omitted from `locations` while remaining in `locationNodes` — printed SDL and resolved semantics diverge with no error.

Related: type-system-extension-merging.md, nonstandard-spec-extensions.md, ../execution/oneof-enforcement.md, ../execution/omitted-nullable-arguments.md.
