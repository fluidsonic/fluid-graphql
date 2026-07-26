# Individual validation rule quirks and known gaps

Per-rule behaviors that look like bugs or hide as correct code; matters when editing these rules or interpreting their output.

**`SelectionUnambiguityRule` has a dead cycle guard.** `groupByResponseName` threads a `visitedFragments: MutableSet<String>` parameter through every overload but never adds to or checks it — unlike the working pattern in `SubscriptionRootFieldExclusivityRule.collectFields` and `collectVariableRefs` (`VariableRefCollector.kt`). Since `FragmentCycleDetectionRule` only reports and never aborts the shared walk, cyclic fragments reach this rule's unguarded recursion. The file also carries FIXMEs (conflicting fragments that cannot co-occur, `@skip`/`@include` ignored, argument defaults ignored).

**`VariablesInAllowedPositionRule` scopes by document order.** Its `varDefMap` resets only in `onOperationDefinition`, so variable references inside fragment definitions are checked against whichever operation was most recently visited — or silently skipped if the fragment precedes all operations. Map misses `?: return` silently; the undefined-variable error is reported by `AllVariableUsesDefinedRule` instead. It also carries the OneOf nullable-variable check, which bypasses the shared OneOf message (oneof-enforcement.md).

**Variable-usage rules are hybrid collectors:** definitions come from visitor callbacks, but uses come from the standalone `collectVariableRefs`, which recursively follows fragment spreads (the operation subtree cannot see fragment definitions elsewhere), guards cycles with a visited set, and skips undefined fragments.

**`SubscriptionRootFieldExclusivityRule` rejects a lone `__typename`** root field — introspection fields do not count as the one allowed root field, despite the error message saying "exactly one root field". It resolves fields through fragment spreads.

**`ValueValidityRule` accepts any literal for custom scalars** except null against a non-null type — deliberate, encoded in `ValueValidityRuleTest` fixtures whose Scalar entries intentionally produce no errors.
