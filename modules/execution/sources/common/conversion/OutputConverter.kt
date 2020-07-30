package io.fluidsonic.graphql


// FIXME Also dispatch to execution.outputConverter for non-null & list.
internal object OutputConverter {

	fun convertOutput(value: Any, type: GType, parentType: GObjectType, fieldDefinition: GFieldDefinition, context: DefaultExecutorContext): GResult<Any> {
		@Suppress("NAME_SHADOWING")
		val context = Context(
			execution = context,
			fieldDefinition = fieldDefinition,
			isUsingCoercerProvidedByType = false,
			parentType = parentType,
			type = type,
			value = value
		)

		return when (val coercer = context.execution.outputCoercer) {
			null -> coerceValue(context = context)
			else -> coerceValueWithCoercer(coercer = coercer, context = context)
		}
	}


	private fun coerceLeafValue(value: Any, type: GLeafType, context: Context): GResult<Any> {
		return when (type) {
			GBooleanType -> when (value) {
				is Boolean -> value
				else -> context.invalidValueError()
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
				else -> context.invalidValueError()
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
				else -> context.invalidValueError()
			}

			GIntType -> when (value) {
				is Byte -> value.toInt()
				is Int -> value
				is Long -> value.toIntOrNull() ?: context.invalidValueError()
				is Short -> value.toInt()
				is String -> value
				is UByte -> value.toInt()
				is UInt -> value.toIntOrNull() ?: context.invalidValueError()
				is ULong -> value.toIntOrNull() ?: context.invalidValueError()
				is UShort -> value.toInt()
				else -> context.invalidValueError()
			}

			GStringType -> when (value) {
				is String -> value
				else -> context.invalidValueError()
			}

			else -> when (val coercer = type.outputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
				null -> value
				else -> return coerceValueWithCoercer(
					coercer = coercer,
					context = context.copy(isUsingCoercerProvidedByType = true)
				)
			}
		}.let { GResult.success(it) }
	}


	@Suppress("UNCHECKED_CAST")
	private fun coerceObjectValue(value: Map<String, Any?>, type: GObjectType, context: Context): GResult<Any> =
		when (val coercer = type.outputCoercer?.takeUnless { context.isUsingCoercerProvidedByType }) {
			null -> GResult.success(value)
			else -> coerceValueWithCoercer(
				coercer = coercer as GOutputCoercer<Any>,
				context = context.copy(isUsingCoercerProvidedByType = true)
			)
		}


	@Suppress("UNCHECKED_CAST")
	private fun coerceValue(context: Context): GResult<Any> =
		when (val type = context.type) {
			is GLeafType -> coerceLeafValue(value = context.value, type = type, context = context)
			is GObjectType -> coerceObjectValue(
				value = context.value as Map<String, Any?>, // DefaultFieldSelectionExecutor will always provide a value of this type
				type = type,
				context = context
			)
			else -> error("Output conversion only supports leaf and object types but ${type.kind} type '${type.toRef()}' was encountered.")
		}


	private fun coerceValueWithCoercer(coercer: GOutputCoercer<Any>, context: Context): GResult<Any> =
		GResult.catchErrors {
			with(coercer) { context.coerceOutput(context.value) }
		}


	private data class Context(
		override val execution: DefaultExecutorContext,
		override val fieldDefinition: GFieldDefinition,
		val isUsingCoercerProvidedByType: Boolean,
		override val parentType: GObjectType,
		override val type: GType,
		val value: Any
	) : GOutputCoercerContext {

		override fun invalidValueError(details: String?): Nothing =
			error(buildString {
				append("Output coercion encountered an invalid resolved value for field '")
				append(fieldDefinition.name)
				append("' of type '")
				append(fieldDefinition.type)
				append("' in type '")
				append(parentType.name)
				append("'")

				if (details != null) {
					append(" (")
					append(details)
					append(")")
				}

				append(":\n")
				append(value::class.qualifiedOrSimpleName ?: "<anonymous class>")
				append(": ")
				append(value)
			})


		override fun next(): Any? =
			coerceValue(context = this)
	}
}
