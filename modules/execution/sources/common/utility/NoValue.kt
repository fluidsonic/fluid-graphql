package io.fluidsonic.graphql


internal object NoValue {

	inline fun <Value> handle(value: Value, ifNoValue: () -> Value): Value =
		when (value) {
			NoValue -> ifNoValue()
			else -> value
		}


	override fun toString() =
		"<no value>"
}
