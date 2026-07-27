package io.fluidsonic.graphql

// http://spec.graphql.org/draft/#CoerceVariableValues()
internal object VariableInputConverter {

	/**
	 * Caps how many variable coercion errors are reported.
	 *
	 * Ported from graphql-js `ExecutionArgs.options.maxCoercionErrors`, which is marked `@internal` and
	 * defaults to 50, so the limit deliberately is not configurable. Without it a single large list
	 * variable of untrusted input yields one error per element.
	 */
	private object CoercionErrorLimit {

		private const val MAX_ERRORS = 50

		private val terminalError = GError(
			message = "Too many errors processing variables, error limit reached. Execution aborted.",
			isRequestError = true,
		)

		/**
		 * Runs [coerce], collecting a coercion failure into [errors] instead of aborting, so that sibling
		 * values are still coerced and reported. Mirrors graphql-js, which keeps coercing and feeds every
		 * failure to its `onError` callback.
		 *
		 * Takes the error list rather than a [Context] so that a variable whose declared type is unusable can
		 * be reported without first fabricating a [Context] around a type that does not exist.
		 *
		 * Throws once [MAX_ERRORS] errors have been collected, so the terminal error is reported last.
		 */
		inline fun collecting(errors: MutableList<GError>, coerce: () -> Any?): Any? = try {
			coerce()
		} catch (exception: GErrorException) {
			for (error in exception.errors) {
				if (errors.size >= MAX_ERRORS) {
					throw GErrorException(terminalError)
				}

				errors += error
			}

			NoValue
		}
	}

