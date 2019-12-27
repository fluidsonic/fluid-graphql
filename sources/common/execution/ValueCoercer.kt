package io.fluidsonic.graphql


// Internal for now. Review API before making it public.
internal class ValueCoercer( // FIXME do we need this outside of GExecutor? if not then move some local functions below to this class
	private val document: GDocument,
	private val schema: GSchema,
	private val variableValues: Map<String, Any?> = emptyMap(),
	private val pathBuilder: GPath.Builder? = null // FIXME may not work as class property with later parallelization
) {

	private fun coerceArgumentValue(
		value: GValue,
		typeRef: GTypeRef,
		rootTypeRef: GTypeRef = typeRef
	): GResult<Any?> = GResult {

		val _rootTypeRef = rootTypeRef // https://youtrack.jetbrains.com/issue/KT-35677

		fun GValue.coerceToType(
			typeRef: GTypeRef,
			rootTypeRef: GTypeRef = _rootTypeRef
		) =
			coerceArgumentValue(
				value = this,
				typeRef = typeRef,
				rootTypeRef = rootTypeRef
			).orNull()

		fun GValue.Variable.resolve() =
			variableValues.getOrElseNullable(name) {
				invalidOperationError("Variable '$$name' cannot be resolved.")
			}

		fun invalidValueError(message: String, cause: Throwable? = null) =
			collectError(GError(
				message = message,
				path = pathBuilder?.snapshot(),
				nodes = listOf(value),
				cause = cause
			))

		fun invalidValueTypeError() =
			invalidValueError("Unexpected '${value.kind}' for argument of type '${rootTypeRef}'.")


		when (typeRef) {
			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
			is GListTypeRef ->
				when (value) {
					is GValue.List -> value.elements.mapIndexed { index, element ->
						pathBuilder.withListIndex(index) {
							element.coerceToType(typeRef.elementType)
						}
					}
					is GValue.Null -> null
					is GValue.Variable -> value.resolve()

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.Object,
					is GValue.String -> listOf(value.coerceToType(typeRef.elementType))
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
			is GNonNullTypeRef ->
				when (value) {
					is GValue.Null -> invalidValueTypeError()

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.List,
					is GValue.Object,
					is GValue.String -> value.coerceToType(typeRef.nullableType)

					is GValue.Variable -> value.resolve()
				}

			is GNamedTypeRef -> {
				val type = schema.resolveType(typeRef)
					?: invalidOperationError("Type '$typeRef' cannot be resolved.")

				when (type) {
					// https://graphql.github.io/graphql-spec/draft/#sec-Boolean.Input-Coercion
					is GBooleanType ->
						when (value) {
							is GValue.Boolean -> value.value

							is GValue.Null -> null
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
							is GValue.String -> value.value

							is GValue.Null -> null
							is GValue.Variable -> value.resolve()

							is GValue.Enum,
							is GValue.List,
							is GValue.Object -> invalidValueTypeError()
						}

					// https://graphql.github.io/graphql-spec/draft/#sec-Enums.Input-Coercion
					is GEnumType ->
						when (value) {
							is GValue.Enum -> type.value(value.name)
								?: invalidValueError("Invalid value '${value.name}' for enum '${type.name}'.")

							is GValue.Null -> null
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

							is GValue.Null -> null
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
							is GValue.String -> value.value

							is GValue.Null -> null
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
							is GValue.Object ->
								type.argumentDefinitions.associate { argument ->
									val argumentValue = value.field(argument.name)?.value ?: argument.defaultValue
									if (argumentValue !== null)
										pathBuilder.withFieldName(argument.name) {
											argument.name to argumentValue.coerceToType(
												typeRef = argument.type,
												rootTypeRef = argument.type
											)
										}
									else
										argument.name to invalidValueError(
											"Missing field '${argument.name}' of type '${argument.type}' " +
												"in value for input object '${type.name}'."
										)
								}

							is GValue.Null -> null
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

							is GValue.Null -> null
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
							is GValue.String -> value.value

							is GValue.Null -> null
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
						invalidOperationError("Argument '$pathBuilder' must have an input type but has output type '${type.name}'.")
				}
			}
		}
	}


	// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
	fun coerceArgumentValues(
		arguments: List<GArgument>,
		argumentDefinitions: List<GArgumentDefinition>
	): GResult<Map<String, Any?>> = GResult {
		val argumentValues = arguments.associate { it.name to it.value }

		argumentDefinitions.associate { argument ->
			val argumentValue = argumentValues.getOrElseNullable(argument.name) { argument.defaultValue }
			if (argumentValue !== null)
				pathBuilder.withFieldName(argument.name) {
					argument.name to coerceArgumentValue(
						value = argumentValue,
						typeRef = argument.type
					).orNull()
				}
			else
				argument.name to collectError(GError(
					message = "Missing argument '${argument.name}' of type '${argument.type}'.",
					path = pathBuilder?.snapshot()
				))
		}
	}


	private fun coerceKotlinValueToType(
		value: Any?,
		typeRef: GTypeRef,
		rootTypeRef: GTypeRef = typeRef
	): GResult<Any?> = GResult {

		val _rootTypeRef = rootTypeRef

		fun Any?.coerce(
			typeRef: GTypeRef,
			rootTypeRef: GTypeRef = _rootTypeRef
		) =
			coerceKotlinValueToType(
				value = this,
				typeRef = typeRef,
				rootTypeRef = rootTypeRef
			).orNull()

		fun invalidValueError(message: String, cause: Throwable? = null) =
			collectError(GError(
				message = message,
				path = pathBuilder?.snapshot(),
				cause = cause
			))

		fun invalidValueTypeError() =
			invalidValueError("Unexpected value for argument of type '${rootTypeRef}'.")


		when (typeRef) {
			is GListTypeRef ->
				when (value) {
					is Iterable<*> -> value.mapIndexed { index, element ->
						pathBuilder.withListIndex(index) {
							element.coerce(typeRef.elementType)
						}
					}

					null -> null
					else -> listOf(value.coerce(typeRef.elementType))
				}

			is GNonNullTypeRef ->
				when (value) {
					null -> invalidValueTypeError()
					else -> value.coerce(typeRef.nullableType)
				}

			is GNamedTypeRef -> {
				val type = schema.resolveType(typeRef)
					?: invalidOperationError("Type '$typeRef' cannot be resolved.")

				when (type) {
					is GBooleanType ->
						when (value) {
							is Boolean -> value

							null -> null
							else -> invalidValueTypeError()
						}

					is GCustomScalarType ->
						// FIXME support conversion function
						when (value) {
							is Boolean -> value
							is Float -> value
							is Int -> value
							is String -> value

							null -> null
							else -> invalidValueTypeError()
						}

					is GEnumType ->
						when (value) {
							is String -> type.value(value)
								?: invalidValueError("Invalid value '${value}' for enum '${type.name}'.")

							null -> null
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
							is Map<*, *> ->
								type.argumentDefinitions.associate { argument ->
									val argumentValue = value.getOrElseNullable(argument.name) { argument.defaultValue }
									if (argumentValue !== null)
										pathBuilder.withFieldName(argument.name) {
											argument.name to argumentValue.coerce(
												typeRef = argument.type,
												rootTypeRef = argument.type
											)
										}
									else
										argument.name to invalidValueError("Missing field '${argument.name}' in value for input object '$type'.")
								}

							null -> null
							else -> invalidValueTypeError()
						}

					is GIntType ->
						when (value) {
							is Int -> value

							null -> null
							else -> invalidValueTypeError()
						}

					is GStringType ->
						when (value) {
							is String -> value

							null -> null
							else -> invalidValueTypeError()
						}

					is GInterfaceType,
					is GObjectType,
					is GUnionType ->
						invalidOperationError("Variable '$pathBuilder' must have an input type but has output type '$typeRef'.")
				}
			}
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#CoerceVariableValues()
	fun coerceVariableValues(
		variableValues: Map<String, Any?>,
		variableDefinitions: List<GVariableDefinition>
	): GResult<Map<String, Any?>> = GResult {
		variableDefinitions.associate { variable ->
			val variableType = schema.resolveType(variable.type.underlyingName)
				?: invalidOperationError("Type '${variable.type}' of variable '$${variable.name}' cannot be resolved.")

			if (!variableType.isInputType())
				invalidOperationError("Variable '$${variable.name}' must have an input type but has output type '${variableType.name}'.")

			val variableValue = variableValues.getOrElse(variable.name) { variable.defaultValue }
			if (variableValue !== null)
				pathBuilder.withFieldName(variable.name) {
					variable.name to coerceKotlinValueToType(
						value = variableValue,
						typeRef = variable.type
					).orNull()
				}
			else
				variable.name to collectError(GError(
					message = "Missing variable '${variable.name}' of type '${variable.type}'.",
					path = pathBuilder?.snapshot()
				))
		}
	}


	private fun invalidOperationError(message: String, errors: List<GError> = emptyList()): Nothing =
		invalidOperationError(document = document, schema = schema, message = message, errors = errors)


	companion object {

		fun create(
			document: GDocument,
			schema: GSchema,
			variableDefinitions: List<GVariableDefinition> = emptyList(),
			variableValues: Map<String, Any?> = emptyMap(),
			pathBuilder: GPath.Builder? = null
		): GResult<ValueCoercer> = GResult {
			val variableCoercer = ValueCoercer(
				document = document,
				schema = schema
			)

			@Suppress("NAME_SHADOWING")
			val variableValues = variableCoercer.coerceVariableValues(
				variableValues = variableValues,
				variableDefinitions = variableDefinitions
			).or { return it }

			ValueCoercer(
				document = document,
				schema = schema,
				variableValues = variableValues,
				pathBuilder = pathBuilder
			)
		}
	}
}
