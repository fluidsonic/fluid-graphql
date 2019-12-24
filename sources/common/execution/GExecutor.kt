package io.fluidsonic.graphql


interface GExecutor {

	fun createContext(
		schema: GSchema,
		document: GDocument,
		rootValue: Any,
		operationName: String? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		externalContext: Any? = null,
		defaultResolver: GFieldResolver<*>? = null
	): GResult<GExecutionContext> = GResult {
		val operation = getOperation(document = document, name = operationName) // FIXME check type
			.or { return it }

		val coercedVariableValues = valueCoercer.coerceVariableValues(schema, operation, variableValues)
			.or { return it }

		GExecutionContext(
			defaultResolver = defaultResolver,
			document = document,
			externalContext = externalContext,
			operation = operation,
			rootValue = rootValue,
			schema = schema,
			variableValues = coercedVariableValues
		)
	}


	// FIXME move?
	// FIXME rework - this is wrong on so many levels
	private fun List<GDirective>.getArgumentValue(
		context: GExecutionContext,
		directive: GDirectiveDefinition,
		argument: String
	): GResult<Any?> = GResult {
		this@getArgumentValue
			.firstOrNull { it.name == directive.name }
			?.arguments
			?.get(argument)
			?.value
			?.let { value ->
				val directiveArgument = directive.arguments[argument]!!

				valueCoercer.coerceArgumentValue(
					value = value,
					type = directive.arguments[argument]!!.type,
					defaultValue = directiveArgument.defaultValue?.value,
					variableValues = context.variableValues
				).orNull()
			}
	}


	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	fun collectFields(
		context: GExecutionContext, // FIXME make executors instances instead of passing context everywhere?
		objectType: GObjectType,
		selectionSet: GSelectionSet,
		groupedFields: MutableMap<String, MutableList<GFieldSelection>> = mutableMapOf(),
		visitedFragments: MutableSet<String> = mutableSetOf()
	): Map<String, List<GFieldSelection>> {
		loop@ for (selection in selectionSet.selections) {
			if (!shouldIncludeSelection(selection, context = context))
				continue

			// FIXME error handling
			when (selection) {
				is GFieldSelection -> {
					val responseKey = selection.alias ?: selection.name
					groupedFields.getOrPut(responseKey) { mutableListOf() } += selection
				}

				is GFragmentSelection -> {
					val fragmentName = selection.name
					if (visitedFragments.contains(fragmentName))
						continue@loop

					visitedFragments += fragmentName

					val fragment = context.document.fragments[fragmentName]
						?: continue@loop

					val fragmentType = context.schema.resolveType(fragment.typeCondition)
					if (fragmentType == null || !doesFragmentTypeApply(fragmentType, to = objectType))
						continue@loop

					collectFields(
						context = context,
						objectType = objectType,
						selectionSet = fragment.selectionSet,
						groupedFields = groupedFields,
						visitedFragments = visitedFragments
					)
				}

				is GInlineFragmentSelection -> {
					val fragmentTypeCondition = selection.typeCondition
					if (fragmentTypeCondition != null) {
						val fragmentType = context.schema.resolveType(fragmentTypeCondition)
						if (fragmentType == null || !doesFragmentTypeApply(fragmentType, to = objectType))
							continue@loop
					}

					collectFields(
						context = context,
						objectType = objectType,
						selectionSet = selection.selectionSet,
						groupedFields = groupedFields,
						visitedFragments = visitedFragments
					)
				}
			}
		}

		return groupedFields
	}


