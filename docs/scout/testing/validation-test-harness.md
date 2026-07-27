# assertValidationRule harness: dual trimming and single-rule isolation

Contracts of the validation test helpers in `modules/execution/tests/utility/Assertions.kt`; getting any of them wrong produces confusing failures.

**Dual whitespace convention in one call.** `document`/`schema` strings are processed with `trimMargin()` (they MUST use `|` line prefixes); expected `errors` strings are compared after `trimIndent()` (they MUST NOT use `|`). Mixing the styles silently changes the effective source text and shifts every line/column in the expected errors. Note `assertExecution` (in `modules/execution/tests/utility/ExecutionAssertions.kt`) passes documents verbatim — callers trim themselves.

**One rule, in isolation.** The helper runs only the rule under test on a fresh `ValidationContext` via `contextualize(context)` with `maxErrors` uncapped — never the full `Validator.default` pipeline — and it is the harness behind well over a hundred cases. Fixtures are therefore deliberately invalid under other rules (queries selecting nonexistent fields, SDL inside executable documents); do not "fix" them. Consequence: this harness alone would still pass if the whole pipeline were unreachable — the tests that close that hole are described in validator-pipeline-coverage.md. When `schema` is omitted, it defaults to `GSchema(document)` — some tests deliberately validate schema SDL that way.

**The `<document>` label lies.** Secondary locations pointing into the schema SDL render with the same `<document>:line:column` label as query locations; only the excerpt's text identifies which source a block indexes.

Related: expected-error-fixtures.md, test-packages-and-helpers.md, validator-pipeline-coverage.md.
