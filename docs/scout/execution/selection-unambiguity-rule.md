# SelectionUnambiguityRule: a memoizing, pairwise port of graphql-js

Why §5.3.2 (field selection merging) looks unlike every other rule in `modules/execution/sources/validation/rules/SelectionUnambiguityRule.kt`; matters before "simplifying" any of it.

It is a line-for-line port of graphql-js' `OverlappingFieldsCanBeMergedRule`, down to messages, error counts and node lists — divergence is the defect, see ../spec/graphql-js-parity.md. Consequences that read as defects but are not:

- **It is a `class` with `companion object : Factory(::SelectionUnambiguityRule)`, not a `Singleton`,** because it carries per-validation memo state (`cachedFieldsAndFragmentSpreads`, `comparedFieldsAndFragmentPairs`, `comparedFragmentPairs`). Reusing one instance across validations would leak conflicts between documents — see validation-rule-authoring.md.
- **It hooks `onSelectionSet`, so it fires for every selection set including nested ones.** Memoization, not a narrower hook, is what prevents re-walking and duplicate errors. The predecessor rule checked only the first selection set of the first definition and so left nested selections and every later definition unchecked.
- **`FieldMap` deliberately has no `equals`.** The memo and pair sets key on identity, matching upstream keying on the `Map` instance; giving it structural equality would collapse distinct selection sets.
- **Fragment cycles are harmless without a cycle guard.** `collectFieldsAndFragmentSpreads` records fragment spreads but never expands them, so collection never recurses across fragments; the pairwise `comparedFragmentPairs` set terminates the cross-fragment comparison instead.
- `argumentValuesAreEqual` compares argument values through `stringifyValue`, which sorts input-object fields by name so reordered fields do not read as a conflict. Remaining FIXMEs: `@skip`/`@include` and argument default values are ignored.

Related: validation-rule-quirks.md, ../language/parallel-visitor.md.
