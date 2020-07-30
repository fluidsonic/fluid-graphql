package io.fluidsonic.graphql


public fun interface GExceptionHandler {

	public fun GExceptionHandlerContext.handleException(exception: Throwable): GError
}
