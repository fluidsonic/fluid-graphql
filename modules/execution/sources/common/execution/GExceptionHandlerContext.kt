package io.fluidsonic.graphql


public interface GExceptionHandlerContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val execution: GExecutorContext

	@SchemaBuilderKeywordB
	public val origin: GExceptionOrigin
}
