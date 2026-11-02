# VariablesInAllowedPositionRule: per-operation scope

How the rule chooses which variable usages it checks and which fragments it checks at all; matters when editing the rule or reading its errors for shared fragments.

The rule checks each operation as the validation traversal leaves it (`onOperationDefinition`, after children), like graphql-js. For that operation it compares the operation's own variable definitions with its own usages and with those of every fragment it reaches, as listed by `collectReferencedFragments` in `modules/execution/sources/validation/rules/VariableRefCollector.kt`.

Consequences: a fragment shared by two operations is reported once per operation, each time against that operation's variable types. A fragment that no operation reaches is never checked, even though it is defined. Pinned by `testRejectsNullableVariableOfEachOperationSharingFragment` in `Sec5_8_5_VariableUsagesAllowedTests`.

Typed usages and expected types come from a separate `VisitorContext` traversal, `VariableUsageCollector`, run per operation or fragment. Fragment results are cached by name in `usagesByFragmentName`.

Pitfalls: a usage of an undefined variable returns silently here (`?: return`); `AllVariableUsesDefinedRule` reports it. The OneOf nullable-variable check also lives in this rule (oneof-enforcement.md).

Related: validation-rule-quirks.md, variable-reference-collector.md.
