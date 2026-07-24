# Two DSL generations coexist; the GraphQL* triad is the target style

Which builder style to imitate when adding DSL surface in `modules/dsl`.

The schema builder (`modules/dsl/sources/GSchemaBuilder.kt` and `DefaultSchemaBuilder.kt`, with nested builder interfaces and the SchemaBuilder* marker annotations) is the older generation — both files open with `// TODO Rework this into GraphQL* types.` The document/operation side is the newer pattern, a triad per concern: a sealed public `*Builder`/container interface, a sealed public `*Scope` interface (what DSL lambdas receive), and a non-sealed internal `*Internal` interface carrying the mutable state (for example `GraphQLArgumentsContainer` / `GraphQLArgumentsContainerScope` / `GraphQLArgumentsContainerInternal` in `modules/dsl/sources/GraphQLArgumentsContainer.kt`). New DSL surface should follow the GraphQL* triad, not the GSchemaBuilder style.

DSL entry points like `arguments { }`, `directives { }`, `query { }` are public inline extensions on the sealed scope interfaces that downcast with a `when (this) { is <Container> -> ... }` that has no else branch and silently does nothing otherwise. This is not dead code: the scopes are sealed and every scope implementor is also the container, so the branch always matches. The shape exists because a member function on the interface could not be inline, and an inline extension cannot touch the internal mutable state directly — replicate it exactly for new blocks (see the `arguments` extension in `GraphQLArgumentsContainer.kt`).

Related: argument-value-traps.md, kotlin-workarounds.md.
