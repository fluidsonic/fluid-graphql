package io.fluidsonic.graphql


@InternalGraphqlApi
interface VisitCoordinator<out Result, in Data> {

	fun visit(node: GNode, data: Data): Result


	companion object
}
