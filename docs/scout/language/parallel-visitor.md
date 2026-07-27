# parallelize() is a single-pass multiplexer, not concurrency

Semantics of `Iterable<Visitor>.parallelize()` (`modules/language/sources/visitors/ParallelVisitor.kt`); matters for validation (all 29 rules run through it) and any multi-visitor code.

No threads or coroutines are involved. `ParallelVisitor` runs all visitors in lock-step over one shared walker: for each node every visitor's `onNode` runs and returns before the next visitor sees that node, and the subtree is traversed only afterwards. Rules therefore share one deterministic traversal, firing per node in registration order — so their errors come out in document order rather than grouped by rule.

Control-flow semantics invert naive expectations:

- **Traversal is iterative over a heap stack** (`ParallelVisit.step`), not recursion through the visitors, so nesting costs heap rather than one call frame per visitor (nesting-depth-limits.md). Consequence: `visitChildren()` returns *before* the subtree is traversed and only records the data the subtree is visited with. Work that must follow a subtree belongs in `visit.afterChildren { … }` — see visitor-traversal.md.
- **skipChildren is isolated per visitor:** a skipping visitor sees no node of that subtree, but the walker still descends as long as any other visitor has not skipped (`ParallelVisit.shouldDescend`). A skipped node runs no `afterChildren` block for that visitor either, so registering one and then skipping drops it silently.
- **abort removes only the aborting visitor;** traversal stops entirely only when every visitor has aborted. An abort supersedes that visitor's skip.
- **Never compose as `parallelize().contextualize()`.** It typechecks, but advances the `VisitorContext` only for the root node and thereby silences most validation rules; use `parallelizeContextualized()`. Verified to bite — see the `contextDependentRuleMarkers` comment in `modules/execution/tests/validation/ValidatorCorpusTest.kt`.

The golden-stack tests pin the exact interleaving — see ../testing/ast-golden-tests.md.

Related: visitor-traversal.md, ../execution/validation-rule-authoring.md.
