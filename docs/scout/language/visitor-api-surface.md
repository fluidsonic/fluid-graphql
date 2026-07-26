# Visitor API surface tricks: HIDDEN shims, dead hooks, and cross-module opt-in

How the visitor hierarchy in `modules/language/sources/visitors/Visitor.kt` shapes its API, and traps when subclassing or extending it.

**Cross-module internals are `public` + `@InternalGraphqlApi`.** The whole visitor framework is public but gated behind `@InternalGraphqlApi` (`modules/language/sources/utility/InternalGraphqlApi.kt`), a `@RequiresOptIn` at Level.ERROR. This is the mechanism for sharing internals across the three separately compiled modules where Kotlin `internal` cannot reach; dsl/execution are the intended consumers. Every nested class and companion must repeat the annotation even though Kotlin does not require it — see ../gradle/binary-compatibility-validator.md.

**`@Deprecated(level = HIDDEN) final override` shims shape the surface.** The `WithoutData` and `Typed` variants hide the data-taking methods this way while keeping dispatch working. When adding a new `GNode` type, this shim pattern must be replicated exactly in every variant class, or the new callback leaks or misroutes.

**Two hooks are dead code in `Visitor.Hierarchical.WithoutData`.** The hidden final overrides for `GScalarType` and `GArgumentDefinition` bypass their dataless same-named hooks: `onScalarType` routes to `onNamedType` and `onArgumentDefinition` routes to `onAny`. Overriding the dataless versions in a subclass silently never fires — no compiler warning.

**Scalars do not reach `onLeafType`.** In both Hierarchical variants, `GScalarType` routes to `onNamedType`, so overriding `onLeafType` catches enums but not scalars, despite scalars being GraphQL leaf types.

Related: visitor-traversal.md, parallel-visitor.md.
