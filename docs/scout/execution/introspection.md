# Introspection runs against a swapped meta-schema with the user schema as root

How `__schema`/`__type`/`__typename` execute; matters when touching `Introspection.kt`, the field-selection executor, or anything comparing against graphql-js.

`modules/execution/sources/introspection/Introspection.kt` builds the meta-schema with the library's own typed execution DSL, dogfooding the resolver machinery. Its resolvers obtain the schema being introspected via `execution.root as GSchema` — an implicit protocol with `DefaultFieldSelectionExecutor.executeIntrospection`, which copies the executor context replacing `schema` with `Introspection.schema`, `root` with the user schema, and `rootType` with `Introspection.schemaType`. All nested selections under `__schema`/`__type` therefore resolve against the meta-schema; user-configured type-specific coercers/resolvers see a different schema inside introspection subtrees. Dispatch happens per response-key group inside the field-selection executor, not as a pre-pass.

Meta-field injection: `GSchema.fieldDefinition(name, parentType)` (`modules/execution/sources/extensions/GSchema.execution.kt`) injects `__schema`/`__type` only when the parent type is the query root; `__typename` on every type. `TypeResolver.resolveType` falls back to `Introspection.schema` by default, which is why user schemas never define `__`-prefixed types yet fragments may use them as type conditions.

Behavioral deviations from graphql-js: `__schema.types` excludes the introspection meta-types and orders user types before the five built-in scalars alphabetically; directive `locations` come back alphabetically sorted. Introspected `defaultValue` is a GraphQL-encoded string (a String default is quoted).

The meta-schema is hand-maintained, so a specified introspection field exists only if someone added it here; the header still cites the June 2018 specification while the content tracks the draft (`specifiedByURL`, `__InputValue.isDeprecated`, `__Directive.isRepeatable`, `__Type.isOneOf`, the `DIRECTIVE_DEFINITION` enum value). `isOneOf` resolves to `null` for anything that is not an input object, matching graphql-js rather than returning `false`. Adding a built-in directive shifts golden expectations in `IntrospectionTests`.

Related: resolver-wiring.md, ../spec/spec-citations.md.
