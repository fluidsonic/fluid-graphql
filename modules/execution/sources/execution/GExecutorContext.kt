package io.fluidsonic.graphql


// FIXME add (default)outputCoercer
public interface GExecutorContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val document: GDocument

	@SchemaBuilderKeywordB
	public val extensions: GExecutorContextExtensionSet

	@SchemaBuilderKeywordB // FIXME ok?
	public val operation: GOperationDefinition

	@SchemaBuilderKeywordB // FIXME ok?
	public val root: Any

	@SchemaBuilderKeywordB // FIXME ok?
	public val rootType: GObjectType

	@SchemaBuilderKeywordB // FIXME ok?
	public val schema: GSchema

	@SchemaBuilderKeywordB // FIXME ok?
	public val variableValues: Map<String, Any?>


	public interface Child {

		@SchemaBuilderKeywordB // FIXME ok?
		public val execution: GExecutorContext
	}
}