	// https://graphql.github.io/graphql-spec/June2018/#CompleteValue()
	fun completeValue(
		context: GExecutionContext,
		fieldType: GType,
		fields: List<GFieldSelection>,
		result: Any?
	): GResult<Any?> = GResult {
		val fieldName = fields.first().name

		if (result == null) {
			if (fieldType is GNonNullType)
				collectError(GError("Non-null field '$fieldName' of type $fieldType has resulted in a null value."))

			return@GResult null
		}

		when (fieldType) {
			is GEnumType,
			is GScalarType -> {
				return@GResult result // FIXME coerced(result) ?: GValue.Null
			}

			is GInterfaceType,
			is GObjectType,
			is GUnionType -> {
				// FIXME
//				if (result !is GObjectValue) {
//					context.errors += GError("Field '$fieldName' of type $fieldType has resulted in a value of an incorrect type: $result")
//
//					return GNullValue
//				}

				val objectType = fieldType as? GObjectType
					?: resolveAbstractType(context, fieldType, result)

				val subSelectionSet = mergeSelectionSets(fields)

				return@GResult executeSelectionSet(
					context = context,
					selectionSet = subSelectionSet,
					objectType = objectType,
					objectValue = result
				)
			}

			is GListType -> {
				val elementType = fieldType.ofType

				return@GResult (result as Collection<*>).map { element ->
					completeValue(
						context,
						elementType,
						fields,
						element
					).orNull()
				}
			}

			is GNonNullType -> {
				val nullableType = fieldType.ofType
				val completedResult = completeValue(context, nullableType, fields, result)
				if (completedResult is GResult.Success && completedResult.value === null)
					collectError(GError("Non-null field '$fieldName' of type $fieldType has resulted in a null value."))

				return@GResult completedResult.orNull()
			}

			is GInputObjectType ->
				error("Unexpected input object in result")
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteField()
	fun executeField(
		context: GExecutionContext,
		objectType: GObjectType,
		objectValue: Any,
		fields: List<GFieldSelection>
	): GResult<Any?> = GResult {
		val fieldSelection = fields.first()
		val fieldDefinition = context.schema.getFieldDefinition(type = objectType, name = fieldSelection.name)
			?: error("Field '${fieldSelection.name}' cannot be selected on '$objectType'. Validate the operation before executing it.")

		val resolvedValue = resolveFieldValue(
			context = context,
			objectType = objectType,
			objectValue = objectValue,
			fieldDefinition = fieldDefinition,
			fieldSelection = fieldSelection
		).or { return it }

		completeValue(
			context = context,
			fieldType = fieldDefinition.type,
			fields = fields,
			result = resolvedValue
		).or { return it }
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	fun executeQuery(
		context: GExecutionContext
	): GPartialResult<Map<String, Any?>> = GPartialResult {
		val rootType: GObjectType = context.schema.rootTypeForOperationType(context.operation.type) ?: run {
			collectError(GError("Schema is not configured for ${context.operation.type} operations."))

			return@GPartialResult emptyMap<String, Any?>()
		}

		return executeSelectionSet(
			context = context,
			selectionSet = context.operation.selectionSet,
			objectType = rootType,
			objectValue = context.rootValue
		)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	fun executeRequest(
		context: GExecutionContext
	): Map<String, Any?> {
		val result = when (context.operation.type) {
			GOperationType.query -> executeQuery(context)
			GOperationType.mutation -> TODO() // executeMutation(operation, schema, coercedVariableValues, initialValue)
			GOperationType.subscription -> TODO() // subscribe(operation, schema, coercedVariableValues, initialValue)
		}

		return if (result.errors.isNotEmpty())
			mapOf("errors" to result.errors, "data" to result.value)
		else
			mapOf("data" to result.value)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteSelectionSet()
	fun executeSelectionSet(
		context: GExecutionContext,
		selectionSet: GSelectionSet,
		objectType: GObjectType,
		objectValue: Any
	): GPartialResult<Map<String, Any?>> = GPartialResult {
		val groupedFieldSets = collectFields(
			context = context,
			objectType = objectType,
			selectionSet = selectionSet
		)

		groupedFieldSets.mapValues { (_, fields) ->
			executeField(
				context = context,
				objectType = objectType,
				objectValue = objectValue,
				fields = fields
			).orNull()
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#DoesFragmentTypeApply()
	fun doesFragmentTypeApply(
		fragmentType: GType,
		to: GObjectType
	) =
		to.isSubtypeOf(fragmentType)


	// https://graphql.github.io/graphql-spec/June2018/#GetOperation()
	fun getOperation(
		document: GDocument,
		name: String?
	): GResult<GOperationDefinition> = GResult {
		if (name != null)
			document.operations.firstOrNull { it.name == name }
				?: return failWith(GError("There is no operation named '$name' in the document."))
		else
			document.operations.singleOrNull()
				?: return failWith(GError(
					if (document.operations.isEmpty()) "There are no operations in the document."
					else "There are multiple operations in the document. You must specify the name explicitly."
				))
	}


	// https://graphql.github.io/graphql-spec/draft/#MergeSelectionSets()
	fun mergeSelectionSets(fields: List<GFieldSelection>): GSelectionSet {
		val selections = mutableListOf<GSelection>()
		for (field in fields) {
			val fieldSelectionSet = field.selectionSet?.selections.orEmpty()
			if (fieldSelectionSet.isNotEmpty())
				selections += fieldSelectionSet
		}

		return GSelectionSet(selections = selections)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ResolveAbstractType()
	fun resolveAbstractType(
		context: GExecutionContext,
		abstractType: GType,
		objectValue: Any
	): GObjectType {
		val possibleTypes = when (abstractType) { // FIXME generalize
			is GInterfaceType ->
				// TODO probably inefficient
				context.schema.types.values
					.filterIsInstance<GObjectType>()
					.filter { it.interfaces.contains(abstractType) }

			is GUnionType ->
				abstractType.types

			else ->
				error("What to do?") // FIXME
		}

		val actualType = possibleTypes.firstOrNull { it.kotlinType?.isInstance(objectValue) ?: false }
			?: error("FIXME")

		return actualType
	}


	// https://graphql.github.io/graphql-spec/June2018/#ResolveFieldValue()
	fun resolveFieldValue(
		context: GExecutionContext,
		objectType: GObjectType,
		objectValue: Any,
		fieldDefinition: GFieldDefinition,
		fieldSelection: GFieldSelection
	): GResult<Any?> = GResult {
		val argumentValues = valueCoercer.coerceArgumentValues(
			context = context,
			fieldDefinition = fieldDefinition,
			fieldSelection = fieldSelection
		).or { return it }

		val resolver = fieldDefinition.resolver as GFieldResolver<Any>?
			?: error("No resolver registered for '$objectType.${fieldDefinition.name}'.")

		val resolverContext = object : GFieldResolver.Context {

			override val arguments: Map<String, Any?>
				get() = argumentValues

			override val parentType: GNamedType
				get() = objectType

			override val schema: GSchema
				get() = context.schema
		}

		try {
			with(resolver) { objectValue.resolve(resolverContext) }
		}
		catch (cause: Throwable) {
			collectError(GError(
				message = "Resolving the field value was not possible.", // FIXME don't re-wrap GError
				cause = cause
			))
		}
	}


	fun shouldIncludeSelection(selection: GSelection, context: GExecutionContext): Boolean {
		val skip = selection.directives.getArgumentValue(context, GSpecification.defaultSkipDirective, "if")
			.or { error("FIXME needs validation: $it") }
			as Boolean
		if (skip)
			return false

		val include = selection.directives.getArgumentValue(context, GSpecification.defaultIncludeDirective, "if")
			.or { error("FIXME needs validation: $it") }
			as Boolean

		return include
	}


	val valueCoercer: GValueCoercer
		get() = GValueCoercer.default


	companion object {

		val default = object : GExecutor {}
	}
}
