# Conventions for writing validation rules

Unstated contracts every rule in `modules/execution/sources/validation/rules/` follows; matters when adding or modifying rules.

**Stateless rules are Singletons; stateful rules need Factory.** `object X : ValidationRule.Singleton()` reuses one instance process-wide (`Validator.default` is static). Rules with mutable fields are declared `class X : ValidationRule()` with `companion object : Factory(::X)` so `Validator.validate`'s `provide()` call constructs a fresh instance per run. Nothing enforces this: a Singleton with mutable state compiles fine and leaks state across (and between concurrent) validations. `Singleton.provide()` is hidden via `@Deprecated(HIDDEN)` — not dead code.

**All rules share ONE traversal** via `parallelizeContextualized()` — see ../language/parallel-visitor.md. `skipChildren()` prunes only the calling rule's view. `reportError()` does **not** always return: `ValidationContext.reportError` throws a private exception once `maxErrors` errors exist, unwinding the whole traversal (validation-error-limit.md), so a rule must not rely on code after it running. `ValidationContext` injects the execution module's `GSchema.fieldDefinition` extension so introspection meta-fields resolve; without it, `__typename` selections would report as nonexistent.

**Error ownership is partitioned.** Every rule early-returns (`?: return // Cannot validate ...`) when a `related*` lookup is null; the corresponding `*ExistenceRule` solely owns that error, giving one error per root cause instead of cascades. A rule reporting on null related context breaks this.

**Granularity convention:** uniqueness/exclusivity violations produce a single GError listing every occurrence (including the first) as location blocks; independent violations each produce their own GError.

**Post-subtree work goes in `visit.afterChildren { … }`,** never in code after `visit.visitChildren()` — under the shared traversal that call returns before the subtree is walked (../language/visitor-traversal.md). A rule registered in `Validator.default` also needs a fixture in `ValidatorRuleCoverageTest` or the build fails (../testing/validator-pipeline-coverage.md).

Related: validation-rule-names.md, validation-rule-quirks.md, selection-unambiguity-rule.md.
