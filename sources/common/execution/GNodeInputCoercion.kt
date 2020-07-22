package io.fluidsonic.graphql


// FIXME Allow nulls for error states to eagerly collect all errors.
//       Prevent null-value errors in parent if children have errors.
//       Fail at top-level.

// FIXME do we have to pass down null for `null` -> `[null]` conversion?

// FIXME make mutable context to avoid excessive parameters

// FIXME incorrect projection of Environment?
interface GNodeInputCoercion<Environment : Any> {

	fun coerceDefaultValue(
		typeRef: GTypeRef,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any?


	fun coerceEnumValue(
		value: GValue,
		type: GEnumType,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any


	fun coerceInputObjectValue(
		value: GValue,
		type: GInputObjectType,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any


	fun coerceListValue(
		value: GValue,
		typeRef: GListTypeRef,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any


	fun coerceNamedTypeValue(
		value: GValue,
		type: GNamedType,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any


	fun coerceNonNullValue(
		value: GValue,
		typeRef: GNonNullTypeRef,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any


	fun coerceScalarValue(
		value: GValue,
		type: GScalarType,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any


	fun coerceValue(
		value: GValue?,
		typeRef: GTypeRef,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any?


	fun coerceValues(
		values: Map<String, GValue>,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		arguments: Collection<GArgumentDefinition>,
		context: GNodeInputCoercionContext<Environment>
	): Map<String, Any?>


	fun coerceVariableValue(
		value: GVariableRef,
		typeRef: GTypeRef,
		parentType: GCompositeType?,
		field: GFieldDefinition?,
		argument: GArgumentDefinition,
		context: GNodeInputCoercionContext<Environment>
	): Any?


	companion object {

		@Suppress("UNCHECKED_CAST")
		fun <Environment : Any> default(): GNodeInputCoercion<Environment> =
			Default as GNodeInputCoercion<Environment>
	}


	interface Decorator<Environment : Any> : GNodeInputCoercion<Environment> {

		val decorated: GNodeInputCoercion<Environment>


		override fun coerceDefaultValue(
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any? =
			decorated.coerceDefaultValue(
				typeRef = typeRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceEnumValue(
			value: GValue,
			type: GEnumType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any =
			decorated.coerceEnumValue(
				value = value,
				type = type,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceInputObjectValue(
			value: GValue,
			type: GInputObjectType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any =
			decorated.coerceInputObjectValue(
				value = value,
				type = type,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceListValue(
			value: GValue,
			typeRef: GListTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any =
			decorated.coerceListValue(
				value = value,
				typeRef = typeRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceNamedTypeValue(
			value: GValue,
			type: GNamedType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any =
			decorated.coerceNamedTypeValue(
				value = value,
				type = type,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceNonNullValue(
			value: GValue,
			typeRef: GNonNullTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any =
			decorated.coerceNonNullValue(
				value = value,
				typeRef = typeRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceScalarValue(
			value: GValue,
			type: GScalarType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any =
			decorated.coerceScalarValue(
				value = value,
				type = type,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceValue(
			value: GValue?,
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any? =
			decorated.coerceValue(
				value = value,
				typeRef = typeRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)


		override fun coerceValues(
			values: Map<String, GValue>,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			arguments: Collection<GArgumentDefinition>,
			context: GNodeInputCoercionContext<Environment>
		): Map<String, Any?> =
			decorated.coerceValues(
				values = values,
				parentType = parentType,
				field = field,
				arguments = arguments,
				context = context
			)


		override fun coerceVariableValue(
			value: GVariableRef,
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Environment>
		): Any? =
			decorated.coerceVariableValue(
				value = value,
				typeRef = typeRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)
	}


	private object Default : GNodeInputCoercion<Any> {

		override fun coerceDefaultValue(
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any? =
			argument.defaultValue
				.ifNull { context.error("A value must be provided for argument '${argument.name}'.") }
				.let { value ->
					coerceValue(
						value = value,
						typeRef = typeRef,
						parentType = parentType,
						field = field,
						argument = argument,
						context = context
					)
				}


		override fun coerceEnumValue(
			value: GValue,
			type: GEnumType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any =
			with(context) {
				checkNotNull(type.parseValueNode)(value)
					?: context.error("Value is not valid for argument '${argument.name}' of Enum type '${argument.type}'.")
			}


		// https://graphql.github.io/graphql-spec/draft/#sec-Input-Objects.Input-Coercio
		override fun coerceInputObjectValue(
			value: GValue,
			type: GInputObjectType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any = when (value) {

			is GObjectValue ->
				type.argumentDefinitions
					.associate { inputField ->
						context.path.withName(inputField.name) {
							inputField.name to context.coercion.coerceValue(
								value = value.field(inputField.name)?.value,
								typeRef = inputField.type,
								parentType = type,
								field = null,
								argument = inputField,
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
		override fun coerceListValue(
			value: GValue,
			typeRef: GListTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any = when (value) {

			is GListValue ->
				value.elements.mapIndexed { index, element ->
					context.path.withIndex(index) {
						context.coercion.coerceValue(
							value = element,
							typeRef = typeRef.elementType,
							parentType = parentType,
							field = field,
							argument = argument,
							context = context
						)
					}
				}

			else ->
				listOf(context.coercion.coerceValue(
					value = value,
					typeRef = typeRef.elementType,
					parentType = parentType,
					field = field,
					argument = argument,
					context = context
				))
		}


		override fun coerceNamedTypeValue(
			value: GValue,
			type: GNamedType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any = when (type) {

			is GInputObjectType -> context.coercion.coerceInputObjectValue(
				value = value,
				type = type,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)

			is GEnumType -> context.coercion.coerceEnumValue(
				value = value,
				type = type,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)

			is GScalarType -> context.coercion.coerceScalarValue(
				value = value,
				type = type,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)

			is GCompositeType -> context.error("Type '${argument.type}' is not an input type.")
		}


		// https://graphql.github.io/graphql-spec/draft/#sec-Type-System.Non-Null.Input-Coercion
		override fun coerceNonNullValue(
			value: GValue,
			typeRef: GNonNullTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any =
			context.coercion.coerceValue(
				value = value,
				typeRef = typeRef.nullableRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			) ?: context.error("'null' is not a valid value for argument '${argument.name}' of Non-Null type '${argument.type}'.")


		override fun coerceScalarValue(
			value: GValue,
			type: GScalarType,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any =
			with(context) {
				checkNotNull(type.parseValueNode)(value)
					?: context.error("Value is not a valid value for argument '${argument.name}' of Scalar type '${argument.type}'.")
			}


		override fun coerceValue(
			value: GValue?,
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any? = when (value) {

			null -> context.coercion.coerceDefaultValue(
				typeRef = typeRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)

			is GVariableRef -> context.coercion.coerceVariableValue(
				value = value,
				typeRef = typeRef,
				parentType = parentType,
				field = field,
				argument = argument,
				context = context
			)

			is GNullValue ->
				if (typeRef is GNonNullTypeRef)
					context.error("'null' is not a valid value for argument '${argument.name}' of Non-Null type '${argument.type}'.")
				else
					null

			else -> when (typeRef) {

				is GListTypeRef -> context.coercion.coerceListValue(
					value = value,
					typeRef = typeRef,
					parentType = parentType,
					field = field,
					argument = argument,
					context = context
				)

				is GNonNullTypeRef -> context.coercion.coerceNonNullValue(
					value = value,
					typeRef = typeRef,
					parentType = parentType,
					field = field,
					argument = argument,
					context = context
				)

				is GNamedTypeRef -> context.coercion.coerceNamedTypeValue(
					value = value,
					type = context.schema.resolveType(typeRef) ?: context.error("Type '$typeRef' cannot be resolved."),
					parentType = parentType,
					field = field,
					argument = argument,
					context = context
				)
			}
		}


		// https://graphql.github.io/graphql-spec/draft/#CoerceArgumentValues()
		override fun coerceValues(
			values: Map<String, GValue>,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			arguments: Collection<GArgumentDefinition>,
			context: GNodeInputCoercionContext<Any>
		): Map<String, Any?> =
			arguments.associate { argument ->
				context.path.withName(argument.name) {
					argument.name to context.coercion.coerceValue(
						value = values[argument.name],
						typeRef = argument.type,
						parentType = parentType,
						field = field,
						argument = argument,
						context = context
					)
				}
			}


		override fun coerceVariableValue(
			value: GVariableRef,
			typeRef: GTypeRef,
			parentType: GCompositeType?,
			field: GFieldDefinition?,
			argument: GArgumentDefinition,
			context: GNodeInputCoercionContext<Any>
		): Any? =
			context.variableValues.getOrElseNullable(value.name) {
				coerceDefaultValue(
					typeRef = typeRef,
					parentType = parentType,
					field = field,
					argument = argument,
					context = context
				)
			}
	}
}
