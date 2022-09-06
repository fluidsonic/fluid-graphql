package io.fluidsonic.graphql


// FIXME simplify API?
public interface GExecutor {

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


	public suspend fun execute(
		document: GDocument,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		extensions: GExecutorContextExtensionSet = GExecutorContextExtensionSet.empty(),
	): GResult<Map<String, Any?>>


	public fun serializeResult(result: GResult<Map<String, Any?>>): Map<String, Any?>


	public companion object {

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
