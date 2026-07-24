# Known deviations from GraphQL spec behavior in the executor

Places where execution diverges from the specification's prescribed behavior; matters when embedding the executor or writing tests.

- **Null on a non-null field crashes execute().** `DefaultFieldSelectionExecutor.complete` (`modules/execution/sources/execution/DefaultFieldSelectionExecutor.kt`) calls Kotlin `error()` for a null resolved on a `GNonNullType` field, outside any catch wrapper — the whole `execute()` call throws `IllegalStateException` instead of producing a field error with null-bubbling. Pinned by `testNonNullFieldReturningNull_producesError` in `modules/execution/tests/conversion/OutputCoercionTests.kt` (uses `assertFailsWith`, not the execution helpers).
- **Unresolvable abstract types also `error()`.** `resolveAbstractType` throws when no possible type matches the value (see resolver-wiring.md).
- **Non-Collection values for list fields pass through silently** — no error, no coercion.
- **String output for an Int field passes through.** `OutputConverter.coerceLeafValue`'s GIntType branch returns a String value unchanged, while the variable-input side rejects String for Int. No evidence documents whether this is intentional; treat it as observable behavior, not contract.
- **Custom scalars without coercers are identity pass-throughs** for arbitrary Kotlin values — no validation, no rejection at schema build.
- **Argument/input-value deprecation IS implemented** — do not trust the test name `testInputValueIsDeprecatedNotImplemented` (`modules/execution/tests/spec/introspection/InputValueIntrospectionTests.kt`); it is a leftover from before the "Implement full GraphQL spec compliance" commit added `WithOptionalDeprecation` to `GArgumentDefinition` and `__InputValue.isDeprecated`/`deprecationReason` to introspection. Residual deviation: the built-in `defaultDeprecatedDirective` (`modules/language/sources/GLanguage.kt`) declares only `ENUM_VALUE` and `FIELD_DEFINITION` locations, not the spec's `ARGUMENT_DEFINITION`/`INPUT_FIELD_DEFINITION`.

Spec null-bubbling does work for the `GErrorException` channel via the `flatMapErrors` block at the end of `complete`.
