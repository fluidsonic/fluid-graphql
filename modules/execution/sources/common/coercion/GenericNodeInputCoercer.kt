package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
internal object GenericNodeInputCoercer {

	fun coerceArguments(
		node: GNode.WithArguments,
		definitions: Collection<GArgumentDefinition>,
		fieldSelectionPath: GPath,
		context: GExecutorContext
	): GResult<Map<String, Any?>> {
		if (definitions.isEmpty())
			return GResult.success(emptyMap())

		val parentNode = when (node) {
			is GDirective -> node
			is GFieldSelection -> node
			else -> error("'node' must be a directive or a field selection.")
		}

		return definitions.associate { argumentDefinition ->
			val argumentType = context.schema.resolveType(argumentDefinition.type) ?: validationError(
				message = "Type '${argumentDefinition.type}' cannot be resolved.",
				argumentDefinition = argumentDefinition
			)

			argumentDefinition.name to coerceValue(
				value = node.arguments.firstOrNull { it.name == argumentDefinition.name }?.value,
				type = argumentType,
				context = Context(
					argumentDefinition = argumentDefinition,
					fieldSelectionPath = fieldSelectionPath,
					fullType = argumentType,
					isDefaultValue = false,
					parentNode = parentNode,
					executorContext = context
				)
			)
		}.flatten()
	}


	private fun coerceValue(value: GValue?, type: GType, context: Context): GResult<Any?> = when (value) {
		null -> coerceValueAbsence(defaultValue = context.argumentDefinition?.defaultValue, type = type, context = context)
		is GVariableRef -> coerceVariableValue(value = value, type = type, context = context)
		is GNullValue -> when (type) {
			is GNonNullType -> context.invalidValueResult(value = value, type = type)
			else -> GResult.success()
		}
		else -> when (type) {
			is GListType -> coerceValueForList(value = value, type = type, context = context)
			is GNonNullType -> coerceValueForNonNull(value = value, type = type, context = context)
			is GNamedType -> when (type) {
				is GEnumType -> coerceValueForEnum(value, type = type, context = context)
				is GInputObjectType -> coerceValueForInputObject(value, type = type, context = context)
				is GScalarType -> coerceValueForScalar(value, type = type, context = context)
				is GCompositeType -> validationError(
					message = "${type.kind.toString().capitalize()} '${type.name}' is not an input type.",
					argumentDefinition = context.argumentDefinition
				)
			}
		}
	}


	fun coerceValue(value: GValue, type: GType, parentNode: GNode, executorContext: GExecutorContext): GResult<Any?> =
		coerceValue(
			value = value,
			type = type,
			context = Context(
				argumentDefinition = parentNode as? GArgumentDefinition,
				fieldSelectionPath = null,
				fullType = type,
				isDefaultValue = false,
				parentNode = parentNode,
				executorContext = executorContext
			)
		)


	private fun <Value> coerceValue(value: Value, uncoercedValue: GValue, coercer: GNodeInputCoercer<Value>, context: Context): GResult<Any?> =
		GResult.catch { coercer(context.External(value = uncoercedValue), value) }


	private fun coerceValueAbsence(defaultValue: GValue?, type: GType, context: Context): GResult<Any?> =
		defaultValue
			.ifNull { return context.missingValueResult() }
			.let { coerceValue(it, type = type, context = context.copy(isDefaultValue = true)) }


	private fun coerceValueForEnum(value: GValue, type: GEnumType, context: Context): GResult<Any?> =
		when (val coercer = type.nodeInputCoercer) {
			null ->
				when (value) {
					is GEnumValue -> type.values.firstOrNull { it.name == value.name }?.name?.let { GResult.success(it) }
					else -> null
				} ?: context.invalidValueResult(
					value = value,
					type = type,
					details = "valid values: ${type.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }}"
				)

			else -> coerceValue(value, uncoercedValue = value, coercer = coercer, context = context)
		}


	// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercion
	private fun coerceValueForInputObject(value: GValue, type: GInputObjectType, context: Context): GResult<Any?> {
		return when (value) {
			is GObjectValue -> type.argumentDefinitions
				.associate { argumentDefinition ->
					val argumentType = context.executorContext.schema.resolveType(argumentDefinition.type) ?: validationError(
						message = "Type '${argumentDefinition.type}' cannot be resolved.",
						argumentDefinition = argumentDefinition
					)

					argumentDefinition.name to coerceValue(
						value = value.argument(argumentDefinition.name)?.value,
						type = argumentType,
						context = context.copy(
							argumentDefinition = argumentDefinition,
							fullType = argumentType,
							parentNode = value
						)
					)
				}
				.flatten()
				.flatMapValue { arguments ->
					when (val coercer = type.nodeInputCoercer) {
						null -> GResult.success(arguments)
						else -> coerceValue(arguments, uncoercedValue = value, coercer = coercer, context = context)
					}
				}

			else -> context.invalidValueResult(value, type = type)
		}
	}


	// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
	private fun coerceValueForList(value: GValue, type: GListType, context: Context): GResult<Any?> =
		when (value) {
			is GListValue -> value.elements.map { element ->
				coerceValue(element, type = type.elementType, context = context)
			}.flatten()

			else -> coerceValue(value, type = type.elementType, context = context).mapValue(::listOf)
		}


	// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
	private fun coerceValueForNonNull(value: GValue, type: GNonNullType, context: Context): GResult<Any?> =
		coerceValue(value, type = type.wrappedType, context = context)


	private fun coerceValueForScalar(value: GValue, type: GScalarType, context: Context): GResult<Any?> {
		@Suppress("IMPLICIT_CAST_TO_ANY")
		return when (type) {
			GBooleanType -> when (value) {
				is GBooleanValue -> value.value
				else -> return context.invalidValueResult(value, type = type)
			}

			GFloatType -> when (value) {
				is GFloatValue -> value.value
				is GIntValue -> value.value.toDouble()
				else -> return context.invalidValueResult(value, type = type)
			}

			GIdType -> when (value) {
				is GIntValue -> value.value.toString()
				is GStringValue -> value.value
				else -> return context.invalidValueResult(value, type = type)
			}

			GIntType -> when (value) {
				is GIntValue -> value.value
				else -> return context.invalidValueResult(value, type = type)
			}

			GStringType -> when (value) {
				is GStringValue -> value.value
				else -> return context.invalidValueResult(value, type = type)
			}

			else -> when (val coercer = type.nodeInputCoercer) {
				null -> value.unwrap()
				else -> return coerceValue(value, uncoercedValue = value, coercer = coercer, context = context)
			}
		}.let { GResult.success(it) }
	}


	private fun coerceVariableValue(value: GVariableRef, type: GType, context: Context): GResult<Any?> =
		when {
			context.executorContext.variableValues.containsKey(value.name) -> GResult.success(context.executorContext.variableValues[value.name])
			else -> coerceValueAbsence(defaultValue = context.argumentDefinition?.defaultValue, type = type, context = context)
		}


	private fun validationError(message: String, argumentDefinition: GArgumentDefinition?): Nothing =
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
		})


	private data class Context(
		val argumentDefinition: GArgumentDefinition?,
		val fieldSelectionPath: GPath?,
		val fullType: GType,
		private val isDefaultValue: Boolean,
		val parentNode: GNode,
		val executorContext: GExecutorContext
	) {

		fun invalidValueResult(value: GValue, type: GType, details: String? = null): GResult<Nothing?> =
			GResult.failure(makeValueError(value = value, type = type, details = details))


		private fun makeValueError(value: GValue, type: GType, details: String? = null) = GError(
			message = buildString {
				if (isDefaultValue) {
					append("Default ")
					append(value.kind)
				}
				else
					append(value.kind.toString().capitalize())

				append(" value is not valid for ")

				val fullTypeRef = GTypeRef(fullType)
				val typeRef = GTypeRef(type)

				val argumentDefinition = argumentDefinition
				if (argumentDefinition != null) {
					if (typeRef != fullTypeRef) {
						append("type '")
						append(typeRef)
						append("' in ")
					}

					append("argument '")
					append(argumentDefinition.name)
					append("' with type '")
					append(fullTypeRef)
					append("'")
				}
				else {
					append("type '")
					append(typeRef)
					append("'")
				}

				if (details != null) {
					append(" (")
					append(details)
					append(")")
				}

				append(".")
			},
			path = fieldSelectionPath,
			nodes = listOf(if (isDefaultValue) parentNode else value)
		)


		fun missingValueResult(): GResult<Nothing?> =
			GResult.failure(GError(
				message = buildString {
					append("A value of type '")
					append(GTypeRef(fullType))
					append("' must be provided")

					val argumentDefinition = argumentDefinition
					if (argumentDefinition != null) {
						append(" for argument '")
						append(argumentDefinition.name)
						append("'")
					}

					append(".")
				},
				path = fieldSelectionPath,
				nodes = listOf(parentNode)
			))


		inner class External(
			private val value: GValue
		) : GNodeInputCoercerContext, GExecutorContext by executorContext {

			override val argumentDefinition
				get() = this@Context.argumentDefinition


			override val type
				get() = this@Context.fullType.underlyingNamedType


			override fun invalidValueError(details: String?) =
				throw makeValueError(value = value, type = type, details = details)
		}
	}
}
