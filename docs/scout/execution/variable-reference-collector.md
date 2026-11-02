# Variable-usage collector: what counts as a use

The shared `collectVariableRefs` decides which variables an operation uses for both variable-usage rules; matters when a variable appears only in a directive or a fragment.

It takes the `GOperationDefinition`, not its selection set, so the operation's own directives are scanned too. `collectFromDirectives` in `modules/execution/sources/validation/rules/VariableRefCollector.kt` reads directive arguments on:

- the operation itself;
- fields and inline fragments;
- fragment spreads, and the fragment definitions those spreads reach.

A spread's own directives count even when its fragment was already visited or is undefined; only the fragment body is skipped in those cases. Fragment definitions that no spread reaches are never scanned, so a variable used only in their directives is reported "defined but never used" (pinned by `testRejectsVariableUsedOnlyInDirectiveOfUnreachedFragment` in `Sec5_8_4_AllVariablesUsedTests`). The traversal follows spec §5.8.4 (All Variables Used). Cycles are guarded by a visited set.

Related: validation-rule-quirks.md, ../language/nesting-depth-limits.md.
