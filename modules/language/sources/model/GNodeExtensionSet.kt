package io.fluidsonic.graphql


public interface GNodeExtensionSet<out Node : GNode> {

	public operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

	public fun isEmpty(): Boolean

	override fun toString(): String


	public companion object {

		public inline operator fun <Node : GNode> invoke(action: Builder<Node>.() -> Unit): GNodeExtensionSet<Node> =
			Builder.default<Node>().apply(action).build()


		public fun <Node : GNode> empty(): GNodeExtensionSet<Node> =
			Empty
	}


	public interface Builder<out Node : GNode> {

		public fun build(): GNodeExtensionSet<Node> // FIXME make private

		public operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

		public operator fun <Value : Any> set(key: GNodeExtensionKey<in Value>, value: Value?)

		override fun toString(): String


		public companion object {

			public fun <Node : GNode> default(): Builder<Node> =
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


public fun GNodeExtensionSet<*>.isNotEmpty(): Boolean =
	!isEmpty()
