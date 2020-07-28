package io.fluidsonic.graphql


// Children must be traversed in the order they would occur in a proper GraphQL document.
@InternalGraphqlApi
interface NodeWalker {

	val child: GNode?
	val parent: GNode?

	fun ascend(): Boolean
	fun descend(): Boolean
	fun nextChild(): GNode?
}
