package io.fluidsonic.graphql


@InternalGraphqlApi
public interface Visit {

	public val hasVisitedChildren: Boolean
	public val isAborting: Boolean
	public val isSkippingChildren: Boolean

	public fun abort()
	public fun skipChildren()
	public fun visitChildren()


	@Suppress("FunctionName")
	public fun __unsafeVisitChildren(data: Any?) // FIXME How to make this generic with type projection issues in the Visitor?
}
