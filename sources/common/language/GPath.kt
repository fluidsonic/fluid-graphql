package io.fluidsonic.graphql


class GPath(elements: List<Element> = emptyList()) {

	val elements = elements.toList()


	override fun equals(other: Any?) =
		(this === other || (other is GPath && elements == other.elements))


	override fun hashCode() =
		elements.hashCode()


	override fun toString() =
		elements.joinToString()


	companion object {

		val root = GPath()
	}


	// Internal for now. Review API before making it public.
	internal class Builder {

		private val stack = mutableListOf<Any>()


		fun snapshot() =
			GPath(stack.map { element ->
				when (element) {
					is Int -> Element.ListIndex(element)
					is String -> Element.FieldName(element)
					else -> error("not possible")
				}
			})


		override fun toString() =
			snapshot().toString()


		inline fun <R> withFieldName(name: String, block: () -> R): R {
			stack += name

			return try {
				block()
			}
			finally {
				stack.removeAt(stack.size - 1)
			}
		}


		inline fun <R> withListIndex(index: Int, block: () -> R): R {
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

		class FieldName(val value: String) : Element() {

			override fun equals(other: Any?) =
				(this === other || (other is FieldName && value == other.value))


			override fun hashCode() =
				value.hashCode()


			override fun toString() =
				".$value"
		}


		class ListIndex(val value: Int) : Element() {

			override fun equals(other: Any?) =
				(this === other || (other is ListIndex && value == other.value))


			override fun hashCode() =
				value


			override fun toString() =
				"[$value]"
		}
	}
}


internal inline fun <R> GPath.Builder?.withFieldName(name: String, block: () -> R) =
	if (this !== null) withFieldName(name, block)
	else block()


internal inline fun <R> GPath.Builder?.withListIndex(index: Int, block: () -> R) =
	if (this !== null) withListIndex(index, block)
	else block()
