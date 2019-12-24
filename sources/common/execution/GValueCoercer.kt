package io.fluidsonic.graphql


interface GValueCoercer {

	// FIXME not a good name
	fun coerceArgumentValue(
		value: GAst.Value,
		type: GType,
		nonNullType: GNonNullType? = type as? GNonNullType,
		rootType: GType = type,
		defaultValue: Any? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		pathBuilder: GPath.Builder = GPath.Builder()
	): GResult<Any?> = GResult {

		fun GAst.Value.coerce(
			childType: GType,
			isNewArgument: Boolean = false,
			defaultValue: Any? = null
		) =
			coerceArgumentValue(
				value = this,
				type = childType,
				nonNullType = type as? GNonNullType,
				rootType = if (isNewArgument) childType else rootType,
				defaultValue = defaultValue,
				variableValues = variableValues,
				pathBuilder = pathBuilder
			)

		fun GAst.Value.Variable.resolve() =
			variableValues.getOrElseNullable(name.value) {
				error("Variable '$name' cannot be resolved. Validate the operation before executing it.")
			}

		fun invalidValueError(message: String, cause: Throwable? = null) =
			collectError(GError(
				message = message,
				path = pathBuilder.snapshot(),
				nodes = listOf(value),
				cause = cause
			))

		fun invalidValueTypeError() =
			invalidValueError("Unexpected '${value.type}' for argument of type '${rootType}'.")


		when (type) {
			// https://graphql.github.io/graphql-spec/draft/#sec-Boolean.Input-Coercion
			is GBooleanType ->
				when (value) {
					is GAst.Value.Boolean -> value.value
					is GAst.Value.Null -> defaultValue
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Enum,
					is GAst.Value.Float,
					is GAst.Value.Int,
					is GAst.Value.List,
					is GAst.Value.Object,
					is GAst.Value.String -> invalidValueTypeError()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Scalars.Input-Coercion
			is GCustomScalarType ->
				// FIXME support conversion function
				when (value) {
					is GAst.Value.Boolean -> value.value
					is GAst.Value.Float -> value.value
					is GAst.Value.Int -> value.value
					is GAst.Value.Null -> defaultValue
					is GAst.Value.String -> value.value
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Enum,
					is GAst.Value.List,
					is GAst.Value.Object -> invalidValueTypeError()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Enums.Input-Coercion
			is GEnumType ->
				when (value) {
					is GAst.Value.Enum -> type.values[value.name]
						?: invalidValueError("Invalid value '${value.name}' for enum '$type'.")
					is GAst.Value.Null -> defaultValue
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Boolean,
					is GAst.Value.Float,
					is GAst.Value.Int,
					is GAst.Value.List,
					is GAst.Value.Object,
					is GAst.Value.String -> invalidValueTypeError()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Float.Input-Coercion
			is GFloatType ->
				when (value) {
					is GAst.Value.Float -> value.value
					is GAst.Value.Int -> value.value.toFloat()
					is GAst.Value.Null -> defaultValue
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Boolean,
					is GAst.Value.Enum,
					is GAst.Value.List,
					is GAst.Value.Object,
					is GAst.Value.String -> invalidValueTypeError()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-ID.Input-Coercion
			is GIDType ->
				when (value) {
					is GAst.Value.Int -> value.value
					is GAst.Value.Null -> defaultValue
					is GAst.Value.String -> value.value
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Boolean,
					is GAst.Value.Enum,
					is GAst.Value.Float,
					is GAst.Value.List,
					is GAst.Value.Object -> invalidValueTypeError()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercion
			is GInputObjectType ->
				when (value) {
					is GAst.Value.Null -> defaultValue
					is GAst.Value.Object -> {
						val argumentValues = value.fields.associate { it.name.value to it.value }

						type.arguments.values.associate { argument ->
							pathBuilder.withFieldName(argument.name) {
								val argumentValue = argumentValues[argument.name]
									?: invalidValueError("Missing field '${argument.name}' in value for input object '$type'.")

								argument.name to argumentValue?.coerce(
									childType = argument.type,
									isNewArgument = true,
									defaultValue = argument.defaultValue
								)
							}
						}
					}
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Boolean,
					is GAst.Value.Enum,
					is GAst.Value.Float,
					is GAst.Value.Int,
					is GAst.Value.List,
					is GAst.Value.String -> invalidValueTypeError()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Int.Input-Coercion
			is GIntType ->
				when (value) {
					is GAst.Value.Int -> value.value
					is GAst.Value.Null -> defaultValue
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Boolean,
					is GAst.Value.Enum,
					is GAst.Value.Float,
					is GAst.Value.List,
					is GAst.Value.Object,
					is GAst.Value.String -> invalidValueTypeError()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
			is GListType ->
				when (value) {
					is GAst.Value.List -> value.elements.mapIndexed { index, element ->
						pathBuilder.withListIndex(index) {
							element.coerce(type.ofType)
						}
					}
					is GAst.Value.Null -> null
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Boolean,
					is GAst.Value.Enum,
					is GAst.Value.Float,
					is GAst.Value.Int,
					is GAst.Value.Object,
					is GAst.Value.String -> listOf(value.coerce(type.ofType))
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
			is GNonNullType ->
				when (value) {
					is GAst.Value.Boolean,
					is GAst.Value.Enum,
					is GAst.Value.Float,
					is GAst.Value.Int,
					is GAst.Value.List,
					is GAst.Value.Object,
					is GAst.Value.String -> value.coerce(type.ofType)

					is GAst.Value.Null -> defaultValue
						?: invalidValueTypeError()

					is GAst.Value.Variable -> value.resolve()
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-String.Input-Coercion
			is GStringType ->
				when (value) {
					is GAst.Value.Null -> defaultValue
					is GAst.Value.String -> value.value
					is GAst.Value.Variable -> value.resolve()

					is GAst.Value.Boolean,
					is GAst.Value.Enum,
					is GAst.Value.Float,
					is GAst.Value.Int,
					is GAst.Value.List,
					is GAst.Value.Object -> invalidValueTypeError()
				}

			is GInterfaceType,
			is GObjectType,
			is GUnionType ->
				error("Argument '$pathBuilder' must have an input type but has output type '$type'.")
		}
	}


	// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
	fun coerceArgumentValues(
		context: GExecutionContext,
		fieldDefinition: GFieldDefinition,
		fieldSelection: GFieldSelection
	): GResult<Map<String, Any?>> = GResult {
		val argumentValues = fieldSelection.arguments.associate { it.name to it.value }
		val pathBuilder = GPath.Builder()

		fieldDefinition.arguments.values.associate { argument ->
			pathBuilder.withFieldName(argument.name) {
				val argumentValue = argumentValues[argument.name]
				if (argumentValue === null)
					argument.name to collectError(GError(
						message = "Missing argument '${argument.name}' of type '${argument.type}' for field '${fieldDefinition.name}'.",
						path = pathBuilder.snapshot()
					))
				else
					argument.name to coerceArgumentValue(
						value = argumentValue,
						type = argument.type,
						defaultValue = argument.defaultValue,
						pathBuilder = pathBuilder,
						variableValues = context.variableValues
					).orNull()
			}
		}
	}


	// FIXME not a good name
	fun coerceVariableValue(
		value: Any?,
		type: GType,
		nonNullType: GNonNullType? = type as? GNonNullType,
		rootType: GType = type,
		pathBuilder: GPath.Builder = GPath.Builder()
	): GResult<Any?> = GResult {

		inline fun Any?.coerce(
			childType: GType,
			isNewArgument: Boolean = false,
			defaultValue: Any? = null
		) =
			coerceVariableValue(
				value = this,
				type = childType,
				nonNullType = type as? GNonNullType,
				rootType = if (isNewArgument) childType else rootType,
				pathBuilder = pathBuilder
			)

		inline fun invalidValueError(message: String, cause: Throwable? = null) =
			collectError(GError(
				message = message,
				path = pathBuilder.snapshot(),
				cause = cause
			))

		inline fun invalidValueTypeError() =
			invalidValueError("Unexpected value for argument of type '${rootType}'.")


		when (type) {
			is GBooleanType ->
				when (value) {
					null -> null
					is Boolean -> value
					else -> invalidValueTypeError()
				}

			is GCustomScalarType ->
				// FIXME support conversion function
				when (value) {
					null -> null
					is Boolean -> value
					is Float -> value
					is Int -> value
					is String -> value
					else -> invalidValueTypeError()
				}

			is GEnumType ->
				when (value) {
					null -> null
					is String -> type.values[value] ?: invalidValueError("Invalid value '${value}' for enum '$type'.")
					else -> invalidValueTypeError()
				}

			is GFloatType ->
				when (value) {
					null -> null
					is Float -> value
					is Int -> value.toFloat()
					else -> invalidValueTypeError()
				}

			is GIDType ->
				when (value) {
					null -> null
					is Int -> value
					is String -> value
					else -> invalidValueTypeError()
				}

			is GInputObjectType ->
				when (value) {
					null -> null
					is Map<*, *> -> {

						type.arguments.values.associate { argument ->
							pathBuilder.withFieldName(argument.name) {
								val argumentValue = value.getOrElseNullable(argument.name) {
									(argument.defaultValue
										?.mapValue { it.coerce(argument.type) }
										?: invalidValueError("Missing field '${argument.name}' in value for input object '$type'."))
										?.value
								}

								argument.name to argumentValue?.coerce(
									childType = argument.type,
									isNewArgument = true,
									defaultValue = argument.defaultValue
								)
							}
						}
					}
					else -> invalidValueTypeError()
				}

			is GIntType ->
				when (value) {
					null -> null
					is Int -> value
					else -> invalidValueTypeError()
				}

			is GListType ->
				when (value) {
					null -> null
					is Iterable<*> -> value.mapIndexed { index, element ->
						pathBuilder.withListIndex(index) {
							element.coerce(type.ofType)
						}
					}
					else -> listOf(value.coerce(type.ofType))
				}

			is GNonNullType ->
				when (value) {
					null -> invalidValueTypeError()
					else -> value.coerce(type.ofType)
				}

			is GStringType ->
				when (value) {
					null -> null
					is String -> value
					else -> invalidValueTypeError()
				}

			is GInterfaceType,
			is GObjectType,
			is GUnionType ->
				error("Variable '$pathBuilder' must have an input type but has output type '$type'.")
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#CoerceVariableValues()
	fun coerceVariableValues(
		schema: GSchema,
		operation: GOperationDefinition,
		variableValues: Map<String, Any?>
	): GResult<Map<String, Any?>> = GResult {
		val pathBuilder = GPath.Builder()

		operation.variableDefinitions.associate { variable ->
			pathBuilder.withFieldName(variable.name) {
				val variableType = schema.resolveType(variable.type)
					?: return@associate variable.name to collectError(GError(
						message = "Variable '${variable.name}' references unknown type '${variable.type}'.",
						path = pathBuilder.snapshot()
					))

				if (!variableType.isInputType()) {
					return@associate variable.name to collectError(GError(
						message = "Variable '${variable.name}' expects a value of type '$variableType' which cannot be used as an input type.",
						path = pathBuilder.snapshot()
					))
				}

				val variableValue = variableValues[variable.name]
				if (variableValue === null)
					if (variable.defaultValue === null)
						variable.name to collectError(GError(
							message = "Missing variable '${variable.name}' of type '${variable.type}'.",
							path = pathBuilder.snapshot()
						))
					else
						variable.name to coerceArgumentValue(
							value = variable.defaultValue,
							type = variableType,
							pathBuilder = pathBuilder
						).orNull()
				else
					variable.name to coerceVariableValue(
						value = variableValue,
						type = variableType,
						pathBuilder = pathBuilder
					).orNull()
			}
		}
	}


	companion object {

		val default = object : GValueCoercer {}
	}
}
