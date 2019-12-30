package io.fluidsonic.graphql


internal interface VisitCoordinator<out Result, in Data> {

	fun visit(node: GNode, data: Data): Result


	companion object
}
