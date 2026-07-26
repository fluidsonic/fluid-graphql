# Throw or report? The origin of the bad input decides, not the severity

The rule every execution-path failure must follow; get it wrong and either a client sees internal detail or a wiring bug answers HTTP 200.

What settles it is **where the offending input came from**, not how serious the failure looks:

- from the **schema** (a declared type that does not resolve, a field typed with an input type, a field with no resolver) or from a **caller of an internal API** (an empty selection list, `unwrap()` on a variable reference) → fail loudly with Kotlin `error()`/`require()`, so it escapes `execute(...)` as `IllegalStateException`/`IllegalArgumentException`;
- from the **document or the variable values** → a `GError` in the result, request error or field error per the specification.

The `validationError` helpers in `NodeInputConverter` and `VariableInputConverter` are the funnel for the first group; `GError(...).throwException()` for the second. Escape is prevented by catch boundaries that catch `GErrorException` **only** — in `DefaultExecutor.execute`, `DefaultFieldSelectionExecutor.execute` and both `DefaultSelectionSetExecutor` entry points — so a Kotlin `error()` travels straight out while a raised `GError` always lands in the result.

Two pairs look nearly identical in the code and sit on opposite sides: an **argument** definition whose declared type cannot be resolved is schema-derived and throws, while a **variable** definition in the same situation is document-derived and becomes a request error; and "the schema names a type that does not exist" throws, while "this runtime object matches none of the possible types" is a field error because it depends on what a resolver returned.

`modules/execution/tests/execution/ErrorClassificationTests.kt` states the rule in its header comment and pins both groups.

Related: error-channels.md, validation-entry-points.md.
