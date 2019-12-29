package io.fluidsonic.graphql


internal interface VisitDispatcher<out Result, Data> {

	fun dispatchVisit(node: GAst, data: Data, coordination: VisitCoordination<Data>): Result


	companion object
}


@Suppress("UNCHECKED_CAST")
internal fun <Result, Data> VisitDispatcher(visitor: Visitor<Result, Data>) =
	visitor as? VisitDispatcher<Result, Data>
		?: object : VisitDispatcher<Result, Data> {

			override fun dispatchVisit(node: GAst, data: Data, coordination: VisitCoordination<Data>) =
				visitor.dispatchVisit(node = node, data = data, coordination = coordination)
		}
