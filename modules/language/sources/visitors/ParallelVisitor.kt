package io.fluidsonic.graphql

private class ParallelVisitor<Data>(
	private val children: List<Visitor<Unit, Data>>,
	private val enter: (node: GNode, data: Data) -> Unit,
	private val leave: (data: Data) -> Unit,
) : Visitor<Unit, Data>() {

	// FIXME We must use Visit to orchestrate node traversal rather than doing so by ourselves in ParallelVisit.
	override fun onNode(node: GNode, data: Data, visit: Visit) = ParallelVisit(root = node, data = data, children = children, enter = enter, leave = leave).run()
		.also { visit.skipChildren() }
}

/**
 * Runs every visitor of [children] over the subtree rooted at `root`, one node at a time and every visitor in list
 * order for that node.
 *
 * The traversal is iterative: [run] loops over an explicit stack of [Frame]s rather than descending by recursing
 * through the visitors. A level of nesting therefore costs one heap frame instead of one call stack frame per
 * visitor, which is what lets a deeply nested document be traversed at all. It is also why work that must happen
 * after a node's subtree has to be registered with [Visit.afterChildren]: [Visit.visitChildren] returns long before
 * the subtree has been traversed.
 */
private class ParallelVisit<Data>(
	root: GNode,
	private val data: Data,
	private val children: List<Visitor<Unit, Data>>,
	private val enter: (node: GNode, data: Data) -> Unit,
	private val leave: (data: Data) -> Unit,
) {

	init {
		require(children.isNotEmpty()) { "'children' must not be empty." }
	}

	private var abortedVisitorCount = 0
	private val abortedVisitors = BooleanArray(children.size)
	private val frames = mutableListOf<Frame>()

	// For every visitor the depth of the node whose children it skipped, or -1 while it is not skipping. It keeps
	// skipping until that node is left, so it sees no node of that subtree while the other visitors still do.
	private val skippedAtDepths = IntArray(children.size) { -1 }

	private val visits = children.mapIndexed { index, visitor -> ChildVisit(parent = this, visitor = visitor, index = index) }
	private val walker = root.walk()

	fun run() {
		try {
			// The root never arrives through the child loop below — `DefaultNodeWalker` starts with `child = root`.
			push(node = checkNotNull(walker.child) { "Walker inconsistency." }, data = Array(children.size) { data })

			var isRunning = true
			while (isRunning && frames.isNotEmpty()) {
				isRunning = step()
			}
		} finally {
			// Every node entered must be left even when the traversal is abandoned, or `enter` and `leave` stop
			// pairing up and the caller's state stays advanced. Blocks registered with `afterChildren` are dropped
			// here on purpose: the traversal did not complete.
			while (frames.isNotEmpty()) {
				frames.removeAt(frames.size - 1)

				leave(data)
			}
		}
	}

	/** Advances the traversal by one node, and returns whether it should continue. */
	private fun step(): Boolean {
		val frame = frames.last()

		if (!frame.hasDescended) {
			// Traversal stops only once *every* visitor has aborted — until then the others still have work to do.
			val isAbandoned = abortedVisitorCount == visits.size
			if (!isAbandoned) {
				if (shouldDescend() && walker.descend()) {
					frame.hasDescended = true
				} else {
					pop()
				}
			}

			return !isAbandoned
		}

		val child = walker.nextChild()
		if (child !== null) {
			push(node = child, data = frame.data.copyOf())
		} else {
			walker.ascend()
			pop()
		}

		return true
	}

	private fun push(node: GNode, data: Array<Any?>) {
		enter(node, this.data)

		frames += Frame(data = data)

		// Flat, not nested: every visitor's `onNode` runs and returns before the next visitor sees the node, and the
		// subtree is traversed by the loop afterwards rather than from within the last visitor's frame.
		for (index in visits.indices) {
			if (abortedVisitors[index] || skippedAtDepths[index] >= 0) {
				continue
			}

			visits[index].dispatch(node = node)
		}
	}

	private fun pop() {
		val depth = frames.size
		val frame = frames.removeAt(depth - 1)

		runAfterChildren(frame, depth = depth)

		for (index in skippedAtDepths.indices) {
			if (skippedAtDepths[index] == depth) {
				skippedAtDepths[index] = -1
			}
		}

		leave(data)
	}

	private fun runAfterChildren(frame: Frame, depth: Int) {
		// Visiting order across visitors, registration order within one visitor — which is the order in which the
		// blocks were appended, so replaying the list is enough. `visitInParallel` calls its visitors' `leave` in
		// the same order it calls their `enter`.
		frame.afterChildren.forEach { registered ->
			// A visitor that skipped this node's children, or that has aborted, does not leave the node at all — the
			// traversal leaves it on behalf of the visitors that are still descending. Mirrors `visitInParallel`,
			// which emits no `leave` for either.
			val hasLeft = abortedVisitors[registered.visitorIndex] || skippedAtDepths[registered.visitorIndex] == depth
			if (!hasLeft) {
				registered.block()
			}
		}
	}

	// One visitor skipping a node's children must not hide them from the others, so the traversal descends unless
	// every visitor is skipping. A visitor that has aborted counts as not skipping: it merely sees nothing anymore,
	// and the nodes it would have hidden are still due to the others.
	private fun shouldDescend() = skippedAtDepths.any { it < 0 }

	private fun onVisitorAbort(index: Int) {
		abortedVisitors[index] = true
		abortedVisitorCount += 1

		// An abort supersedes a skip, so that the visitor no longer holds the subtree back from the others.
		skippedAtDepths[index] = -1
	}

	private fun onVisitorSkipChildren(index: Int) {
		skippedAtDepths[index] = frames.size
	}

	/** The traversal state of one node that must outlive the visitors' calls for it. */
	private class Frame(
		/** The data each visitor is given for this node, and passes on to its children unless it replaces it. */
		val data: Array<Any?>,
	) {

		val afterChildren = mutableListOf<RegisteredBlock>()
		var hasDescended = false
	}

	private class RegisteredBlock(val visitorIndex: Int, val block: () -> Unit)

	private class ChildVisit<Data>(private val parent: ParallelVisit<Data>, private val visitor: Visitor<Unit, Data>, private val index: Int) : Visit {

		// How the visitor used this [Visit] for the node currently being dispatched. Everything that outlives one
		// dispatch — whether the visitor has aborted, whether it is skipping a subtree — lives in [ParallelVisit].
		//
		// The state is [State.idle] whenever no dispatch is in progress, which is what lets every method below reject
		// a [Visit] used outside the `onNode` it was handed to. It also means the node being dispatched is always the
		// top frame, so that is where these methods read and write per-node state.
		private var state = State.idle

		@Suppress("UNCHECKED_CAST")
		fun dispatch(node: GNode) {
			state = State.beforeVisitingChildren

			try {
				visitor.onNode(node = node, data = parent.frames.last().data[index] as Data, visit = this)
			} finally {
				state = State.idle
			}
		}

		override fun abort() {
			check(state !== State.idle) { ".abort() cannot be called here." }

			if (isAborting) {
				return
			}

			state = State.aborted

			parent.onVisitorAbort(index)
		}

		override fun afterChildren(block: () -> Unit) {
			check(state !== State.idle) { ".afterChildren() cannot be called here." }

			parent.frames.last().afterChildren += RegisteredBlock(visitorIndex = index, block = block)
		}

		override val hasVisitedChildren
			get() = state === State.afterVisitingChildren

		override val isAborting
			get() = parent.abortedVisitors[index]

		override val isSkippingChildren
			get() = isAborting || parent.skippedAtDepths[index] >= 0

		override fun skipChildren() {
			check(state !== State.idle) { ".skipChildren() cannot be called here." }
			check(state !== State.afterVisitingChildren) { ".skipChildren() cannot be called after .visitChildren() for the same node." }

			if (state === State.aborted || state === State.skippingChildren) {
				return
			}

			state = State.skippingChildren

			parent.onVisitorSkipChildren(index)
		}

		override fun visitChildren() = visitChildren(data = parent.frames.last().data[index])

		private fun visitChildren(data: Any?) {
			check(state !== State.idle) { ".visitChildren() cannot be called here." }
			check(state !== State.afterVisitingChildren) { "Cannot call .visitChildren() multiple times for the same node." }

			if (state === State.aborted || state === State.skippingChildren) {
				return
			}

			state = State.afterVisitingChildren

			// Descending is what the loop does anyway. All this contributes is the data the subtree is visited with,
			// and the record that the visitor asked for the descent, which `hasVisitedChildren` reports.
			parent.frames.last().data[index] = data
		}

		override fun __unsafeVisitChildren(data: Any?) = visitChildren(data = data)

		private enum class State {

			aborted,
			afterVisitingChildren,
			beforeVisitingChildren,
			idle,
			skippingChildren,
		}
	}
}

@InternalGraphqlApi
public fun <Data> Iterable<Visitor<Unit, Data>>.parallelize(): Visitor<Unit, Data> = parallelize(enter = { _, _ -> }, leave = {})

/**
 * Like [parallelize], but advances the [VisitorContext] exactly once per node — before the first visitor sees that
 * node and restored only after every visitor has finished with it and its entire subtree.
 *
 * This exists as a separate function rather than as a `parallelize().contextualize()` composition because
 * [ParallelVisitor] drives the traversal itself, so an outer contextualizing visitor would only ever see the root.
 */
@InternalGraphqlApi
public fun <Data : VisitorContext> Iterable<Visitor<Unit, Data>>.parallelizeContextualized(): Visitor<Unit, Data> =
	parallelize(enter = { node, context -> context.enter(node) }, leave = { context -> context.leave() })

private fun <Data> Iterable<Visitor<Unit, Data>>.parallelize(enter: (node: GNode, data: Data) -> Unit, leave: (data: Data) -> Unit): Visitor<Unit, Data> =
	toList()
		.ifEmpty { null }
		?.let { ParallelVisitor(children = it, enter = enter, leave = leave) }
		?: Visitor.noOp()
