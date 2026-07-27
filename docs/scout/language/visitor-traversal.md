# Visitor traversal semantics: last-node result, auto-descend, then(), contextualize()

Non-obvious contracts of the visitor framework in `modules/language/sources/visitors/`; matters when writing any visitor (validation rules included).

**`visit()` returns the LAST visited node's result, not the root's.** `DefaultVisit.dispatchVisit` (`DefaultVisitCoordinator.kt`) overwrites `result` for every traversed node. A visitor wanting the root's result must prevent descent — `visit.abort()` or `visit.skipChildren()`; `ChainingVisitor.onNode` uses `abort()` with the comment that only the root node is of interest.

**Children are visited automatically** when a handler returns without calling `visitChildren()` or `skipChildren()`. A second `visitChildren()` on the same node throws; after `skipChildren()` it is a silent no-op.

**Code after `visitChildren()` is NOT a portable post-order idiom.** `DefaultVisit` traverses the subtree inline, so trailing code runs afterwards — but `ParallelVisit` (parallel-visitor.md) returns immediately, running the same code *before* the subtree. Use `visit.afterChildren { … }` instead, as `AllVariablesUsedRule` and `FragmentDefinitionUsageRule` do. Such a block is silently dropped if that visitor skipped the node's children or aborted, or if an exception unwinds the traversal.

**`Visitor.then(next)` is not two tree passes.** `ChainingVisitor` calls the first visitor's `onNode` once on the root, feeds that single result as the `data` for a fresh full traversal by the next visitor, then aborts. It is "compute context at the root, then traverse with it".

**`VisitorContext` only updates under `contextualize()` or `parallelizeContextualized()`.** The `related*` properties are advanced only by `ContextProvidingVisitor` (via `VisitorContext.with`) or `ParallelVisit`'s per-node `enter`/`leave`; a raw `Visitor<R, VisitorContext>` sees a context that never changes, with no failure signal. When the wrapped visitor did not descend itself, `ContextProvidingVisitor` calls `visitChildren()` inside the `with(node)` frame so children see the pushed context — the framework's automatic descend would only run after the context was popped.

Related: parallel-visitor.md, visitor-api-surface.md.
