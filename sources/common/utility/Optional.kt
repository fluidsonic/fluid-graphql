package io.fluidsonic.graphql


// FIXME fluid-stdlib
data class Optional<out Value>(
	val value: Value
) {

	inline fun <MappedValue : Any> mapValue(mapping: (Value) -> MappedValue) =
		Optional(value.let(mapping))


	companion object
}
