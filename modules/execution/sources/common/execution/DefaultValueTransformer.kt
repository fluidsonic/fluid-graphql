package io.fluidsonic.graphql


// FIXME

// default for custom scalars
//parseValue: (GCoercionContext<*>.(value: Any) -> Any?)? = Any?::identity,
//parseValueNode: (GCoercionContext<*>.(value: GValue) -> Any?)? = parseValue?.let {
//	{ value -> value.unwrap()?.let { parseValue(it) } }
//},
//serializeValue: (GCoercionContext<*>.(value: Any) -> Any?)? = Any?::identity,


// default for enum
//parseValue: (GCoercionContext<*>.(value: Any) -> Any?)? = { value ->
//	(value as? String)?.takeIf { valueName -> values.any { it.name == valueName } }
//},
//parseValueNode: (GCoercionContext<*>.(value: GValue) -> Any?)? = parseValue?.let {
//	{ value ->
//		(value as? GEnumValue)
//			.ifNull { error("GraphQL enum '${name.value}' expects an enum value literal but got ${value.kind}: $value") }
//			.let { parseValue(it) }
//	}
//},
//serializeValue: (GCoercionContext<*>.(value: Any) -> Any?)? = { value ->
//	(value as? String)
//		.ifNull { error("The default serializer for GraphQL enum '${name.value}' expects a String value but got ${value::class}: $value") }
//		.also { valueName ->
//			if (values.none { it.name == valueName })
//				error("'$valueName' is not a valid value for GraphQL enum '${name.value}'.")
//		}
//},

// FIXME
// input obj
// parseValue: (GCoercionContext<*>.(arguments: Map<String, Any?>) -> Any?) = Any?::identity,
