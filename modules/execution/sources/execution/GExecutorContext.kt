package io.fluidsonic.graphql


// FIXME add (default)outputCoercer
/**
 * Context available throughout a single GraphQL operation execution.
 *
 * Provides access to the parsed document, selected operation, schema, variable values,
 * and any per-request extensions passed via [GExecutorContextExtensionSet].
 */
public interface GExecutorContext {

	/** The parsed GraphQL document being executed. */
	public val document: GDocument

	/** Per-request extensions attached when calling [GExecutor.execute]. */
	public val extensions: GExecutorContextExtensionSet

	/** The specific operation being executed within the [document]. */
	public val operation: GOperationDefinition

	/** The root value returned by [GRootResolver] for this operation. */
	public val root: Any

	/** The object type of the root operation (Query, Mutation, or Subscription). */
	public val rootType: GObjectType

	/** The schema being executed against. */
	public val schema: GSchema

	/** Coerced variable values for the current operation. */
	public val variableValues: Map<String, Any?>


	/**
	 * Implemented by resolver and coercer contexts that are nested within an [GExecutorContext].
	 */
	public interface Child {

		/** The parent [GExecutorContext] for this execution. */
		public val execution: GExecutorContext
	}
}
