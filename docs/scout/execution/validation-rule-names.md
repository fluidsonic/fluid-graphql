# Finding validation rules: naming scheme, wrong spec-URL comments, overlaps

How to locate the rule that implements a given spec section; matters because both the names and the header comments defeat the obvious search strategies.

**Names follow a constraint-noun scheme, not graphql-js names:** `…ExistenceRule`, `…ExclusivityRule`, `…ValidityRule`, `…PossibilityRule`, `…UnambiguityRule`, `…RequirementRule`, `…UsageRule`, `…ExecutabilityRule`. Examples: UniqueOperationNames → `OperationDefinitionNameExclusivityRule`; OverlappingFieldsCanBeMerged → `SelectionUnambiguityRule` (its test file cites the graphql-js suite it ports); NoUnusedFragments → `FragmentDefinitionUsageRule`; KnownArgumentNames → `ArgumentExistenceRule`. Three rules keep graphql-js-style names: `AllVariableUsesDefinedRule`, `AllVariablesUsedRule`, `VariablesInAllowedPositionRule` (plus `FragmentCycleDetectionRule`, matching neither scheme). Searching for graphql-js names finds nothing.

**The spec-URL header comments are unreliable copy-paste artifacts.** Several rules cite the wrong spec section (multiple cite `#sec-Fragment-Name-Uniqueness` while implementing cycle detection, fragment usage, variable uniqueness, or variable-type validity); some rules carry no link at all. Match rules by behavior and error message, never by the URL comment, and do not propagate these URLs into new rules.

**Section 5.3.3 has two overlapping registered rules:** `FieldSubselectionRule` and `ScalarLeavesRule` are both in `Validator.default` (`modules/execution/sources/validation/Validator.kt`) with overlapping firing conditions and different message styles. Also: `modules/execution/tests/validation/InputNameExclusivityRuleTest.kt` is misnamed — it exercises `OperationDefinitionNameExclusivityRule`; no InputNameExclusivityRule exists.

Related: validation-rule-authoring.md, validation-rule-quirks.md.
