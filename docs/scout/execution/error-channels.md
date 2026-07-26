# Error channels: GErrorException is the only in-band path; exceptionHandler cannot recover values

How errors flow through execution; matters for resolvers, coercers, and anyone configuring `GExecutor.default`.

`GResult.catchErrors` (`modules/language/sources/model/GResult.kt`) catches ONLY `GErrorException`. The resolver idiom for a GraphQL field error is `GError(message = ...).throwException()` — this lands in the response `errors` list with path/location and **bypasses the exceptionHandler entirely** (tests in `modules/execution/tests/execution/ExceptionHandlerTests.kt` assert the handler "Shouldn't be called"). Plain thrown exceptions go the other way: with no `exceptionHandler` configured, `DefaultExecutorContext.withExceptionHandler` rethrows them unchanged, so they escape `execute()` as raw exceptions instead of appearing in `errors`. A configured handler must return a `GError`, which is immediately rethrown as `GErrorException` — one error per exception, no swallowing, no substituting a value. `DefaultExecutor` carries `// FIXME exception handling`.

Path stamping is asymmetric: for `GErrorException` errors, `withExceptionHandler` copies the origin path only for FieldResolver/OutputCoercer origins, only when the origin path is non-empty and the error has no path yet — NodeInputCoercer/VariableInputCoercer/RootResolver errors keep whatever path they carry. Handler-produced errors are different: the origin path is stamped for any origin kind whenever the error has no path.

Which of the two response shapes an error produces is not decided by the raiser — see response-shape.md.

Related: error-classification.md, response-shape.md, spec-deviations.md.
