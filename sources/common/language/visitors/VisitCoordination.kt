package io.fluidsonic.graphql


internal interface Visit<out Result> {

	fun run(): Result


	companion object
}


internal interface VisitCoordination<in Data> {

	var automaticallyVisitsChildren: Boolean
	val isAborting: Boolean
	val isSkippingChildren: Boolean

	fun abort()
	fun skipChildren()
	fun visitChildren(): Boolean
	fun visitChildren(data: Data): Boolean


	companion object;


	abstract class Wrapped<in Data, in TransformedData>(
		protected val next: VisitCoordination<TransformedData>
	) : VisitCoordination<Data> {

		override var automaticallyVisitsChildren: Boolean
			get() = next.automaticallyVisitsChildren
			set(value) {
				next.automaticallyVisitsChildren = value
			}

		override val isAborting get() = next.isAborting
		override val isSkippingChildren get() = next.isSkippingChildren


		override fun abort() =
			next.abort()


		override fun skipChildren() =
			next.skipChildren()


		override fun visitChildren() =
			next.visitChildren()
	}
}


private class ParallelVisitCoordination<Data>(
	node: GAst,
	data: Data,
	dispatchers: List<VisitDispatcher<Unit, Data>>
) : Visit<Unit> {

	init {
		require(dispatchers.isNotEmpty()) { "'dispatchers' must not be empty." }
	}

	private var childIndex = 0
	private var state = State.initial
	private val walker = node.walk()

	private val children = dispatchers.mapIndexed { index, dispatcher ->
		DispatcherInfo(
			parent = this,
			dispatcher = dispatcher,
			index = index,
			data = data
		)
	}


	val isAborting
		get() = state == State.aborted


	private fun runDescendent() {
		check(childIndex == children.size)

		println("$childIndex: runDescendent()")

		if (walker.descend()) {
			childIndex = 0
			walker.nextChild()

			runChildren()

			walker.ascend()
		}

		println("$childIndex: return from runDescendent()")
	}


	private fun runChildren() {
		check(childIndex == 0)

		println("$childIndex: runChildren()")

		var node = walker.child
		while (node !== null) {
			childIndex = 0

			do runChild(node = node)
			while (childIndex < children.size)
			println("$childIndex: return from runChild() LOOP after ${node::class}")

			node = walker.nextChild()
		}

		println("$childIndex: return from runChildren()")
	}


	private fun runChild(node: GAst) {
		println("$childIndex: runChild() { node: ${node::class} }")

		val index = childIndex
		childIndex += 1

		children[index].dispatchVisit(node = node)

		println("$childIndex: return from runChild() (from $index)")
	}


	private fun next() {
		println("$childIndex: next()")

		if (childIndex == children.size)
			runDescendent()
		else
			runChild(node = checkNotNull(walker.child) { "Internal inconsistency." })
	}


	private fun onChildAbort() {
		if (children.all { it.state === DispatcherInfo.State.aborted })
			state = State.aborted
		else
			next()
	}


	private fun onChildSkipChildren() {
		if (children.any { it.state !== DispatcherInfo.State.skippingChildren })
			next()
	}


	private fun onChildVisitChildren() {
		next()
	}


	override fun run() {
		check(state == State.initial) { "A Visit cannot be run multiple times." }

		state = State.visiting

		try {
			next()

			state = State.completed
		}
		catch (e: Throwable) {
			state = State.aborted

			throw e
		}
	}


	private class DispatcherInfo<Data>(
		private val parent: ParallelVisitCoordination<Data>,
		val dispatcher: VisitDispatcher<Unit, Data>,
		val index: Int,
		var data: Data,
		override var automaticallyVisitsChildren: Boolean = true,
		var state: State = State.initial
	) : VisitCoordination<Data> {

		override fun abort() {
			println("${index}: abort() { state: $state }")

			when (state) {
				State.aborted ->
					return

				State.afterVisitingChildren,
				State.beforeVisitingChildren,
				State.skippingChildren -> {
					state = State.aborted

					parent.onChildAbort()
				}

				State.completed,
				State.initial ->
					error(".abort() cannot be called here.")
			}
		}


		fun dispatchVisit(node: GAst) {
			println("${index}: dispatchVisit() { state: $state }")

			if (isSkippingChildren)
				return

			val previousState = state

			this.state = State.beforeVisitingChildren

			try {
				dispatcher.dispatchVisit(node = node, data = data, coordination = this)

				if (state === State.beforeVisitingChildren && automaticallyVisitsChildren)
					visitChildren()
			}
			finally {
				if (!isAborting)
					this.state = previousState
			}
		}


		override val isAborting
			get() = state === State.aborted


		override val isSkippingChildren
			get() = isAborting || state === State.skippingChildren


		override fun skipChildren() {
			println("${index}: skipChildren() { state: $state }")

			when (state) {
				State.aborted,
				State.skippingChildren ->
					return

				State.afterVisitingChildren ->
					error(".skipChildren() cannot be called after .visitChildren() for the same node.")

				State.beforeVisitingChildren -> {
					state = State.skippingChildren

					parent.onChildSkipChildren()
				}

				State.completed,
				State.initial ->
					error(".skipChildren() cannot be called here.")
			}
		}


		override fun visitChildren() =
			visitChildren(data = data)


		override fun visitChildren(data: Data): Boolean {
			println("${index}: visitChildren(data: $data) { state: $state }")

			return when (state) {
				State.aborted,
				State.skippingChildren ->
					false

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
				State.initial ->
					error(".visitChildren() cannot be called here.")
			}
		}


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


// FIXME nonsense
internal fun <Data> Iterable<Visitor<Unit, Data>>.parallelize(): Visitor<Unit, Data> =
	ParallelVisitCoordination(
		dispatchers = map(::VisitDispatcher),
		node = node,
		data = data
	)
