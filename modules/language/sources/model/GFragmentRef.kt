package io.fluidsonic.graphql


public class GFragmentRef(
	public val name: String,
) {

	override fun equals(other: Any?): Boolean =
		this === other || (other is GFragmentRef && name == other.name)


	override fun hashCode(): Int =
		name.hashCode()


	override fun toString(): String =
		name
}
