# GExecutor.execute() never validates, despite its KDoc (and CLAUDE.md) saying it does

The single most misleading documented behavior in the library; matters whenever executing documents or debugging "validation" errors.

`GExecutor.execute` KDoc says "Validates and executes the given document", and CLAUDE.md describes the executor as "Parses, validates, and executes" — but `DefaultExecutor` (`modules/execution/sources/execution/DefaultExecutor.kt`) contains no validation call at all: the flow is `getOperation` → `makeContext` → `executeOperation`. Internal comments in `DefaultFieldSelectionExecutor` treat unvalidated input as a caller error.

Consequences:

- Callers wanting spec validation must run it separately before `execute()`. The whole-pipeline entry point is `GDocument.validate(schema)`, returning `List<GError>` (empty on success); internally this is `Validator.validate` over `Validator.default`.
- Invalid documents that reach `execute()` fail at execution time: an unknown field yields a serialized response with `data: null` plus a runtime field error (pinned by `testErrorForInvalidFieldInNonValidatedQuery` in `modules/execution/tests/execution/ErrorTests.kt`), not a validation error list.
- Deeper invalidity surfaces as thrown `IllegalStateException` from conversion code, whose messages start with "There is an error in the document. It should be validated before use:" — exceptions, not GraphQL errors.

Related: error-channels.md, spec-deviations.md.
