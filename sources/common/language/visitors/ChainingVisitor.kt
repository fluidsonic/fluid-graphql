package io.fluidsonic.graphql


private class ChainingVisitor<out Result, IntermediateResult, in Data>(
	private val current: Visitor<IntermediateResult, Data>,
	private val nextCoordinator: VisitCoordinator<Result, IntermediateResult>
) : Visitor<Result, Data>() {

	// FIXME won't work as children will recursively call this too
	override fun onNode(node: GAst, data: Data, visit: Visit) =
		nextCoordinator.visit(node = node, data = current.onNode(node, data = data, visit = visit))
}


internal fun <Result, IntermediateResult, Data> Visitor<IntermediateResult, Data>.then(
	next: Visitor<Result, IntermediateResult>
): Visitor<Result, Data> =
	ChainingVisitor(current = this, nextCoordinator = VisitCoordinator.default(next))


internal fun <Result, IntermediateResult, Data> Visitor<IntermediateResult, Data>.then(
	nextCoordinator: VisitCoordinator<Result, IntermediateResult>
): Visitor<Result, Data> =
	ChainingVisitor(current = this, nextCoordinator = nextCoordinator)
