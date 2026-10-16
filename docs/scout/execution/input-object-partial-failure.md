# Variable input objects with a failed field are dropped, not coerced

What the variable path does with an input object when one of its own fields fails coercion; matters when writing an input-object coercer or changing the @oneOf check for variables.

**A failed field drops the whole object.** In `VariableInputConverter.coerceValueForInputObject`, the error count is captured before the fields are coerced. If it has grown by the time the fields are collected (`hasFailedField`), the method returns `NoValue` before the type's `inputValueCoercer` runs. A composite coercer therefore never sees a map missing a field that failed. Absent fields are different: they are dropped and the coercer still runs.

**The check compares counts on purpose.** The errors list is shared by every variable (see variable-coercion.md), so testing for any errors at all would suppress this object's coercer when only another variable failed. Pinned by `VariableCoercionTests.testInputObjectVariableCoercion_runsCoercerWhenOnlyAnotherVariableIsInvalid`.

**@oneOf counts supplied keys once a field failed.** The coerced map has lost the failed field, so the one-field check then counts the declared fields the client supplied, close to graphql-js 17's `validateInputValue` (which also counts unknown keys; this converter ignores those everywhere): `{a: <bad>}` reports only the field error, `{a: <bad>, b: 1}` reports it plus the violation. Without a failed field it still checks the coerced map. See oneof-enforcement.md.

Related: variable-coercion.md, oneof-enforcement.md.
