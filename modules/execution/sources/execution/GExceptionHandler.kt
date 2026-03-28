package io.fluidsonic.graphql


/**
 * Handles exceptions thrown during GraphQL execution (resolvers, coercers).
 *
 * Implement this interface to translate exceptions into [GError] values that are included in
 * the GraphQL response. The [GExceptionHandlerContext] provides the [GExceptionOrigin] indicating
 * where the exception was thrown.
 *
 * Provide a handler via [GExecutor.default].
 */
public fun interface GExceptionHandler {

	/** Handles [exception] and returns a [GError] to include in the GraphQL response. */
	public fun GExceptionHandlerContext.handleException(exception: Throwable): GError
}