	private fun coerceValue(value: Any?, type: GType, context: Context): Any? {
		if (!context.hasValue) {
			return coerceValueAbsence(
				defaultValue = (context.argumentDefinition ?: context.variableDefinition).defaultValue,
				type = type,
				context = context,
			)
		}

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
						argumentDefinition = context.argumentDefinition,
					)
				}
			}
		}
	}

	private fun coerceValueAbsence(defaultValue: GValue?, type: GType, context: Context): Any? {
		return defaultValue
			.ifNull {
				// A non-null type with no default is required by definition, so an absent value is invalid.
				when (type) {
					is GNonNullType -> context.invalid()
					else -> return NoValue
				}
			}
			.let { value ->
				context.execution.nodeInputConverter.convertValue(
					value = value,
					type = type,
					parentNode = context.argumentDefinition ?: context.variableDefinition,
					context = context.execution,
				).valueOrThrow()
			}
	}

	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForEnum(value: Any, type: GEnumType, context: Context): Any? = when (val coercer = type.inputValueCoercer) {
		null -> (value as? String)
			?.let { type.value(it) }
			?.name
			?: context.invalid(details = "valid values: ${type.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }}")

		else -> coerceValueWithCoercer(coercer = coercer as GInputValueCoercer<Any?>, context = context)
	}

	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForInputObject(value: Any, type: GInputObjectType, context: Context): Any? = when (value) {
		is Map<*, *> ->
			type.argumentDefinitions
				.associate { argumentDefinition ->
					val argumentType = context.execution.schema.resolveType(argumentDefinition.type) ?: validationError(
						message = "Type '${argumentDefinition.type}' cannot be resolved.",
						variableDefinition = context.variableDefinition,
						argumentDefinition = argumentDefinition,
					)
					val argumentValue = value[argumentDefinition.name]
					val argumentContext = context.copy(
						argumentDefinition = argumentDefinition,
						fullType = argumentType,
						fullValue = argumentValue,
						hasValue = value.containsKey(argumentDefinition.name),
						path = context.path.addName(argumentDefinition.name),
						type = argumentType,
						value = argumentValue,
					)

					argumentDefinition.name to CoercionErrorLimit.collecting(errors = context.errors) { convertValue(context = argumentContext) }
				}
				.filterValues { it != NoValue }
				.let { argumentValues ->
					// Checked on the coerced result, so absent fields have already been dropped.
					// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects.Input-Coercion
					if (type.directive(GLanguage.defaultOneOfDirective.name) !== null) {
						val argumentValue = argumentValues.entries.singleOrNull()
						if (argumentValue === null || argumentValue.value === null) {
							GError(
								message = oneOfViolationMessage(typeName = type.name),
								path = context.path,
								nodes = listOf(context.variableDefinition),
							).throwException()
						}
					}

					when (val coercer = type.inputValueCoercer) {
						null -> argumentValues
						else -> coerceValueWithCoercer(
							coercer = coercer as GInputValueCoercer<Any?>,
							context = context.copy(value = argumentValues),
						)
					}
				}

		else -> context.invalid()
	}

	private fun coerceValueForList(value: Any, type: GListType, context: Context): List<Any?> = when (value) {
		is Collection<*> ->
			value
				.mapIndexed { index, element ->
					val elementContext = context.copy(
						path = context.path.addIndex(index),
						type = type.elementType,
						value = element,
					)

					CoercionErrorLimit.collecting(errors = context.errors) { convertValue(context = elementContext) }
				}

		else -> listOf(convertValue(context = context.copy(type = type.elementType)))
	}

	@Suppress("UNUSED_PARAMETER")
	private fun coerceValueForNonNull(value: Any, type: GNonNullType, context: Context): Any? = convertValue(context = context.copy(type = type.wrappedType))

	// Precedence: a coercer attached to the type wins over the coercion the type performs itself.
	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForScalar(value: Any, type: GScalarType, context: Context): Any? = when (val coercer = type.inputValueCoercer) {
		null -> context.enrichingCoercionFailure { type.coerceInputValue(value) }
		else -> coerceValueWithCoercer(coercer = coercer as GInputValueCoercer<Any?>, context = context)
	}

	private fun coerceValueWithCoercer(coercer: GInputValueCoercer<Any?>, context: Context): Any? = context.execution.withExceptionHandler(
		origin = { GExceptionOrigin.InputValueCoercer(coercer = coercer, context = context, path = context.path) },
	) {
		coercer.coerceInputValue(context.value)
	}

	fun convertValues(values: Map<String, Any?>, operation: GOperationDefinition, context: DefaultExecutorContext): GResult<Map<String, Any?>> {
		if (operation.variableDefinitions.isEmpty()) {
			return GResult.success(emptyMap())
		}

		val errors = mutableListOf<GError>()

		val coercedValues = try {
			operation.variableDefinitions
				.associate { variableDefinition ->
					val variableValue = values[variableDefinition.name]
					val variableType = context.schema.resolveType(variableDefinition.type)

					// Each variable is coerced independently so that one bad value does not hide the others.
					variableDefinition.name to CoercionErrorLimit.collecting(errors) {
						// A variable definition comes from the *document*, so a client provokes an unusable type
						// simply by naming one. That makes it a request error rather than a broken schema:
						// https://spec.graphql.org/draft/#sec-Coercing-Variable-Values requires one, and
						// graphql-js answers one too. Contrast `validationError`, which serves the
						// schema-derived argument lookalikes and throws.
						if (variableType === null || !variableType.isInputType()) {
							GError(
								message = "Variable \"$${variableDefinition.name}\" expected value of type " +
									"\"${variableDefinition.type}\" which cannot be used as an input type.",
								nodes = listOf(variableDefinition),
								isRequestError = true,
							).throwException()
						}

						convertValue(
							context = Context(
								argumentDefinition = null,
								errors = errors,
								execution = context,
								hasValue = values.containsKey(variableDefinition.name),
								fullType = variableType,
								fullValue = variableValue,
								path = GPath.ofName(variableDefinition.name),
								variableDefinition = variableDefinition,
								type = variableType,
								value = variableValue,
							),
						)
					}
				}
				.filterValues { it != NoValue }
		} catch (exception: GErrorException) {
			// The coercion error limit was reached; its terminal error is the last one reported.
			errors += exception.errors

			null
		}

		// Variable coercion happens before execution begins, so a failure here is a request error.
		return when {
			errors.isNotEmpty() -> GResult.failure(errors.map { error -> error.copy(isRequestError = true) })
			else -> GResult.success(checkNotNull(coercedValues))
		}
	}

	private fun convertValue(context: Context): Any? = coerceValue(value = context.value, type = context.type, context = context)

	// The schema is broken in a way no client can provoke, so this fails loudly instead of becoming a GraphQL
	// error in the response. Note this serves *argument* definitions, which are schema-derived; the
	// document-derived variable lookalike is a request error raised inline in `convertValues`.
	private fun validationError(message: String, variableDefinition: GVariableDefinition, argumentDefinition: GArgumentDefinition?): Nothing = error(
		buildString {
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
		},
	)

	private data class Context(
		val argumentDefinition: GArgumentDefinition?,
		/** Coercion errors collected so far across the whole operation. Shared by every derived context. */
		val errors: MutableList<GError>,
		override val execution: DefaultExecutorContext,
		val hasValue: Boolean,
		val fullType: GType,
		val fullValue: Any?,
		val path: GPath,
		val variableDefinition: GVariableDefinition,
		val type: GType,
		val value: Any?,
	) : GExecutorContext.Child {

		/**
		 * Runs [coerce], turning the bare message of a coercion failure into a fully positioned error.
		 *
		 * The leaf type reports only what is wrong with the value; the variable it was supplied for is
		 * known here and nowhere else.
		 */
		fun <Result> enrichingCoercionFailure(coerce: () -> Result): Result = try {
			coerce()
		} catch (exception: GErrorException) {
			invalid(details = exception.errors.first().message)
		}

		fun invalid(details: String? = null): Nothing = makeInvalidValueError(details = details).throwException()

		private fun makeInvalidValueError(details: String?): GError {
			if (!hasValue) {
				return GError(
					message = buildString {
						append("A value must be provided for ")
						append("variable '")
						append(path.toString())
						append("' of type '")
						append(fullType.name)
						append("'.")
					},
					nodes = listOf(variableDefinition),
				)
			}

			return GError(
				message = buildString {
					append(
						when (value) {
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
						},
					)
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
				nodes = listOf(variableDefinition),
			)
		}
	}
}
