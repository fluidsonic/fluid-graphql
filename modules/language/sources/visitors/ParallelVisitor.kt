package io.fluidsonic.graphql


private class ParallelVisitor<Data>(
	private val children: List<Visitor<Unit, Data>>,
) : Visitor<Unit, Data>() {

	// FIXME We must use Visit to orchestrate node traversal rather than doing so by ourself in ParallelVisit.
	override fun onNode(node: GNode, data: Data, visit: Visit) =
		ParallelVisit(node = node, data = data, children = children).run()
			.also { visit.skipChildren() }
}


private class ParallelVisit<in Data>(
	node: GNode,
	data: Data,
	children: List<Visitor<Unit, Data>>,
) {

	init {
		require(children.isNotEmpty()) { "'children' must not be empty." }
	}


	private var childIndex = 0
	private var state = State.initial
	private val walker = node.walk()

	private val children = children.mapIndexed { index, dispatcher ->
		ChildVisit(
			parent = this,
			visitor = dispatcher,
			index = index,
			data = data
		)
	}


	private val isAborting
		get() = state == State.aborted


	private fun onChildAbort() {
		if (children.all { it.state === ChildVisit.State.aborted })
			state = State.aborted
		else
			runNext()
	}


	private fun onChildSkipChildren() {
		if (children.any { it.state !== ChildVisit.State.skippingChildren })
			runNext()
	}


	private fun onChildVisitChildren() {
		runNext()
	}


	fun run() {
		state = State.visiting

		try {
			runNext()

			state = State.completed
		}
		catch (e: Throwable) {
			state = State.aborted

			throw e
		}
	}


	private fun runChild(node: GNode) {
		val index = childIndex
		childIndex += 1

		children[index].dispatchVisit(node = node)
	}


	private fun runChildren() {
		check(childIndex == 0)

		var node = walker.child
		while (node !== null) {
			childIndex = 0

			do runChild(node = node)
			while (childIndex < children.size)

			node = walker.nextChild()
		}
	}


	private fun runDescendent() {
		check(childIndex == children.size)

		if (walker.descend()) {
			childIndex = 0
			walker.nextChild()

			runChildren()

			walker.ascend()
		}
	}


	private fun runNext() {
		if (childIndex == children.size)
			runDescendent()
		else
			runChild(node = checkNotNull(walker.child) { "Internal inconsistency." })
	}


	private class ChildVisit<in Data>(
		private val parent: ParallelVisit<Data>,
		private val visitor: Visitor<Unit, Data>,
		private val index: Int,
		private var data: Data,
		state: State = State.initial,
	) : Visit {

		var state = state
			private set


		override fun abort() {
			when (state) {
				State.aborted ->
					return

				State.afterVisitingChildren,
				State.beforeVisitingChildren,
				State.skippingChildren,
				-> {
					state = State.aborted

					parent.onChildAbort()
				}

				State.completed,
				State.initial,
				->
					error(".abort() cannot be called here.")
			}
		}


		fun dispatchVisit(node: GNode) {
			if (isSkippingChildren)
				return

			val previousState = state

			state = State.beforeVisitingChildren

			try {
				visitor.onNode(node, data = data, visit = this)

				if (state === State.beforeVisitingChildren)
					visitChildren()
			}
			finally {
				if (!isAborting)
					state = previousState
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

				State.beforeVisitingChildren -> {
					state = State.skippingChildren

					parent.onChildSkipChildren()
				}

				State.completed,
				State.initial,
				->
					error(".skipChildren() cannot be called here.")
			}
		}


		override fun visitChildren() =
			visitChildren(data = data)


		private fun visitChildren(data: Data) {
			when (state) {
				State.aborted,
				State.skippingChildren,
				->
					Unit

				State.afterVisitingChildren ->
					error("Cannot call .visitChildren() multiple times for the same node.")

				State.beforeVisitingChildren -> {
					val previousData = this.data

					this.data = data

					try {
						parent.onChildVisitChildren()
					}
					finally {
						this.data = previousData

						if (!isAborting)
							state = State.afterVisitingChildren
					}

					!isAborting
				}

				State.completed,
				State.initial,
				->
					error(".visitChildren() cannot be called here.")
			}
		}


		@Suppress("UNCHECKED_CAST")
		override fun __unsafeVisitChildren(data: Any?) =
			visitChildren(data as Data)


		enum class State {

			aborted,
			afterVisitingChildren,
			beforeVisitingChildren,
			completed,
			initial,
			skippingChildren
		}
	}


	private enum class State {

		aborted,
		completed,
		initial,
		visiting
	}
}


@InternalGraphqlApi
public fun <Data> Iterable<Visitor<Unit, Data>>.parallelize(): Visitor<Unit, Data> =
	toList()
		.ifEmpty { null }
		?.let(::ParallelVisitor)
		?: Visitor.noOp()
