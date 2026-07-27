package io.fluidsonic.graphql

// http://spec.graphql.org/draft/#CoerceArgumentValues()
internal object NodeInputConverter {

	private fun coerceValue(context: Context): Any? = when (val value = context.value) {
		is GValue? -> coerceValue(value = value, type = context.type, context = context)
		else -> value
	}

	private fun coerceValue(value: GValue?, type: GType, context: Context): Any? = when (value) {
		null -> coerceValueAbsence(defaultValue = context.argumentDefinition?.defaultValue, context = context)
		is GVariableRef -> coerceVariableValue(value = value, context = context)
		is GNullValue -> when (type) {
			is GNonNullType -> context.invalid()
			else -> null
		}

		else -> when (type) {
			is GListType -> coerceValueForList(value = value, type = type, context = context)
			is GNonNullType -> coerceValueForNonNull(value = value, type = type, context = context)
			is GNamedType -> when (type) {
				is GEnumType -> coerceValueForEnum(value, type = type, context = context)
				is GInputObjectType -> coerceValueForInputObject(value, type = type, context = context)
				is GScalarType -> coerceValueForScalar(value, type = type, context = context)
				is GCompositeType -> validationError(
					message = "${type.kind.toString().replaceFirstChar { it.uppercase() }} '${type.name}' is not an input type.",
					argumentDefinition = context.argumentDefinition,
				)
			}
		}
	}

	// An argument or input object field that is not supplied and has no default value must be *absent* from
	// the coerced map unless its type forbids null, mirroring `VariableInputConverter.coerceValueAbsence`.
	// https://spec.graphql.org/draft/#sec-Coercing-Field-Arguments
	private fun coerceValueAbsence(defaultValue: GValue?, context: Context): Any? = defaultValue
		.ifNull {
			return when (context.type) {
				is GNonNullType -> context.invalid()
				else -> NoValue
			}
		}
		.let {
			convertValue(
				context = context.copy(
					fullValue = defaultValue,
					isDefaultValue = true,
					value = defaultValue,
				),
			)
		}

	// http://spec.graphql.org/draft/#sec-Enums.Input-Coercion
	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForEnum(value: GValue, type: GEnumType, context: Context): Any? = when (val coercer = type.inputLiteralCoercer) {
		null -> (value as? GEnumValue)
			?.let { type.value(it.name) }
			?.name
			?: context.invalid(details = "valid values: ${type.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }}")

		else -> coerceValueWithCoercer(coercer = coercer as GInputLiteralCoercer<Any?>, context = context)
	}

	// http://spec.graphql.org/draft/#sec-Input-Objects.Input-Coercion
	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForInputObject(value: GValue, type: GInputObjectType, context: Context): Any? = when (value) {
		is GObjectValue -> {
			if (type.directive(GLanguage.defaultOneOfDirective.name) !== null) {
				coerceOneOfValue(value = value, type = type, context = context)
			}

			type.argumentDefinitions
				.associate { argumentDefinition ->
					val argumentType = context.execution.schema.resolveType(argumentDefinition.type) ?: validationError(
						message = "Type '${argumentDefinition.type}' cannot be resolved.",
						argumentDefinition = argumentDefinition,
					)
					val argumentValue = value.argument(argumentDefinition.name)?.value

					argumentDefinition.name to convertValue(
						context = context.copy(
							argumentDefinition = argumentDefinition,
							fullType = argumentType,
							fullValue = argumentValue,
							parentNode = value,
							type = argumentType,
							value = argumentValue,
						),
					)
				}
				.filterValues { it != NoValue }
				.let { argumentValues ->
					when (val coercer = type.inputLiteralCoercer) {
						null -> argumentValues
						else -> coerceValueWithCoercer(
							coercer = coercer as GInputLiteralCoercer<Any?>,
							context = context.copy(value = argumentValues),
						)
					}
				}
		}

		else -> context.invalid()
	}

	// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects.Input-Coercion
	private fun coerceOneOfValue(value: GObjectValue, type: GInputObjectType, context: Context) {
		val field = value.arguments.distinctBy { it.name }.singleOrNull()
			?: oneOfViolation(message = oneOfViolationMessage(typeName = type.name), value = value, context = context)

		when (val fieldValue = field.value) {
			// A variable is not a literal value, so its runtime value decides whether the constraint holds.
			is GVariableRef -> when {
				!context.execution.variableValues.containsKey(fieldValue.name) -> oneOfViolation(
					message = "Expected variable \"$${fieldValue.name}\" provided to field \"${field.name}\" " +
						"for OneOf Input Object type \"${type.name}\" to provide a runtime value.",
					value = value,
					context = context,
				)

				context.execution.variableValues[fieldValue.name] === null -> oneOfViolation(
					message = "Expected variable \"$${fieldValue.name}\" provided to field \"${field.name}\" " +
						"for OneOf Input Object type \"${type.name}\" not to be null.",
					value = value,
					context = context,
				)
			}

			is GNullValue -> oneOfViolation(message = oneOfViolationMessage(typeName = type.name), value = value, context = context)

			else -> Unit
		}
	}

	private fun oneOfViolation(message: String, value: GValue, context: Context): Nothing = GError(
		message = message,
		path = context.fieldSelectionPath,
		nodes = listOf(value),
	).throwException()

	// http://spec.graphql.org/draft/#sec-Type-System.List.Input-Coercion
	private fun coerceValueForList(value: GValue, type: GListType, context: Context): List<Any?> = when (value) {
		is GListValue -> value.elements.map { element ->
			convertValue(
				context = context.copy(
					type = type.elementType,
					value = element,
				),
			)
		}

		else -> listOf(convertValue(context = context.copy(type = type.elementType)))
	}

