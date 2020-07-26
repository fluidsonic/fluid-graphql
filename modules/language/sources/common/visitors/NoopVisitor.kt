package io.fluidsonic.graphql


private object NoopVisitor : Visitor<Unit, Any?>() {

	override fun onNode(node: GNode, data: Any?, visit: Visit) =
		Unit
}


internal fun <Data> Visitor.Companion.noop(): Visitor<Unit, Data> =
	NoopVisitor
