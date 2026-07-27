# Known deviations from GraphQL spec behavior in the executor

Places where execution diverges from the specification's prescribed behavior — plus conforming behavior that reads like a deviation; matters when embedding the executor or writing tests.

## Actual deviations

- **Subscriptions are not executed at all.** `DefaultExecutor.executeRequest` answers a subscription operation with a request error ("Subscription operations are not yet supported.") instead of a stream.
- **Non-Collection values for list fields pass through silently.** In `DefaultFieldSelectionExecutor.complete`, the `GListType` branch falls through to `GResult.success(value)` for anything that is not a `Collection` — no error, no coercion.
- **Custom scalars without coercers are identity pass-throughs** for arbitrary Kotlin values — no validation, no rejection at schema build.
- **Non-finite `Float` literals are rejected**, where graphql-js accepts them — `GFloatValue` is finite by construction, so such a literal cannot even be represented (comment on `GFloatType.coerceInputLiteral`).
- **`ID` rejects an integral `Double` outside `Long`'s range** (`1e30`), where graphql-js answers `"1e+30"`. `GIdType.fromNumber` range-checks before `toLong()`, which otherwise saturates into a *different* identifier; Kotlin spells the value `"1.0E30"`, so upstream's answer is unreachable without a JavaScript number formatter. Pinned by `coerceOutputValue_idRejectsIntegralNumbersBeyondLongRange` in `modules/language/tests/model/BuiltinScalarCoercionTests.kt`.

## Fixed in 0.19.0 — do not re-derive them from an old note

- An omitted nullable argument is now **absent** from the coerced map rather than rejected (variable-coercion.md).
- A `String` resolved for an `Int` or `Float` field is now parsed with JavaScript `Number(string)` semantics rather than passed through — see `jsNumber` in `modules/language/sources/utility/JsNumber.kt` and the matrix in `modules/execution/tests/conversion/OutputCoercionMatrixTests.kt`.

## Conforming — do NOT read these as deviations

- **Field-error null bubbling works as specified** for both channels — a `GErrorException` and a non-null field resolving to `null` both propagate through the `flatMapErrors` block at the end of `DefaultFieldSelectionExecutor.complete` (pinned by `modules/execution/tests/execution/NullPropagationTests.kt`).
- **Argument/input-value deprecation IS implemented** — do not trust the test name `testInputValueIsDeprecatedNotImplemented` (`modules/execution/tests/spec/introspection/InputValueIntrospectionTests.kt`); it is a leftover from before `WithOptionalDeprecation` was added to `GArgumentDefinition` and `__InputValue.isDeprecated`/`deprecationReason` to introspection.

Related: error-classification.md, response-shape.md.
