# Resolvers ride on AST node extensions; fieldResolver is middleware, not fallback

How field resolution is wired; matters for schema construction, abstract types, and executor configuration.

There is no resolver registry (no graphql-java RuntimeWiring equivalent). Resolvers attach to individual `GFieldDefinition` nodes via `FieldDefinitionResolverExtensionKey` (`modules/execution/sources/resolution/`), Kotlin classes to `GObjectType` nodes via the `kotlinType` extension. Consequence: a `GSchema` parsed from SDL text has no resolvers or kotlinType mappings — everything then depends on the executor-level `fieldResolver`, and abstract types are unexecutable.

**The executor-level `fieldResolver` runs FIRST, not as fallback.** `GExecutor.default`'s KDoc calls it a fallback, but `DefaultFieldSelectionExecutor.resolveFieldValue` invokes it for every field when non-null; the definition-attached resolver only runs if it calls `context.next()`. `DefaultFieldResolverContext.next()` throws `IllegalStateException` when no further resolver exists — a wiring bug that deliberately escapes `execute()` rather than becoming a GraphQL error, and there is no property-lookup default resolver to fall back on.

**Abstract types resolve by Kotlin class, first match wins.** `resolveAbstractType` scans `schema.getPossibleTypes` and picks the first object type whose `kotlinType` (bound via the DSL's `Object<KotlinType>(...)` generic) satisfies `isInstance(value)`. No resolveType hook exists (`// FIXME support default resolver`); a possible type missing `kotlinType` can never match, and the failure is a **field error** in the response (it depends on what a resolver returned), not an exception. Note the file/symbol mismatch: file `ObjectTypeKotlinTypeExtensionKey.kt`, object `ObjectKotlinTypeNodeExtensionKey`.

**Root resolvers cannot be lambdas** (suspend function with extension receiver; test comments cite KT-40165) — use an object expression or `GRootResolver.constant(value)`. Enum values cross the resolver boundary as plain Strings equal to the enum value name — no Kotlin enum classes involved (pinned in `modules/execution/tests/spec/execution/CoercionTests.kt`).

Related: dsl-bridge.md, error-classification.md, spec-deviations.md.
