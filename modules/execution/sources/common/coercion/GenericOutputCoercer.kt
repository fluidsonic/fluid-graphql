package io.fluidsonic.graphql


@OptIn(ExperimentalUnsignedTypes::class)
internal object GenericOutputCoercer {

	fun coerceLeafValue(field: GFieldDefinition, parentType: GObjectType, type: GLeafType, value: Any, context: GExecutorContext): GResult<Any> {
		@Suppress("NAME_SHADOWING")
		val context = Context(
			field = field,
			parentType = parentType,
			type = type,
			value = value,
			executorContext = context
		)

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

			else -> when (val coercer = type.outputCoercer) {
				null -> value
				else -> return coerceValue(value, coercer = coercer, context = context)
			}
		}.let { GResult.success(it) }
	}


	fun coerceObjectValue(
		value: Map<String, Any?>,
		type: GObjectType,
		parentType: GObjectType,
		field: GFieldDefinition,
		context: GExecutorContext
	): GResult<Any> =
		when (val coercer = type.outputCoercer) {
			null -> GResult.success(value)
			else -> coerceValue(
				value = value,
				coercer = coercer,
				context = Context(
					field = field,
					parentType = parentType,
					type = type,
					value = value,
					executorContext = context
				)
			)
		}


	private fun <Value : Any> coerceValue(value: Value, coercer: GOutputCoercer<Value>, context: Context): GResult<Any> =
		GResult.catch { coercer(context, value) }


	private class Context(
		override val field: GFieldDefinition,
		override val parentType: GObjectType,
		override val type: GType,
		private val value: Any?,
		executorContext: GExecutorContext
	) : GOutputCoercerContext, GExecutorContext by executorContext {

		override fun invalidValueError(details: String?): Nothing =
			error(buildString {
				append("Output coercion encountered an invalid resolved value for field '")
				append(field.name)
				append("' of type '")
				append(field.type)
				append("' in type '")
				append(parentType.name)
				append("'")

				if (details != null) {
					append(" (")
					append(details)
					append(")")
				}

				if (value != null) {
					append(":\n")
					append(value::class.qualifiedName ?: "<anonymous class>")
					append(": ")
					append(value)
				}
				else
					append(": null")
			})
	}
}
