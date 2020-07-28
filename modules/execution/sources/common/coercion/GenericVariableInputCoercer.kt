package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#CoerceVariableValues()
@OptIn(ExperimentalUnsignedTypes::class)
internal object GenericVariableInputCoercer {

	private fun coerceValue(value: Any?, type: GType, context: Context): GResult<Any?> {
		return when (value) {
			NoValue -> coerceValueAbsence(
				defaultValue = (context.argumentDefinition ?: context.variableDefinition).defaultValue,
				type = type,
				context = context
			)

			null -> when (type) {
				is GNonNullType -> return context.invalidValueResult(value = value, type = type)
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


	private fun <Value : Any> coerceValue(value: Value, coercer: GVariableInputCoercer<Value>, context: Context): GResult<Any?> =
		GResult.catch { coercer(context.External(value = value), value) }


	private fun coerceValueAbsence(defaultValue: GValue?, type: GType, context: Context): GResult<Any?> =
		defaultValue
			.ifNull { return context.missingValueResult() }
			.let { value ->
				context.executorContext.nodeInputCoercer.coerceValue(
					value = value,
					type = type,
					parentNode = context.argumentDefinition ?: context.variableDefinition,
					executorContext = context.executorContext
				)
			}


	private fun coerceValueForEnum(value: Any, type: GEnumType, context: Context): GResult<Any?> =
		when (val coercer = type.variableInputCoercer) {
			null ->
				when (value) {
					is String -> type.values.firstOrNull { it.name == value }?.name?.let { GResult.success(it) }
					else -> null
				} ?: context.invalidValueResult(
					value = value,
					type = type,
					details = "valid values: ${type.values.sortedBy { it.name }.joinToString(separator = ", ") { "\"${it.name}\"" }}"
				)

			else -> coerceValue(value, coercer = coercer, context = context)
		}


	private fun coerceValueForInputObject(value: Any, type: GInputObjectType, context: Context): GResult<Any?> =
		when (value) {
			is Map<*, *> -> type.argumentDefinitions
				.associate { argumentDefinition ->
					val argumentType = context.executorContext.schema.resolveType(argumentDefinition.type) ?: validationError(
						message = "Type '${argumentDefinition.type}' cannot be resolved.",
						variableDefinition = context.variableDefinition,
						argumentDefinition = argumentDefinition
					)

					argumentDefinition.name to coerceValue(
						value = value.getOrElseNullable(argumentDefinition) { NoValue },
						type = argumentType,
						context = context.copy(
							argumentDefinition = argumentDefinition,
							fullType = argumentType,
							path = context.path.addName(argumentDefinition.name)
						)
					)
				}
				.flatten()
				.flatMapValue { arguments ->
					when (val coercer = type.variableInputCoercer) {
						null -> GResult.success(arguments)
						else -> coerceValue(arguments, coercer = coercer, context = context)
					}
				}

			else -> context.invalidValueResult(value, type = type)
		}


	private fun coerceValueForList(value: Any, type: GListType, context: Context): GResult<Any?> =
		when (value) {
			is Collection<*> -> value
				.mapIndexed { index, element ->
					coerceValue(element, type = type.elementType, context = context.copy(path = context.path.addIndex(index)))
				}
				.flatten()

			else -> coerceValue(value, type = type.elementType, context = context).mapValue(::listOf)
		}


	private fun coerceValueForNonNull(value: Any, type: GNonNullType, context: Context): GResult<Any?> =
		coerceValue(value, type = type.wrappedType, context = context)


	private fun coerceValueForScalar(value: Any, type: GScalarType, context: Context): GResult<Any?> {
		return when (type) {
			GBooleanType -> when (value) {
				is Boolean -> value
				else -> return context.invalidValueResult(value, type = type)
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
				else -> return context.invalidValueResult(value, type = type)
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
				else -> return context.invalidValueResult(value, type = type)
			}

			GIntType -> when (value) {
				is Byte -> value.toInt()
				is Int -> value
				is Long -> value.toIntOrNull() ?: return context.invalidValueResult(value, type = type)
				is Short -> value.toInt()
				is UByte -> value.toInt()
				is UInt -> value.toIntOrNull() ?: return context.invalidValueResult(value, type = type)
				is ULong -> value.toIntOrNull() ?: return context.invalidValueResult(value, type = type)
				is UShort -> value.toInt()
				else -> return context.invalidValueResult(value, type = type)
			}

			GStringType -> when (value) {
				is String -> value
				else -> return context.invalidValueResult(value, type = type)
			}

			else -> when (val coercer = type.variableInputCoercer) {
				null -> value
				else -> return coerceValue(value, coercer = coercer, context = context)
			}
		}.let { GResult.success(it) }
	}


	fun coerceValues(values: Map<String, Any?>, operation: GOperationDefinition, context: DefaultExecutorContext): GResult<Map<String, Any?>> =
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

					variableDefinition.name to coerceValue(
						value = values.getOrElseNullable(variableDefinition.name) { NoValue },
						type = variableType,
						context = Context(
							argumentDefinition = null,
							fullType = variableType,
							path = GPath.ofName(variableDefinition.name),
							variableDefinition = variableDefinition,
							executorContext = context
						)
					)
				}
				.flatten()
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
		val argumentDefinition: GArgumentDefinition?,
		val fullType: GType,
		val path: GPath,
		val variableDefinition: GVariableDefinition,
		val executorContext: DefaultExecutorContext
	) {

		fun invalidValueResult(value: Any?, type: GType, details: String? = null): GResult<Nothing?> =
			GResult.failure(makeInvalidValueError(value = value, type = type, details = details))


		private fun makeInvalidValueError(value: Any?, type: GType, details: String?) = GError(
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

				val typeRef = GTypeRef(type)
				val fullTypeRef = GTypeRef(fullType)
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


		fun missingValueResult(): GResult<Nothing?> =
			GResult.failure(GError(
				message = buildString {
					append("A value must be provided for ")
					append("variable '")
					append(path.toString())
					append("' of type '")
					append(fullType.name)
					append("'.")
				},
				nodes = listOf(variableDefinition)
			))


		inner class External(
			val value: Any?
		) : GVariableInputCoercerContext, GExecutorContext by executorContext {

			override val argumentDefinition
				get() = this@Context.argumentDefinition


			override val variableDefinition
				get() = this@Context.variableDefinition


			override val type
				get() = fullType.underlyingNamedType


			override fun invalidValueError(details: String?): Nothing =
				throw makeInvalidValueError(value = value, type = type, details = details)
		}
	}
}
