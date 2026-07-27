# A scalar coerces itself: four members on GScalarType, no coercion tables

Where scalar coercion lives since 0.19.0; matters for anyone looking for the old `when` tables in the execution converters, and for writing a custom scalar.

`GScalarType` (`modules/language/sources/model/nodes/GNode.kt`) declares four open members that mirror graphql-js: `coerceOutputValue`, `coerceInputValue`, `coerceInputLiteral`, `valueToLiteral`. The five built-ins live in `modules/language/sources/model/nodes/BuiltinScalarTypes.kt` and override what each of them needs; `GCustomScalarType` inherits the defaults, which are identity for the two value directions and `null` for the two literal ones — so a custom scalar without an attached coercer accepts and passes through anything.

Two traps in that surface:

- **`coerceInputLiteral` is a nullable property, not an overridable function.** Absent is deliberately distinct from "returns null": a caller that finds it `null` unwraps the literal generically and hands the result to `coerceInputValue` instead. Both `NodeInputConverter.coerceValueWithType` and `GSchema.validateValue`'s `reportRejectedScalarLiteral` implement exactly that fallback — keep them in step.
- **A coercion raises a bare, context-free `GError`.** The caller enriches it with the argument, variable or field; see `enrichingCoercionFailure` in each converter's private `Context`.

Validation delegates to the very same coercion, so validation and execution cannot disagree about which literals are legal — see `GSchema.validateValueForExecution` and gschema-quirks.md.

Related: builtin-scalar-instances.md, ../execution/coercer-attachment.md.
