# Individual validation rule quirks and known gaps

Per-rule behaviors that look like bugs or hide as correct code; matters when editing these rules or interpreting their output.

**`ScalarLeavesRule` names the wrapped type in its message but decides on the underlying one.** Leaf-ness is tested against `data.relatedType.underlyingNamedType`, while the message interpolates `type.name` of the full wrapper — so a list-of-object field reports `of type "[Object]" must have a selection of subfields` (pinned in `ScalarLeavesRuleTest`). That mismatch is upstream's wording, not a bug; do not "fix" it. It is the sole rule for §5.3.3.

**`VariablesInAllowedPositionRule` scopes by document order.** Its `varDefMap` resets only in `onOperationDefinition`, so variable references inside fragment definitions are checked against whichever operation was most recently visited — or silently skipped if the fragment precedes all operations. Map misses `?: return` silently; the undefined-variable error is reported by `AllVariableUsesDefinedRule` instead. It also carries the OneOf nullable-variable check, which bypasses the shared OneOf message (oneof-enforcement.md).

**Variable-usage rules are hybrid collectors:** definitions come from visitor callbacks, but uses come from the standalone `collectVariableRefs`, which recursively follows fragment spreads (the operation subtree cannot see fragment definitions elsewhere), guards cycles with a visited set, and skips undefined fragments.

**`SubscriptionRootFieldExclusivityRule` rejects a lone `__typename`** root field — introspection fields do not count as the one allowed root field, despite the error message saying "exactly one root field". It resolves fields through fragment spreads.

**`ValueValidityRule` owns almost none of its own behaviour.** It delegates every leaf literal to `GSchema.validateValueForExecution`, so a rejection reports the *scalar's* bare wording (`Int cannot represent non-integer value: "…"`) and a search for the old `Type 'Int' does not allow value` finds nothing. Its `attachedScalarLiteralCoercion` helper must keep resolving a coercer exactly the way `NodeInputConverter.coerceValueForScalar` does. Any literal is still accepted for a custom scalar (except null against a non-null type), because `GCustomScalarType` inherits identity coercion — pinned by `ValueValidityRuleTest.testAcceptsAnyResolvedValueForCustomerScalar`.

Related: selection-unambiguity-rule.md, validation-rule-authoring.md, validation-rule-names.md, ../language/scalar-coercion.md.
