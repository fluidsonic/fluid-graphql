# `by`-delegated fragment()/variable() registration is deferred; errors surface at build()

How the document DSL's property-delegated fragment and variable definitions actually register; matters when debugging "unused" definition failures or reasoning about definition order.

The `fragment(typeCondition) { }` and `variable(type) { }` overloads without an explicit name return a `RefFactory` and register nothing immediately — the definition is only created inside `provideDelegate`, when the property name becomes known. Each factory is tracked in `unusedFragmentDefinitionRefFactories` / `unusedVariableRefFactories` (`modules/dsl/sources/GraphQLFragmentDefinitionContainer.kt`, `GraphQLVariableContainer.kt`); `provideDelegate` removes it, and `build()` calls `finalize()`, which throws if any factory was never `by`-delegated.

The `IllegalStateException` is constructed eagerly as a `RefFactory` property (`notDelegatedError`) and only thrown later via `throwNotDelegatedError()`. Because a JVM throwable captures its stack trace at construction, the trace points at the original `fragment()`/`variable()` call site even though the throw happens inside `build()`. Making that construction lazy would shift the trace to `build()` and lose the only pointer to the mistake.

Consequences:

- Forgetting `by` fails only at `build()` time, far from the mistake.
- Delegating the same factory from two properties registers two separate definitions, one per property name (removing the factory from the unused list is a no-op the second time).
- Declaration order in the built document follows delegation order, not call order.

Related: builder-generations.md, argument-value-traps.md.
