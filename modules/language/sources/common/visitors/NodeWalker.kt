package io.fluidsonic.graphql


// Children must be traversed in the order they would occur in a proper GraphQL document.
@InternalGraphqlApi
public interface NodeWalker {

	public val child: GNode?
	public val parent: GNode?

	public fun ascend(): Boolean
	public fun descend(): Boolean
	public fun nextChild(): GNode?
}
