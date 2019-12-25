package io.fluidsonic.graphql


interface GValueCoercer {

	// FIXME not a good name
	fun coerceArgumentValue(
		value: GValue,
		typeRef: GTypeRef,
		schema: GSchema,
		nonNullTypeRef: GNonNullTypeRef? = typeRef as? GNonNullTypeRef,
		rootTypeRef: GTypeRef = typeRef,
		defaultValue: Any? = null,
		variableValues: Map<String, Any?> = emptyMap(),
		pathBuilder: GPath.Builder = GPath.Builder()
	): GResult<Any?> = GResult {

		fun GValue.coerce(
			childTypeRef: GTypeRef,
			isNewArgument: Boolean = false,
			defaultValue: Any? = null
		) =
			coerceArgumentValue(
				value = this,
				typeRef = childTypeRef,
				schema = schema,
				nonNullTypeRef = typeRef as? GNonNullTypeRef,
				rootTypeRef = if (isNewArgument) childTypeRef else rootTypeRef,
				defaultValue = defaultValue,
				variableValues = variableValues,
				pathBuilder = pathBuilder
			).orNull()

		fun GValue.Variable.resolve() =
			variableValues.getOrElseNullable(name) {
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
			invalidValueError("Unexpected '${value.type}' for argument of type '${rootTypeRef}'.")


		when (typeRef) {
			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
			is GListTypeRef ->
				when (value) {
					is GValue.List -> value.elements.mapIndexed { index, element ->
						pathBuilder.withListIndex(index) {
							element.coerce(typeRef.elementType)
						}
					}
					is GValue.Null -> null
					is GValue.Variable -> value.resolve()

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.Object,
					is GValue.String -> listOf(value.coerce(typeRef.elementType))
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
			is GNonNullTypeRef ->
				when (value) {
					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.List,
					is GValue.Object,
					is GValue.String -> value.coerce(typeRef.nullableType)

					is GValue.Null -> defaultValue
						?: invalidValueTypeError()

					is GValue.Variable -> value.resolve()
				}

			is GNamedTypeRef -> {
				val type = schema.resolveType(typeRef)
					?: error("FIXME validation message: $typeRef") // FIXME

				when (type) {
					// https://graphql.github.io/graphql-spec/draft/#sec-Boolean.Input-Coercion
					is GBooleanType ->
						when (value) {
							is GValue.Boolean -> value.value
							is GValue.Null -> defaultValue
							is GValue.Variable -> value.resolve()

							is GValue.Enum,
							is GValue.Float,
							is GValue.Int,
							is GValue.List,
							is GValue.Object,
							is GValue.String -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-Scalars.Input-Coercion
					is GCustomScalarType ->
						// FIXME support conversion function
						when (value) {
							is GValue.Boolean -> value.value
							is GValue.Float -> value.value
							is GValue.Int -> value.value
							is GValue.Null -> defaultValue
							is GValue.String -> value.value
							is GValue.Variable -> value.resolve()

							is GValue.Enum,
							is GValue.List,
							is GValue.Object -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-Enums.Input-Coercion
					is GEnumType ->
						when (value) {
							is GValue.Enum -> type.valuesByName[value.name]
								?: invalidValueError("Invalid value '${value.name}' for enum '$type'.")
							is GValue.Null -> defaultValue
							is GValue.Variable -> value.resolve()

							is GValue.Boolean,
							is GValue.Float,
							is GValue.Int,
							is GValue.List,
							is GValue.Object,
							is GValue.String -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-Float.Input-Coercion
					is GFloatType ->
						when (value) {
							is GValue.Float -> value.value
							is GValue.Int -> value.value.toFloat()
							is GValue.Null -> defaultValue
							is GValue.Variable -> value.resolve()

							is GValue.Boolean,
							is GValue.Enum,
							is GValue.List,
							is GValue.Object,
							is GValue.String -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-ID.Input-Coercion
					is GIDType ->
						when (value) {
							is GValue.Int -> value.value
							is GValue.Null -> defaultValue
							is GValue.String -> value.value
							is GValue.Variable -> value.resolve()

							is GValue.Boolean,
							is GValue.Enum,
							is GValue.Float,
							is GValue.List,
							is GValue.Object -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercion
					is GInputObjectType ->
						when (value) {
							is GValue.Null -> defaultValue
							is GValue.Object -> {
								type.arguments.associate { argument ->
									pathBuilder.withFieldName(argument.name) {
										val argumentValue = value.fieldsByName[argument.name]
											?: invalidValueError("Missing field '${argument.name}' in value for input object '$type'.")

										argument.name to argumentValue?.coerce(
											childTypeRef = argument.type,
											isNewArgument = true,
											defaultValue = argument.defaultValue
										)
									}
								}
							}
							is GValue.Variable -> value.resolve()

							is GValue.Boolean,
							is GValue.Enum,
							is GValue.Float,
							is GValue.Int,
							is GValue.List,
							is GValue.String -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-Int.Input-Coercion
					is GIntType ->
						when (value) {
							is GValue.Int -> value.value
							is GValue.Null -> defaultValue
							is GValue.Variable -> value.resolve()

							is GValue.Boolean,
							is GValue.Enum,
							is GValue.Float,
							is GValue.List,
							is GValue.Object,
							is GValue.String -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-String.Input-Coercion
					is GStringType ->
						when (value) {
							is GValue.Null -> defaultValue
							is GValue.String -> value.value
							is GValue.Variable -> value.resolve()

							is GValue.Boolean,
							is GValue.Enum,
							is GValue.Float,
							is GValue.Int,
							is GValue.List,
							is GValue.Object -> invalidValueTypeError()
						}

					is GInterfaceType,
					is GObjectType,
					is GUnionType ->
						error("Argument '$pathBuilder' must have an input type but has output type '$typeRef'.")
				}
			}
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

		fieldDefinition.arguments.associate { argument ->
			pathBuilder.withFieldName(argument.name) {
				val argumentValue = argumentValues[argument.name]
				if (argumentValue === null && argument.defaultValue === null)
					argument.name to collectError(GError(
						message = "Missing argument '${argument.name}' of type '${argument.type}' for field '${fieldDefinition.name}'.",
						path = pathBuilder.snapshot()
					))
				else
					argument.name to coerceArgumentValue(
						value = argumentValue ?: argument.defaultValue!!,
						typeRef = argument.type,
						schema = context.schema,
						defaultValue = argument.defaultValue,
						variableValues = context.variableValues,
						pathBuilder = pathBuilder
					).orNull()
			}
		}
	}


	// FIXME not a good name
	fun coerceVariableValue(
		value: Any?,
		typeRef: GTypeRef,
		schema: GSchema,
		nonNullTypeRef: GNonNullTypeRef? = typeRef as? GNonNullTypeRef,
		rootTypeRef: GTypeRef = typeRef,
		pathBuilder: GPath.Builder = GPath.Builder()
	): GResult<Any?> = GResult {

		fun Any?.coerce(
			childTypeRef: GTypeRef,
			isNewArgument: Boolean = false
		) =
			coerceVariableValue(
				value = this,
				typeRef = childTypeRef,
				schema = schema,
				nonNullTypeRef = typeRef as? GNonNullTypeRef,
				rootTypeRef = if (isNewArgument) childTypeRef else rootTypeRef,
				pathBuilder = pathBuilder
			).orNull()

		fun invalidValueError(message: String, cause: Throwable? = null) =
			collectError(GError(
				message = message,
				path = pathBuilder.snapshot(),
				cause = cause
			))

		fun invalidValueTypeError() =
			invalidValueError("Unexpected value for argument of type '${rootTypeRef}'.")


		when (typeRef) {
			is GListTypeRef ->
				when (value) {
					null -> null
					is Iterable<*> -> value.mapIndexed { index, element ->
						pathBuilder.withListIndex(index) {
							element.coerce(typeRef.elementType)
						}
					}
					else -> listOf(value.coerce(typeRef.elementType))
				}

			is GNonNullTypeRef ->
				when (value) {
					null -> invalidValueTypeError()
					else -> value.coerce(typeRef.nullableType)
				}

			is GNamedTypeRef -> {
				val type = schema.resolveType(typeRef)
					?: error("FIXME validation message") // FIXME

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
							is String -> type.valuesByName[value] ?: invalidValueError("Invalid value '${value}' for enum '$type'.")
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

								type.arguments.associate { argument ->
									pathBuilder.withFieldName(argument.name) {
										// FIXME conversion
										val argumentValue = value.getOrElseNullable(argument.name) {
											argument.defaultValue
												?: invalidValueError("Missing field '${argument.name}' in value for input object '$type'.")
										}

										argument.name to argumentValue?.coerce(
											childTypeRef = argument.type,
											isNewArgument = true
											//defaultValue = argument.defaultValue // FIXME
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

					is GStringType ->
						when (value) {
							null -> null
							is String -> value
							else -> invalidValueTypeError()
						}

					is GInterfaceType,
					is GObjectType,
					is GUnionType ->
						error("Variable '$pathBuilder' must have an input type but has output type '$typeRef'.")
				}
			}
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
				val variableType = schema.resolveType(variable.type.underlyingName)
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
							typeRef = variable.type,
							schema = schema,
							pathBuilder = pathBuilder
						).orNull()
				else
					variable.name to coerceVariableValue(
						value = variableValue,
						typeRef = variable.type,
						schema = schema,
						pathBuilder = pathBuilder
					).orNull()
			}
		}
	}


	companion object {

		val default = object : GValueCoercer {}
	}
}
