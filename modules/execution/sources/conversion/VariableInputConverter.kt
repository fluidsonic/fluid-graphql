package io.fluidsonic.graphql


// http://spec.graphql.org/draft/#CoerceVariableValues()
internal object VariableInputConverter {

	private fun coerceValue(value: Any?, type: GType, context: Context): Any? {
		if (!context.hasValue)
			return coerceValueAbsence(
				defaultValue = (context.argumentDefinition ?: context.variableDefinition).defaultValue,
				type = type,
				context = context
			)

		return when (value) {
			null -> when (type) {
				is GNonNullType -> context.invalid()
				else -> null
			}

			else -> when (type) {
				is GListType -> coerceValueForList(value = value, type = type, context = context)
				is GNonNullType -> coerceValueForNonNull(value = value, type = type, context = context)
				is GNamedType -> when (type) {
					is GEnumType -> coerceValueForEnum(value = value, type = type, context = context)
					is GInputObjectType -> coerceValueForInputObject(value, type = type, context = context)
					is GScalarType -> coerceValueForScalar(value, type = type, context = context)
					is GCompositeType -> validationError(
						message = "${type.kind.toString().replaceFirstChar { it.uppercase() }} '${type.name}' is not an input type.",
						variableDefinition = context.variableDefinition,
						argumentDefinition = context.argumentDefinition
					)
				}
			}
		}
	}


	private fun coerceValueAbsence(defaultValue: GValue?, type: GType, context: Context): Any? {
		return defaultValue
			.ifNull {
				when (type) {
					is GNonNullType -> when (context.argumentDefinition?.isRequired()) {
						false -> return NoValue
						true, null -> context.invalid()
					}

					else -> return NoValue
				}
			}
			.let { value ->
				context.execution.nodeInputConverter.convertValue(
					value = value,
					type = type,
					parentNode = context.argumentDefinition ?: context.variableDefinition,
					context = context.execution
				).valueOrThrow()
			}
	}


	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForEnum(value: Any, type: GEnumType, context: Context): Any? =
		when (val coercer = type.variableInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
			null -> (value as? String)
				?.let { type.value(it) }
				?.name
				?: context.invalid(details = "valid values: ${type.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }}")

			else -> coerceValueWithCoercer(coercer = coercer as GVariableInputCoercer<Any?>, context = context.copy(isUsingCoercerProvidedByType = true))
		}


	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForInputObject(value: Any, type: GInputObjectType, context: Context): Any? = when (value) {
		is Map<*, *> -> type.argumentDefinitions
			.associate { argumentDefinition ->
				val argumentType = TypeResolver.resolveType(context.execution.schema, argumentDefinition.type) ?: validationError(
					message = "Type '${argumentDefinition.type}' cannot be resolved.",
					variableDefinition = context.variableDefinition,
					argumentDefinition = argumentDefinition
				)
				val argumentValue = value[argumentDefinition.name]

				argumentDefinition.name to convertValue(context = context.copy(
					argumentDefinition = argumentDefinition,
					fullType = argumentType,
					fullValue = argumentValue,
					hasValue = value.containsKey(argumentDefinition.name),
					path = context.path.addName(argumentDefinition.name),
					type = argumentType,
					value = argumentValue
				))
			}
			.filterValues { it != NoValue }
			.let { argumentValues ->
				when (val coercer = type.variableInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
					null -> argumentValues
					else -> coerceValueWithCoercer(coercer = coercer as GVariableInputCoercer<Any?>, context = context.copy(
						isUsingCoercerProvidedByType = true,
						value = argumentValues
					))
				}
			}

		else -> context.invalid()
	}


	private fun coerceValueForList(value: Any, type: GListType, context: Context): List<Any?> =
		when (value) {
			is Collection<*> -> value
				.mapIndexed { index, element ->
					convertValue(context = context.copy(
						path = context.path.addIndex(index),
						type = type.elementType,
						value = element
					))
				}

			else -> listOf(convertValue(context = context.copy(type = type.elementType)))
		}


	@Suppress("UNUSED_PARAMETER")
	private fun coerceValueForNonNull(value: Any, type: GNonNullType, context: Context): Any? =
		convertValue(context = context.copy(type = type.wrappedType))


	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForScalar(value: Any, type: GScalarType, context: Context): Any? {
		return when (type) {
			GBooleanType -> when (value) {
				is Boolean -> value
				else -> context.invalid()
			}

			GFloatType -> when (value) {
				is Byte -> value.toDouble()
				is Double -> value
				is Float -> value.toDouble()
				is Int -> value.toDouble()
				is Long -> value.toDouble()
				is Short -> value.toDouble()
				is UByte -> value.toDouble()
				is UInt -> value.toDouble()
				is ULong -> value.toDouble()
				is UShort -> value.toDouble()
				else -> context.invalid()
			}

			GIdType -> when (value) {
				is Byte -> value.toString()
				is Int -> value.toString()
				is Long -> value.toString()
				is Short -> value.toString()
				is String -> value
				is UByte -> value.toString()
				is UInt -> value.toString()
				is ULong -> value.toString()
				is UShort -> value.toString()
				else -> context.invalid()
			}

			GIntType -> when (value) {
				is Byte -> value.toInt()
				is Int -> value
				is Long -> value.toIntOrNull() ?: context.invalid()
				is Short -> value.toInt()
				is UByte -> value.toInt()
				is UInt -> value.toIntOrNull() ?: context.invalid()
				is ULong -> value.toIntOrNull() ?: context.invalid()
				is UShort -> value.toInt()
				else -> context.invalid()
			}

			GStringType -> when (value) {
				is String -> value
				else -> context.invalid()
			}

			else -> when (val coercer = type.variableInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
				null -> value
				else -> return coerceValueWithCoercer(
					coercer = coercer as GVariableInputCoercer<Any?>,
					context = context.copy(isUsingCoercerProvidedByType = true)
				)
			}
		}
	}


