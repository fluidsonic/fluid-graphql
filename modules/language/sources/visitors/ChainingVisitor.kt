package io.fluidsonic.graphql


private class ChainingVisitor<out Result, IntermediateResult, in Data>(
	private val current: Visitor<IntermediateResult, Data>,
	private val nextCoordinator: VisitCoordinator<Result, IntermediateResult>
) : Visitor<Result, Data>() {

	override fun onNode(node: GNode, data: Data, visit: Visit) =
		nextCoordinator.visit(node = node, data = current.onNode(node, data = data, visit = visit))
			.also { visit.abort() } // we're only interested in the root node
}


@InternalGraphqlApi
public fun <Result, IntermediateResult, Data> Visitor<IntermediateResult, Data>.then(
	next: Visitor<Result, IntermediateResult>
): Visitor<Result, Data> =
	ChainingVisitor(current = this, nextCoordinator = VisitCoordinator.default(next))


@InternalGraphqlApi
public fun <Result, IntermediateResult, Data> Visitor<IntermediateResult, Data>.then(
	nextCoordinator: VisitCoordinator<Result, IntermediateResult>
): Visitor<Result, Data> =
	ChainingVisitor(current = this, nextCoordinator = nextCoordinator)
