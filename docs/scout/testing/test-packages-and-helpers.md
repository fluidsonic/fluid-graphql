# Test package and style conventions the code does not announce

Where new tests must live and which local styles to imitate; matters when adding any test.

**Tests use `package testing`, not the production package.** Every test and test utility in `modules/language/tests` (and the spec suites under `modules/execution/tests/spec/`) declares `package testing` with `import io.fluidsonic.graphql.*`. Note that Kotlin visibility is not package-scoped, so this does not hide internals — but as practiced, tests use only the public API (none opts into `@InternalGraphqlApi`). Critically, shared helpers (`assertAst`, `AstBuilder`, kitchen-sink fixtures, `StackCollectingVisitor`) are declared in the `testing` package, so a test placed in the mirrored production package will not resolve them without explicit imports.

**The spec suites for execution/introspection/response bypass the documented helpers.** CLAUDE.md advertises `assertExecution` from `modules/execution/tests/utility/`, but the test classes under `tests/spec/execution`, `tests/spec/introspection`, and `tests/spec/response` build schemas inline with `GraphQL.schema {}`, run `GExecutor.default(schema).execute(...)` inside `kotlinx.coroutines.test.runTest`, and assert on the `serializeResult` Map directly (which must include the top-level `data` key, and omits `errors` on success). Follow the local file's style, not CLAUDE.md.

**`*Test` versus `*Tests` in the visitors directory follow two distinct styles:** singular classes (`VisitorTest`, `ParallelVisitorTest`, ...) assert exact golden traversal order; plural classes (`ParallelVisitorTests`, `ContextProvidingVisitorTests`) assert individual behaviors order-insensitively. Put new exhaustive-order coverage in `*Test`, behavioral cases in `*Tests`.

Related: validation-test-harness.md, ast-golden-tests.md.
