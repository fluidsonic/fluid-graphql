package io.fluidsonic.graphql

@InternalGraphqlApi
public interface Visit {

	public val hasVisitedChildren: Boolean
	public val isAborting: Boolean
	public val isSkippingChildren: Boolean

	public fun abort()

	/**
	 * Registers [block] to run once this visitor leaves the current node, after that node's children have been
	 * traversed for it.
	 *
	 * This replaces writing code after [visitChildren]: a coordinator may traverse the children iteratively rather
	 * than inside the [visitChildren] call, in which case that call returns before the subtree has been visited and
	 * only a block registered here still runs afterwards.
	 *
	 * [block] runs at most once, and only for the visitor that registered it. Registering multiple blocks for the
	 * same node is allowed; they run in registration order, and across the visitors of a parallel traversal in
	 * visiting order — so a visitor's blocks run before those of every visitor that saw the node later.
	 *
	 * Registering a block does not by itself cause the children to be traversed. This [Visit] must not be used from
	 * within [block]: the visit of the node has finished by then, so every method of it fails.
	 *
	 * The block does **not** run when this visitor skipped *that* node's children, nor when this visitor has aborted
	 * by the time the node is left. Skipping ends this visitor's business with the node there and then, even though
	 * the traversal goes on to leave the node on behalf of the visitors still descending — and it is per node, so
	 * this visitor's blocks for that node's ancestors are unaffected. Both mirror `visitInParallel`, which emits no
	 * `leave` for a visitor that skipped a node's children, nor for one that broke out of the traversal. A block
	 * likewise does not run if the traversal is abandoned rather than completed — once every visitor has aborted, or
	 * an exception unwinds it.
	 *
	 * Registering a block and then calling [skipChildren] therefore drops it silently.
	 */
	public fun afterChildren(block: () -> Unit)

	public fun skipChildren()
	public fun visitChildren()

	@Suppress("FunctionName")
	public fun __unsafeVisitChildren(data: Any?) // FIXME How to make this generic with type projection issues in the Visitor?
}
