package io.fluidsonic.graphql


private class DefaultNodeWalker(root: GNode) : NodeWalker {

	private var childIndex = -1
	private val childIndexStack = mutableListOf<Int>()
	private val childStack = mutableListOf<GNode?>()
	private val parentStack = mutableListOf<GNode?>()


	override var child: GNode? = root
		private set


	override var parent: GNode? = null
		private set


	override fun ascend(): Boolean {
		if (parentStack.isEmpty())
			return false

		val stackIndex = parentStack.size - 1

		child = childStack.removeAt(stackIndex)
		childIndex = childIndexStack.removeAt(stackIndex)
		parent = parentStack.removeAt(stackIndex)

		return true
	}


	override fun descend(): Boolean {
		val child = child
		if (child === null || !child.hasChildren())
			return false

		parentStack += parent
		childIndexStack += childIndex
		childStack += child

		this.parent = child
		this.child = null
		this.childIndex = -1

		return true
	}


	override fun nextChild(): GNode? {
		val parent = parent ?: return null

		if (childIndex >= 0 && child === null)
			return null

		childIndex += 1
		child = parent.childAt(childIndex)

		return child
	}
}


@InternalGraphqlApi
public fun GNode.walk(): NodeWalker =
	DefaultNodeWalker(root = this)
