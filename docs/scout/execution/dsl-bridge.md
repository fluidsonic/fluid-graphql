# GDslForExecution: how execution extends the dsl schema builder

The bridge that adds resolvers and coercers to a DSL that knows nothing about them; matters when defining executable schemas or refactoring the builder overloads.

The dsl module's builder has no resolver/coercer API. `modules/execution/sources/dsl/GDslForExecution.kt` attaches them to AST nodes through extension keys (`FieldDefinitionResolverExtensionKey`, `ObjectKotlinTypeNodeExtensionKey`, and the LeafType coercer keys). The resolver and coercer attach points enforce single assignment via `require(extension(key) == null)` — attaching a second resolver or coercer to the same node throws at schema-build time. (The Kotlin-type extension attach in the typed `Object` overloads has no such check.)

The typed `Object<KotlinType>(...)` overloads record the Kotlin KClass as a node extension and wrap the builder in `GDslForExecution.ObjectTypeDefinitionBuilder`, which hides the inherited untyped `field()` via `@Deprecated(level = HIDDEN)` plus a `@JvmName("fieldWithType")` replacement, steering overload resolution to the typed `FieldDefinitionBuilder` lambda. This deprecation-plus-JvmName trick is fragile under refactoring; the HIDDEN-deprecation shim (without the `@JvmName` part) is the same device the visitor hierarchy uses (see ../language/visitor-api-surface.md).

Scalars have three separate coercer channels, not one: `coerceInputLiteral` (inline document values), `coerceInputValue` (variables) and `coerceOutputValue`. Registering one leaves the others on the scalar's own coercion — a frequent source of asymmetry (see variable-coercion.md for which channel handles default values, and coercer-attachment.md for precedence).

Related: resolver-wiring.md, coercer-attachment.md.
