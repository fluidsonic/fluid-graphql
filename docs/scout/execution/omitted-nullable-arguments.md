# Live defect: an omitted nullable argument is rejected as if it were required

A known spec violation in literal argument coercion, still live and deliberately left for a later release rather than overlooked; it distorts test fixtures across the execution module, so do not "simplify" one back into the trap.

`NodeInputConverter.coerceValueAbsence` (`modules/execution/sources/conversion/NodeInputConverter.kt`) errors for **any** absent value that has no default value, without consulting nullability at all. Because `convertArguments` in the same file iterates *every* argument definition of the field or directive — not only the ones the selection actually supplies — a query as ordinary as `{ field }` fails when the schema declares `field(input: Input)` with a nullable `input`. The reported message is "A value of type 'Input' must be provided for argument 'input'.". The specification's *Coercing Field Arguments* requires the argument to simply be absent from the coerced map instead.

Only the literal path is affected. `VariableInputConverter.coerceValueAbsence` does consult `context.argumentDefinition?.isRequired()`, so the same schema behaves correctly for values supplied through variables, and `NodeInputConverter.coerceVariableValue` returns `null` for an unsupplied variable in a nullable position.

Consequence for fixtures: any test that needs an omitted input field must use an input object with a single field, or supply every field. `modules/execution/tests/conversion/OneOfDirectiveCoercionTests.kt` documents exactly this workaround in its header comment — its odd single-field `SingleOneOfInput` type exists because of this defect, not because of `@oneOf`.

Related: variable-coercion.md, coercer-chaining.md.
