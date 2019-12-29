package io.fluidsonic.graphql


internal interface VisitCoordinator<Result, Data> {

	fun coordinate(node: GAst, data: Data, dispatcher: VisitDispatcher<Result, Data>): Visit<Result>


	companion object {

		@Suppress("UNCHECKED_CAST")
		fun <Result, Data> default() =
			DefaultVisitCoordinator as VisitCoordinator<Result, Data>
	}
}


private object DefaultVisitCoordinator : VisitCoordinator<Any?, Any?> {

	override fun coordinate(node: GAst, data: Any?, dispatcher: VisitDispatcher<Any?, Any?>) =
		Coordination(node = node, data = data, dispatcher = dispatcher)


	private class Coordination<Result, Data>(
		private val dispatcher: VisitDispatcher<Result, Data>,
		private val data: Data,
		node: GAst
	) : Visit<Result>, VisitCoordination<Data> {

		private var result: Result? = null
		private var state = State.initial
		private var walker = node.walk()

		override var automaticallyVisitsChildren = true


		override fun abort() {
			when (state) {
				State.aborted ->
					return

				State.afterVisitingChildren,
				State.beforeVisitingChildren,
				State.skippingChildren ->
					state = State.aborted

				State.completed,
				State.initial ->
					error(".abort() cannot be called here.")
			}
		}


		private fun dispatchVisit(node: GAst, data: Data) {
			if (isAborting)
				return

			// We don't put `this.data` on the stack.
			// It's only relevant in `visitChildren()`, which has already been called at this point, and it can only be called at most once per node.

			val previousState = state

			this.state = State.beforeVisitingChildren

			try {
				result = dispatcher.dispatchVisit(node = node, data = data, coordination = this)

				if (state === State.beforeVisitingChildren && automaticallyVisitsChildren)
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


		override val isAborting
			get() = state === State.aborted


		override val isSkippingChildren
			get() = isAborting || state === State.skippingChildren


		override fun run(): Result {
			check(state == State.initial) { "A Visit cannot be run multiple times." }

			try {
				dispatchVisit(
					node = walker.child ?: error("Walker inconsistency."),
					data = data
				)

				state = State.completed

				@Suppress("UNCHECKED_CAST")
				return result as Result
			}
			catch (e: Throwable) {
				state = State.aborted

				throw e
			}
		}


		override fun skipChildren() {
			when (state) {
				State.aborted,
				State.skippingChildren ->
					return

				State.afterVisitingChildren ->
					error(".skipChildren() cannot be called after .visitChildren() for the same node.")

				State.beforeVisitingChildren ->
					state = State.skippingChildren

				State.completed,
				State.initial ->
					error(".skipChildren() cannot be called here.")
			}
		}


		override fun visitChildren() =
			visitChildren(data = data)


		override fun visitChildren(data: Data): Boolean =
			when (state) {
				State.aborted,
				State.skippingChildren ->
					false

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

					!isAborting
				}

				State.completed,
				State.initial ->
					error(".visitChildren() cannot be called here.")
			}


		private enum class State {

			aborted,
			afterVisitingChildren,
			beforeVisitingChildren,
			completed,
			initial,
			skippingChildren,
		}
	}
}
