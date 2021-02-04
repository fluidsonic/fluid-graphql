package io.fluidsonic.graphql


@InternalGraphqlApi
public interface VisitCoordinator<out Result, in Data> {

	public fun visit(node: GNode, data: Data): Result


	public companion object
}
