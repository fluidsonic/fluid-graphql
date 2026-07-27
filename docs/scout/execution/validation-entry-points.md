# Only the documentSource execute() overloads validate — the GDocument one skips silently

Which entry point validates, and what an unvalidated document does instead of erroring; matters for every comparison of executor behaviour and for reading test fixtures.

`modules/execution/sources/execution/GExecutor.kt` declares three `execute` overloads with deliberately different contracts, implemented in `modules/execution/sources/execution/DefaultExecutor.kt`:

- the two `documentSource` overloads parse, then `document.validate(schema)`, and abort without executing if that reports anything (mirrors `graphql()` in graphql-js),
- `execute(document: GDocument)` never validates (mirrors `execute()` in graphql-js).

So the *same* document answers differently depending on the overload — the `documentSource` path returns request errors and `serializeResult` omits the `"data"` key entirely, while the `GDocument` path returns `{"data": {}}`. Pinned by `ValidationContractTests`; a comparison that mixes the two produces contradictory conclusions.

An unvalidated document does not produce errors for what validation would have caught: an unknown field, a spread of an undefined fragment, and an unresolvable type condition are all **skipped**, leaving the response key absent rather than present-and-null. Two different mechanisms do it: an unknown field selection yields the shared `NoValue` sentinel (`modules/execution/sources/utility/NoValue.kt`) from `DefaultFieldSelectionExecutor.executeField`, which `isAbsent` filters out of the result map, while the fragment cases return early during `collectFieldSelections` — both in `DefaultSelectionSetExecutor`. Pinned by `modules/execution/tests/execution/SkippedSelectionTests.kt` and `ErrorTests` (`testInvalidFieldInNonValidatedQueryIsSkipped`).

`GDocument.validate(schema)` (`modules/execution/sources/extensions/GDocument.execution.kt`) flags every error it returns as `isRequestError`, and caps the result at 101 errors with no way to change that — see validation-error-limit.md.

Related: error-classification.md, error-channels.md, validation-error-limit.md, ../spec/graphql-js-parity.md.