	// http://spec.graphql.org/draft/#sec-Type-System.Non-Null.Input-Coercion
	@Suppress("UNUSED_PARAMETER")
	private fun coerceValueForNonNull(value: GValue, type: GNonNullType, context: Context): Any? = convertValue(context = context.copy(type = type.wrappedType))

	// Precedence: a coercer attached to the type wins over the coercion the type performs itself. A scalar
	// that defines no literal coercion at all — as opposed to one whose literal coercion returns `null` —
	// sees the literal converted generically and handed to its value coercion instead.
	@Suppress("UNCHECKED_CAST")
	private fun coerceValueForScalar(value: GValue, type: GScalarType, context: Context): Any? = when (val coercer = type.inputLiteralCoercer) {
		null -> context.enrichingCoercionFailure { coerceValueWithType(value = value, type = type) }
		else -> coerceValueWithCoercer(coercer = coercer as GInputLiteralCoercer<Any?>, context = context)
	}

	private fun coerceValueWithType(value: GValue, type: GScalarType): Any? = when (val coerceLiteral = type.coerceInputLiteral) {
		// `coerceValue` answers a null literal before scalar dispatch, so `unwrap` cannot yield null here.
		null -> type.coerceInputValue(checkNotNull(value.unwrap()) { "A null literal must not reach scalar coercion." })
		else -> coerceLiteral(value)
	}

	private fun coerceValueWithCoercer(coercer: GInputLiteralCoercer<Any?>, context: Context): Any? = context.execution.withExceptionHandler(
		origin = { GExceptionOrigin.InputLiteralCoercer(coercer = coercer, context = context, path = context.fieldSelectionPath) },
	) {
		coercer.coerceInputLiteral(context.value)
	}

	fun convertArguments(
		node: GNode.WithArguments,
		definitions: Collection<GArgumentDefinition>,
		fieldSelectionPath: GPath,
		context: DefaultExecutorContext,
	): GResult<Map<String, Any?>> {
		if (definitions.isEmpty()) {
			return GResult.success(emptyMap())
		}

		val parentNode = when (node) {
			is GDirective -> node
			is GFieldSelection -> node
			else -> error("'node' must be a directive or a field selection.")
		}

		return GResult.catchErrors {
			definitions.associate { argumentDefinition ->
				val argumentType = context.schema.resolveType(argumentDefinition.type) ?: validationError(
					message = "Type '${argumentDefinition.type}' cannot be resolved.",
					argumentDefinition = argumentDefinition,
				)
				val argumentValue = node.argument(argumentDefinition.name)?.value

				argumentDefinition.name to convertValue(
					context = Context(
						argumentDefinition = argumentDefinition,
						execution = context,
						fieldSelectionPath = fieldSelectionPath,
						fullType = argumentType,
						fullValue = argumentValue,
						isDefaultValue = false,
						parentNode = parentNode,
						type = argumentType,
						value = argumentValue,
					),
				)
			}
				.filterValues { it != NoValue }
		}
	}

	private fun convertValue(context: Context): Any? = coerceValue(context = context)

	fun convertValue(value: GValue, type: GType, parentNode: GNode, context: DefaultExecutorContext): GResult<Any?> = GResult.catchErrors {
		convertValue(
			context = Context(
				argumentDefinition = parentNode as? GArgumentDefinition,
				execution = context,
				fieldSelectionPath = null,
				fullType = type,
				fullValue = value,
				isDefaultValue = false,
				parentNode = parentNode,
				type = type,
				value = value,
			),
		)
	}

	private fun coerceVariableValue(value: GVariableRef, context: Context): Any? = when {
		context.execution.variableValues.containsKey(value.name) ->
			context.execution.variableValues[value.name]
		context.argumentDefinition?.defaultValue != null ->
			coerceValueAbsence(defaultValue = context.argumentDefinition.defaultValue, context = context)
		context.type is GNonNullType ->
			context.invalid()
		else ->
			null
	}

	// The schema or document is broken in a way no client can provoke, so this fails loudly instead of
	// becoming a GraphQL error in the response.
	private fun validationError(message: String, argumentDefinition: GArgumentDefinition?): Nothing = error(
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
		},
	)

	private data class Context(
		val argumentDefinition: GArgumentDefinition?,
		override val execution: DefaultExecutorContext,
		val fieldSelectionPath: GPath?,
		val fullType: GType,
		val fullValue: GValue?,
		private val isDefaultValue: Boolean,
		val parentNode: GNode,
		val type: GType,
		val value: Any?,
	) : GExecutorContext.Child {

		/**
		 * Runs [coerce], turning the bare message of a coercion failure into a fully positioned error.
		 *
		 * The leaf type reports only what is wrong with the value; the argument it was written for and the
		 * literal itself are known here and nowhere else.
		 */
		fun <Result> enrichingCoercionFailure(coerce: () -> Result): Result = try {
			coerce()
		} catch (exception: GErrorException) {
			invalid(details = exception.errors.first().message)
		}

		fun invalid(details: String? = null): Nothing = makeValueError(details = details).throwException()

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
				nodes = listOf(parentNode),
			)

			return GError(
				message = buildString {
					if (isDefaultValue) {
						append("Default ")
						append(fullValue.kind)
					} else {
						append(fullValue.kind.toString().replaceFirstChar { it.uppercase() })
					}

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
					} else {
						append("type '")
						append(typeRef)
						append("'")
					}

					if (details != null) {
						append(" (")
						append(details)
						append(")")
					}

					append(".\nThe invalid value is: ")
					append(fullValue)
				},
				path = fieldSelectionPath,
				nodes = listOf(if (isDefaultValue) parentNode else fullValue),
			)
		}
	}
}
