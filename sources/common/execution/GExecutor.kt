package io.fluidsonic.graphql


interface GExecutor {

	fun createContext(
		schema: GSchema,
		document: GDocument,
		rootValue: Any,
		operationName: String? = null,
		variableValues: GVariableValues = emptyMap(),
		externalContext: Any? = null,
		defaultResolver: GFieldResolver<*>? = null
	): GResult<GExecutionContext> {
		val operation = getOperation(document, operationName)
			.onFailure { return it }

		val coercedVariableValues = coerceVariableValues(schema, operation, variableValues)
			.onFailure { return it }

		return GResult.Success(GExecutionContext(
			defaultResolver = defaultResolver,
			document = document,
			externalContext = externalContext,
			operation = operation,
			rootValue = rootValue,
			schema = schema,
			variableValues = coercedVariableValues
		))
	}


	// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
	fun coerceArgumentValues(
		context: GExecutionContext,
		fieldDefinition: GFieldDefinition,
		fieldSelection: GFieldSelection
	): Map<String, Any> {
		val coercedValues = mutableMapOf<String, Any>()
		val argumentValues = fieldSelection.arguments.associate { it.name to it.value }

		val argumentDefinitions = fieldDefinition.arguments.values
		for (argumentDefinition in argumentDefinitions) {
			val argumentName = argumentDefinition.name
			val argumentType = argumentDefinition.type

			val argumentValue = argumentValues[argumentName]
			if (argumentValue == null) {
				val defaultValue = argumentDefinition.defaultValue
				if (defaultValue != null)
					coercedValues[argumentName] = defaultValue
				else if (argumentType is GNonNullType)
					context.errors += GError("Required argument '$argumentName' of type $argumentType was not provided.")

				continue
			}

			if (argumentValue is GVariableReference) {
				val variableName = argumentValue.name

				val variableValue = context.variableValues[variableName]
				if (variableValue == null) {
					val defaultValue = argumentDefinition.defaultValue
					if (defaultValue != null)
						coercedValues[argumentName] = defaultValue
					else if (argumentType is GNonNullType)
						context.errors += GError(
							message = "Required argument '$argumentName' of type $argumentType is set to variable " +
								"$$variableName, but no value was provided."
						)
				}
				else
					coercedValues[argumentName] = variableValue

				continue
			}

			if (coercedValues[argumentName] == GNullValue && argumentType is GNonNullType)
				context.errors += GError("Required argument '$argumentName' of type $argumentType must not be null.")

			// FIXME actually coerce input
			coercedValues[argumentName] = argumentValue
		}

		return coercedValues
	}


	// https://graphql.github.io/graphql-spec/June2018/#CoerceVariableValues()
	fun coerceVariableValues(
		schema: GSchema,
		operation: GOperationDefinition,
		variableValues: GVariableValues
	) = GResult.collect { addError ->
		val coercedValues = hashMapOf<String, Any>()

		for (variableDefinition in operation.variableDefinitions) {
			val variableName = variableDefinition.name
			val variableType = schema.resolveType(variableDefinition.type)
			if (variableType == null) {
				addError("Variable $$variableName references unknown type ${variableDefinition.type}.")
				continue
			}

			if (!variableType.isInputType()) {
				addError("Variable $$variableName expects a value of type $variableType which cannot be used as an input type.")
				continue
			}

			val value = variableValues[variableName]
			if (value == null) {
				val defaultValue = variableDefinition.defaultValue
				if (defaultValue != null)
					coercedValues[variableName] = defaultValue
				else if (variableType is GNonNullType)
					addError("A value for required variable $$variableName of type $variableType was not provided.")

				continue
			}

			if (value == GNullValue && variableType is GNonNullType) {
				addError("Variable $$variableName of type $variableType must not be null.")
				continue
			}

			// FIXME actually coerce input
			coercedValues[variableName] = value
		}

		coercedValues
	}


	fun List<GDirective>.getArgumentValue(
		context: GExecutionContext,
		directive: String,
		argument: String
	) = this
		.firstOrNull { it.name == directive }
		?.arguments
		?.get(argument)
		?.value
		?.let { value ->
			if (value is GVariableReference)
				context.variableValues[value.name]
			else
				value
		}


