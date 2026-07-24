# Coercer "chain" is a single re-entrancy flag, and built-in scalars bypass type coercers

How `next()` actually works in `modules/execution/sources/conversion/`; matters when writing custom coercers.

The KDoc's "next coercer in the chain" implies a pipeline; there is no list. The chain is exactly two levels: the executor-level coercer (from the executor context) is tried first in `convertValue`/`convertOutput`, and its `next()` falls through to built-in dispatch. A type-attached coercer is invoked with the context copied to `isUsingCoercerProvidedByType = true`; every type-dispatch site guards with `type.xxxCoercer?.takeUnless { context.isUsingCoercerProvidedByType }`, so the type coercer's `next()` re-enters dispatch, skips itself, and lands on default behavior. There is no mechanism for a third level or composing multiple type coercers.

**Built-in scalars ignore type-attached coercers.** In `coerceValueForScalar` (both input converters) and `OutputConverter.coerceLeafValue`, the five built-in scalar types are handled by hardcoded branches; the type's coercer extension is only consulted in the `else` branch reached by custom scalars. A coercer attached to, say, the Int type silently never fires — only executor-level coercers can intercept built-ins.

**Input-object coercers receive the already field-coerced Map.** Both input converters coerce every field first, then invoke the type's coercer with `context.value` replaced by the resulting `Map<String, Any?>` (hence the `Map`-typed InputObject coercer keys). The coercer never sees the raw `GObjectValue`. `next()` from it diverges: in `NodeInputConverter` it is a passthrough (top-level `coerceValue` only dispatches `GValue?` values, so the Map comes back unchanged), but in `VariableInputConverter` it re-enters `coerceValueForInputObject` and re-coerces every field of the already-coerced Map — with type-attached field coercers suppressed, because the copied field contexts inherit `isUsingCoercerProvidedByType = true`.

Related: variable-coercion.md, dsl-bridge.md.
