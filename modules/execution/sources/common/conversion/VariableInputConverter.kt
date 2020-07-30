package io.fluidsonic.graphql


// FIXME If we stick with the context.next() approach we may as well drop GResult internally and use only exceptions.
// http://spec.graphql.org/draft/#CoerceVariableValues()
internal object VariableInputConverter {

	private fun coerceValue(value: Any?, type: GType, context: Context): GResult<Any?> {
		if (!context.hasValue)
			return coerceValueAbsence(
				defaultValue = (context.argumentDefinition ?: context.variableDefinition).defaultValue,
				type = type,
				context = context
			)

		return when (value) {
			null -> when (type) {
				is GNonNullType -> return context.invalidValueResult()
				else -> GResult.success()
			}

			else -> when (type) {
				is GListType -> coerceValueForList(value = value, type = type, context = context)
				is GNonNullType -> coerceValueForNonNull(value = value, type = type, context = context)
				is GNamedType -> when (type) {
					is GEnumType -> coerceValueForEnum(value = value, type = type, context = context)
					is GInputObjectType -> coerceValueForInputObject(value, type = type, context = context)
					is GScalarType -> coerceValueForScalar(value, type = type, context = context)
					is GCompositeType -> validationError(
						message = "${type.kind.toString().capitalize()} '${type.name}' is not an input type.",
						variableDefinition = context.variableDefinition,
						argumentDefinition = context.argumentDefinition
					)
				}
			}
		}
	}


	private fun coerceValueAbsence(defaultValue: GValue?, type: GType, context: Context): GResult<Any?> =
		defaultValue
			.ifNull { return context.invalidValueResult() }
			.let { value ->
				context.execution.nodeInputConverter.convertValue(
					value = value,
					type = type,
					parentNode = context.argumentDefinition ?: context.variableDefinition,
					executorContext = context.execution
				)
			}


	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForEnum(value: Any, type: GEnumType, context: Context): GResult<Any?> =
		when (val coercer = type.variableInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
			null ->
				when (value) {
					is String -> type.value(value)?.name?.let { GResult.success(it) }
					else -> null
				} ?: context.invalidValueResult(
					details = "valid values: ${type.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }}"
				)

			else -> coerceValueWithCoercer(coercer = coercer as GVariableInputCoercer<Any?>, context = context.copy(isUsingCoercerProvidedByType = true))
		}


	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForInputObject(value: Any, type: GInputObjectType, context: Context): GResult<Any?> = when (value) {
		is Map<*, *> -> type.argumentDefinitions
			.associate { argumentDefinition ->
				val argumentType = context.execution.schema.resolveType(argumentDefinition.type) ?: validationError(
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
			.flatten()
			.flatMapValue { argumentValues ->
				when (val coercer = type.variableInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
					null -> GResult.success(argumentValues)
					else -> coerceValueWithCoercer(coercer = coercer as GVariableInputCoercer<Any?>, context = context.copy(
						isUsingCoercerProvidedByType = true,
						value = argumentValues
					))
				}
			}

		else -> context.invalidValueResult()
	}


	private fun coerceValueForList(value: Any, type: GListType, context: Context): GResult<Any?> =
		when (value) {
			is Collection<*> -> value
				.mapIndexed { index, element ->
					convertValue(context = context.copy(
						path = context.path.addIndex(index),
						type = type.elementType,
						value = element
					))
				}
				.flatten()

			else -> convertValue(context = context.copy(type = type.elementType)).mapValue { listOf(it) }
		}


	@Suppress("UNUSED_PARAMETER")
	private fun coerceValueForNonNull(value: Any, type: GNonNullType, context: Context): GResult<Any?> =
		convertValue(context = context.copy(type = type.wrappedType))


	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForScalar(value: Any, type: GScalarType, context: Context): GResult<Any?> {
		return when (type) {
			GBooleanType -> when (value) {
				is Boolean -> value
				else -> return context.invalidValueResult()
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
				else -> return context.invalidValueResult()
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
				else -> return context.invalidValueResult()
			}

			GIntType -> when (value) {
				is Byte -> value.toInt()
				is Int -> value
				is Long -> value.toIntOrNull() ?: return context.invalidValueResult()
				is Short -> value.toInt()
				is UByte -> value.toInt()
				is UInt -> value.toIntOrNull() ?: return context.invalidValueResult()
				is ULong -> value.toIntOrNull() ?: return context.invalidValueResult()
				is UShort -> value.toInt()
				else -> return context.invalidValueResult()
			}

			GStringType -> when (value) {
				is String -> value
				else -> return context.invalidValueResult()
			}

			else -> when (val coercer = type.variableInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
				null -> value
				else -> return coerceValueWithCoercer(
					coercer = coercer as GVariableInputCoercer<Any?>,
					context = context.copy(isUsingCoercerProvidedByType = true)
				)
			}
		}.let { GResult.success(it) }
	}


	private fun coerceValueWithCoercer(coercer: GVariableInputCoercer<Any?>, context: Context): GResult<Any?> =
		GResult.catchErrors {
			with(coercer) { context.coerceVariableInput(context.value) }
		}


	fun convertValues(values: Map<String, Any?>, operation: GOperationDefinition, context: DefaultExecutorContext): GResult<Map<String, Any?>> =
		when {
			operation.variableDefinitions.isEmpty() ->
				GResult.success(emptyMap())

			else -> operation.variableDefinitions
				.associate { variableDefinition ->
					val variableType = context.schema.resolveType(variableDefinition.type) ?: validationError(
						message = "Type '$variableDefinition.type' cannot be resolved.",
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
				.flatten()
		}


	private fun convertValue(context: Context): GResult<Any?> =
		when (val coercer = context.execution.variableInputCoercer) {
			null -> coerceValue(value = context.value, type = context.type, context = context)
			else -> coerceValueWithCoercer(coercer = coercer, context = context)
		}


	private fun validationError(
		message: String,
		variableDefinition: GVariableDefinition,
		argumentDefinition: GArgumentDefinition?
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
		val path: GPath,
		override val variableDefinition: GVariableDefinition,
		override val type: GType,
		val value: Any?
	) : GVariableInputCoercerContext {

		override fun invalidValueError(details: String?) =
			makeInvalidValueError(details = details).throwException()


		fun invalidValueResult(details: String? = null): GResult<Nothing?> =
			GResult.failure(makeInvalidValueError(details = details))


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
