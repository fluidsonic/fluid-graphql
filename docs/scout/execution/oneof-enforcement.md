# `@oneOf` is enforced at five sites and two escape the obvious grep

Where the OneOf input object rules are actually implemented; matters when changing their wording or behaviour, and when writing fixtures that supply a OneOf field by variable.

Three sites report the *value* violation ("exactly one field, and that field non-null") through the shared `oneOfViolationMessage` in `modules/language/sources/GLanguage.kt`: `GSchema.validateValue` (reached from `ValueValidityRule`), `NodeInputConverter.coerceOneOfValue`, and `VariableInputConverter.coerceValueForInputObject` (which checks the *coerced* map, after absent fields were dropped). Change the wording in `GLanguage`, never at a call site, or the paths start disagreeing. The duplication is intentional: `validateValue` can only count *syntactic* fields, so a field whose value is a variable passes validation and is re-checked at coercion time. Only the variable-specific wordings (variable unsupplied, variable null) live in `NodeInputConverter`.

Two more sites do not use the shared message, so grepping `oneOfViolationMessage` silently misses them:

- `modules/execution/sources/validation/rules/VariablesInAllowedPositionRule.kt` rejects a *nullable* variable used for a OneOf field, independently of any supplied value. Its comment records that graphql-js reports this from the same rule; the message is upstream's verbatim.
- `validateOneOfInputObjectType` in `modules/language/sources/model/SchemaValidation.kt` enforces the *type-level* rule — a OneOf field must be nullable and must not carry a default — and is reached only through `GSchema.validate()`.

Note the deliberate asymmetry the second bullet creates and the first depends on: the field must be nullable, the variable supplying it must not be.

Consequence for fixtures: a document supplying a OneOf field by variable must declare that variable non-null or the document itself fails validation — see the `testOneOfInputObject_acceptsSingleFieldProvidedByVariable` comment in `modules/execution/tests/validation/BuiltinDirectiveValidationTests.kt`.

Related: ../language/gschema-quirks.md, validation-rule-quirks.md, ../spec/graphql-js-parity.md.
