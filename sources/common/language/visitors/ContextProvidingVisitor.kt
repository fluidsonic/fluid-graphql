package io.fluidsonic.graphql


private class ContextProvidingVisitor<out Result, in Context : VisitorContext>(
	private val next: Visitor<Result, Context>
) : Visitor<Result, Context>() {

	override fun onNode(node: GNode, data: Context, visit: Visit) =
		data.with(node) {
			next.onNode(node = node, data = data, visit = visit)
		}
}


internal fun <Result, Context : VisitorContext> Visitor<Result, Context>.contextualize(): Visitor<Result, Context> =
	ContextProvidingVisitor(next = this)


internal fun <Result, Context : VisitorContext> Visitor<Result, Context>.contextualize(context: Context): Visitor<Result, Any?> =
	Visitor.ofResult(context).then(ContextProvidingVisitor(next = this))
