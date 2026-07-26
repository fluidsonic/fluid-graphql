package io.fluidsonic.graphql

// `children` is snapshotted once per `descend()` rather than re-derived per child. `GNode.childAt()` rescans from
// index 0 through `forEachChild` on every call, so indexing it would make walking n siblings cost O(n²).
private class DefaultNodeWalker(root: GNode) : NodeWalker {

	private var childIndex = -1
	private val childIndexStack = mutableListOf<Int>()
	private var childList: List<GNode> = emptyList()
	private val childListStack = mutableListOf<List<GNode>>()
	private val childStack = mutableListOf<GNode?>()
	private val parentStack = mutableListOf<GNode?>()

	override var child: GNode? = root
		private set

	override var parent: GNode? = null
		private set

	override fun ascend(): Boolean {
		if (parentStack.isEmpty()) {
			return false
		}

		val stackIndex = parentStack.size - 1

		child = childStack.removeAt(stackIndex)
		childIndex = childIndexStack.removeAt(stackIndex)
		childList = childListStack.removeAt(stackIndex)
		parent = parentStack.removeAt(stackIndex)

		return true
	}

	override fun descend(): Boolean {
		val child = child
		val childList = child?.children().orEmpty()
		if (childList.isEmpty()) {
			return false
		}

		parentStack += parent
		childIndexStack += childIndex
		childListStack += this.childList
		childStack += child

		this.parent = child
		this.child = null
		this.childIndex = -1
		this.childList = childList

		return true
	}

	override fun nextChild(): GNode? {
		if (parent === null || (childIndex >= 0 && child === null)) {
			return null
		}

		childIndex += 1
		child = childList.getOrNull(childIndex)

		return child
	}
}

@InternalGraphqlApi
public fun GNode.walk(): NodeWalker = DefaultNodeWalker(root = this)
