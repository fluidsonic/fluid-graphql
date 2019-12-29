package io.fluidsonic.graphql


private class ChainingVisitor<out Result, IntermediateResult, Data>(
	private val first: Visitor<IntermediateResult, Data>,
	private val second: Visitor<Result, IntermediateResult>
) : CustomizedVisitor<Result, Data>() {

	override fun dispatchVisit(node: GAst, data: Data, coordination: VisitCoordination<Data>) =
		node.accept(second, data = node.accept(first, data = data))
}


@Suppress("UNCHECKED_CAST")
internal fun <Result, IntermediateResult, Data> Visitor<IntermediateResult, Data>.then(next: Visitor<Result, IntermediateResult>): Visitor<Result, Data> =
	when {
		this is Visitor.Identity<*> -> next as Visitor<Result, Data>  // Data === IntermediateResult
		next is Visitor.Identity<*> -> this as Visitor<Result, Data>  // IntermediateResult === Result
		else -> ChainingVisitor(first = this, second = next)
	}
