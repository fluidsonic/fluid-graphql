package io.fluidsonic.graphql


// FIXME exception handling
internal class DefaultExecutor(
	private val exceptionHandler: GExceptionHandler?,
	private val fieldResolver: GFieldResolver<Any>?,
	private val nodeInputCoercer: GNodeInputCoercer<Any?>?,
	private val outputCoercer: GOutputCoercer<Any>?,
	private val schema: GSchema,
	private val rootResolver: GRootResolver,
	private val variableInputCoercer: GVariableInputCoercer<Any?>?
) : GExecutor {

	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	override suspend fun execute(
		document: GDocument,
		operationName: String?,
		variableValues: Map<String, Any?>,
		extensions: GExecutorContextExtensionSet
	): GResult<Any?> =
		getOperation(document = document, name = operationName)
			.flatMapValue { operation ->
				makeContext(
					document = document,
					operation = operation,
					variableValues = variableValues,
					extensions = extensions
				)
			}
			.flatMapValue { context ->
				executeOperation(
					strategy = when (context.operation.type) {
						GOperationType.query -> Strategy.parallel
						GOperationType.mutation -> Strategy.serial
						GOperationType.subscription -> error("Subscription operations are not yet supported.")
					},
					context = context
				)
			}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	private suspend fun executeOperation(
		strategy: Strategy, // FIXME use
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
		extensions: GExecutorContextExtensionSet,
		operation: GOperationDefinition,
		variableValues: Map<String, Any?>
	): GResult<DefaultExecutorContext> {
		val context = DefaultExecutorContext(
			document = document,
			exceptionHandler = exceptionHandler,
			extensions = extensions,
			fieldResolver = fieldResolver,
			fieldSelectionExecutor = DefaultFieldSelectionExecutor,
			nodeInputCoercer = nodeInputCoercer,
			nodeInputConverter = NodeInputConverter,
			operation = operation,
			outputCoercer = outputCoercer,
			outputConverter = OutputConverter,
			rootType = schema.rootTypeForOperationType(operation.type)
				?: error("Schema is not configured for ${operation.type} operations."), // FIXME GError?
			root = Unit,
			schema = schema,
			selectionSetExecutor = DefaultSelectionSetExecutor,
			variableInputCoercer = variableInputCoercer,
			variableInputConverter = VariableInputConverter,
			variableValues = emptyMap()
		)

		return context.variableInputConverter.convertValues(
			values = variableValues,
			operation = operation,
			context = context
		).flatMapValue { coercedVariableValues ->
			resolveRoot(context = context).mapValue { root ->
				context.copy(
					root = root,
					variableValues = coercedVariableValues
				)
			}
		}
	}


	private suspend fun resolveRoot(context: DefaultExecutorContext): GResult<Any> =
		GResult.catchErrors {
			context.withExceptionHandler(origin = { GExceptionOrigin.RootResolver(resolver = rootResolver, context = context) }) {
				with(rootResolver) { context.resolveRoot() }
			}
		}


	@OptIn(ExperimentalStdlibApi::class)
	private fun serializeError(error: GError): Map<String, Any?> =
		buildMap {
			put("message", error.message)

			(error.nodes.mapNotNull { it.origin } + error.origins)
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
		buildMap {
			put("data", result.valueOrNull())

			if (result.errors.isNotEmpty())
				put("errors", result.errors.map(::serializeError))
		}


	private enum class Strategy {

		parallel,
		serial
	}
}
