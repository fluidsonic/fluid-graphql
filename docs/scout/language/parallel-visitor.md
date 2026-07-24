# parallelize() is a single-pass multiplexer, not concurrency

Semantics of `Iterable<Visitor>.parallelize()` (`modules/language/sources/visitors/ParallelVisitor.kt`); matters for validation (all ~30 rules run through it) and any multi-visitor code.

No threads or coroutines are involved. `ParallelVisitor` runs all visitors in lock-step over one shared walker: for each node, every visitor's handler runs (each staying on the stack while its siblings run) before any child is descended into. Rules therefore share one deterministic traversal, firing per node in registration order.

Control-flow semantics invert naive expectations:

- **skipChildren is isolated per visitor:** a visitor calling `skipChildren()` is dropped from deeper nodes, but the walker still descends as long as any other visitor has not skipped. One validation rule pruning its own view does not hide nodes from sibling rules.
- **abort removes only the aborting visitor;** traversal stops entirely only when every visitor has aborted.
- **Traversal advances via mutual recursion:** a visitor's `visitChildren()` call does not visit children directly — it triggers the parent to dispatch the next sibling visitor (or, once all visitors have run for the node, to descend). `ParallelVisitor.onNode` calls `skipChildren()` on the outer Visit because it orchestrates traversal itself (a FIXME there says traversal should instead be orchestrated via Visit).
- `parallelize()` on an empty iterable returns `Visitor.noOp()`, but `ParallelVisit` itself requires a non-empty visitor list.

The golden-stack tests pin the exact interleaving — see ../testing/ast-golden-tests.md.

Related: visitor-traversal.md, ../execution/validation-rule-authoring.md.
