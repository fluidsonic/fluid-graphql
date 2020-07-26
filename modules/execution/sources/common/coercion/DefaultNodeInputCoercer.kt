package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
// FIXME Either rework to support concurrency or document non-support and use accordingly.
internal class DefaultNodeInputCoercer(
	private val fieldSelectionPath: GPath.Builder?,
	private val schema: GSchema, // FIXME Needs introspection schema for introspection arguments.
	private val variableValues: Map<String, Any?>
) {

	private var argumentDefinition: GArgumentDefinition? = null
	private val context = Context()
	private var isDefaultValue = false

	private lateinit var fullTypeRef: GTypeRef
	private lateinit var parentNode: GNode


	fun coerceArguments(node: GNode.WithArguments, definitions: Collection<GArgumentDefinition>): Map<String, Any?> {
		if (definitions.isEmpty())
			return emptyMap()

		parentNode = when (node) {
			is GDirective -> node
			is GFieldSelection -> node
			else -> error("'node' must be a directive or a field selection.")
		}

		return definitions.associate { fieldSelectionArgumentDefinition ->
			argumentDefinition = fieldSelectionArgumentDefinition
			fullTypeRef = fieldSelectionArgumentDefinition.type

			fieldSelectionArgumentDefinition.name to coerceValue(
				node.arguments.firstOrNull { it.name == fieldSelectionArgumentDefinition.name }?.value,
				typeRef = fieldSelectionArgumentDefinition.type
			)
		}
	}


	private fun coerceValue(value: GValue?, typeRef: GTypeRef): Any? = when (value) {
		null -> coerceValueAbsence(default = argumentDefinition?.defaultValue, typeRef = typeRef)
		is GVariableRef -> coerceVariableValue(value = value, typeRef = typeRef)
		is GNullValue -> when (typeRef) {
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


	fun coerceValue(value: GValue, typeRef: GTypeRef, parentNode: GNode): Any? {
		this.argumentDefinition = null
		this.fullTypeRef = typeRef
		this.parentNode = parentNode

		return coerceValue(value, typeRef = typeRef)
	}


	private fun <Value> coerceValue(value: Value, initialValue: GValue, type: GNamedType, coercer: GNodeInputCoercer<Value>): Any? {
		context.type = type
		context.value = initialValue

		return coercer(context, value)
	}


	private fun coerceValueAbsence(default: GValue?, typeRef: GTypeRef): Any? =
		default
			.ifNull { missingValueError() }
			.let { value ->
				val wasDefaultValue = isDefaultValue
				isDefaultValue = true

				try {
					coerceValue(value, typeRef = typeRef)
				}
				finally {
					isDefaultValue = wasDefaultValue
				}
			}


	private fun coerceValueForEnum(value: GValue, type: GEnumType): Any? =
		when (val coercer = type.nodeInputCoercer) {
			null -> value // FIXME What is the right default behavior? also have to convert GValues
			else -> coerceValue(value, initialValue = value, type = type, coercer = coercer)
		}


	// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercion
	private fun coerceValueForInputObject(value: GValue, type: GInputObjectType): Any? {
		val argumentDefinition = this.argumentDefinition
		val parentNode = this.parentNode

		return when (value) {
			is GObjectValue ->
				try {
					this.parentNode = value

					type.argumentDefinitions
						.associate { inputObjectArgumentDefinition ->
							this.argumentDefinition = inputObjectArgumentDefinition

							inputObjectArgumentDefinition.name to coerceValue(
								value.argument(inputObjectArgumentDefinition.name)?.value,
								typeRef = inputObjectArgumentDefinition.type
							)
						}
						.let { argumentValues ->
							when (val coercer = type.nodeInputCoercer) {
								null -> argumentValues
								else -> coerceValue(argumentValues, initialValue = value, type = type, coercer = coercer)
							}
						}
				}
				finally {
					this.argumentDefinition = argumentDefinition
					this.parentNode = parentNode
				}

			else -> invalidValueError(value, type = type)
		}
	}


	// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
	private fun coerceValueForList(value: GValue, typeRef: GListTypeRef): Any? =
		when (value) {
			is GListValue -> value.elements.map { element ->
				coerceValue(element, typeRef = typeRef.elementType)
			}
			else -> listOf(coerceValue(value, typeRef = typeRef.elementType))
		}


	// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
	private fun coerceValueForNonNull(value: GValue, typeRef: GNonNullTypeRef): Any? =
		coerceValue(value, typeRef = typeRef.nullableRef)


	private fun coerceValueForScalar(value: GValue, type: GScalarType): Any? =
		when (type) {
			GBooleanType -> when (value) {
				is GBooleanValue -> value.value
				else -> invalidValueError(value, type = type)
			}

			GFloatType -> when (value) {
				is GFloatValue -> value.value
				is GIntValue -> value.value.toDouble()
				else -> invalidValueError(value, type = type)
			}

			GIdType -> when (value) {
				is GIntValue -> value.value.toString()
				is GStringValue -> value.value
				else -> invalidValueError(value, type = type)
			}

			GIntType -> when (value) {
				is GIntValue -> value.value
				else -> invalidValueError(value, type = type)
			}

			GStringType -> when (value) {
				is GStringValue -> value.value
				else -> invalidValueError(value, type = type)
			}

			else -> when (val coercer = type.nodeInputCoercer) {
				null -> value // FIXME What is the right default behavior? also have to convert GValues
				else -> coerceValue(value, initialValue = value, type = type, coercer = coercer)
			}
		}


	private fun coerceVariableValue(value: GVariableRef, typeRef: GTypeRef): Any? =
		variableValues.getOrElseNullable(value.name) {
			coerceValueAbsence(default = argumentDefinition?.defaultValue, typeRef = typeRef)
		}


	private fun invalidValueError(value: GValue, type: GNamedType, details: String? = null): Nothing =
		invalidValueError(value, typeRef = GTypeRef(type.name), details = details)


	private fun invalidValueError(value: GValue, typeRef: GTypeRef, details: String? = null): Nothing =
		throw GError(
			message = buildString {
				if (isDefaultValue) {
					append("Default ")
					append(value.kind)
				}
				else
					append(value.kind.toString().capitalize())

				append(" value is not valid for ")

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
			path = fieldSelectionPath?.snapshot(),
			nodes = listOf(if (isDefaultValue) parentNode else value)
		)


	private fun missingValueError(): Nothing =
		throw GError(
			message = buildString {
				append("A value of type '")
				append(fullTypeRef)
				append("' must be provided")

				val argumentDefinition = argumentDefinition
				if (argumentDefinition != null) {
					append(" for argument '")
					append(argumentDefinition.name)
					append("'")
				}

				append(".")
			},
			path = fieldSelectionPath?.snapshot(),
			nodes = listOf(parentNode)
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
		})


	private inner class Context : GNodeInputCoercerContext {

		lateinit var value: GValue

		override lateinit var type: GNamedType


		override val argumentDefinition
			get() = this@DefaultNodeInputCoercer.argumentDefinition

		override val schema
			get() = this@DefaultNodeInputCoercer.schema

		override val variableValues
			get() = this@DefaultNodeInputCoercer.variableValues


		override fun invalidValueError(details: String?) =
			invalidValueError(value, type = type, details = details)
	}
}
