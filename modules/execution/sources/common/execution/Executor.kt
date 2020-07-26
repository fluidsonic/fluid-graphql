package io.fluidsonic.graphql


// FIXME refactor all this, esp. value coercing
// FIXME refactor per-field-selection execution into own class & parallelize for queries
// FIXME We mix field names and aliases. Revisit all usages, also in coercion errors.
internal class Executor<Environment : Any> private constructor(
	private val defaultResolver: GFieldResolver<Environment, Any>? = null,
	private val document: GDocument,
	private val environment: Environment,
	private val operation: GOperationDefinition,
	private val pathBuilder: GPath.Builder, // FIXME may not work as class property with later parallelization
	private val rootResolver: GRootResolver,
	private val schema: GSchema,
	private val variableValues: Map<String, Any?>
) {

	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	private fun collectFieldSelections(
		selection: GSelection,
		parentType: GObjectType,
		fieldSelectionsByResponseKey: MutableMap<String, MutableList<GFieldSelection>>,
		visitedFragmentNames: MutableSet<String>
	) {
		when (selection) {
			is GFieldSelection -> {
				val responseKey = selection.alias ?: selection.name
				fieldSelectionsByResponseKey.getOrPut(responseKey) { mutableListOf() } += selection
			}

			is GFragmentSelection -> {
				val fragmentName = selection.name
				if (!visitedFragmentNames.add(fragmentName))
					return

				val fragment = document.fragment(fragmentName)
					?: invalidOperationError("A fragment with name '$fragmentName' is referenced but not defined.")

				val fragmentType = schema.resolveType(fragment.typeCondition)
					?: invalidOperationError("Cannot resolve type '${fragment.typeCondition}' in condition of fragment '$fragmentName'.")

				if (!doesFragmentTypeApply(fragmentType, to = parentType))
					return

				collectFieldSelections(
					selectionSet = fragment.selectionSet,
					parentType = parentType,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragments = visitedFragmentNames
				)
			}

			is GInlineFragmentSelection -> {
				val fragmentTypeCondition = selection.typeCondition
				if (fragmentTypeCondition !== null) {
					val fragmentType = schema.resolveType(fragmentTypeCondition)
						?: invalidOperationError("Cannot resolve type '${fragmentTypeCondition}' in condition of inline fragment.")

					if (!doesFragmentTypeApply(fragmentType, to = parentType))
						return
				}

				collectFieldSelections(
					selectionSet = selection.selectionSet,
					parentType = parentType,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragments = visitedFragmentNames
				)
			}
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	private fun collectFieldSelections(
		selectionSet: GSelectionSet,
		parentType: GObjectType,
		fieldSelectionsByResponseKey: MutableMap<String, MutableList<GFieldSelection>> = mutableMapOf(),
		visitedFragments: MutableSet<String> = mutableSetOf()
	): Map<String, List<GFieldSelection>> {
		selectionSet.selections
			.filter { it.isRequested() }
			.forEach { selection ->
				collectFieldSelections(
					selection = selection,
					parentType = parentType,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragmentNames = visitedFragments
				)
			}

		return fieldSelectionsByResponseKey
	}


	// https://graphql.github.io/graphql-spec/June2018/#CompleteValue()
	private suspend fun completeValue(
		value: Any?,
		fieldType: GType,
		fieldSelections: List<GFieldSelection>,
		fieldDefinition: GFieldDefinition,
		parentType: GObjectType,
		pathBuilder: GPath.Builder
	): GResult<Any?> = GResult {
		val fieldName = fieldSelections.first().name
		val outputCoercer = DefaultOutputCoercer(
			fieldDefinition = fieldDefinition,
			parentType = parentType,
			schema = schema,
			variableValues = variableValues
		)

		when (value) {
			null -> outputCoercer.coerceValueAbsence(fieldType)
			else -> when (fieldType) {
				is GAbstractType ->
					executeSelectionSet(
						selectionSet = mergeSelectionSets(fieldSelections),
						objectType = resolveAbstractType(abstractType = fieldType, objectValue = value),
						objectValue = value,
						pathBuilder = pathBuilder
					).consumeErrors()

				is GInputObjectType ->
					invalidOperationError(
						message = "Field '$fieldName' of object '${parentType.name}' must have an output type but has input type '${fieldType.name}'."
					)

				is GLeafType ->
					outputCoercer.coerceValueAsLeaf(value, type = fieldType)

				is GListType -> outputCoercer.coerceValueAsList( // FIXME what if error?
					value = when (value) {
						is Collection<*> -> value.mapIndexed { index, element ->
							pathBuilder.withIndex(index) {
								completeValue(
									value = element,
									fieldType = fieldType.elementType,
									fieldSelections = fieldSelections,
									fieldDefinition = fieldDefinition,
									parentType = parentType,
									pathBuilder = pathBuilder
								).orNull()
							}
						}
						else -> value
					},
					type = fieldType
				)

				is GObjectType ->
					outputCoercer.coerceValueAsObject( // FIXME what if error?
						value = executeSelectionSet(
							selectionSet = mergeSelectionSets(fieldSelections),
							objectType = fieldType,
							objectValue = value,
							pathBuilder = pathBuilder
						).consumeErrors(),
						type = fieldType
					)

				is GNonNullType -> {
					val completedResult = completeValue(
						value = value,
						fieldType = fieldType.nullableType,
						fieldSelections = fieldSelections,
						fieldDefinition = fieldDefinition,
						parentType = parentType,
						pathBuilder = pathBuilder
					)
					if (completedResult is GResult.Success && completedResult.value === null)
						collectError(GError( // FIXME isn't this an internal error?
							message = "Field '$fieldName' of non-null type '${fieldType.name}' has resolved in a null value.",
							nodes = fieldSelections,
							path = pathBuilder.snapshot()
						))

					completedResult.orNull()?.let { value ->
						outputCoercer.coerceValueAsNonNull(value, type = fieldType)
					}
				}
			}
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#DoesFragmentTypeApply()
	private fun doesFragmentTypeApply(
		fragmentType: GType,
		to: GObjectType
	) =
		to.isSubtypeOf(fragmentType)


	// FIXME return structured data and separate serialization
	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	suspend fun execute(): Map<String, Any?> {
		val result = when (operation.type) {
			GOperationType.query -> executeQuery()
			GOperationType.mutation -> executeMutation()
			GOperationType.subscription -> TODO() // FIXME
		}

		return if (result.errors.isEmpty())
			mapOf("data" to result.value)
		else
			mapOf("errors" to result.errors.map(this::serializeError), "data" to result.value)
	}


	private fun fieldDefinition(node: GNode.WithFieldDefinitions, name: String): GFieldDefinition? =
		when (name) {
			"__schema" ->
				if (node === schema.queryType) Introspection.schemaField else null

			"__type" ->
				if (node === schema.queryType) Introspection.typeField else null

			"__typename" ->
				Introspection.typenameField

			else ->
				node.field(name)
		}


	// FIXME
	private fun serializeError(error: GError) =
		mapOf(
			"message" to error.message
		)


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteField()
	private suspend fun executeField(
		objectType: GObjectType,
		objectValue: Any,
		fieldSelections: List<GFieldSelection>,
		pathBuilder: GPath.Builder
	): GResult<Any?> = GResult {
		val fieldSelection = fieldSelections.first()
		val fieldDefinition = fieldDefinition(node = objectType, name = fieldSelection.name)
			?: invalidOperationError("There is no field named '${fieldSelection.name}' on type '${objectType.name}'.")
		val fieldType = schema.resolveType(fieldDefinition.type)
			?: invalidOperationError("Cannot resolve type '${fieldDefinition.type}' for field '${fieldDefinition.name}' of '${objectType.name}'.")

		val resolvedValue = resolveFieldValue(
			fieldDefinition = fieldDefinition,
			fieldSelections = fieldSelections,
			objectType = objectType,
			objectValue = objectValue,
			pathBuilder = pathBuilder
		).or { return it }

		completeValue(
			value = resolvedValue,
			fieldType = fieldType,
			fieldSelections = fieldSelections,
			fieldDefinition = fieldDefinition,
			parentType = objectType,
			pathBuilder = pathBuilder
		).or { return it }
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	private suspend fun executeMutation(): GPartialResult<Map<String, Any?>> = GPartialResult {
		val rootType = schema.rootTypeForOperationType(operation.type) ?: run {
			collectError(GError("Schema is not configured for ${operation.type} operations."))

			return@GPartialResult emptyMap<String, Any?>()
		}

		return executeSelectionSet(
			selectionSet = operation.selectionSet,
			objectType = rootType,
			objectValue = resolveRootValue(operationType = GOperationType.mutation, operationTypeDefinition = rootType),
			pathBuilder = GPath.Builder()
		)
	}


	// FIXME parallelize
	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	private suspend fun executeQuery(): GPartialResult<Map<String, Any?>> = GPartialResult {
		val rootType = schema.rootTypeForOperationType(operation.type) ?: run {
			collectError(GError("Schema is not configured for ${operation.type} operations."))

			return@GPartialResult emptyMap<String, Any?>()
		}

		return executeSelectionSet(
			selectionSet = operation.selectionSet,
			objectType = rootType,
			objectValue = resolveRootValue(operationType = GOperationType.query, operationTypeDefinition = rootType),
			pathBuilder = GPath.Builder()
		)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteSelectionSet()
	private suspend fun executeSelectionSet(
		selectionSet: GSelectionSet,
		objectType: GObjectType,
		objectValue: Any,
		pathBuilder: GPath.Builder
	): GPartialResult<Map<String, Any?>> = GPartialResult {
		val fieldSelectionsByResponseKey = collectFieldSelections(
			parentType = objectType,
			selectionSet = selectionSet
		)

		fieldSelectionsByResponseKey.mapValues { (_, fields) ->
			executeField(
				objectType = objectType,
				objectValue = objectValue,
				fieldSelections = fields,
				pathBuilder = pathBuilder
			).orNull()
		}
	}


	// https://graphql.github.io/graphql-spec/draft/#MergeSelectionSets()
	private fun mergeSelectionSets(fieldSelections: List<GFieldSelection>) =
		GSelectionSet(
			selections = fieldSelections.flatMap { it.selectionSet?.selections.orEmpty() }
		)


	// https://graphql.github.io/graphql-spec/June2018/#ResolveAbstractType()
	private fun resolveAbstractType(
		abstractType: GAbstractType,
		objectValue: Any
	) =
		schema.getPossibleTypes(abstractType)
			.firstOrNull { it.kotlinType?.isInstance(objectValue) ?: false }
			?: invalidOperationError("Cannot resolve abstract type '${abstractType.name}' for ${objectValue::class}: $objectValue")


	// https://graphql.github.io/graphql-spec/June2018/#ResolveFieldValue()
	private suspend fun resolveFieldValue(
		fieldDefinition: GFieldDefinition,
		fieldSelections: List<GFieldSelection>,
		objectType: GObjectType,
		objectValue: Any,
		pathBuilder: GPath.Builder
	): GResult<Any?> = GResult {
		val anyFieldSelection = fieldSelections.first()
		val argumentCoercer = DefaultNodeInputCoercer(fieldSelectionPath = pathBuilder, schema = schema, variableValues = variableValues)
		val argumentValues = argumentCoercer.coerceArguments(node = anyFieldSelection, definitions = fieldDefinition.argumentDefinitions)

		// FIXME type safety
		val resolver = fieldDefinition.resolver as GFieldResolver<Environment, Any>?
			?: defaultResolver
			?: error("No resolver registered for '${objectType.name}.${fieldDefinition.name}' and no default resolver was specified.")

		val resolverContext = object : GFieldResolverContext<Environment> {

			override val arguments get() = argumentValues
			override val environment get() = this@Executor.environment
			override val fieldDefinition get() = fieldDefinition
			override val parentTypeDefinition get() = objectType
			override val schema get() = this@Executor.schema
		}

		try {
			resolver.resolveField(parent = objectValue, context = resolverContext)
		}
		catch (cause: GError) {
			collectError(cause.copy(
				cause = cause,
				nodes = fieldSelections,
				path = pathBuilder.snapshot()
			))
		}
		catch (cause: Throwable) {
			// FIXME remove
			println(cause)

			collectError(GError(
				message = "The field value is not available at the moment.",
				cause = cause,
				nodes = fieldSelections,
				path = pathBuilder.snapshot()
			))
		}
	}


	private suspend fun resolveRootValue(operationType: GOperationType, operationTypeDefinition: GObjectType) =
		rootResolver.resolveRoot(context = object : GRootResolverContext {

			override val operationType = operationType
			override val operationTypeDefinition = operationTypeDefinition
			override val schema = this@Executor.schema
		})


	private fun GNode.WithDirectives.getDirectiveValues(definition: GDirectiveDefinition): Map<String, Any?>? =
		directives.firstOrNull { it.name == definition.name }
			?.let { directive ->
				DefaultNodeInputCoercer(fieldSelectionPath = pathBuilder, schema = schema, variableValues = variableValues)
					.coerceArguments(node = directive, definitions = definition.argumentDefinitions)
			}


	private fun invalidOperationError(message: String, errors: List<GError> = emptyList()): Nothing =
		error(buildString {
			append("The operation is invalid: $message\n")
			append("Validate each operation before executing it.\n\n")

			if (errors.isNotEmpty()) {
				append("Errors:\n")
				append(errors.joinToString("\n\n"))
				append("\n\n")
			}

			append("Document:\n$document\n\n")
			append("Schema:\n$schema")
		})


	// FIXME type casting
	private fun GSelection.isRequested(): Boolean {
		val skip = getDirectiveValues(GLanguage.defaultSkipDirective)?.get("if") as Boolean? ?: false
		if (skip)
			return false

		val include = getDirectiveValues(GLanguage.defaultIncludeDirective)?.get("if") as Boolean? ?: true
		if (!include)
			return false

		return true
	}


	companion object {

		fun <Environment : Any> create(
			schema: GSchema,
			document: GDocument,
			environment: Environment,
			rootResolver: GRootResolver = GRootResolver.unit(),
			operationName: String? = null,
			variableValues: Map<String, Any?> = emptyMap(),
			defaultResolver: GFieldResolver<Environment, Any>? = null
		): GResult<Executor<Environment>> = GResult {
			// FIXME check type
			val operation = getOperation(
				document = document,
				name = operationName
			).or { return it }

			val pathBuilder = GPath.Builder()

			val variableValueCoercer = DefaultVariableInputCoercer(
				schema = schema,
				defaultValueCoercer = DefaultNodeInputCoercer(
					fieldSelectionPath = null,
					schema = schema,
					variableValues = emptyMap()
				)
			)

			Executor(
				defaultResolver = defaultResolver,
				document = document, // FIXME document parsing should happen inside the executor because the resulting errors must be serialized in the result
				environment = environment,
				operation = operation,
				pathBuilder = pathBuilder,
				rootResolver = rootResolver,
				schema = schema,
				// FIXME coercion should happen inside the executor because the resulting errors must be serialized in the result
				variableValues = variableValueCoercer.coerceValues(values = variableValues, operation = operation)
			)
		}


		// https://graphql.github.io/graphql-spec/June2018/#GetOperation()
		private fun getOperation(
			document: GDocument,
			name: String?
		): GResult<GOperationDefinition> = GResult {
			document.operation(name)
				?: return failWith(GError(
					if (name != null) "There is no operation named '$name' in the document."
					else "There are no anonymous operation in the document."
				))
		}
	}
}


// FIXME move to extension
// FIXME add a way to execute and returning either data or errors rather than a response containing serialized errors
suspend fun <Environment : Any> GDocument.execute(
	schema: GSchema,
	rootResolver: GRootResolver = GRootResolver.unit(),
	environment: Environment,
	operationName: String? = null,
	variableValues: Map<String, Any?> = emptyMap(),
	defaultResolver: GFieldResolver<Environment, Any>? = null
): Map<String, Any?> =
	Executor.create(
		schema = schema,
		document = this,
		environment = environment,
		rootResolver = rootResolver,
		operationName = operationName,
		variableValues = variableValues,
		defaultResolver = defaultResolver
	)
		.consumeErrors { throw it.errors.first() } // FIXME ??
		.execute()


// FIXME move to extension
// FIXME add a way to execute and returning either data or errors rather than a response containing serialized errors
suspend fun GDocument.execute(
	schema: GSchema,
	rootResolver: GRootResolver = GRootResolver.unit(),
	operationName: String? = null,
	variableValues: Map<String, Any?> = emptyMap(),
	defaultResolver: GFieldResolver<Unit, Any>? = null
): Map<String, Any?> =
	execute(
		schema = schema,
		rootResolver = rootResolver,
		environment = Unit,
		operationName = operationName,
		variableValues = variableValues,
		defaultResolver = defaultResolver
	)
