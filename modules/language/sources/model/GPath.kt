package io.fluidsonic.graphql


// FIXME refactor
public class GPath(elements: List<Element> = emptyList()) {

	public val elements: List<Element> = elements.toList()


	public fun addIndex(index: Int): GPath =
		GPath(elements + Element.Index(index))


	public fun addName(name: String): GPath =
		GPath(elements + Element.Name(name))


	override fun equals(other: Any?): Boolean =
		(this === other || (other is GPath && elements == other.elements))


	override fun hashCode(): Int =
		elements.hashCode()


	override fun toString(): String =
		elements.joinToString(separator = "")


	public companion object {

		public val root: GPath = GPath()


		public fun ofName(name: String): GPath =
			GPath(listOf(Element.Name(name)))
	}


	public class Builder {

		@PublishedApi
		internal val stack: MutableList<Any> = mutableListOf<Any>()


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


		public inline fun <Result> withName(name: String, block: () -> Result): Result {
			stack += name

			return try {
				block()
			}
			finally {
				stack.removeAt(stack.size - 1)
			}
		}


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


	public sealed class Element {

		public class Name(public val value: String) : Element() {

			override fun equals(other: Any?): Boolean =
				(this === other || (other is Name && value == other.value))


			override fun hashCode(): Int =
				value.hashCode()


			override fun toString(): String =
				".$value"
		}


		public class Index(public val value: Int) : Element() {

			override fun equals(other: Any?): Boolean =
				(this === other || (other is Index && value == other.value))


			override fun hashCode(): Int =
				value


			override fun toString(): String =
				"[$value]"
		}
	}
}


internal inline fun <Result> GPath.Builder?.withName(name: String, block: () -> Result): Result =
	if (this !== null) withName(name, block)
	else block()


internal inline fun <Result> GPath.Builder?.withIndex(index: Int, block: () -> Result): Result =
	if (this !== null) withIndex(index, block)
	else block()
