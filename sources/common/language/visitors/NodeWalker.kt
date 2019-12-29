package io.fluidsonic.graphql


// Children must be traversed in the order they would occur in a proper GraphQL document.
internal interface NodeWalker {

	val child: GAst?
	val parent: GAst?

	fun ascend(): Boolean
	fun descend(): Boolean
	fun nextChild(): GAst?
}


private class NodeWalkerImpl(root: GAst) : NodeWalker {

	private var childIndex = -1
	private val childIndexStack = mutableListOf<Int>()
	private val childStack = mutableListOf<GAst?>()
	private val parentStack = mutableListOf<GAst?>()


	override var child: GAst? = root
		private set


	override var parent: GAst? = null
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


	override fun nextChild(): GAst? {
		val parent = parent ?: return null

		if (childIndex >= 0 && child === null)
			return null

		childIndex += 1
		child = parent.childAt(childIndex)

		return child
	}
}


internal fun GAst.walk(): NodeWalker =
	NodeWalkerImpl(root = this)
