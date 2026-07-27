# What gives way first on a deeply nested document: the parser, not validation

Where the pipeline's nesting ceiling actually sits; matters when a deeply nested document blows the stack and when adding a validation rule.

Validation used to be the low ceiling: each rule got its own walk and descended by recursion, so one level of selection nesting cost one call frame *per rule* and the stack gave out long before the parser did. It is now iterative over a heap stack (`ParallelVisit` — see parallel-visitor.md), so nesting costs heap. The recursive-descent parser is therefore the binding limit: a document deep enough to fail does so in `GDocument.parse`, before any rule runs. Do not go looking inside validation first.

Three rules still recurse per level of selection nesting themselves, which keeps validation's own ceiling well below the bare traversal's:

- `collectVariableRefs` (`modules/execution/sources/validation/rules/VariableRefCollector.kt`), used by both variable-usage rules,
- `FragmentDefinitionUsageRule.collectReferencedFragmentNames`,
- `SelectionUnambiguityRule` — the mutual recursion between `findConflict` and `findConflictsBetweenSubSelectionSets`.

A new rule that walks selection sets itself lowers the ceiling again; prefer the shared traversal's per-node callbacks. `ValidatorNestingDepthTest` (`modules/execution/tests/validation/`) pins 1000 levels through the public `GDocument.validate`.

Concrete depths are heap- and stack-size dependent, so treat only the *ordering* — parser first, then the recursing rules, then the traversal — as durable.

Related: parser-input-limits.md, parallel-visitor.md, ../execution/validation-rule-authoring.md.
