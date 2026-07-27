# Expected validation errors: paste them back, except where a header forbids it

Which validation expectations are regenerated from a test run and which are hand-derived; pasting the actual value into the wrong one silently blesses a behaviour change.

**The default is paste-back.** Assertions compare full `GError.describe()` renderings (message, `<origin>:line:column` block, source excerpt, caret marker). Only when the error *count* differs does `assertErrors` (`modules/execution/tests/utility/Assertions.kt`) print the actual errors as Kotlin raw strings via `toKotlinRawString("\t\t\t\t")`; a comment states this exists so they can be copied into test code. A same-count content mismatch yields a plain string diff instead, and the raw-string calls in `assertError` are commented out. Workflow: run with an empty `errors` list, paste the printed output.

**The `ValueValidityRule*Errors.kt` files are generated that way and are position-locked.** They hold the expectations for two giant fixtures, `ValueValidityRuleDocumentRejectionTest` and `ValueValidityRuleSchemaRejectionTest`; their excerpts index those exact fixtures line by line, so a fixture and its expectation files can only be edited together. Inserting one line into a fixture shifts every excerpt below it and forces regenerating all of the sibling `*Errors.kt` files — hundreds of golden strings — so a new case belongs in `ValueValidityDelegationTests.kt` instead of in the shared fixture.

**`ValueValidityDelegationTests.kt` is the opposite and says so in its header.** Nothing there is regenerated: every expected message was read off the scalar's own failure wording in `modules/language/sources/model/nodes/BuiltinScalarTypes.kt`, and every expected error *count* was taken from graphql-js (upstream keeps checking sibling values after rejecting one). A failure there is a decision about whether the behaviour change is wanted — never a paste-back.

Related: validation-test-harness.md, ../language/scalar-coercion.md, ../spec/graphql-js-parity.md.
