# Variable coercion: NoValue sentinel, node-path defaults, and ordering contracts

Cross-file contracts in `modules/execution/sources/conversion/VariableInputConverter.kt` and `NodeInputConverter.kt`; matters for custom variable coercers and absence semantics.

**Absent is not null.** When a variable or input-object field has no value and no default, `VariableInputConverter` returns the internal `NoValue` sentinel (`modules/execution/sources/utility/NoValue.kt`), and `convertValues`/`coerceValueForInputObject` strip those entries with `filterValues`. Unsupplied variables are entirely absent from the coerced map. The same sentinel doubles as the executor's "field skipped" marker — see validation-entry-points.md — so do not repurpose it. Escape hatch in `coerceValueAbsence`: a NonNull-typed value with no default still yields `NoValue` (not an error) when `context.argumentDefinition?.isRequired() == false` — the non-standard `@optional` case. `NodeInputConverter` has no sentinel; absence there errors or takes the default.

**Defaults go through the NODE path.** `coerceValueAbsence` delegates default values to `nodeInputConverter.convertValue` because defaults are AST `GValue` nodes. A custom scalar's node-input coercer runs for defaulted variables; its variable-input coercer does not.

**Inline variable references are a raw lookup.** `NodeInputConverter.coerceVariableValue` returns `execution.variableValues[name]` with no re-coercion — type-level node coercers never fire for values supplied via variables.

**Coercion does not stop at the first bad value.** Every variable, input-object field and list element is coerced inside `CoercionErrorLimit.collecting`, which swallows the `GErrorException`, appends its errors to one shared mutable list carried on every derived `Context`, and yields `NoValue` so siblings still get coerced. After 50 collected errors it throws a fixed terminal error, reported last; the cap deliberately has no knob, mirroring graphql-js's `@internal` `maxCoercionErrors`. Pinned by `modules/execution/tests/conversion/MaxCoercionErrorsTests.kt`.

**Variable coercers see a provisional context.** `DefaultExecutor.makeContext` builds the context with `root = Unit` and empty `variableValues`, runs variable coercion, then copies in the real root. A variable coercer reading `execution.root` or `execution.variableValues` sees placeholders; the root resolver only runs after all variables coerced successfully.

Related: coercer-chaining.md, response-shape.md, omitted-nullable-arguments.md.
