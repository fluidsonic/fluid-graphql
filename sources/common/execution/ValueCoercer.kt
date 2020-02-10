package io.fluidsonic.graphql


// Internal for now. Review API before making it public.
// FIXME refactor!
internal class ValueCoercer( // FIXME do we need this outside of Executor? if not then move some local functions below to that class
	private val document: GDocument,
	private val schema: GSchema,
	environment: Any,
	private val variableValues: Map<String, Any?> = emptyMap(),
	private val pathBuilder: GPath.Builder? = null // FIXME may not work as instance property with later parallelization
) {

	private val context = object : GValueConversionContext<Any> {

		override val environment = environment
		override val schema get() = this@ValueCoercer.schema
	}


	private fun coerceArgumentValue(
		value: GValue?,
		definition: GArgumentDefinition,
		typeRef: GTypeRef
	): GResult<Any?> = GResult {

		fun GValue.coerceToType(typeRef: GTypeRef) =
			coerceArgumentValue(
				value = this,
				definition = definition,
				typeRef = typeRef
			).orNull()

		fun invalidValueError(message: String, cause: Throwable? = null) =
			collectError(GError(
				message = message,
				path = pathBuilder?.snapshot(),
				nodes = value?.let(::listOf).orEmpty(),
				cause = cause
			))

		fun invalidValueTypeError() =
			invalidValueError("Unexpected ${value!!.kind} value for argument of type '${definition.type}'.")

		@Suppress("NAME_SHADOWING")
		var value = value

		if (value is GVariableRef) {
			if (variableValues.containsKey(value.name))
				return@GResult coerceArgumentVariableValue(
					value = variableValues[value.name],
					definition = definition,
					typeRef = typeRef
				)
			else
				value = null
		}

		if (value === null) {
			val defaultValue = definition.defaultValue
			if (defaultValue !== null)
				value = defaultValue
			else
				return failWith(GError(
					message = "Missing argument '${definition.name}' of type '${definition.type}'.",
					path = pathBuilder?.snapshot()
				))
		}

		if (value is GNullValue && typeRef is GNonNullTypeRef)
			return@GResult invalidValueTypeError()

		when (typeRef) {
			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
			is GListTypeRef ->
				when (value) {
					is GListValue ->
						value.elements.mapIndexed { index, element ->
							pathBuilder.withListIndex(index) {
								element.coerceToType(typeRef.elementType)
							}
						}

					is GNullValue -> null
					else -> listOf(value.coerceToType(typeRef.elementType))
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
			is GNonNullTypeRef ->
				when (value) {
					is GNullValue -> invalidValueTypeError()
					else -> value.coerceToType(typeRef.nullableRef) ?: invalidValueTypeError()
				}

			is GNamedTypeRef -> {
				val type = schema.resolveType(typeRef)
					?: invalidOperationError("Type '$typeRef' cannot be resolved.")

				when (type) {
					// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercion
					is GInputObjectType ->
						when (value) {
							is GObjectValue ->
								type.argumentDefinitions
									.associate { argument ->
										pathBuilder.withFieldName(argument.name) {
											argument.name to coerceArgumentValue(
												value = value.field(argument.name)?.value,
												definition = argument,
												typeRef = argument.type
											).orNull()
										}
									}
									.let { argumentValues ->
										with(type) {
											with(context) {
												parseValue(argumentValues)
											}
										}
									}

							is GNullValue -> null
							else -> invalidValueTypeError()
						}


					is GLeafType ->
						when (value) {
							is GNullValue -> null
							else ->
								with(context) {
									checkNotNull(type.parseValueNode)(value) // FIXME validation has to check that type is allowed for input
								}
						}

					is GCompositeType ->
						invalidOperationError("Argument '$pathBuilder' must have an input type but has output type '${type.name}'.")
				}
			}
		}
	}


	private fun coerceArgumentVariableValue(
		value: Any?,
		definition: GArgumentDefinition,
		typeRef: GTypeRef
	): GResult<Any?> = GResult {

		fun Any?.coerceToType(typeRef: GTypeRef) =
			coerceArgumentVariableValue(
				value = this,
				definition = definition,
				typeRef = typeRef
			).orNull()

		fun invalidValueError(message: String, cause: Throwable? = null) =
			collectError(GError(
				message = message,
				path = pathBuilder?.snapshot(),
				cause = cause
			))

		fun invalidValueTypeError() =
			invalidValueError("Variable value '$value' has incorrect type for argument of type '${definition.type}'.")

		when (typeRef) {
			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
			is GListTypeRef ->
				when (value) {
					is List<*> ->
						value.mapIndexed { index, element ->
							pathBuilder.withListIndex(index) {
								element.coerceToType(typeRef.elementType)
							}
						}

					null -> null
					else -> value.coerceToType(typeRef.elementType)
				}

			// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
			is GNonNullTypeRef ->
				when (value) {
					null -> invalidValueTypeError()
					else -> value.coerceToType(typeRef.nullableRef) ?: invalidValueTypeError()
				}

			is GNamedTypeRef -> {
				val type = schema.resolveType(typeRef)
					?: invalidOperationError("Type '$typeRef' cannot be resolved.")

				when (type) {
					// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercion
					is GInputObjectType ->
						when (value) {
							is Map<*, *> ->
								type.argumentDefinitions
									.associate { argument ->
										val defaultValue = argument.defaultValue

										when {
											value.containsKey(argument.name) ->
												pathBuilder.withFieldName(argument.name) {
													argument.name to value[argument.name].coerceToType(argument.type)
												}

											else ->
												pathBuilder.withFieldName(argument.name) {
													argument.name to coerceArgumentValue(
														value = defaultValue,
														definition = argument,
														typeRef = argument.type
													).orNull()
												}
										}
									}
									.let { argumentValues ->
										with(type) {
											with(context) {
												parseValue(argumentValues)
											}
										}
									}

							null -> null
							else -> invalidValueTypeError()
						}


					is GLeafType ->
						when (value) {
							null -> null
							else ->
								with(context) {
									checkNotNull(type.parseValue)(value) // FIXME validation has to check that type is allowed for input
								}
						}

					is GCompositeType ->
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
			pathBuilder.withFieldName(argument.name) {
				argument.name to coerceArgumentValue(
					value = argumentValues[argument.name],
					definition = argument,
					typeRef = argument.type
				).orNull()
			}
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
					else -> value.coerce(typeRef.nullableRef)
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

					is GIdType ->
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


	// FIXME use parsing functions
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
			environment: Any,
			variableDefinitions: List<GVariableDefinition> = emptyList(),
			variableValues: Map<String, Any?> = emptyMap(),
			pathBuilder: GPath.Builder? = null
		): GResult<ValueCoercer> = GResult {
			val variableCoercer = ValueCoercer(
				document = document,
				schema = schema,
				environment = environment
			)

			@Suppress("NAME_SHADOWING")
			val variableValues = variableCoercer.coerceVariableValues(
				variableValues = variableValues,
				variableDefinitions = variableDefinitions
			).or { return it }

			ValueCoercer(
				document = document,
				schema = schema,
				environment = environment,
				variableValues = variableValues,
				pathBuilder = pathBuilder
			)
		}
	}
}
