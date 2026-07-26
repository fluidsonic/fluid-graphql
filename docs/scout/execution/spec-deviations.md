# Known deviations from GraphQL spec behavior in the executor

Places where execution diverges from the specification's prescribed behavior — plus conforming behavior that reads like a deviation; matters when embedding the executor or writing tests.

## Actual deviations

- **Subscriptions are not executed at all.** `DefaultExecutor.executeRequest` answers a subscription operation with a request error ("Subscription operations are not yet supported.") instead of a stream.
- **Non-Collection values for list fields pass through silently.** In `DefaultFieldSelectionExecutor.complete`, the `GListType` branch falls through to `GResult.success(value)` for anything that is not a `Collection` — no error, no coercion.
- **String output for an Int field passes through.** `OutputConverter.coerceLeafValue`'s `GIntType` branch returns a `String` value unchanged, while the variable-input side rejects String for Int. No evidence documents whether this is intentional; treat it as observable behavior, not contract.
- **Custom scalars without coercers are identity pass-throughs** for arbitrary Kotlin values — no validation, no rejection at schema build.
- **Literal arguments that are simply omitted are rejected** even when nullable — see omitted-nullable-arguments.md.

## Conforming — do NOT read these as deviations

- **Field-error null bubbling works as specified** for both channels — a `GErrorException` and a non-null field resolving to `null` both propagate through the `flatMapErrors` block at the end of `DefaultFieldSelectionExecutor.complete` (pinned by `modules/execution/tests/execution/NullPropagationTests.kt`).
- **Argument/input-value deprecation IS implemented** — do not trust the test name `testInputValueIsDeprecatedNotImplemented` (`modules/execution/tests/spec/introspection/InputValueIntrospectionTests.kt`); it is a leftover from before `WithOptionalDeprecation` was added to `GArgumentDefinition` and `__InputValue.isDeprecated`/`deprecationReason` to introspection.

Related: error-classification.md, response-shape.md.