	private fun coerceValueWithCoercer(coercer: GVariableInputCoercer<Any?>, context: Context): Any? =
		context.execution.withExceptionHandler(origin = { GExceptionOrigin.VariableInputCoercer(coercer = coercer, context = context) }) {
			with(coercer) { context.coerceVariableInput(context.value) }
		}


	fun convertValues(values: Map<String, Any?>, operation: GOperationDefinition, context: DefaultExecutorContext): GResult<Map<String, Any?>> =
		when {
			operation.variableDefinitions.isEmpty() ->
				GResult.success(emptyMap())

			else -> GResult.catchErrors {
				operation.variableDefinitions
					.associate { variableDefinition ->
						val variableType = TypeResolver.resolveType(context.schema, variableDefinition.type) ?: validationError(
							message = "Type '${variableDefinition.type}' cannot be resolved.",
							variableDefinition = variableDefinition,
							argumentDefinition = null
						)
						val variableValue = values[variableDefinition.name]

						variableDefinition.name to convertValue(context = Context(
							argumentDefinition = null,
							execution = context,
							hasValue = values.containsKey(variableDefinition.name),
							fullType = variableType,
							fullValue = variableValue,
							isUsingCoercerProvidedByType = false,
							path = GPath.ofName(variableDefinition.name),
							variableDefinition = variableDefinition,
							type = variableType,
							value = variableValue
						))
					}
					.filterValues { it != NoValue }
			}
		}


	private fun convertValue(context: Context): Any? =
		when (val coercer = context.execution.variableInputCoercer) {
			null -> coerceValue(value = context.value, type = context.type, context = context)
			else -> coerceValueWithCoercer(coercer = coercer, context = context)
		}


	private fun validationError(
		message: String,
		variableDefinition: GVariableDefinition,
		argumentDefinition: GArgumentDefinition?,
	): Nothing =
		error(buildString {
			append("There is an error in the document. It should be validated before use:\n")
			append(message)

			if (argumentDefinition != null) {
				append("\n\nArgument:\n")
				append(argumentDefinition)
				argumentDefinition.origin?.let { origin ->
					append("\n\n")
					append(origin.describe())
				}
			}

			append("\n\nVariable:\n")
			append(variableDefinition)
			variableDefinition.origin?.let { origin ->
				append("\n\n")
				append(origin.describe())
			}
		})


	private data class Context(
		override val argumentDefinition: GArgumentDefinition?,
		override val execution: DefaultExecutorContext,
		override val hasValue: Boolean,
		val fullType: GType,
		val fullValue: Any?,
		val isUsingCoercerProvidedByType: Boolean,
		override val path: GPath,
		override val variableDefinition: GVariableDefinition,
		override val type: GType,
		val value: Any?,
	) : GVariableInputCoercerContext {

		override fun invalid(details: String?) =
			makeInvalidValueError(details = details).throwException()


		private fun makeInvalidValueError(details: String?): GError {
			if (!hasValue)
				return GError(
					message = buildString {
						append("A value must be provided for ")
						append("variable '")
						append(path.toString())
						append("' of type '")
						append(fullType.name)
						append("'.")
					},
					nodes = listOf(variableDefinition)
				)

			return GError(
				message = buildString {
					append(when (value) {
						null -> "Null value"
						is Byte -> "Byte value"
						is Double -> "Double value"
						is Float -> "Float value"
						is Int -> "Int value"
						is List<*> -> "List value"
						is Collection<*> -> "Collection value"
						is Long -> "Long value"
						is Map<*, *> -> "Map value"
						is Short -> "Short value"
						is UByte -> "UByte value"
						is UInt -> "UInt value"
						is ULong -> "ULong value"
						is UShort -> "UShort value"
						is String -> "String value"
						else -> "Value"
					})
					append(" is not valid for ")

					val typeRef = type.toRef()
					val fullTypeRef = fullType.toRef()
					if (typeRef != fullTypeRef) {
						append("type '")
						append(typeRef)
						append("' in ")
					}

					append("variable '")
					append(path.toString())
					append("' with type '")
					append(fullTypeRef)
					append("'")

					if (details != null) {
						append(" (")
						append(details)
						append(")")
					}

					append(".")
				},
				nodes = listOf(variableDefinition)
			)
		}


		override fun next(): Any? =
			coerceValue(value = value, type = type, context = this)
	}
}
