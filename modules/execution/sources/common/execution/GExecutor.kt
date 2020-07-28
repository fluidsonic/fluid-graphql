package io.fluidsonic.graphql


interface GExecutor {

	val defaultFieldResolver: GFieldResolver<Any>?
	val schema: GSchema
	val rootResolver: GRootResolver


	suspend fun execute(
		documentSource: String,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap()
	): GResult<Any?> =
		execute(
			documentSource = GDocumentSource.of(documentSource),
			operationName = operationName,
			variableValues = variableValues
		)


	suspend fun execute(
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


	suspend fun execute(
		document: GDocument,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap()
	): GResult<Any?>


	fun serializeResult(result: GResult<Any?>): Map<String, Any?>


	companion object {

		fun default(
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
