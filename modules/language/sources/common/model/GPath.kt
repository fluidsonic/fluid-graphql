package io.fluidsonic.graphql


class GPath(elements: List<Element> = emptyList()) {

	val elements: List<Element> = elements.toList()


	override fun equals(other: Any?): Boolean =
		(this === other || (other is GPath && elements == other.elements))


	override fun hashCode(): Int =
		elements.hashCode()


	override fun toString(): String =
		elements.joinToString(separator = "")


	companion object {

		val root = GPath()
	}


	class Builder {

		@PublishedApi
		internal val stack = mutableListOf<Any>()


		fun snapshot(): GPath =
			GPath(stack.map { element ->
				when (element) {
					is Int -> Element.Index(element)
					is String -> Element.Name(element)
					else -> error("not possible")
				}
			})


		override fun toString(): String =
			snapshot().toString()


		inline fun <Result> withName(name: String, block: () -> Result): Result {
			stack += name

			return try {
				block()
			}
			finally {
				stack.removeAt(stack.size - 1)
			}
		}


		inline fun <Result> withIndex(index: Int, block: () -> Result): Result {
			stack += index

			return try {
				block()
			}
			finally {
				stack.removeAt(stack.size - 1)
			}
		}
	}


	sealed class Element {

		class Name(val value: String) : Element() {

			override fun equals(other: Any?): Boolean =
				(this === other || (other is Name && value == other.value))


			override fun hashCode(): Int =
				value.hashCode()


			override fun toString(): String =
				".$value"
		}


		class Index(val value: Int) : Element() {

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
