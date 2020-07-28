package io.fluidsonic.graphql


internal class DefaultExecutor(
	override val defaultFieldResolver: GFieldResolver<Any>?,
	override val schema: GSchema,
	override val rootResolver: GRootResolver
) : GExecutor {

	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	override suspend fun execute(
		document: GDocument,
		operationName: String?,
		variableValues: Map<String, Any?>
	): GResult<Any?> =
		getOperation(document = document, name = operationName)
			.flatMapValue { operation -> makeContext(document = document, operation = operation, variableValues = variableValues) }
			.flatMapValue { context ->
				executeOperation(
					order = when (context.operation.type) {
						GOperationType.query -> Order.parallel
						GOperationType.mutation -> Order.serial
						GOperationType.subscription -> error("Subscription operations are not yet supported.")
					},
					context = context
				)
			}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	private suspend fun executeOperation(
		order: Order, // FIXME use & parallelize
		context: DefaultExecutorContext
	): GResult<Any?> =
		context.selectionSetExecutor.execute(
			selectionSet = context.operation.selectionSet,
			parent = context.root,
			parentType = context.rootType,
			path = GPath.root,
			context = context
		)


	// https://graphql.github.io/graphql-spec/June2018/#GetOperation()
	private fun getOperation(document: GDocument, name: String?) =
		document.operation(name)
			?.let { GResult.success(it) }
			?: GResult.failure(GError(
				if (name != null) "There is no operation named '$name' in the document."
				else "There are no anonymous operation in the document."
			))


	private suspend fun makeContext(
		document: GDocument,
		operation: GOperationDefinition,
		variableValues: Map<String, Any?>
	): GResult<DefaultExecutorContext> {
		val context = DefaultExecutorContext(
			defaultFieldResolver = defaultFieldResolver,
			document = document,
			fieldSelectionExecutor = DefaultFieldSelectionExecutor,
			nodeInputCoercer = GenericNodeInputCoercer,
			operation = operation,
			outputCoercer = GenericOutputCoercer,
			rootType = schema.rootTypeForOperationType(operation.type)
				?: error("Schema is not configured for ${operation.type} operations."), // FIXME GError?
			root = Unit,
			schema = schema,
			selectionSetExecutor = DefaultSelectionSetExecutor,
			variableInputCoercer = GenericVariableInputCoercer,
			variableValues = emptyMap()
		)

		return context.variableInputCoercer.coerceValues(
			values = variableValues,
			operation = operation,
			context = context
		)
			.flatMapValue { coercedVariableValues ->
				resolveRoot(context = context).mapValue { root ->
					context.copy(
						root = root,
						variableValues = coercedVariableValues
					)
				}
			}
	}


	private suspend fun resolveRoot(context: DefaultExecutorContext): GResult<Any> =
		GResult.catchErrors { rootResolver.resolveRoot(context) }


	@OptIn(ExperimentalStdlibApi::class)
	private fun serializeError(error: GError): Map<String, Any?> =
		buildMap<String, Any?> {
			put("message", error.message)

			error.origins
				.filter { it.line > 0 && it.column > 0 }
				.map { mapOf("line" to it.line, "column" to it.column) }
				.ifEmpty { null }
				?.let { locations ->
					put("locations", locations)
				}

			error.path?.let { path ->
				put("path", path.elements.map { element ->
					when (element) {
						is GPath.Element.Index -> element.value
						is GPath.Element.Name -> element.value
					}
				})
			}

			error.extensions
				.ifEmpty { null }
				?.let { extensions ->
					put("extensions", extensions)
				}
		}


	@OptIn(ExperimentalStdlibApi::class)
	override fun serializeResult(result: GResult<Any?>): Map<String, Any?> =
		buildMap<String, Any?> {
			put("data", result.valueOrNull())

			if (result.errors.isNotEmpty())
				put("errors", result.errors.map(::serializeError))
		}


	private enum class Order {

		parallel,
		serial
	}
}
