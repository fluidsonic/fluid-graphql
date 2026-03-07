package io.fluidsonic.graphql


// FIXME simplify API?
/**
 * Executes GraphQL operations against a schema.
 *
 * Use [GExecutor.default] to create an instance configured with a schema, resolvers, and coercers.
 * Call one of the [execute] overloads to run a query, mutation, or subscription document.
 * Use [serializeResult] to convert the result into a plain map suitable for JSON serialization.
 */
public interface GExecutor {

	/**
	 * Parses [documentSource] as a GraphQL document and executes it.
	 *
	 * Returns a [GResult] containing the response data map on success, or errors on failure.
	 */
	public suspend fun execute(
		documentSource: String,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		extensions: GExecutorContextExtensionSet = GExecutorContextExtensionSet.empty(),
	): GResult<Map<String, Any?>> =
		execute(
			documentSource = GDocumentSource.of(documentSource),
			extensions = extensions,
			operationName = operationName,
			variableValues = variableValues
		)


	/**
	 * Parses [documentSource] as a GraphQL document and executes it.
	 *
	 * Returns a [GResult] containing the response data map on success, or errors on failure.
	 */
	public suspend fun execute(
		documentSource: GDocumentSource.Parsable,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		extensions: GExecutorContextExtensionSet = GExecutorContextExtensionSet.empty(),
	): GResult<Map<String, Any?>> =
		GDocument.parse(documentSource).flatMapValue { document ->
			execute(
				document = document,
				extensions = extensions,
				operationName = operationName,
				variableValues = variableValues
			)
		}


	/**
	 * Validates and executes the given [document].
	 *
	 * Returns a [GResult] containing the response data map on success, or errors on failure.
	 */
	public suspend fun execute(
		document: GDocument,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		extensions: GExecutorContextExtensionSet = GExecutorContextExtensionSet.empty(),
	): GResult<Map<String, Any?>>


	/**
	 * Converts an execution result into a plain `Map<String, Any?>` following the GraphQL response format.
	 *
	 * The returned map always contains a `"data"` key on success, and an `"errors"` key when there are errors.
	 */
	public fun serializeResult(result: GResult<Map<String, Any?>>): Map<String, Any?>


	public companion object {

		/**
		 * Creates a [GExecutor] with the given [schema] and optional customization.
		 *
		 * @param schema The GraphQL schema to execute against.
		 * @param exceptionHandler Handles exceptions thrown by resolvers and coercers. Defaults to propagating the exception.
		 * @param fieldResolver Fallback resolver used when a field has no resolver attached directly to its definition.
		 * @param nodeInputCoercer Fallback coercer for inline input values (AST nodes) not handled by type-specific coercers.
		 * @param outputCoercer Fallback coercer for output values not handled by type-specific coercers.
		 * @param variableInputCoercer Fallback coercer for variable input values not handled by type-specific coercers.
		 * @param rootResolver Provides the root value passed to top-level field resolvers. Defaults to [GRootResolver.unit].
		 */
		public fun default(
			schema: GSchema,
			exceptionHandler: GExceptionHandler? = null,
			fieldResolver: GFieldResolver<Any>? = null,
			nodeInputCoercer: GNodeInputCoercer<Any?>? = null,
			outputCoercer: GOutputCoercer<Any>? = null,
			variableInputCoercer: GVariableInputCoercer<Any?>? = null,
			rootResolver: GRootResolver = GRootResolver.unit(),
		): GExecutor =
			DefaultExecutor(
				exceptionHandler = exceptionHandler,
				fieldResolver = fieldResolver,
				nodeInputCoercer = nodeInputCoercer,
				outputCoercer = outputCoercer,
				schema = schema,
				rootResolver = rootResolver,
				variableInputCoercer = variableInputCoercer
			)
	}
}
