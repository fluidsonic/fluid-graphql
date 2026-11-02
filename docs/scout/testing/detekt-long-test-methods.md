# detekt LongMethod also applies to test functions

Test functions are not exempt from detekt's length rule, so a validation test that pins many multi-line expected errors can fail `./gradlew check` even when every assertion is correct.

`config/detekt/detekt.yml` excludes `**/tests/**` only for `TooManyFunctions` and `MagicNumber`. `LongMethod` is not overridden, so the default threshold of 60 lines applies to `@Test` functions too. Each `assertValidationRule` expected-error block runs about eight lines for a two-line document, so roughly six or seven error blocks push one test past the limit. The spec validation tests under `modules/execution/tests/spec/validation/` are the usual case.

The failure is reported as `LongMethod` on the test name. Two ways to clear it: split the test into several `@Test` functions along its cases, or trim the expectations. The module baselines (`modules/execution/detekt-baseline.xml` and `modules/dsl/detekt-baseline.xml`) already hold many `LongMethod` entries for older test methods, so a baseline entry is the existing alternative, but new tests should be split rather than added there.

Related: expected-error-fixtures.md, validation-test-harness.md.
