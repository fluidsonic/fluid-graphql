# Variable-usage collector: what counts as a use

The shared `collectVariableRefs` decides which variables an operation uses for both variable-usage rules; matters when a variable appears only in a directive or a fragment.

It takes the `GOperationDefinition`, not its selection set, so the operation's own directives are scanned too. `collectFromDirectives` in `modules/execution/sources/validation/rules/VariableRefCollector.kt` reads directive arguments on the operation, on fields and inline fragments, and on fragment spreads, whose own directives count even when the spread's fragment is undefined or was already collected.

The result is the operation's own references in document order, then the body of each reached fragment once. Reached fragments come from `collectReferencedFragments` in graphql-js order: a selection set's own spreads come before nested ones, and nested selection sets are visited last in, first out (pinned by `testRejectsUndefinedVariablesOfFragmentsInUpstreamOrder` in `Sec5_8_3_AllVariableUsesDefinedTests`). Cycles and repeats are stopped by the collected-name set there, not by a visited set during recursion.

Fragment definitions that no spread reaches are never scanned, so a variable used only in their directives is reported "defined but never used" (pinned by `testRejectsVariableUsedOnlyInDirectiveOfUnreachedFragment` in `Sec5_8_4_AllVariablesUsedTests`). The traversal follows spec §5.8.4 (All Variables Used).

Related: validation-rule-quirks.md, variables-in-allowed-position-scope.md, ../language/nesting-depth-limits.md.
