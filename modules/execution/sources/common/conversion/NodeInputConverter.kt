package io.fluidsonic.graphql


// FIXME If we stick with the context.next() approach we may as well drop GResult internally and use only exceptions.
// http://spec.graphql.org/draft/#CoerceArgumentValues()
internal object NodeInputConverter {

	private fun coerceValue(context: Context): GResult<Any?> =
		when (val value = context.value) {
			is GValue? -> coerceValue(value = value, type = context.type, context = context)
			else -> GResult.success(value)
		}


	private fun coerceValue(value: GValue?, type: GType, context: Context): GResult<Any?> = when (value) {
		null -> coerceValueAbsence(defaultValue = context.argumentDefinition?.defaultValue, context = context)
		is GVariableRef -> coerceVariableValue(value = value, context = context)
		is GNullValue -> when (type) {
			is GNonNullType -> context.invalidValueResult()
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


	private fun coerceValueAbsence(defaultValue: GValue?, context: Context): GResult<Any?> =
		defaultValue
			.ifNull { return context.invalidValueResult() }
			.let {
				convertValue(context = context.copy(
					fullValue = defaultValue,
					isDefaultValue = true,
					value = defaultValue
				))
			}


	// http://spec.graphql.org/draft/#sec-Enums.Input-Coercion
	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForEnum(value: GValue, type: GEnumType, context: Context): GResult<Any?> =
		when (val coercer = type.nodeInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
			null ->
				when (value) {
					is GEnumValue -> type.value(value.name)?.name?.let { GResult.success(it) }
					else -> null
				} ?: context.invalidValueResult(
					details = "valid values: ${type.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }}"
				)

			else -> coerceValueWithCoercer(coercer = coercer as GNodeInputCoercer<Any?>, context = context.copy(isUsingCoercerProvidedByType = true))
		}


	// http://spec.graphql.org/draft/#sec-Input-Objects.Input-Coercion
	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForInputObject(value: GValue, type: GInputObjectType, context: Context): GResult<Any?> = when (value) {
		is GObjectValue -> type.argumentDefinitions
			.associate { argumentDefinition ->
				val argumentType = context.execution.schema.resolveType(argumentDefinition.type) ?: validationError(
					message = "Type '${argumentDefinition.type}' cannot be resolved.",
					argumentDefinition = argumentDefinition
				)
				val argumentValue = value.argument(argumentDefinition.name)?.value

				argumentDefinition.name to convertValue(context = context.copy(
					argumentDefinition = argumentDefinition,
					fullType = argumentType,
					fullValue = argumentValue,
					parentNode = value,
					type = argumentType,
					value = argumentValue
				))
			}
			.flatten()
			.flatMapValue { argumentValues ->
				when (val coercer = type.nodeInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
					null -> GResult.success(value)
					else -> coerceValueWithCoercer(coercer = coercer as GNodeInputCoercer<Any?>, context = context.copy(
						isUsingCoercerProvidedByType = true,
						value = argumentValues
					))
				}
			}

		else -> context.invalidValueResult()
	}


	// http://spec.graphql.org/draft/#sec-Type-System.List.Input-Coercion
	private fun coerceValueForList(value: GValue, type: GListType, context: Context): GResult<Any?> =
		when (value) {
			is GListValue -> value.elements.map { element ->
				convertValue(context = context.copy(
					type = type.elementType,
					value = element
				))
			}.flatten()

			else -> convertValue(context = context.copy(type = type.elementType)).mapValue { listOf(it) }
		}


	// http://spec.graphql.org/draft/#sec-Type-System.Non-Null.Input-Coercion
	@Suppress("UNUSED_PARAMETER")
	private fun coerceValueForNonNull(value: GValue, type: GNonNullType, context: Context): GResult<Any?> =
		convertValue(context = context.copy(type = type.wrappedType))


	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForScalar(value: GValue, type: GScalarType, context: Context): GResult<Any?> {
		return when (type) {
			GBooleanType -> when (value) {
				is GBooleanValue -> value.value
				else -> return context.invalidValueResult()
			}

			GFloatType -> when (value) {
				is GFloatValue -> value.value
				is GIntValue -> value.value.toDouble()
				else -> return context.invalidValueResult()
			}

			GIdType -> when (value) {
				is GIntValue -> value.value.toString()
				is GStringValue -> value.value
				else -> return context.invalidValueResult()
			}

			GIntType -> when (value) {
				is GIntValue -> value.value
				else -> return context.invalidValueResult()
			}

			GStringType -> when (value) {
				is GStringValue -> value.value
				else -> return context.invalidValueResult()
			}

			else -> when (val coercer = type.nodeInputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
				null -> value.unwrap()
				else -> return coerceValueWithCoercer(
					coercer = coercer as GNodeInputCoercer<Any?>,
					context = context.copy(isUsingCoercerProvidedByType = true)
				)
			}
		}.let { GResult.success(it) }
	}


	private fun coerceValueWithCoercer(coercer: GNodeInputCoercer<Any?>, context: Context): GResult<Any?> =
		GResult.catchErrors {
			with(coercer) { context.coerceNodeInput(context.value) }
		}


	fun convertArguments(
		node: GNode.WithArguments,
		definitions: Collection<GArgumentDefinition>,
		fieldSelectionPath: GPath,
		context: DefaultExecutorContext
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
			val argumentValue = node.argument(argumentDefinition.name)?.value

			argumentDefinition.name to convertValue(context = Context(
				argumentDefinition = argumentDefinition,
				execution = context,
				fieldSelectionPath = fieldSelectionPath,
				fullType = argumentType,
				fullValue = argumentValue,
				isDefaultValue = false,
				isUsingCoercerProvidedByType = false,
				parentNode = parentNode,
				type = argumentType,
				value = argumentValue
			))
		}.flatten()
	}


	private fun convertValue(context: Context): GResult<Any?> =
		when (val coercer = context.execution.nodeInputCoercer) {
			null -> coerceValue(context = context)
			else -> coerceValueWithCoercer(coercer = coercer, context = context)
		}


	fun convertValue(value: GValue, type: GType, parentNode: GNode, executorContext: DefaultExecutorContext): GResult<Any?> =
		convertValue(context = Context(
			argumentDefinition = parentNode as? GArgumentDefinition,
			execution = executorContext,
			fieldSelectionPath = null,
			fullType = type,
			fullValue = value,
			isDefaultValue = false,
			isUsingCoercerProvidedByType = false,
			parentNode = parentNode,
			type = type,
			value = value
		))


	private fun coerceVariableValue(value: GVariableRef, context: Context): GResult<Any?> =
		when {
			context.execution.variableValues.containsKey(value.name) -> GResult.success(context.execution.variableValues[value.name])
			else -> coerceValueAbsence(defaultValue = context.argumentDefinition?.defaultValue, context = context)
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
		override val argumentDefinition: GArgumentDefinition?,
		override val execution: DefaultExecutorContext,
		val fieldSelectionPath: GPath?,
		val fullType: GType,
		val fullValue: GValue?,
		private val isDefaultValue: Boolean,
		val isUsingCoercerProvidedByType: Boolean,
		val parentNode: GNode,
		override val type: GType,
		val value: Any?
	) : GNodeInputCoercerContext {

		override fun invalidValueError(details: String?) =
			makeValueError(details = details).throwException()


		fun invalidValueResult(details: String? = null): GResult<Nothing?> =
			GResult.failure(makeValueError(details = details))


		private fun makeValueError(details: String? = null): GError {
			val fullValue = fullValue ?: return GError(
				message = buildString {
					append("A value of type '")
					append(fullType.toRef())
					append("' must be provided")

					val argumentDefinition = argumentDefinition
					if (argumentDefinition != null) {
						append(" for argument '")
						append(argumentDefinition.name)
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
				nodes = listOf(parentNode)
			)

			return GError(
				message = buildString {
					if (isDefaultValue) {
						append("Default ")
						append(fullValue.kind)
					}
					else
						append(fullValue.kind.toString().capitalize())

					append(" value is not valid for ")

					val fullTypeRef = fullType.toRef()
					val typeRef = type.toRef()

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
				nodes = listOf(if (isDefaultValue) parentNode else fullValue)
			)
		}


		override fun next(): Any? =
			coerceValue(context = this)
	}
}
