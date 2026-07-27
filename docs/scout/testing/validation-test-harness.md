# assertValidationRule harness: dual trimming, single-rule isolation, paste-back workflow

Contracts of the validation test helpers in `modules/execution/tests/utility/Assertions.kt`; getting any of them wrong produces confusing failures.

**Dual whitespace convention in one call.** `document`/`schema` strings are processed with `trimMargin()` (they MUST use `|` line prefixes); expected `errors` strings are compared after `trimIndent()` (they MUST NOT use `|`). Mixing the styles silently changes the effective source text and shifts every line/column in the expected errors. Note `assertExecution` (in `modules/execution/tests/utility/ExecutionAssertions.kt`) passes documents verbatim — callers trim themselves.

**One rule, in isolation.** The helper runs only the rule under test on a fresh `ValidationContext` via `contextualize(context)` with `maxErrors` uncapped — never the full `Validator.default` pipeline — and it is the harness behind roughly 250 test cases. Fixtures are therefore deliberately invalid under other rules (queries selecting nonexistent fields, SDL inside executable documents); do not "fix" them. Consequence: this harness alone would still pass if the whole pipeline were unreachable — the tests that close that hole are described in validator-pipeline-coverage.md. When `schema` is omitted, it defaults to `GSchema(document)` — some tests deliberately validate schema SDL that way.

**Expected errors are copy-pasted, not hand-written.** Assertions compare full `GError.describe()` renderings (message, `<origin>:line:column` blocks, source excerpt, caret marker). Only when the error *count* differs does `assertErrors` print actual errors as Kotlin raw strings via `toKotlinRawString("\t\t\t\t")` — a comment states this exists so they can be copied into test code; a same-count content mismatch yields a plain string diff (the raw-string calls in `assertError` are commented out). Workflow: run with an empty `errors` list, paste the raw-string output.

**The `<document>` label lies.** Secondary locations pointing into the schema SDL render with the same `<document>:line:column` label as query locations; only the excerpt's text identifies which source a block indexes.

Related: test-packages-and-helpers.md, validator-pipeline-coverage.md.
