# The three tests that keep validation from going inert, and what each is for

Why `modules/execution/tests/validation/` holds pipeline-level tests on top of the ~250 per-rule cases; matters before adding a rule or reorganising the shared traversal.

The per-rule cases go through `assertValidationRule`, which never touches `Validator` (validation-test-harness.md), so a rule the shared traversal fails to dispatch to is dead in production with its own tests green. That happened and went unnoticed. Three tests close it:

- **`ValidatorRuleCoverageTest`** — every rule must fire *through* `Validator`, both alone and alongside the other 28. `testEveryRegisteredRuleHasACase` fails when a rule in `Validator.default` has no `RuleCase`, so **adding a rule means adding a fixture**. It matches rules by `qualifiedName` minus a `.Companion` suffix, because six rules are registered through a companion object whose `simpleName` is just `"Companion"`.
- **`ValidatorCorpusTest`** — pins the exact message set the whole rule set produces via the public `GDocument.validate`. Messages are compared **sorted**, deliberately: firing order is expected to change when the traversal is reorganised. Its `contextDependentRuleMarkers` list exists because a broken `VisitorContext` leaves rules silent rather than failing; the file's comment records that `SelectionUnambiguityRule` is *not* a canary there — it resolves types itself.
- **`ValidatorNestingDepthTest`** — a 1000-level nested selection must validate, pinning that the traversal is iterative rather than recursive per rule.

Also `ValidatorErrorLimitTest` — see ../execution/validation-error-limit.md.

Related: ../execution/validation-rule-authoring.md, ../language/parallel-visitor.md.
