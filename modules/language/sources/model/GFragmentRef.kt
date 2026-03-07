package io.fluidsonic.graphql


/**
 * A lightweight reference to a named GraphQL fragment, identified by its [name].
 *
 * Used to refer to a [GFragmentDefinition] without holding a direct reference to the full AST node.
 */
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
