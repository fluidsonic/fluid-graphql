# Attached coercers: context-free, one per channel, replace rather than chain

How a custom coercer is wired and what it may assume; matters when writing one or when looking for the removed chaining machinery.

Since 0.19.0 a coercer is a plain single-argument function — no receiver, no context object, no `next()`, and no executor-level fallback on `GExecutor.default`. There are three channels, each with its own `fun interface` in `modules/execution/sources/conversion/`: `GInputLiteralCoercer` (inline document values), `GInputValueCoercer` (variables), `GOutputValueCoercer` (resolved values). They attach to nodes through six extension accessors — `inputLiteralCoercer`, `inputValueCoercer` and `outputValueCoercer` on `GLeafType`, the first two on `GInputObjectType`, the last on `GObjectType`.

**Precedence, not composition.** Every dispatch site (`coerceValueForScalar` and `coerceValueForEnum` in both input converters, `coerceLeafValue` in `OutputConverter`) checks for the attached coercer *first*; if there is one, the type's own coercion never runs. There is no way to delegate back to it.

**Composite coercers receive the already-coerced map.** An input-object or object-type coercer is invoked with `context.value` replaced by the `Map<String, Any?>` of coerced members — hence the `Map`-typed extension keys. It never sees the raw `GObjectValue`.

The DSL entry points on `ScalarTypeDefinitionBuilder` are named after the channel (`coerceInputLiteral`, `coerceInputValue`, `coerceOutputValue`) in `modules/execution/sources/dsl/GDslForExecution.kt`; registering one leaves the other two on the type's own coercion.

Related: ../language/scalar-coercion.md, ../language/builtin-scalar-instances.md, dsl-bridge.md.
