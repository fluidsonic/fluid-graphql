# Variable coercion: NoValue sentinel, literal-path defaults, and ordering contracts

Cross-file contracts in `modules/execution/sources/conversion/VariableInputConverter.kt` and `NodeInputConverter.kt`; matters for absence semantics and for reading the coerced argument map.

**Absent is not null.** When a variable, argument or input-object field has no value and no default, both converters return the internal `NoValue` sentinel (`modules/execution/sources/utility/NoValue.kt`) and strip those entries with `filterValues { it != NoValue }` — so an omitted nullable argument is simply missing from the coerced map, never `null` and never an error. Only a non-null type with no default makes `coerceValueAbsence` report an error. The same sentinel doubles as the executor's "field skipped" marker — see validation-entry-points.md — so do not repurpose it.

**Defaults go through the LITERAL path.** `VariableInputConverter.coerceValueAbsence` delegates default values to `nodeInputConverter.convertValue` because defaults are AST `GValue` nodes. A scalar's `inputLiteralCoercer` therefore runs for defaulted variables; its `inputValueCoercer` does not.

**Inline variable references are a raw lookup.** `NodeInputConverter.coerceVariableValue` returns `execution.variableValues[name]` with no re-coercion — literal coercers never fire for values supplied via variables.

**Coercion does not stop at the first bad value.** Every variable, input-object field and list element is coerced inside `CoercionErrorLimit.collecting`, which swallows the `GErrorException`, appends its errors to one shared mutable list carried on every derived `Context`, and yields `NoValue` so siblings still get coerced. After 50 collected errors it throws a fixed terminal error, reported last; the cap deliberately has no knob, mirroring graphql-js's `@internal` `maxCoercionErrors`. Pinned by `modules/execution/tests/conversion/MaxCoercionErrorsTests.kt`.

**Variables are coerced before the root exists.** `DefaultExecutor.makeContext` builds the context with `root = Unit` and empty `variableValues`, coerces, and only then runs `resolveRoot` and copies both in.

Related: coercer-attachment.md, response-shape.md.
