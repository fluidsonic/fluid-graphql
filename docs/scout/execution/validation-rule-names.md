# Finding validation rules: naming scheme and wrong spec-URL comments

How to locate the rule that implements a given spec section; matters because both the names and the header comments defeat the obvious search strategies.

**Names follow a constraint-noun scheme, not graphql-js names:** `…ExistenceRule`, `…ExclusivityRule`, `…ValidityRule`, `…PossibilityRule`, `…UnambiguityRule`, `…RequirementRule`, `…UsageRule`, `…ExecutabilityRule`. Examples: UniqueOperationNames → `OperationDefinitionNameExclusivityRule`; OverlappingFieldsCanBeMerged → `SelectionUnambiguityRule` (its test file cites the graphql-js suite it ports); NoUnusedFragments → `FragmentDefinitionUsageRule`; KnownArgumentNames → `ArgumentExistenceRule`. Three rules keep graphql-js-style names: `AllVariableUsesDefinedRule`, `AllVariablesUsedRule`, `VariablesInAllowedPositionRule` (plus `FragmentCycleDetectionRule`, matching neither scheme). Searching for graphql-js names finds nothing.

**The spec-URL header comments are unreliable copy-paste artifacts.** Several rules cite the wrong spec section (multiple cite `#sec-Fragment-Name-Uniqueness` while implementing cycle detection, fragment usage, variable uniqueness, or variable-type validity); some rules carry no link at all. Match rules by behavior and error message, never by the URL comment, and do not propagate these URLs into new rules.

**The registry, not the rules directory, is the authority on what runs.** `Validator.default` (`modules/execution/sources/validation/Validator.kt`) lists all 29 rules; a rule file absent from that list does not run at all. Leaf-field selections (§5.3.3) are covered by `ScalarLeavesRule` alone — the formerly overlapping `FieldSubselectionRule` was deleted, so its name and its distinct message wording survive only in old commits. `ValidatorRuleCoverageTest` fails if a registered rule has no fixture, so the list cannot silently grow (../testing/validator-pipeline-coverage.md).

Related: validation-rule-authoring.md, validation-rule-quirks.md.
