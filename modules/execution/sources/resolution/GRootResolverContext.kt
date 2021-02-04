package io.fluidsonic.graphql


public interface GRootResolverContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val execution: GExecutorContext
}
