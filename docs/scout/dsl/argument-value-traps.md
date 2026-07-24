# Argument and selection traps in the document DSL

Three silent or surprising behaviors when building documents with `GraphQL.document { }`.

**Bare `obj { }` / `list { }` inside `arguments { }` is silently discarded.** Only `"name" to value` entries become field arguments. A value expression not bound to an argument name is dropped with no error — variables referenced inside it still get declared on the operation. `modules/dsl/tests/OperationTests.kt` (`testVariables`) locks this: the golden output prints the field with no arguments at all. Always bind every value to an argument name.

**Kotlin's Pair-creating `to` is poisoned.** `GraphQLArgumentsBuilderScope` (`modules/dsl/sources/GraphQLArgumentsBuilder.kt`) declares a generic `infix fun <A, B> A.to(that: B)` with `@Deprecated(level = ERROR)`, so unsupported value types fail compilation instead of silently creating a `kotlin.Pair`. Supporting a new value type therefore requires adding a dedicated typed `to` overload there, and legitimate Pair usage inside these blocks is impossible.

**Duplicate effective field names are rejected — stricter than the GraphQL spec.** `GraphQLSelectionsContainerInternal.selection()` (`modules/dsl/sources/GraphQLSelectionsContainer.kt`) throws when a field selection's effective name (alias, else name) matches an existing direct sibling. The spec permits duplicates (they merge), so some valid documents are inexpressible. Duplicates arriving via fragment spreads or inline fragments are not checked.

Related: builder-generations.md, fragment-variable-delegation.md.
