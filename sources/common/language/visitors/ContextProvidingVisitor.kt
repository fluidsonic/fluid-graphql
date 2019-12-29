package io.fluidsonic.graphql


private class ContextProvidingVisitor<out Result, Context : VisitorContext>(
	private val next: Visitor<Result, Context>
) : CustomizedVisitor<Result, Context>() {

	override fun dispatchVisit(node: GAst, data: Context, coordination: VisitCoordination<Context>) =
		data.with(node) {
			next.dispatchVisit(node = node, data = data, coordination = coordination)
		}
}


internal fun <Result, Context : VisitorContext> Visitor<Result, Context>.contextualize(): Visitor<Result, Context> =
	ContextProvidingVisitor(next = this)
