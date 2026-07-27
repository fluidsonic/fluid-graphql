package io.fluidsonic.graphql

// FIXME Also dispatch to execution.outputConverter for non-null & list.
internal object OutputConverter {

	fun convertOutput(
		value: Any,
		type: GType,
		parentType: GObjectType,
		path: GPath,
		fieldDefinition: GFieldDefinition,
		context: DefaultExecutorContext,
	): GResult<Any> {
		val coercerContext = Context(
			execution = context,
			fieldDefinition = fieldDefinition,
			parentType = parentType,
			path = path,
			type = type,
			value = value,
		)

		return GResult.catchErrors {
			coerceValue(context = coercerContext)
		}
	}

	// Precedence: a coercer attached to the type wins over the coercion the type performs itself.
	// `GEnumType` has no such coercion, so its output is an unvalidated pass-through.
	private fun coerceLeafValue(value: Any, type: GLeafType, context: Context): Any = when (val coercer = type.outputValueCoercer) {
		null -> when (type) {
			is GScalarType -> context.enrichingCoercionFailure { type.coerceOutputValue(value) }
			is GEnumType -> value
		}

		else -> coerceValueWithCoercer(coercer = coercer, context = context)
	}

	@Suppress("UNCHECKED_CAST")
	private fun coerceObjectValue(value: Map<String, Any?>, type: GObjectType, context: Context): Any = when (val coercer = type.outputValueCoercer) {
		null -> value
		else -> coerceValueWithCoercer(coercer = coercer as GOutputValueCoercer<Any>, context = context)
	}

	@Suppress("UNCHECKED_CAST")
	private fun coerceValue(context: Context): Any = when (val type = context.type) {
		is GLeafType -> coerceLeafValue(value = context.value, type = type, context = context)
		is GObjectType -> coerceObjectValue(
			value = context.value as Map<String, Any?>, // DefaultFieldSelectionExecutor will always provide a value of this type
			type = type,
			context = context,
		)

		// A broken schema, not something a client can provoke.
		else -> error("Output conversion only supports leaf and object types but ${type.kind} type '${type.toRef()}' was encountered.")
	}

	private fun coerceValueWithCoercer(coercer: GOutputValueCoercer<Any>, context: Context): Any = context.execution.withExceptionHandler(
		origin = { GExceptionOrigin.OutputValueCoercer(coercer = coercer, context = context, path = context.path) },
	) {
		coercer.coerceOutputValue(context.value)
	}

	private data class Context(
		override val execution: DefaultExecutorContext,
		val fieldDefinition: GFieldDefinition,
		val parentType: GObjectType,
		val path: GPath,
		val type: GType,
		val value: Any,
	) : GExecutorContext.Child {

		/**
		 * Runs [coerce], turning the bare message of a coercion failure into a fully positioned error.
		 *
		 * The leaf type reports only what is wrong with the value; the field, its type and the enclosing
		 * type are known here and nowhere else.
		 */
		fun <Result> enrichingCoercionFailure(coerce: () -> Result): Result = try {
			coerce()
		} catch (exception: GErrorException) {
			invalid(details = exception.errors.first().message)
		}

		private fun invalid(details: String): Nothing = GError(
			message = buildString {
				append("Output coercion encountered an invalid resolved value for field '")
				append(fieldDefinition.name)
				append("' of type '")
				append(fieldDefinition.type)
				append("' in type '")
				append(parentType.name)
				append("' (")
				append(details)
				append("):\n")
				append(value::class.qualifiedName ?: "<anonymous class>")
				append(": ")
				append(value)
			},
			path = path,
		).throwException()
	}
}
