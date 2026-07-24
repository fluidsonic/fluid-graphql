# Variable coercion: NoValue sentinel, node-path defaults, and ordering contracts

Cross-file contracts in `modules/execution/sources/conversion/VariableInputConverter.kt` and `NodeInputConverter.kt`; matters for custom variable coercers and absence semantics.

**Absent is not null.** When a variable or input-object field has no value and no default, `VariableInputConverter` returns the internal `NoValue` sentinel, and `convertValues`/`coerceValueForInputObject` strip those entries with `filterValues`. Unsupplied variables are entirely absent from the coerced map. Escape hatch in `coerceValueAbsence`: a NonNull-typed value with no default still yields `NoValue` (not an error) when `context.argumentDefinition?.isRequired() == false` — the non-standard `@optional` case. `NodeInputConverter` has no sentinel; absence there errors or takes the default.

**Defaults go through the NODE path.** `coerceValueAbsence` delegates default values to `nodeInputConverter.convertValue` because defaults are AST `GValue` nodes. A custom scalar's node-input coercer runs for defaulted variables; its variable-input coercer does not.

**Inline variable references are a raw lookup.** `NodeInputConverter.coerceVariableValue` returns `execution.variableValues[name]` with no re-coercion — type-level node coercers never fire for values supplied via variables, and variable coercion must complete before any argument conversion.

**Variable coercers see a provisional context.** `DefaultExecutor.makeContext` builds the context with `root = Unit` and empty `variableValues`, runs variable coercion, then copies in the real root. A variable coercer reading `execution.root` or `execution.variableValues` sees placeholders; the root resolver only runs after all variables coerced successfully.

Related: coercer-chaining.md, error-channels.md.
