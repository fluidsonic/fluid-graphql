package io.fluidsonic.graphql


class GPath(elements: List<Element> = emptyList()) {

	val elements = elements.toList()


	override fun equals(other: Any?) =
		(this === other || (other is GPath && elements == other.elements))


	override fun hashCode() =
		elements.hashCode()


	override fun toString() =
		elements.joinToString(separator = "")


	companion object {

		val root = GPath()
	}


	class Builder {

		@PublishedApi
		internal val stack = mutableListOf<Any>()


		fun snapshot() =
			GPath(stack.map { element ->
				when (element) {
					is Int -> Element.Index(element)
					is String -> Element.Name(element)
					else -> error("not possible")
				}
			})


		override fun toString() =
			snapshot().toString()


		inline fun <R> withName(name: String, block: () -> R): R {
			stack += name

			return try {
				block()
			}
			finally {
				stack.removeAt(stack.size - 1)
			}
		}


		inline fun <R> withIndex(index: Int, block: () -> R): R {
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

			override fun equals(other: Any?) =
				(this === other || (other is Name && value == other.value))


			override fun hashCode() =
				value.hashCode()


			override fun toString() =
				".$value"
		}


		class Index(val value: Int) : Element() {

			override fun equals(other: Any?) =
				(this === other || (other is Index && value == other.value))


			override fun hashCode() =
				value


			override fun toString() =
				"[$value]"
		}
	}
}


internal inline fun <R> GPath.Builder?.withName(name: String, block: () -> R) =
	if (this !== null) withName(name, block)
	else block()


internal inline fun <R> GPath.Builder?.withIndex(index: Int, block: () -> R) =
	if (this !== null) withIndex(index, block)
	else block()
