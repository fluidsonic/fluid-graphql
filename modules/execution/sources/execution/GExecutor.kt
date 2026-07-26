package io.fluidsonic.graphql

/**
 * Executes GraphQL operations against a schema.
 *
 * Use [GExecutor.default] to create an instance configured with a schema, resolvers, and coercers.
 * Call one of the [execute] overloads to run a query, mutation, or subscription document.
 * Use [serializeResult] to convert the result into a plain map suitable for JSON serialization.
 *
 * The overloads differ in whether they validate:
 *
 * - The two `documentSource` overloads parse, **validate** and then execute — the complete request
 *   pipeline, equivalent to `graphql()` in `graphql-js`. Use these unless you have a reason not to.
 * - [execute] taking a parsed [GDocument] **skips validation** entirely, equivalent to `execute()` in
 *   `graphql-js`. It assumes the document is already known to be valid.
 *
 * Only failures a client can actually cause — request errors and field errors — are reported in the
 * returned [GResult]. A failure that means the schema is broken or that an API was misused, such as a
 * field without a resolver or a declared type the schema does not define, throws instead: it needs to
 * fail loudly during development rather than answer a client with an internal detail.
 *
 * @see <a href="https://spec.graphql.org/draft/#sec-Executing-Requests">GraphQL specification: Executing Requests</a>
 */
public interface GExecutor {

	/**
	 * Parses [documentSource] as a GraphQL document, validates it against the schema and executes it.
	 *
	 * Returns a [GResult] containing the response data map on success, or errors on failure.
	 *
	 * The document is not executed if parsing or validation fails. All errors reported in that case are
	 * request errors (see [GError.isRequestError]), so [serializeResult] omits the `"data"` key entirely.
	 *
	 * @see <a href="https://spec.graphql.org/draft/#sec-Validation">GraphQL specification: Validation</a>
	 */
	public suspend fun execute(
		documentSource: String,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		extensions: GExecutorContextExtensionSet = GExecutorContextExtensionSet.empty(),
	): GResult<Map<String, Any?>>

	/**
	 * Parses [documentSource] as a GraphQL document, validates it against the schema and executes it.
	 *
	 * Returns a [GResult] containing the response data map on success, or errors on failure.
	 *
	 * The document is not executed if parsing or validation fails. All errors reported in that case are
	 * request errors (see [GError.isRequestError]), so [serializeResult] omits the `"data"` key entirely.
	 *
	 * @see <a href="https://spec.graphql.org/draft/#sec-Validation">GraphQL specification: Validation</a>
	 */
	public suspend fun execute(
		documentSource: GDocumentSource.Parsable,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		extensions: GExecutorContextExtensionSet = GExecutorContextExtensionSet.empty(),
	): GResult<Map<String, Any?>>

	/**
	 * Executes the already-parsed [document] **without validating it**.
	 *
	 * Returns a [GResult] containing the response data map on success, or errors on failure.
	 *
	 * This mirrors `execute()` in `graphql-js`: the caller guarantees that [document] is valid for the
	 * schema, typically because it was validated earlier (see [GDocument.validate]). Executing an
	 * invalid document is not an error — selections the schema does not define are simply skipped, so
	 * their response keys are absent.
	 *
	 * Use one of the `documentSource` overloads to validate before executing.
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
	 * The returned map contains an `"errors"` key whenever [result] carries errors.
	 *
	 * The `"data"` key is omitted entirely if any of those errors is a request error
	 * (see [GError.isRequestError]). Otherwise the key is always present — set to the response data on
	 * success, or to `null` if a field error propagated all the way to the root.
	 *
	 * @see <a href="https://spec.graphql.org/draft/#sec-Response">GraphQL specification: Response</a>
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
		): GExecutor = DefaultExecutor(
			exceptionHandler = exceptionHandler,
			fieldResolver = fieldResolver,
			nodeInputCoercer = nodeInputCoercer,
			outputCoercer = outputCoercer,
			schema = schema,
			rootResolver = rootResolver,
			variableInputCoercer = variableInputCoercer,
		)
	}
}
