package testing

import io.fluidsonic.graphql.*


fun identityOf(value: Any?): String {
	if (value == null)
		return "null"

	return buildString {
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


expect fun uniqueIdOf(value: Any): Int
