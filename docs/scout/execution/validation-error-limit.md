# Validation stops after 100 errors, and the limit is not reachable from the public API

How `maxErrors` bounds validation and what its result looks like; matters when a caller sees a suspiciously round error count or an error with no location.

`Validator.validate` (`modules/execution/sources/validation/Validator.kt`) takes `maxErrors`, defaulting to `Validator.defaultMaxErrors` = 100, matching graphql-js. The public surface does **not**: `GDocument.validate(schema)` (`modules/execution/sources/extensions/GDocument.execution.kt`) and both validating `execute` overloads always use the default, so 100 is a hard cap for every request. Raising it means calling `Validator` directly, which is `internal`.

Two consequences that surprise readers:

- **A capped result has `maxErrors + 1` entries**, not `maxErrors`. `ValidationContext.collectingErrors` appends a terminal `GError` ("Too many validation errors, error limit reached. Validation aborted.") that carries no nodes and therefore no location block. With `maxErrors = 0` that terminal error is the *only* one reported.
- **It is a stop, not a truncation.** `ValidationContext.reportError` throws a private exception once the limit is reached, unwinding the shared traversal, so rules stop being invoked. `ValidatorErrorLimitTest` asserts the invocation count precisely to distinguish this from an `errors.take(maxErrors)` implementation. Deliberately not a `GErrorException`, since rules and `GSchema.validateValue` raise that for real failures.

Errors come out in **document order**, interleaved across rules, because all 29 rules share one traversal — so which errors survive the cap depends on position in the document, not on rule registration order (`ValidatorCorpusTest.testErrorsAreReportedInDocumentOrderRatherThanGroupedByRule`).

Related: validation-entry-points.md, validation-rule-authoring.md, ../language/parallel-visitor.md.
