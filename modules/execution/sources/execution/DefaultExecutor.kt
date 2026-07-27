package io.fluidsonic.graphql

// FIXME exception handling
internal class DefaultExecutor(
	private val exceptionHandler: GExceptionHandler?,
	private val fieldResolver: GFieldResolver<Any>?,
	private val schema: GSchema,
	private val rootResolver: GRootResolver,
) : GExecutor {

	override suspend fun execute(
		documentSource: String,
		operationName: String?,
		variableValues: Map<String, Any?>,
		extensions: GExecutorContextExtensionSet,
	): GResult<Map<String, Any?>> = execute(
		documentSource = GDocumentSource.of(documentSource),
		extensions = extensions,
		operationName = operationName,
		variableValues = variableValues,
	)

	// Parse, validate, execute — the full request pipeline, mirroring `graphql()` in graphql-js.
	// https://spec.graphql.org/draft/#sec-Validation
	override suspend fun execute(
		documentSource: GDocumentSource.Parsable,
		operationName: String?,
		variableValues: Map<String, Any?>,
		extensions: GExecutorContextExtensionSet,
	): GResult<Map<String, Any?>> = GDocument.parse(documentSource).flatMapValue { document ->
		// The schema is validated on every request, as `graphql()` does. `GSchema.validate()` memoizes, so
		// only the first request pays for it.
		val schemaErrors = schema.validate()
		if (schemaErrors.isNotEmpty()) {
			return@flatMapValue GResult.failure(schemaErrors.map { it.copy(isRequestError = true) })
		}

		val validationErrors = document.validate(schema)
		if (validationErrors.isNotEmpty()) {
			return@flatMapValue GResult.failure(validationErrors)
		}

		execute(
			document = document,
			extensions = extensions,
			operationName = operationName,
			variableValues = variableValues,
		)
	}

	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	override suspend fun execute(
		document: GDocument,
		operationName: String?,
		variableValues: Map<String, Any?>,
		extensions: GExecutorContextExtensionSet,
	): GResult<Map<String, Any?>> {
		// Outside the catch on purpose: a broken schema is not something a client can cause, so it must fail
		// loudly instead of becoming a response error. Mirrors `execute()` in graphql-js.
		schema.assertValid()

		return try {
			executeRequest(document = document, operationName = operationName, variableValues = variableValues, extensions = extensions)
		} catch (exception: GErrorException) {
			// Last resort: no error raised during execution may escape the GResult contract.
			GResult.failure(exception.errors)
		}
	}

	private suspend fun executeRequest(
		document: GDocument,
		operationName: String?,
		variableValues: Map<String, Any?>,
		extensions: GExecutorContextExtensionSet,
	): GResult<Map<String, Any?>> = getOperation(document = document, name = operationName)
		.flatMapValue { operation ->
			makeContext(
				document = document,
				operation = operation,
				variableValues = variableValues,
				extensions = extensions,
			)
		}
		.flatMapValue { context ->
			when (context.operation.type) {
				GOperationType.query -> executeOperation(strategy = Strategy.parallel, context = context)
				GOperationType.mutation -> executeOperation(strategy = Strategy.serial, context = context)
				GOperationType.subscription -> GResult.failure(
					GError(
						message = "Subscription operations are not yet supported.",
						nodes = listOf(context.operation),
						isRequestError = true,
					),
				)
			}
		}

	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	private suspend fun executeOperation(strategy: Strategy, context: DefaultExecutorContext): GResult<Map<String, Any?>> = when (strategy) {
		Strategy.parallel -> context.selectionSetExecutor.execute(
			selectionSet = context.operation.selectionSet,
			parent = context.root,
			parentType = context.rootType,
			path = GPath.root,
			context = context,
		)
		Strategy.serial -> context.selectionSetExecutor.executeSerially(
			selectionSet = context.operation.selectionSet,
			parent = context.root,
			parentType = context.rootType,
			path = GPath.root,
			context = context,
		)
	}

	// https://graphql.github.io/graphql-spec/June2018/#GetOperation()
	private fun getOperation(document: GDocument, name: String?) = when (name) {
		null -> document.definitions.filterIsInstance<GOperationDefinition>().singleOrNull()
		else -> document.operation(name)
	}?.let { GResult.success(it) }
		?: GResult.failure(
			GError(
				message = if (name != null) {
					"There is no operation named '$name' in the document."
				} else {
					"There is no anonymous operation in the document."
				},
				isRequestError = true,
			),
		)

	private suspend fun makeContext(
		document: GDocument,
		extensions: GExecutorContextExtensionSet,
		operation: GOperationDefinition,
		variableValues: Map<String, Any?>,
	): GResult<DefaultExecutorContext> {
		val rootType = schema.rootTypeForOperationType(operation.type)
			?: return GResult.failure(
				GError(
					message = "Schema is not configured for ${operation.type} operations.",
					nodes = listOf(operation),
					isRequestError = true,
				),
			)

		val context = DefaultExecutorContext(
			document = document,
			exceptionHandler = exceptionHandler,
			extensions = extensions,
			fieldResolver = fieldResolver,
			fieldSelectionExecutor = DefaultFieldSelectionExecutor,
			nodeInputConverter = NodeInputConverter,
			operation = operation,
			outputConverter = OutputConverter,
			rootType = rootType,
			root = Unit,
			schema = schema,
			selectionSetExecutor = DefaultSelectionSetExecutor,
			variableInputConverter = VariableInputConverter,
			variableValues = emptyMap(),
		)

		return context.variableInputConverter.convertValues(
			values = variableValues,
			operation = operation,
			context = context,
		).flatMapValue { coercedVariableValues ->
			resolveRoot(context = context).mapValue { root ->
				context.copy(
					root = root,
					variableValues = coercedVariableValues,
				)
			}
		}
	}

	// Root resolution happens before execution begins, so a failure here is a request error.
	private suspend fun resolveRoot(context: DefaultExecutorContext): GResult<Any> = GResult.catchErrors {
		context.withExceptionHandler(origin = { GExceptionOrigin.RootResolver(resolver = rootResolver, context = context) }) {
			with(rootResolver) { context.resolveRoot() }
		}
	}.mapErrors { errors -> errors.map { error -> error.copy(isRequestError = true) } }

	private fun serializeError(error: GError): Map<String, Any?> = buildMap {
		put("message", error.message)

		(error.nodes.mapNotNull { it.origin } + error.origins)
			.filter { it.line > 0 && it.column > 0 }
			.map { mapOf("line" to it.line, "column" to it.column) }
			.ifEmpty { null }
			?.let { locations ->
				put("locations", locations)
			}

		error.path?.let { path ->
			put(
				"path",
				path.elements.map { element ->
					when (element) {
						is GPath.Element.Index -> element.value
						is GPath.Element.Name -> element.value
					}
				},
			)
		}

		error.extensions
			.ifEmpty { null }
			?.let { extensions ->
				put("extensions", extensions)
			}
	}

	override fun serializeResult(result: GResult<Map<String, Any?>>): Map<String, Any?> = buildMap {
		// A request error prevents execution from beginning, so the "data" key must be omitted entirely.
		// A field error keeps the key — set to null if the error propagated all the way to the root.
		// https://spec.graphql.org/draft/#sec-Response
		if (result.errors.none { it.isRequestError }) {
			put("data", result.valueOrNull())
		}

		if (result.errors.isNotEmpty()) {
			put("errors", result.errors.map(::serializeError))
		}
	}

	private enum class Strategy {

		parallel,
		serial,
	}
}
