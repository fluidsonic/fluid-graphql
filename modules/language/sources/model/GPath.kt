package io.fluidsonic.graphql


/**
 * An immutable path through a GraphQL response, composed of [Element.Name] (field name) and
 * [Element.Index] (list index) segments.
 *
 * Used in [GError.path] to pinpoint where in the response an error occurred.
 * Build paths incrementally with [addName] / [addIndex], or use [Builder] when constructing
 * a path during execution.
 */
// FIXME refactor
public class GPath(elements: List<Element> = emptyList()) {

	public val elements: List<Element> = elements.toList()


	/** Returns a new path with [index] appended as a list-index segment. */
	public fun addIndex(index: Int): GPath =
		GPath(elements + Element.Index(index))


	/** Returns a new path with [name] appended as a field-name segment. */
	public fun addName(name: String): GPath =
		GPath(elements + Element.Name(name))


	override fun equals(other: Any?): Boolean =
		(this === other || (other is GPath && elements == other.elements))


	override fun hashCode(): Int =
		elements.hashCode()


	override fun toString(): String =
		buildString {
			elements.forEachIndexed { index, element ->
				when (element) {
					is Element.Index -> {
						append('[')
						append(element.value)
						append(']')
					}

					is Element.Name -> {
						if (index > 0)
							append('.')

						append(element.value)
					}
				}
			}
		}


	public companion object {

		/** An empty path representing the root of a response. */
		public val root: GPath = GPath()


		/** Creates a path with a single field-name segment. */
		public fun ofName(name: String): GPath =
			GPath(listOf(Element.Name(name)))
	}


	/**
	 * A mutable builder for constructing a [GPath] incrementally.
	 *
	 * Use [withName] and [withIndex] to push/pop path segments around a block, and call
	 * [snapshot] at any point to capture the current path as an immutable [GPath].
	 */
	public class Builder {

		@PublishedApi
		internal val stack: MutableList<Any> = mutableListOf()


		/** Returns the current path as an immutable [GPath] snapshot. */
		public fun snapshot(): GPath =
			GPath(stack.map { element ->
				when (element) {
					is Int -> Element.Index(element)
					is String -> Element.Name(element)
					else -> error("not possible")
				}
			})


		override fun toString(): String =
			snapshot().toString()


		/** Pushes [name] as a field-name segment, executes [block], then pops the segment. */
		public inline fun <Result> withName(name: String, block: () -> Result): Result {
			stack += name

			return try {
				block()
			}
			finally {
				stack.removeAt(stack.size - 1)
			}
		}


		/** Pushes [index] as a list-index segment, executes [block], then pops the segment. */
		public inline fun <Result> withIndex(index: Int, block: () -> Result): Result {
			stack += index

			return try {
				block()
			}
			finally {
				stack.removeAt(stack.size - 1)
			}
		}
	}


	/** A single segment in a [GPath], either a field name or a list index. */
	public sealed class Element {

		/** A field-name segment. */
		public class Name(public val value: String) : Element() {

			override fun equals(other: Any?): Boolean =
				(this === other || (other is Name && value == other.value))


			override fun hashCode(): Int =
				value.hashCode()


			override fun toString(): String =
				value
		}


		/** A list-index segment. */
		public class Index(public val value: Int) : Element() {

			override fun equals(other: Any?): Boolean =
				(this === other || (other is Index && value == other.value))


			override fun hashCode(): Int =
				value


			override fun toString(): String =
				value.toString()
		}
	}
}


internal inline fun <Result> GPath.Builder?.withName(name: String, block: () -> Result): Result =
	if (this !== null) withName(name, block)
	else block()


internal inline fun <Result> GPath.Builder?.withIndex(index: Int, block: () -> Result): Result =
	if (this !== null) withIndex(index, block)
	else block()
