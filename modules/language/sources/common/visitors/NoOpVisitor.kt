package io.fluidsonic.graphql


private object NoOpVisitor : Visitor<Unit, Any?>() {

	override fun onNode(node: GNode, data: Any?, visit: Visit) =
		Unit
}


@InternalGraphqlApi
public fun <Data> Visitor.Companion.noOp(): Visitor<Unit, Data> =
	NoOpVisitor
