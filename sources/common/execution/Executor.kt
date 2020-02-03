package io.fluidsonic.graphql


internal class Executor<out Environment : Any> private constructor(
	private val defaultResolver: GFieldResolver<Environment, *>? = null,
	private val document: GDocument,
	private val environment: Environment,
	private val externalContext: Any? = null,
	private val operation: GOperationDefinition,
	private val pathBuilder: GPath.Builder, // FIXME may not work as class property with later parallelization
	private val rootValue: Any,
	private val schema: GSchema,
	private val valueCoercer: ValueCoercer
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
		parentType: GObjectType,
		pathBuilder: GPath.Builder
	): GResult<Any?> = GResult {
		val fieldName = fieldSelections.first().name

		if (value == null) {
			if (fieldType is GNonNullType)
				collectError(GError(
					message = "Non-null field '$fieldName' with type '${fieldType.name}' has resulted in a null value.",
					nodes = fieldSelections,
					path = pathBuilder.snapshot()
				))

			return@GResult null
		}

		// FIXME type checks & coercing
		when (fieldType) {
			is GAbstractType ->
				executeSelectionSet(
					selectionSet = mergeSelectionSets(fieldSelections),
					objectType = resolveAbstractType(abstractType = fieldType, objectValue = value),
					objectValue = value,
					pathBuilder = pathBuilder
				).consumeErrors()

			is GEnumType ->
				value

			is GInputObjectType ->
				invalidOperationError(
					message = "Field '$fieldName' of object '${parentType.name}' must have an output type but has input type '${fieldType.name}'."
				)

			is GListType ->
				(value as Collection<*>).mapIndexed { index, element ->
					pathBuilder.withListIndex(index) {
						completeValue(
							value = element,
							fieldType = fieldType.elementType,
							fieldSelections = fieldSelections,
							parentType = parentType,
							pathBuilder = pathBuilder
						).orNull()
					}
				}

			is GObjectType ->
				executeSelectionSet(
					selectionSet = mergeSelectionSets(fieldSelections),
					objectType = fieldType,
					objectValue = value,
					pathBuilder = pathBuilder
				).consumeErrors()

			is GNonNullType -> {
				val completedResult = completeValue(
					value = value,
					fieldType = fieldType.nullableType,
					fieldSelections = fieldSelections,
					parentType = parentType,
					pathBuilder = pathBuilder
				)
				if (completedResult is GResult.Success && completedResult.value === null)
					collectError(GError(
						message = "Non-null field '$fieldName' of type '${fieldType.name}' has resulted in a null value.",
						nodes = fieldSelections,
						path = pathBuilder.snapshot()
					))

				completedResult.orNull()
			}

			is GScalarType ->
				value
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#DoesFragmentTypeApply()
	private fun doesFragmentTypeApply(
		fragmentType: GType,
		to: GObjectType
	) =
		to.isSubtypeOf(fragmentType)


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	suspend fun execute(): Map<String, Any?> {
		val result = when (operation.type) {
			GOperationType.query -> executeQuery()
			GOperationType.mutation -> TODO() // FIXME
			GOperationType.subscription -> TODO() // FIXME
		}

		return if (result.errors.isEmpty())
			mapOf("data" to result.value)
		else
			mapOf("errors" to result.errors, "data" to result.value)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteField()
	private suspend fun executeField(
		objectType: GObjectType,
		objectValue: Any,
		fieldSelections: List<GFieldSelection>,
		pathBuilder: GPath.Builder
	): GResult<Any?> = GResult {
		val fieldSelection = fieldSelections.first()
		val fieldDefinition = schema.getFieldDefinition(type = objectType, name = fieldSelection.name)
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
			parentType = objectType,
			pathBuilder = pathBuilder
		).or { return it }
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	private suspend fun executeQuery(): GPartialResult<Map<String, Any?>> = GPartialResult {
		val rootType = schema.rootTypeForOperationType(operation.type) ?: run {
			collectError(GError("Schema is not configured for ${operation.type} operations."))

			return@GPartialResult emptyMap<String, Any?>()
		}

		return executeSelectionSet(
			selectionSet = operation.selectionSet,
			objectType = rootType,
			objectValue = rootValue,
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
		val argumentValues = valueCoercer.coerceArgumentValues(
			arguments = fieldSelections.first().arguments,
			argumentDefinitions = fieldDefinition.argumentDefinitions
		).or { return it } // FIXME possible if validated?

		// FIXME type safety
		// FIXME fallback resolver
		val resolver = fieldDefinition.resolver as GFieldResolver<Environment, Any>?
			?: error("No resolver registered for '${objectType.name}.${fieldDefinition.name}'.")

		val resolverContext = object : GFieldResolver.Context<Environment> {

			override val arguments get() = argumentValues
			override val environment get() = this@Executor.environment
			override val parentType get() = objectType
			override val schema get() = this@Executor.schema
		}

		try {
			with(resolver) { objectValue.resolve(resolverContext) }
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


	private fun GNode.WithDirectives.getDirectiveValues(definition: GDirectiveDefinition): Map<String, Any?>? =
		directives.firstOrNull { it.name == definition.name }
			?.let { directive ->
				valueCoercer.coerceArgumentValues(
					arguments = directive.arguments,
					argumentDefinitions = definition.argumentDefinitions
				).or { failure ->
					invalidOperationError(
						message = "Invalid '${definition.name}' directive arguments.",
						errors = failure.errors
					)
				}
			}


	private fun invalidOperationError(message: String, errors: List<GError> = emptyList()): Nothing =
		invalidOperationError(document = document, schema = schema, message = message, errors = errors)


	// FIXME type casting
	private fun GSelection.isRequested(): Boolean {
		val skip = getDirectiveValues(GSpecification.defaultSkipDirective)?.get("if") as Boolean? ?: false
		if (skip)
			return false

		val include = getDirectiveValues(GSpecification.defaultIncludeDirective)?.get("if") as Boolean? ?: true
		if (!include)
			return false

		return true
	}


	companion object {

		fun <Environment : Any> create(
			schema: GSchema,
			document: GDocument,
			environment: Environment,
			rootValue: Any,
			operationName: String? = null,
			variableValues: Map<String, Any?> = emptyMap(),
			externalContext: Any? = null,
			defaultResolver: GFieldResolver<Environment, *>? = null
		): GResult<Executor<Environment>> = GResult {
			// FIXME check type
			val operation = getOperation(
				document = document,
				name = operationName
			).or { return it }

			val pathBuilder = GPath.Builder()

			val valueCoercer = ValueCoercer.create(
				document = document,
				schema = schema,
				variableValues = variableValues,
				pathBuilder = pathBuilder
			).or { return it }

			Executor(
				defaultResolver = defaultResolver,
				document = document,
				environment = environment,
				externalContext = externalContext,
				operation = operation,
				pathBuilder = pathBuilder,
				rootValue = rootValue,
				schema = schema,
				valueCoercer = valueCoercer
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
