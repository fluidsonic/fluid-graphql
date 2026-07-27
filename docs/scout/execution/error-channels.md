# Error channels: GErrorException is the only in-band path; exceptionHandler cannot recover values

How errors flow through execution; matters for resolvers, coercers, and anyone configuring `GExecutor.default`.

`GResult.catchErrors` (`modules/language/sources/model/GResult.kt`) catches ONLY `GErrorException`. The resolver idiom for a GraphQL field error is `GError(message = ...).throwException()` — it lands in the response `errors` list with path/location and **bypasses the exceptionHandler entirely** (tests in `modules/execution/tests/execution/ExceptionHandlerTests.kt` assert the handler "Shouldn't be called"). Plain thrown exceptions go the other way: with no `exceptionHandler` configured, `DefaultExecutorContext.withExceptionHandler` rethrows them unchanged, so they escape `execute()` instead of appearing in `errors`. A configured handler must return a `GError`, immediately rethrown as `GErrorException` — one error per exception, no swallowing, no substituting a value. `DefaultExecutor` carries `// FIXME exception handling`.

**Validation and execution deliberately disagree about a non-`GErrorException` throwable, and must not be "tidied" into consistency.** A scalar coercion is arbitrary code that either path may invoke. `GSchema.validateValue`'s `reportRejectedScalarLiteral` wraps such a throwable into a `GError` — its contract is to *return* the problems found, and a code comment says so. Execution must not: wrapping would silently take the throwable away from the consumer's `GExceptionHandler`.

One case never reaches the handler at all: `CancellationException` is rethrown ahead of the `Throwable` arm — see cancellation-rethrow.md.

Path stamping is asymmetric: for `GErrorException` errors `withExceptionHandler` copies `GExceptionOrigin.path` only for `FieldResolver`/`OutputValueCoercer` origins, only when non-empty and the error has no path — `InputLiteralCoercer`/`InputValueCoercer`/`RootResolver` errors keep whatever path they carry. Handler-produced errors are stamped for any origin kind.

Which of the two response shapes an error produces is not decided by the raiser — see response-shape.md.

Related: cancellation-rethrow.md, error-classification.md, response-shape.md, spec-deviations.md.
