package io.fluidsonic.graphql


/**
 * Context provided to a [GExceptionHandler] when an exception occurs during execution.
 */
public interface GExceptionHandlerContext {

	/** Where in the execution pipeline the exception was thrown. */
	@SchemaBuilderKeywordB // FIXME
	public val origin: GExceptionOrigin
}