	private fun getFieldDefinition(
		schema: GSchema,
		parentType: GType,
		name: String
	) =
		when (name) {
			"__schema" ->
				if (parentType === schema.queryType)
					GIntrospection.schemaField
				else
					null

			"__type" ->
				if (parentType === schema.queryType)
					GIntrospection.typeField
				else
					null

			"__typename" ->
				GIntrospection.typenameField

			else ->
				when (parentType) {
					is GInterfaceType -> parentType.fields[name]
					is GObjectType -> parentType.fields[name]
					else -> null
				}
		}


	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	fun collectFields(
		context: GExecutionContext,
		objectType: GObjectType,
		selectionSet: GSelectionSet,
		groupedFields: MutableMap<String, MutableList<GFieldSelection>> = mutableMapOf(),
		visitedFragments: MutableSet<String> = mutableSetOf()
	): Map<String, List<GFieldSelection>> {
		loop@ for (selection in selectionSet.selections) {
			if (!shouldIncludeSelection(context, selection))
				continue

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
					if (fragmentType == null || !doesFragmentTypeApply(objectType, fragmentType))
						continue@loop

					collectFields(
						context,
						objectType,
						fragment.selectionSet,
						groupedFields,
						visitedFragments
					)
				}

				is GInlineFragmentSelection -> {
					val fragmentTypeCondition = selection.typeCondition
					if (fragmentTypeCondition != null) {
						val fragmentType = context.schema.resolveType(fragmentTypeCondition)
						if (fragmentType == null || !doesFragmentTypeApply(objectType, fragmentType))
							continue@loop
					}

					collectFields(
						context,
						objectType,
						selection.selectionSet,
						groupedFields,
						visitedFragments
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
	): Any? {
		val fieldName = fields.first().name

		if (result == null) {
			if (fieldType is GNonNullType)
				context.errors += GError("Non-null field '$fieldName' of type $fieldType has resulted in a null value.")

			return null
		}

		when (fieldType) {
			is GEnumType,
			is GScalarType -> {
				return result // FIXME coerced(result) ?: GValue.Null
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

				return executeSelectionSet(
					context = context,
					selectionSet = subSelectionSet,
					objectType = objectType,
					objectValue = result
				)
			}

			is GListType -> {
				val elementType = fieldType.ofType

				return (result as Collection<*>).map { element ->
					completeValue(
						context,
						elementType,
						fields,
						element
					)
				}
			}

			is GNonNullType -> {
				val nullableType = fieldType.ofType
				val completedResult = completeValue(context, nullableType, fields, result)
				if (completedResult == null)
					context.errors += GError("Non-null field '$fieldName' of type $fieldType has resulted in a null value.")

				return completedResult
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
	): Any? {
		val fieldSelection = fields.first()

		val fieldDefinition = getFieldDefinition(schema = context.schema, parentType = objectType, name = fieldSelection.name)
			?: return null

		val resolvedValue = resolveFieldValue(
			context = context,
			objectType = objectType,
			objectValue = objectValue,
			fieldDefinition = fieldDefinition,
			fieldSelection = fieldSelection
		)
			?: return null

		return completeValue(
			context = context,
			fieldType = fieldDefinition.type,
			fields = fields,
			result = resolvedValue
		)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	fun executeQuery(
		context: GExecutionContext
	): Map<String, Any?>? {
		val rootType = context.schema.rootTypeForOperationType(context.operation.type) ?: run {
			context.errors += GError("Schema is not configured for ${context.operation.type} operations.")
			return null
		}

		return executeSelectionSet(
			context,
			context.operation.selectionSet,
			rootType,
			context.rootValue
		)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	fun executeRequest(
		context: GExecutionContext
	): Any {
		context.errors.clear()

		val data = when (context.operation.type) {
			GOperationType.query -> executeQuery(context)
			GOperationType.mutation -> TODO() // executeMutation(operation, schema, coercedVariableValues, initialValue)
			GOperationType.subscription -> TODO() // subscribe(operation, schema, coercedVariableValues, initialValue)
		}

		return if (context.errors.isNotEmpty())
			mapOf("errors" to context.errors, "data" to data)
		else
			mapOf("data" to data)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteSelectionSet()
	fun executeSelectionSet(
		context: GExecutionContext,
		selectionSet: GSelectionSet,
		objectType: GObjectType,
		objectValue: Any
	): Map<String, Any?> {
		val groupedFieldSet = collectFields(
			context = context,
			objectType = objectType,
			selectionSet = selectionSet
		)

		val resultMap = mutableMapOf<String, Any?>()
		for ((responseKey, fields) in groupedFieldSet) {
			val responseValue = executeField(
				context = context,
				objectType = objectType,
				objectValue = objectValue,
				fields = fields
			)

			resultMap[responseKey] = responseValue
		}

		return resultMap
	}


	// https://graphql.github.io/graphql-spec/June2018/#DoesFragmentTypeApply()
	fun doesFragmentTypeApply(
		objectType: GObjectType,
		fragmentType: GType
	) =
		objectType.isSubtypeOf(fragmentType)


	// https://graphql.github.io/graphql-spec/June2018/#GetOperation()
	fun getOperation(document: GDocument, operationName: String?): GResult<GOperationDefinition> {
		if (operationName == null) {
			document.operations.singleOrNull()
				?.let { return GResult.Success(it) }

			return GResult.Failure(GError(
				if (document.operations.isEmpty())
					"There are no operations in the document."
				else
					"There are multiple operations in the document. You must specify one by name."
			))
		}
		else {
			document.operations.firstOrNull { it.name == operationName }
				?.let { return GResult.Success(it) }

			return GResult.Failure(GError("There is no operation named '$operationName' in the document."))
		}
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
	): Any? {
		val argumentValues = coerceArgumentValues(
			context = context,
			fieldDefinition = fieldDefinition,
			fieldSelection = fieldSelection
		)

		val resolver = fieldDefinition.resolver as GFieldResolver<Any>?
			?: return null

		val resolverContext = object : GFieldResolver.Context {

			override val arguments: Map<String, Any>
				get() = argumentValues

			override val parentType: GNamedType
				get() = objectType

			override val schema: GSchema
				get() = context.schema
		}

		return with(resolver) { resolverContext.resolve(objectValue) }
	}


	fun shouldIncludeSelection(context: GExecutionContext, selection: GSelection): Boolean {
		val skip = selection.directives.getArgumentValue(context, "skip", "if") == true
		if (skip)
			return false

		val include = selection.directives.getArgumentValue(context, "include", "if") != false

		return include
	}


	companion object {

		val default = object : GExecutor {}
	}
}
