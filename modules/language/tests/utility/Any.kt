package testing

import io.fluidsonic.graphql.GFieldSelection
import io.fluidsonic.graphql.GListType
import io.fluidsonic.graphql.GListValue
import io.fluidsonic.graphql.GName
import io.fluidsonic.graphql.GNode
import io.fluidsonic.graphql.GNonNullType
import io.fluidsonic.graphql.GObjectValue
import io.fluidsonic.graphql.GScalarType
import io.fluidsonic.graphql.GValue

fun identityOf(value: Any?): String = when (value) {
	null -> "null"
	is GListType -> "[${identityOf(value.elementType)}]"
	is GNonNullType -> identityOf(value.nullableType) + "!"
	is GScalarType -> value.name
	else -> buildString {
		append(value::class.simpleName)
		append("@")
		append(uniqueIdOf(value))

		val name = when (value) {
			is GFieldSelection -> value.name
			is GName -> value.value
			is GNode.WithOptionalName -> value.name
			is GListValue -> null
			is GObjectValue -> null
			is GValue -> value.toString()
			else -> null
		}
		if (name != null) {
			append("[")
			append(name)
			append("]")
		}
	}
}

fun uniqueIdOf(value: Any): String = System.identityHashCode(value).toString()
