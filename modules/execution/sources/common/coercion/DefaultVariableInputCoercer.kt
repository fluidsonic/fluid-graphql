package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#CoerceVariableValues()
// FIXME Either rework to support concurrency or document non-support and use accordingly.
@OptIn(ExperimentalUnsignedTypes::class)
internal class DefaultVariableInputCoercer(
	private val schema: GSchema, // FIXME Needs introspection schema for introspection arguments?
	private val defaultValueCoercer: DefaultNodeInputCoercer
) {

	private var argumentDefinition: GArgumentDefinition? = null
	private val context = Context()
	private val pathBuilder = GPath.Builder()

	private lateinit var variableDefinition: GVariableDefinition


	private fun coerceValue(value: Any?, typeRef: GTypeRef): Any? = when (value) {
		NoValue -> coerceValueAbsence(default = variableDefinition.defaultValue, typeRef = typeRef)
		null -> when (typeRef) {
			is GNonNullTypeRef -> invalidValueError(value = value, typeRef = typeRef)
			else -> null
		}
		else -> when (typeRef) {
			is GListTypeRef -> coerceValueForList(value = value, typeRef = typeRef)
			is GNonNullTypeRef -> coerceValueForNonNull(value = value, typeRef = typeRef)
			is GNamedTypeRef -> when (val type = schema.resolveType(typeRef) ?: schemaError("Type '$typeRef' cannot be resolved.")) {
				is GEnumType -> coerceValueForEnum(value, type = type)
				is GInputObjectType -> coerceValueForInputObject(value, type = type)
				is GScalarType -> coerceValueForScalar(value, type = type)
				is GCompositeType -> schemaError("${type.kind.toString().capitalize()} '$typeRef' is not an input type.")
			}
		}
	}


	private fun <Value> coerceValue(value: Value, initialValue: Any?, type: GNamedType, coercer: GVariableInputCoercer<Value>): Any? {
		context.type = type
		context.value = initialValue

		return coercer(context, value)
	}


	private fun coerceValueAbsence(default: GValue?, typeRef: GTypeRef): Any? =
		default
			.ifNull { missingValueError() }
			.let { defaultValueCoercer.coerceValue(it, typeRef = typeRef, parentNode = argumentDefinition ?: variableDefinition) }


	private fun coerceValueForEnum(value: Any, type: GEnumType): Any? =
		when (val coercer = type.variableInputCoercer) {
			null -> value // FIXME What is the right default behavior?
			else -> coerceValue(value, initialValue = value, type = type, coercer = coercer)
		}


	private fun coerceValueForInputObject(value: Any, type: GInputObjectType): Any? {
		val argumentDefinition = this.argumentDefinition

		return when (value) {
			is Map<*, *> ->
				try {
					type.argumentDefinitions
						.associate { objectArgumentDefinition ->
							this.argumentDefinition = objectArgumentDefinition

							pathBuilder.withName(objectArgumentDefinition.name) {
								objectArgumentDefinition.name to
									if (value.containsKey(objectArgumentDefinition.name))
										coerceValue(value[objectArgumentDefinition], typeRef = objectArgumentDefinition.type)
									else
										coerceValueAbsence(default = objectArgumentDefinition.defaultValue, typeRef = objectArgumentDefinition.type)
							}
						}
						.let { argumentValues ->
							when (val coercer = type.variableInputCoercer) {
								null -> argumentValues
								else -> coerceValue(argumentValues, initialValue = value, type = type, coercer = coercer)
							}
						}
				}
				finally {
					this.argumentDefinition = argumentDefinition
				}

			else -> invalidValueError(value, type = type)
		}
	}


	private fun coerceValueForList(value: Any, typeRef: GListTypeRef): Any? =
		when (value) {
			is Collection<*> -> value.mapIndexed { index, element ->
				pathBuilder.withIndex(index) {
					coerceValue(element, typeRef = typeRef.elementType)
				}
			}
			else -> listOf(coerceValue(value, typeRef = typeRef.elementType))
		}


	private fun coerceValueForNonNull(value: Any, typeRef: GNonNullTypeRef): Any? =
		coerceValue(value, typeRef = typeRef.nullableRef)


	private fun coerceValueForScalar(value: Any, type: GScalarType): Any? =
		when (type) {
			GBooleanType -> when (value) {
				is Boolean -> value
				else -> invalidValueError(value, type = type)
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
				else -> invalidValueError(value, type = type)
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
				else -> invalidValueError(value, type = type)
			}

			GIntType -> when (value) {
				is Byte -> value.toInt()
				is Int -> value
				is Long -> value.toIntOrNull() ?: invalidValueError(value, type = type)
				is Short -> value.toInt()
				is UByte -> value.toInt()
				is UInt -> value.toIntOrNull() ?: invalidValueError(value, type = type)
				is ULong -> value.toIntOrNull() ?: invalidValueError(value, type = type)
				is UShort -> value.toInt()
				else -> invalidValueError(value, type = type)
			}

			GStringType -> when (value) {
				is String -> value
				else -> invalidValueError(value, type = type)
			}

			else -> when (val coercer = type.variableInputCoercer) {
				null -> value // FIXME What is the right default behavior? also have to convert GValues
				else -> coerceValue(value, initialValue = value, type = type, coercer = coercer)
			}
		}


	fun coerceValues(values: Map<String, Any?>, operation: GOperationDefinition): Map<String, Any?> {
		if (operation.variableDefinitions.isEmpty())
			return emptyMap()

		return operation.variableDefinitions.associate { operationVariableDefinition ->
			variableDefinition = operationVariableDefinition

			pathBuilder.withName(operationVariableDefinition.name) {
				operationVariableDefinition.name to coerceValue(
					values.getOrElseNullable(operationVariableDefinition.name) { NoValue },
					typeRef = operationVariableDefinition.type
				)
			}
		}
	}


	private fun invalidValueError(value: Any?, type: GNamedType, details: String? = null): Nothing =
		invalidValueError(value, typeRef = GTypeRef(type.name), details = details)


	private fun invalidValueError(value: Any?, typeRef: GTypeRef, details: String? = null): Nothing =
		throw GError(
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

				val fullTypeRef = argumentDefinition?.type ?: variableDefinition.type
				if (typeRef != fullTypeRef) {
					append("type '")
					append(typeRef)
					append("' in ")
				}

				append("variable '")
				append(pathBuilder.snapshot().toString())
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


	private fun missingValueError(): Nothing =
		throw GError(
			message = buildString {
				append("A value must be provided for ")
				append("variable '")
				append(pathBuilder.snapshot().toString())
				append("' of type '")
				append(argumentDefinition?.type ?: variableDefinition.type)
				append("'.")
			},
			nodes = listOf(variableDefinition)
		)


	private fun schemaError(message: String): Nothing =
		error(buildString {
			append("There is an error in the GraphQL schema. It should be validated before use:\n")
			append(message)

			val argumentDefinition = argumentDefinition
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


	private inner class Context : GVariableInputCoercerContext {

		var value: Any? = null

		override lateinit var type: GNamedType


		override val argumentDefinition
			get() = this@DefaultVariableInputCoercer.argumentDefinition

		override val schema
			get() = this@DefaultVariableInputCoercer.schema

		override val variableDefinition
			get() = this@DefaultVariableInputCoercer.variableDefinition


		override fun invalidValueError(details: String?) =
			invalidValueError(value, type = type, details = details)
	}
}
