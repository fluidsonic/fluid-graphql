# `@oneOf` is enforced at four sites and one escapes the obvious grep

Where the "exactly one field, and that field non-null" rule for OneOf input objects is actually implemented; matters when changing its wording or behavior, and when writing fixtures that supply a OneOf field by variable.

Three sites report the plain violation through the shared `oneOfViolationMessage` in `modules/language/sources/GLanguage.kt`: `GSchema.validateValue` (reached from `ValueValidityRule`), `NodeInputConverter.coerceOneOfValue`, and `VariableInputConverter.coerceValueForInputObject` (which checks the *coerced* map, after absent fields were dropped). Change the wording in `GLanguage`, never at a call site, or the paths start disagreeing. The duplication is intentional: `validateValue` can only count *syntactic* fields, so a field whose value is a variable passes validation and is re-checked at coercion time. Only the variable-specific wordings (variable unsupplied, variable null) live in `NodeInputConverter`.

The fourth site does not use the shared message, so grepping `oneOfViolationMessage` silently misses it: `modules/execution/sources/validation/rules/VariablesInAllowedPositionRule.kt` rejects a *nullable* variable used for a OneOf field, independently of any supplied value. Its code comment records that graphql-js reports this from the same rule; the message is upstream's verbatim, double-quoted unlike the single-quoted style of that rule's other message.

Consequence for fixtures: a document supplying a OneOf field by variable must declare that variable non-null or the document itself fails validation — see the `testOneOfInputObject_acceptsSingleFieldProvidedByVariable` comment in `modules/execution/tests/validation/BuiltinDirectiveValidationTests.kt`.

Related: ../language/gschema-quirks.md, validation-rule-quirks.md, ../spec/graphql-js-parity.md.
