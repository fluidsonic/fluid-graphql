package io.fluidsonic.graphql


// FIXME Either rework to support concurrency or document non-support and use accordingly.
@OptIn(ExperimentalUnsignedTypes::class)
internal class DefaultOutputCoercer(
	private val fieldDefinition: GFieldDefinition,
	private val parentType: GNamedType,
	private val schema: GSchema,
	private val variableValues: Map<String, Any?>
) {

	private val context = Context()


	private fun <Value : Any> coerceValue(value: Value, coercer: GOutputCoercer<Value>): Any {
		context.value = value

		return coercer(context, value)
	}


	fun coerceValueAbsence(type: GType): Any? = when (type) {
		is GNonNullType -> invalidValueError(null, details = "non-null expected")
		else -> null
	}


	fun coerceValueAsLeaf(value: Any, type: GLeafType): Any = when (type) {
		GBooleanType -> when (value) {
			is Boolean -> value
			else -> invalidValueError(value)
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
			else -> invalidValueError(value)
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
			else -> invalidValueError(value)
		}

		GIntType -> when (value) {
			is Byte -> value.toInt()
			is Int -> value
			is Long -> value.toIntOrNull() ?: invalidValueError(value)
			is Short -> value.toInt()
			is String -> value
			is UByte -> value.toInt()
			is UInt -> value.toIntOrNull() ?: invalidValueError(value)
			is ULong -> value.toIntOrNull() ?: invalidValueError(value)
			is UShort -> value.toInt()
			else -> invalidValueError(value)
		}

		GStringType -> when (value) {
			is String -> value
			else -> invalidValueError(value)
		}

		else -> when (val coercer = type.outputCoercer) {
			null -> value
			else -> coerceValue(value, coercer = coercer)
		}
	}


	fun coerceValueAsList(value: Any, type: GListType): Any =
		value


	fun coerceValueAsNonNull(value: Any, type: GNonNullType): Any =
		value


	fun coerceValueAsObject(value: Map<String, Any?>, type: GObjectType): Any =
		when (val coercer = type.outputCoercer) {
			null -> value
			else -> coerceValue(value, coercer = coercer)
		}


	private fun invalidValueError(value: Any?, details: String? = null): Nothing =
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

			if (value != null) {
				append(":\n")
				append(value::class.qualifiedName ?: "<anonymous class>")
				append(": ")
				append(value)
			}
			else
				append(": null")
		})


	private inner class Context : GOutputCoercerContext {

		lateinit var value: Any

		override lateinit var type: GNamedType


		override val fieldDefinition
			get() = this@DefaultOutputCoercer.fieldDefinition

		override val schema
			get() = this@DefaultOutputCoercer.schema

		override val variableValues
			get() = this@DefaultOutputCoercer.variableValues


		override fun invalidValueError(details: String?) =
			invalidValueError(value, details = details)
	}
}
