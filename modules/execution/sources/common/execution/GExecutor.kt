package io.fluidsonic.graphql


public interface GExecutor {

	public val defaultFieldResolver: GFieldResolver<Any>?
	public val schema: GSchema
	public val rootResolver: GRootResolver


	public suspend fun execute(
		documentSource: String,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap()
	): GResult<Any?> =
		execute(
			documentSource = GDocumentSource.of(documentSource),
			operationName = operationName,
			variableValues = variableValues
		)


	public suspend fun execute(
		documentSource: GDocumentSource.Parsable,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap()
	): GResult<Any?> =
		GDocument.parse(documentSource).flatMapValue { document ->
			execute(
				document = document,
				operationName = operationName,
				variableValues = variableValues
			)
		}


	public suspend fun execute(
		document: GDocument,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap()
	): GResult<Any?>


	public fun serializeResult(result: GResult<Any?>): Map<String, Any?>


	public companion object {

		public fun default(
			schema: GSchema,
			defaultFieldResolver: GFieldResolver<Any>? = null,
			rootResolver: GRootResolver = GRootResolver.unit()
		): GExecutor =
			DefaultExecutor(
				defaultFieldResolver = defaultFieldResolver,
				schema = schema,
				rootResolver = rootResolver
			)
	}
}
