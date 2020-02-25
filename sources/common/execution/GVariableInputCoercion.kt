package io.fluidsonic.graphql


// FIXME Allow nulls for error states to eagerly collect all errors.
//       Prevent null-value errors in parent if children have errors.
//       Fail at top-level.

// FIXME do we have to pass down null for `null` -> `[null]` conversion?
interface GVariableInputCoercion<Environment : Any> {

	// FIXME does this actually make sense?
	fun coerceListValue(
		value: Any,
		typeRef: GListTypeRef,
		variable: GVariableDefinition,
		context: GVariableInputCoercionContext<Environment>
	): Any = when (value) {

		is Iterable<*> ->
			value.mapIndexed { index, element ->
				context.path.withIndex(index) {
					context.coercion.coerceValue(
						value = element,
						typeRef = typeRef.elementType,
						variable = variable,
						context = context
					)
				}
			}

		else -> listOf(context.coercion.coerceValue(
			value = value,
			typeRef = typeRef.elementType,
			variable = variable,
			context = context
		))
	}


	// FIXME does this actually make sense?
	fun coerceNamedTypeValue(
		value: Any,
		type: GNamedType,
		variable: GVariableDefinition,
		context: GVariableInputCoercionContext<Environment>
	): Any = when (type) {
		is GBooleanType ->
			when (value) {
				is Boolean -> value
				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GCustomScalarType ->
			// FIXME support conversion function
			when (value) {
				is Boolean -> value
				is Float -> value
				is Int -> value
				is String -> value
				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GEnumType ->
			when (value) {
				is String -> type.value(value) ?: context.error("Value is not valid for variable '$${variable.name}' of Enum type '${variable.type}'.")
				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GFloatType ->
			when (value) {
				is Float -> value
				is Int -> value.toFloat()
				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GIdType ->
			when (value) {
				is Int -> value
				is String -> value
				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GInputObjectType ->
			when (value) {
				is Map<*, *> ->
					type.argumentDefinitions.associate { argument ->
						context.path.withName(argument.name) {
							argument.name to context.coercion.coerceValue(
								value = value.getOrElseNullable(argument.name) {
									argument.defaultValue
										?: context.error("A value must be provided for field '${argument.name}' in variable '${variable.name}'.")
								},
								typeRef = argument.type,
								variable = variable, // FIXME pass argument definition & add to error messages!
								context = context
							)
						}
					}

				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GIntType ->
			when (value) {
				is Int -> value
				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GStringType ->
			when (value) {
				is String -> value
				else -> context.error("Invalid type for variable '$${variable.name}'.") // FIXME
			}

		is GCompositeType -> context.error("Type '${variable.type}' is not an input type.")
	}


	// FIXME does this actually make sense?
	fun coerceNonNullValue(
		value: Any,
		typeRef: GNonNullTypeRef,
		variable: GVariableDefinition,
		context: GVariableInputCoercionContext<Environment>
	): Any =
		coerceValue(
			value = value,
			typeRef = typeRef.nullableRef,
			variable = variable,
			context = context
		) ?: context.error("'null' is not a valid value for variable '$${variable.name}' of Non-Null type '${variable.type}'.")


	fun coerceValue(
		value: Any?,
		typeRef: GTypeRef,
		variable: GVariableDefinition,
		context: GVariableInputCoercionContext<Environment>
	): Any? = when (value) {

		null -> null

		else -> when (typeRef) {

			is GListTypeRef -> coerceListValue(
				value = value,
				typeRef = typeRef,
				variable = variable,
				context = context
			)

			is GNonNullTypeRef -> coerceNonNullValue(
				value = value,
				typeRef = typeRef,
				variable = variable,
				context = context
			)

			is GNamedTypeRef -> coerceNamedTypeValue(
				value = value,
				type = context.schema.resolveType(typeRef) ?: context.error("Type '$typeRef' cannot be resolved."),
				variable = variable,
				context = context
			)
		}
	}


	// FIXME use parsing functions
	// https://graphql.github.io/graphql-spec/June2018/#CoerceVariableValues()
	fun coerceValues(
		values: Map<String, Any?>,
		variables: Collection<GVariableDefinition>,
		context: GVariableInputCoercionContext<Environment>
	): Map<String, Any?> =
		variables.associate { variable ->
			context.path.withName(variable.name) {
				variable.name to coerceValue(
					value = values.getOrElseNullable(variable.name) {
						variable.defaultValue ?: context.error("A value must be provided for variable '$${variable.name}'.")
					},
					typeRef = variable.type,
					variable = variable,
					context = context
				)
			}
		}


	companion object {

		fun <Environment : Any> default() = object : GVariableInputCoercion<Environment> {}
	}
}
