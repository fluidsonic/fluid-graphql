# Cancellation is rethrown ahead of every broad `Throwable` catch

A repository-wide rule for any `catch` wide enough to swallow `CancellationException`; both current broad catches wrap caller-supplied code (a field resolver, a scalar coercion).

`DefaultExecutorContext.withExceptionHandler` (`modules/execution/sources/execution/DefaultExecutorContext.kt`) and `GSchema.validateValue`'s `reportRejectedScalarLiteral` (`modules/language/sources/model/GSchema.kt`) each take a `CancellationException` arm *before* their `Throwable` arm and rethrow it unchanged. A new broad catch around caller code must do the same.

The executor site is the one that bites. Field resolvers are `suspend`, so a cancelled request unwinds through them as `CancellationException`; without the guard `withExceptionHandler` hands it to the configured `GExceptionHandler`, which turns cancellation into an ordinary GraphQL error and nulls the field while the caller's coroutine believes it is still running. That was a live bug, not a hypothetical — `ExceptionHandlerTests.testCancellationIsNotHandled` (`modules/execution/tests/execution/ExceptionHandlerTests.kt`) was observed failing that way before the arm was added, and pins it now. Nothing in `GSchema` suspends, but its site carries the same arm.

detekt's `TooGenericExceptionCaught` flags these catches — `@Suppress` at the `GSchema` site, baseline entries in `modules/language/detekt-baseline.xml` and `modules/execution/detekt-baseline.xml`. Treat that flag on a new catch as the cue to add the cancellation arm rather than only to silence the rule.

Note the imports differ by module: `modules/language` has no coroutines dependency and uses `kotlin.coroutines.cancellation.CancellationException`.

Related: error-channels.md (what the handler channel does with everything else).
