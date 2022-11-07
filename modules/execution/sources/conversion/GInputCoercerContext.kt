package io.fluidsonic.graphql


public interface GInputCoercerContext : GExecutorContext.Child {

	@SchemaBuilderKeywordB // FIXME ok?
	public val argumentDefinition: GArgumentDefinition?

	@SchemaBuilderKeywordB // FIXME ok?
	public val type: GType // FIXME make all generic?

	@SchemaBuilderKeywordB // FIXME ok?
	public fun invalid(details: String? = null): Nothing

	@SchemaBuilderKeywordB // FIXME
	public fun next(): Any?
}
