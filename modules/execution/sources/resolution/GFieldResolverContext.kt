package io.fluidsonic.graphql


public interface GFieldResolverContext : GExecutorContext.Child {

	@SchemaBuilderKeywordB // FIXME ok?
	public val arguments: Map<String, Any?>

	@SchemaBuilderKeywordB // FIXME ok?
	public val fieldDefinition: GFieldDefinition

	@SchemaBuilderKeywordB
	public val path: GPath

	@SchemaBuilderKeywordB // FIXME ok?
	public val parentType: GObjectType

	@SchemaBuilderKeywordB // FIXME
	public suspend fun next(): Any?
}
