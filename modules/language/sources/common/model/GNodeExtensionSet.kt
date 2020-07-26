package io.fluidsonic.graphql


interface GNodeExtensionSet : (GNodeExtensionSet.Builder<GFieldDefinition>) -> Unit {

	operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

	override fun invoke(builder: Builder<GFieldDefinition>)

	override fun toString(): String


	companion object {

		inline operator fun <Node : GNode> invoke(action: Builder<Node>.() -> Unit): GNodeExtensionSet =
			Builder.default<Node>().apply(action).build()


		fun empty(): GNodeExtensionSet =
			Empty
	}


	interface Builder<out Node : GNode> {

		fun build(): GNodeExtensionSet

		operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

		operator fun <Value : Any> set(key: GNodeExtensionKey<in Value>, value: Value?)

		override fun toString(): String


		companion object {

			fun <Node : GNode> default(): Builder<Node> =
				Default()
		}


		private class Default<out Node : GNode> : Builder<Node> {

			private val values: MutableMap<GNodeExtensionKey<*>, Any> = hashMapOf()


			override fun build(): GNodeExtensionSet =
				if (values.isNotEmpty())
					Default(values.toMap())
				else
					empty()


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


	private class Default(private val values: Map<GNodeExtensionKey<*>, Any>) : GNodeExtensionSet {

		@Suppress("UNCHECKED_CAST")
		override fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value? =
			values[key] as Value?


		@Suppress("UNCHECKED_CAST")
		override fun invoke(builder: Builder<GFieldDefinition>) {
			for ((key, value) in values)
				builder[key as GNodeExtensionKey<Any>] = value
		}


		override fun toString() =
			values.toString()
	}


	private object Empty : GNodeExtensionSet {

		override fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Nothing? =
			null


		override fun invoke(builder: Builder<GFieldDefinition>) =
			Unit


		override fun toString() =
			"{}"
	}
}

