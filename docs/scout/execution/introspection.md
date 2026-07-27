# Introspection types are ordinary schema members; only their resolvers live in the executor

How `__schema`/`__type`/`__typename` execute; matters when touching introspection, comparing against graphql-js, or wondering why the resolvers are not on the nodes.

The eight introspection types are built by the **language** module (`modules/language/sources/introspection/GIntrospection.kt`, with the shells in `IntrospectionObjectTypes.kt` and `IntrospectionEnumTypes.kt`) and appended to every `GSchema.types`. There is one type-identity domain: `__type(name: "__Type")` and a fragment on `__Type` resolve through the same name lookup as any user type. No schema is swapped in mid-execution any more, and `TypeResolver` is gone — validation rules call `data.schema.resolveType` directly.

**The shells carry no resolvers, by design.** The `GIntrospection` KDoc gives the reason: a resolver needs the execution module's `GFieldResolverContext`, which the language module knows nothing about. So `DefaultFieldResolverContext.resolverForField` falls back to `IntrospectionResolvers.resolverFor(typeName, fieldName)` — an executor-side table keyed by type and field name — when the field definition carries no `resolver` extension. `GNodeExtensionSet { }` always builds from empty and a node's extensions are constructor-only, so nothing could be attached to an already-built shell in any case.

Only the three meta-fields still carry their resolvers directly (`modules/execution/sources/introspection/Introspection.kt`), because this module creates those nodes itself; `executeIntrospection` injects `__schema` and `__type` on the query root only, `__typename` anywhere.

Deviations from graphql-js: directive `locations` come back alphabetically sorted, and an introspected `defaultValue` is a GraphQL-encoded string. `isOneOf` answers `null` for anything that is not an input object, matching upstream.

Related: resolver-wiring.md, ../language/builtin-scalar-instances.md, ../spec/spec-citations.md.
