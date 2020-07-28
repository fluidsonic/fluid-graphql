package io.fluidsonic.graphql


interface GNodeExtensionSet<out Node : GNode> {

	operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

	fun isEmpty(): Boolean

	override fun toString(): String


	companion object {

		inline operator fun <Node : GNode> invoke(action: Builder<Node>.() -> Unit): GNodeExtensionSet<Node> =
			Builder.default<Node>().apply(action).build()


		fun <Node : GNode> empty(): GNodeExtensionSet<Node> =
			Empty
	}


	interface Builder<out Node : GNode> {

		fun build(): GNodeExtensionSet<Node>

		operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

		operator fun <Value : Any> set(key: GNodeExtensionKey<in Value>, value: Value?)

		override fun toString(): String


		companion object {

			fun <Node : GNode> default(): Builder<Node> =
				Default()
		}


		private class Default<out Node : GNode> : Builder<Node> {

			private val values: MutableMap<GNodeExtensionKey<*>, Any> = hashMapOf()


			override fun build(): GNodeExtensionSet<Node> =
				when {
					values.isNotEmpty() -> Default(values.toMap())
					else -> empty()
				}


			@Suppress("UNCHECKED_CAST")
			override fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value? =
				values[key] as Value?


			override fun <Value : Any> set(key: GNodeExtensionKey<in Value>, value: Value?) {
				if (value != null)
					values[key] = value
				else
					values.remove(key)
			}


			override fun toString() =
				values.toString()
		}
	}


	private class Default<out Node : GNode>(private val values: Map<GNodeExtensionKey<*>, Any>) : GNodeExtensionSet<Node> {

		@Suppress("UNCHECKED_CAST")
		override fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value? =
			values[key] as Value?


		override fun isEmpty() =
			values.isEmpty()


		override fun toString() =
			values.toString()
	}


	private object Empty : GNodeExtensionSet<Nothing> {

		override fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Nothing? =
			null


		override fun isEmpty() =
			true


		override fun toString() =
			"{}"
	}
}


fun GNodeExtensionSet<*>.isNotEmpty() =
	!isEmpty()
