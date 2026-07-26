# isRequestError is stamped by phase and decides whether "data" exists at all

What `serializeResult` emits and why; matters for every assertion on a serialized response and for anyone adding an error-raising site.

`GError.isRequestError` (`modules/language/sources/model/GError.kt`) defaults to `false` — a field error, the specification's default case. It is **not** set by whoever raises the error but stamped by the phase in which the failure happened: everything that fails before execution begins is copied to `isRequestError = true` on the way out, even if the raiser built a plain `GError`. The stamping sites are `GError.syntax` (parsing), `ValidationContext.reportError`, `VariableInputConverter.convertValues`, `DefaultExecutor.resolveRoot`, plus the inline operation-selection and unsupported-operation failures in `DefaultExecutor`.

`GExecutor.serializeResult` then chooses between two shapes: if **any** error is a request error the `"data"` key is omitted entirely; otherwise the key is always present — the data on success, or `null` when a field error propagated to the root. The `"errors"` key is present only when there are errors, so a successful response is a one-key map (many tests do full-map equality on that shape).

Consequence for tests: `assertNull(result["data"])` passes for both shapes and therefore pins nothing. Assert on key presence instead, as `ResponseFormatTests`, `RequestErrorResponseShapeTests` and `ExecutingRequestsTests` do.

Related: error-channels.md, error-classification.md, validation-entry-points.md.
