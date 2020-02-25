package io.fluidsonic.graphql


// FIXME Allow nulls for error states to eagerly collect all errors.
//       Prevent null-value errors in parent if children have errors.
//       Fail at top-level.

// FIXME do we have to pass down null for `null` -> `[null]` conversion?
interface GNodeInputCoercion<Environment : Any> {

	fun coerceDefaultValue(
		typeRef: GTypeRef,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any? =
		argument.defaultValue
			.ifNull { context.error("A value must be provided for argument '${argument.name}'.") }
			.let { value -> coerceValue(value = value, typeRef = typeRef, argument = argument, context = context) }


	fun coerceEnumValue(
		value: GValue,
		type: GEnumType,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any =
		with(context) {
			checkNotNull(type.parseValueNode)(value)
				?: context.error("Value is not valid for argument '${argument.name}' of Enum type '${argument.type}'.")
		}


	// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercio
	fun coerceInputObjectValue(
		value: GValue,
		type: GInputObjectType,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any = when (value) {

		is GObjectValue ->
			type.argumentDefinitions
				.associate { field ->
					context.path.withName(field.name) {
						field.name to coerceValue(
							value = value.field(field.name)?.value,
							typeRef = field.type,
							argument = field,
							context = context
						)
					}
				}
				.let { argumentValues -> // FIXME skip if errors!
					with(type) {
						with(context) {
							parseValue(argumentValues)
								?: context.error("Value is not valid for argument '${argument.name}' of Input Object type '${argument.type}'.")
						}
					}
				}

		else -> context.error("'${value.kind}' is not a valid value for argument '${argument.name}' of Input Object type '${argument.type}'.")
	}


	// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.List.Input-Coercion
	fun coerceListValue(
		value: GValue,
		typeRef: GListTypeRef,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any = when (value) {

		is GListValue ->
			value.elements.mapIndexed { index, element ->
				context.path.withIndex(index) {
					context.coercion.coerceValue(
						value = element,
						typeRef = typeRef.elementType,
						argument = argument,
						context = context
					)
				}
			}

		else ->
			listOf(context.coercion.coerceValue(
				value = value,
				typeRef = typeRef.elementType,
				argument = argument,
				context = context
			))
	}


	fun coerceNamedTypeValue(
		value: GValue,
		type: GNamedType,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any = when (type) {

		is GInputObjectType -> coerceInputObjectValue(
			value = value,
			type = type,
			argument = argument,
			context = context
		)

		is GEnumType -> coerceEnumValue(
			value = value,
			type = type,
			argument = argument,
			context = context
		)

		is GScalarType -> coerceScalarValue(
			value = value,
			type = type,
			argument = argument,
			context = context
		)

		is GCompositeType -> context.error("Type '${argument.type}' is not an input type.")
	}


	// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
	fun coerceNonNullValue(
		value: GValue,
		typeRef: GNonNullTypeRef,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any =
		context.coercion.coerceValue(
			value = value,
			typeRef = typeRef.nullableRef,
			argument = argument,
			context = context
		) ?: context.error("'null' is not a valid value for argument '${argument.name}' of Non-Null type '${argument.type}'.")


	fun coerceScalarValue(
		value: GValue,
		type: GScalarType,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any =
		with(context) {
			checkNotNull(type.parseValueNode)(value)
				?: context.error("Value is not a valid value for argument '${argument.name}' of Scalar type '${argument.type}'.")
		}


	fun coerceValue(
		value: GValue?,
		typeRef: GTypeRef,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any? = when (value) {

		null -> coerceDefaultValue(
			typeRef = typeRef,
			argument = argument,
			context = context
		)

		is GVariableRef -> coerceVariableValue(
			value = value,
			typeRef = typeRef,
			argument = argument,
			context = context
		)

		is GNullValue ->
			if (typeRef is GNonNullTypeRef)
				context.error("'null' is not a valid value for argument '${argument.name}' of Non-Null type '${argument.type}'.")
			else
				null

		else -> when (typeRef) {

			is GListTypeRef -> coerceListValue(
				value = value,
				typeRef = typeRef,
				argument = argument,
				context = context
			)

			is GNonNullTypeRef -> coerceNonNullValue(
				value = value,
				typeRef = typeRef,
				argument = argument,
				context = context
			)

			is GNamedTypeRef -> coerceNamedTypeValue(
				value = value,
				type = context.schema.resolveType(typeRef) ?: context.error("Type '$typeRef' cannot be resolved."),
				argument = argument,
				context = context
			)
		}
	}


	// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
	fun coerceValues(
		values: Map<String, GValue>,
		arguments: Collection<GArgumentDefinition>,
		context: GNodeInputCoercionContext<Environment>
	): Map<String, Any?> =
		arguments.associate { argument ->
			context.path.withName(argument.name) {
				argument.name to coerceValue(
					value = values[argument.name],
					argument = argument,
					typeRef = argument.type,
					context = context
				)
			}
		}


	fun coerceVariableValue(
		value: GVariableRef,
		typeRef: GTypeRef,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any? =
		context.variableValues.getOrElseNullable(value.name) {
			coerceDefaultValue(typeRef = typeRef, argument = argument, context = context)
		}


	companion object {

		fun <Environment : Any> default() = object : GNodeInputCoercion<Environment> {}
	}
}
