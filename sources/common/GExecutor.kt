package io.fluidsonic.graphql


interface GExecutor {

	fun createContext(
		schema: GSchema,
		document: GDocument,
		rootValue: GValue.Object,
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
				else if (argumentType is GWrappingType.NonNull)
					context.errors += GError("Required argument '$argumentName' of type $argumentType was not provided.")

				continue
			}

			if (argumentValue is GValue.Variable) {
				val variableName = argumentValue.name

				val variableValue = context.variableValues[variableName]
				if (variableValue == null) {
					val defaultValue = argumentDefinition.defaultValue
					if (defaultValue != null)
						coercedValues[argumentName] = defaultValue
					else if (argumentType is GWrappingType.NonNull)
						context.errors += GError("Required argument '$argumentName' of type $argumentType is set to variable " +
							"$$variableName, but no value was provided.")
				}
				else
					coercedValues[argumentName] = variableValue

				continue
			}

			if (coercedValues[argumentName] == GValue.Null && argumentType is GWrappingType.NonNull)
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
				else if (variableType is GWrappingType.NonNull)
					addError("A value for required variable $$variableName of type $variableType was not provided.")

				continue
			}

			if (value == GValue.Null && variableType is GWrappingType.NonNull) {
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
			if (value is GValue.Variable)
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
			val skip = selection.directives.getArgumentValue(context, "skip", "if") == GValue.Boolean.True
			if (skip)
				continue

			val include = selection.directives.getArgumentValue(context, "include", "if") != GValue.Boolean.False
			if (include)
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

					val fragment = context.fragmentsByName[fragmentName] ?: continue@loop
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

		if (result == GValue.Null) {
			if (fieldType is GWrappingType.NonNull)
				context.errors += GError("Non-null field '$fieldName' of type $fieldType has resulted in a null value.")

			return GValue.Null
		}

		when (fieldType) {
			is GEnumType,
			is GScalarType -> {
				return result // FIXME coerced(result) ?: GValue.Null
			}

			is GInterfaceType,
			is GObjectType,
			is GUnionType -> {
				result as GValue.Object

				val objectType = if (fieldType is GObjectType)
					fieldType
				else
					resolveAbstractType(fieldType, result)

				val subSelectionSet = GSpecification.mergeSelectionSets(fields)

				return executeSelectionSet(
					context,
					subSelectionSet,
					objectType,
					result
				)
			}

			is GWrappingType.List -> {
				val elementType = fieldType.ofType

				return (result as GValue.List)
					.value
					.map { element ->
						completeValue(
							context,
							elementType,
							fields,
							element
						)
					}
					.let { GValue.List(it) }
			}

			is GWrappingType.NonNull -> {
				val nullableType = fieldType.ofType
				val completedResult = completeValue(context, nullableType, fields, result)
				if (completedResult == GValue.Null)
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
		objectValue: GValue.Object,
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
	): GValue.Object? {
		val queryType = context.schema.queryType
		val selectionSet = context.operation.selectionSet

		return executeSelectionSet(
			context,
			selectionSet,
			queryType,
			context.rootValue
		)
	}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteRequest()
	fun executeRequest(
		context: GExecutionContext
	): Map<String, Any?> {
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
		objectValue: GValue.Object
	): GValue.Object {
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

		return GValue.Object(resultMap)
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
	fun resolveAbstractType(abstractType: GType, objectValue: GValue.Object): GObjectType {
		TODO()
	}


	fun resolveFieldValue(
		objectType: GObjectType,
		objectValue: GValue.Object,
		fieldName: String,
		argumentValues: Map<String, GValue>
	): GValue {
		val resolver: (objectValue: GValue.Object, argumentValues: Map<String, GValue>) -> GValue = { _, _ ->
			GValue.String(fieldName)
		}

		return resolver(objectValue, argumentValues)
	}


	companion object {

		val default = object : GExecutor {}
	}
}
