package io.fluidsonic.graphql


// FIXME add (default)outputCoercer
/**
 * Context available throughout a single GraphQL operation execution.
 *
 * Provides access to the parsed document, selected operation, schema, variable values,
 * and any per-request extensions passed via [GExecutorContextExtensionSet].
 */
public interface GExecutorContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val document: GDocument

	/** Per-request extensions attached when calling [GExecutor.execute]. */
	@SchemaBuilderKeywordB
	public val extensions: GExecutorContextExtensionSet

	@SchemaBuilderKeywordB // FIXME ok?
	public val operation: GOperationDefinition

	/** The root value returned by [GRootResolver] for this operation. */
	@SchemaBuilderKeywordB // FIXME ok?
	public val root: Any

	@SchemaBuilderKeywordB // FIXME ok?
	public val rootType: GObjectType

	@SchemaBuilderKeywordB // FIXME ok?
	public val schema: GSchema

	@SchemaBuilderKeywordB // FIXME ok?
	public val variableValues: Map<String, Any?>


	/**
	 * Implemented by resolver and coercer contexts that are nested within an [GExecutorContext].
	 */
	public interface Child {

		@SchemaBuilderKeywordB // FIXME ok?
		public val execution: GExecutorContext
	}
}
