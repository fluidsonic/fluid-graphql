package io.fluidsonic.graphql


private class DefaultVisitCoordinator<Result, in Data>(
	private val visitor: Visitor<Result, Data>,
) : VisitCoordinator<Result, Data> {

	override fun visit(node: GNode, data: Data) =
		DefaultVisit(node = node, data = data, visitor = visitor).visit()
}


private class DefaultVisit<Result, Data>(
	node: GNode,
	private val data: Data,
	private val visitor: Visitor<Result, Data>,
) : Visit {

	private var result: Result? = null
	private var state = State.initial
	private var walker = node.walk()


	override fun abort() {
		when (state) {
			State.aborted ->
				return

			State.afterVisitingChildren,
			State.beforeVisitingChildren,
			State.skippingChildren,
			->
				state = State.aborted

			State.completed,
			State.initial,
			->
				error(".abort() cannot be called here.")
		}
	}


	private fun dispatchVisit(node: GNode, data: Data) {
		if (isAborting)
			return

		// We don't put `this.data` on the stack.
		// It's only relevant in `visitChildren()`, which has already been called at this point, and it can only be called at most once per node.

		val previousState = state

		this.state = State.beforeVisitingChildren

		try {
			result = visitor.onNode(node = node, data = data, visit = this)

			if (state === State.beforeVisitingChildren)
				visitChildren()
		}
		finally {
			if (!isAborting)
				this.state = previousState
		}
	}


	private fun dispatchChildVisits(data: Data) {
		if (walker.descend())
			try {
				var child = walker.nextChild()
				while (child !== null) {
					if (isAborting)
						break

					dispatchVisit(node = child, data = data)

					child = walker.nextChild()
				}
			}
			finally {
				walker.ascend()
			}
	}


	override val hasVisitedChildren
		get() = state == State.afterVisitingChildren


	override val isAborting
		get() = state === State.aborted


	override val isSkippingChildren
		get() = isAborting || state === State.skippingChildren


	override fun skipChildren() {
		when (state) {
			State.aborted,
			State.skippingChildren,
			->
				return

			State.afterVisitingChildren ->
				error(".skipChildren() cannot be called after .visitChildren() for the same node.")

			State.beforeVisitingChildren ->
				state = State.skippingChildren

			State.completed,
			State.initial,
			->
				error(".skipChildren() cannot be called here.")
		}
	}


	fun visit() =
		try {
			dispatchVisit(
				node = walker.child ?: error("Walker inconsistency."),
				data = data
			)

			state = State.completed

			@Suppress("UNCHECKED_CAST")
			result as Result
		}
		catch (e: Throwable) {
			state = State.aborted

			throw e
		}


	override fun visitChildren() =
		visitChildren(data = data)


	private fun visitChildren(data: Data) =
		when (state) {
			State.aborted,
			State.skippingChildren,
			->
				Unit

			State.afterVisitingChildren ->
				error("Cannot call .visitChildren() multiple times for the same node.")

			State.beforeVisitingChildren -> {
				try {
					dispatchChildVisits(data = data)
				}
				finally {
					if (!isAborting)
						state = State.afterVisitingChildren
				}
			}

			State.completed,
			State.initial,
			->
				error(".visitChildren() cannot be called here.")
		}


	@Suppress("UNCHECKED_CAST")
	override fun __unsafeVisitChildren(data: Any?) =
		visitChildren(data = data as Data)


	private enum class State {

		aborted,
		afterVisitingChildren,
		beforeVisitingChildren,
		completed,
		initial,
		skippingChildren,
	}
}


@InternalGraphqlApi
public fun <Result, Data> VisitCoordinator.Companion.default(visitor: Visitor<Result, Data>): VisitCoordinator<Result, Data> =
	DefaultVisitCoordinator(visitor)
