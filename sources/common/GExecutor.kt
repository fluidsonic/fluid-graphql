package io.fluidsonic.graphql


interface GExecutor {

	fun createContext(
		schema: GSchema,
		document: GDocument,
		rootValue: GObjectValue,
		operationName: String? = null,
		variableValues: GVariableValues = emptyMap(),
		externalContext: Any? = null
	): GResult<GExecutionContext> {
		val operation = getOperation(document, operationName)
			.onFailure { return it }

		val coercedVariableValues = coerceVariableValues(schema, operation, variableValues)
			.onFailure { return it }

		val fragmentsByName = document.definitions
			.filterIsInstance<GFragmentDefinition>()
			.associateBy { it.name }

		return GResult.Success(GExecutionContext(
			document = document,
			externalContext = externalContext,
			fragmentsByName = fragmentsByName,
			operation = operation,
			rootValue = rootValue,
			schema = schema,
			variableValues = coercedVariableValues
		))
	}


	// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
	fun coerceArgumentValues(
		context: GExecutionContext,
		objectType: GObjectType,
		field: GFieldSelection
	): Map<String, GValue> {
		val coercedValues = mutableMapOf<String, GValue>()
		val argumentValues = field.arguments.associate { it.name to it.value }
		val fieldName = field.name

		val argumentDefinitions = objectType.field(fieldName)?.args ?: emptyList()
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
						context.errors += GError("Required argument '$argumentName' of type $argumentType is set to variable " +
							"$$variableName, but no value was provided.")
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
		val coercedValues = hashMapOf<String, GValue>()

		for (variableDefinition in operation.variableDefinitions) {
			val variableName = variableDefinition.name
			val variableType = schema.resolveType(variableDefinition.type)
			if (variableType == null) {
				addError("Variable $$variableName references unknown type ${variableDefinition.type}.")
				continue
			}

			if (!GSchema.isInputType(variableType)) {
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
		?.firstOrNull { it.name == argument }
		?.value
		?.let { value ->
			if (value is GVariableReference)
				context.variableValues[value.name]
			else
				value
		}


	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	fun collectFields(
		context: GExecutionContext,
		objectType: GObjectType,
		selectionSet: GSelectionSet,
		groupedFields: MutableMap<String, MutableList<GFieldSelection>> = mutableMapOf(),
		visitedFragments: MutableSet<String> = mutableSetOf()
	): Map<String, List<GFieldSelection>> {
		loop@ for (selection in selectionSet) {
			if (!shouldIncludeSelection(context, selection))
				continue

			when (selection) {
				is GFieldSelection -> {
					val responseKey = selection.alias ?: selection.name
					groupedFields.getOrPut(responseKey) { mutableListOf() } += selection
				}

				is GFragmentSpread -> {
					val fragmentName = selection.name
					if (visitedFragments.contains(fragmentName))
						continue@loop

					visitedFragments += fragmentName

					val fragment = context.fragmentsByName[fragmentName]
						?: continue@loop

					val fragmentType = context.schema.resolveType(fragment.typeCondition.type)
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

				is GInlineFragment -> {
					val fragmentTypeCondition = selection.typeCondition
					if (fragmentTypeCondition != null) {
						val fragmentType = context.schema.resolveType(fragmentTypeCondition.type)
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
		result: GValue
	): GValue {
		val fieldName = fields.first().name

		if (result == GNullValue) {
			if (fieldType is GNonNullType)
				context.errors += GError("Non-null field '$fieldName' of type $fieldType has resulted in a null value.")

			return GNullValue
		}

		when (fieldType) {
			is GEnumType,
			is GScalarType -> {
				return result // FIXME coerced(result) ?: GValue.Null
			}

			is GInterfaceType,
			is GObjectType,
			is GUnionType -> {
				if (result !is GObjectValue) {
					context.errors += GError("Field '$fieldName' of type $fieldType has resulted in a value of an incorrect type: $result")

					return GNullValue
				}

				val objectType = fieldType as? GObjectType
					?: resolveAbstractType(context, fieldType, result)

				val subSelectionSet = GSpecification.mergeSelectionSets(fields)

				return executeSelectionSet(
					context,
					subSelectionSet,
					objectType,
					result
				)
			}

			is GListType -> {
				val elementType = fieldType.ofType

				return (result as GListValue)
					.value
					.map { element ->
						completeValue(
							context,
							elementType,
							fields,
							element
						)
					}
					.let { GListValue(it) }
			}

			is GNonNullType -> {
				val nullableType = fieldType.ofType
				val completedResult = completeValue(context, nullableType, fields, result)
				if (completedResult == GNullValue)
					context.errors += GError("Non-null field '$fieldName' of type $fieldType has resulted in a null value.")

				return completedResult
			}

			is GInputObjectType ->
				error("Unexpected inout object in result")
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteField()
	fun executeField(
		context: GExecutionContext,
		objectType: GObjectType,
		objectValue: GObjectValue,
		fieldType: GType,
		fields: List<GFieldSelection>
	): GValue {
		val field = fields.first()
		val fieldName = field.name
		val argumentValues = coerceArgumentValues(context, objectType, field)
		val resolvedValue = resolveFieldValue(objectType, objectValue, fieldName, argumentValues)

		return completeValue(
			context,
			fieldType,
			fields,
			resolvedValue
		)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteQuery()
	fun executeQuery(
		context: GExecutionContext
	): GObjectValue? {
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
	): GObjectValue {
		context.errors.clear()

		val data = when (context.operation.type) {
			GOperationType.query -> executeQuery(context)
			GOperationType.mutation -> TODO() // executeMutation(operation, schema, coercedVariableValues, initialValue)
			GOperationType.subscription -> TODO() // subscribe(operation, schema, coercedVariableValues, initialValue)
		}

		return GValue.from(
			if (context.errors.isNotEmpty())
				mapOf("errors" to context.errors, "data" to data)
			else
				mapOf("data" to data)
		) as GObjectValue
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteSelectionSet()
	fun executeSelectionSet(
		context: GExecutionContext,
		selectionSet: GSelectionSet,
		objectType: GObjectType,
		objectValue: GObjectValue
	): GObjectValue {
		val groupedFieldSet = collectFields(
			context,
			objectType,
			selectionSet
		)

		val resultMap = mutableMapOf<String, GValue>()
		for ((responseKey, fields) in groupedFieldSet) {
			val fieldName = fields.first().name
			val fieldType = objectType.field(fieldName)?.type ?: continue

			val responseValue = executeField(
				context,
				objectType,
				objectValue,
				fieldType,
				fields
			)
			resultMap[responseKey] = responseValue
		}

		return GObjectValue(resultMap)
	}


	// https://graphql.github.io/graphql-spec/June2018/#DoesFragmentTypeApply()
	fun doesFragmentTypeApply(
		objectType: GObjectType,
		fragmentType: GType
	) =
		objectType.isSubtypeOf(fragmentType)


	// https://graphql.github.io/graphql-spec/June2018/#GetOperation()
	fun getOperation(document: GDocument, operationName: String?): GResult<GOperationDefinition> {
		val operations = document.definitions
			.filterIsInstance<GOperationDefinition>()

		if (operationName == null) {
			operations.singleOrNull()
				?.let { return GResult.Success(it) }

			return GResult.Failure(GError(
				if (operations.isEmpty())
					"There are no operations in the document."
				else
					"There are multiple operations in the document. You must specify one by name."
			))
		}
		else {
			operations.firstOrNull { it.name == operationName }
				?.let { return GResult.Success(it) }


			return GResult.Failure(GError("There is no operation named '$operationName' in the document."))
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#ResolveAbstractType()
	fun resolveAbstractType(
		context: GExecutionContext,
		abstractType: GType,
		objectValue: GObjectValue
	): GObjectType {
		return context.schema.resolveType("Human") as GObjectType
		// FIXME
	}


	// https://graphql.github.io/graphql-spec/June2018/#ResolveFieldValue()
	fun resolveFieldValue(
		objectType: GObjectType,
		objectValue: GObjectValue,
		fieldName: String,
		argumentValues: Map<String, GValue>
	): GValue {
		val resolver: (objectValue: GObjectValue, argumentValues: Map<String, GValue>) -> GValue = { _, _ ->
			if (fieldName == "hero")
				GValue.from(mapOf("id" to "abc"))
			else
				GStringValue(fieldName)
		}

		return resolver(objectValue, argumentValues)
	}


	fun shouldIncludeSelection(context: GExecutionContext, selection: GSelection): Boolean {
		val skip = selection.directives.getArgumentValue(context, "skip", "if") == GBooleanValue.True
		if (skip)
			return false

		val include = selection.directives.getArgumentValue(context, "include", "if") != GBooleanValue.False

		return include
	}


	companion object {

		val default = object : GExecutor {}
	}
}
