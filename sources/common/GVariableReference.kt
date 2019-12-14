package io.fluidsonic.graphql


class GVariableReference(val name: String) {

	override fun equals(other: Any?) =
		this === other || (other is GVariableReference && name == other.name)


	override fun hashCode() =
		name.hashCode()


	override fun toString() =
		"$$name"
}
