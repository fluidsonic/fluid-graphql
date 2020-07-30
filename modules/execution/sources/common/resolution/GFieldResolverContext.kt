package io.fluidsonic.graphql


public interface GFieldResolverContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val execution: GExecutorContext

	@SchemaBuilderKeywordB // FIXME ok?
	public val arguments: Map<String, Any?>

	@SchemaBuilderKeywordB // FIXME ok?
	public val fieldDefinition: GFieldDefinition

	@SchemaBuilderKeywordB // FIXME ok?
	public val parentType: GObjectType

	@SchemaBuilderKeywordB // FIXME
	public suspend fun next(): Any?


	@SchemaBuilderKeywordB // FIXME
	public suspend fun <Parent : Any> GFieldResolver<Parent>.resolveField(parent: Parent): Any? =
		with(this@GFieldResolverContext) { resolveField(parent) }
}
