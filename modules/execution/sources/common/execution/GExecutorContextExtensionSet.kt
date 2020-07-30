package io.fluidsonic.graphql

import io.fluidsonic.graphql.GExecutorContextExtensionSet.*


public interface GExecutorContextExtensionSet {

	public operator fun <Value : Any> get(key: GExecutorContextExtensionKey<out Value>): Value?

	public fun isEmpty(): Boolean

	override fun toString(): String


	public companion object {

		public inline operator fun invoke(action: Builder.() -> Unit): GExecutorContextExtensionSet =
			Builder.default().apply(action).build()


		public fun empty(): GExecutorContextExtensionSet =
			Empty
	}


	public interface Builder {

		public fun build(): GExecutorContextExtensionSet // FIXME make private

		public operator fun <Value : Any> get(key: GExecutorContextExtensionKey<out Value>): Value?

		public operator fun <Value : Any> set(key: GExecutorContextExtensionKey<in Value>, value: Value?)

		override fun toString(): String


		public companion object {

			public fun default(): Builder =
				Default()
		}


		private class Default : Builder {

			private val values: MutableMap<GExecutorContextExtensionKey<*>, Any> = hashMapOf()


			override fun build(): GExecutorContextExtensionSet =
				when {
					values.isNotEmpty() -> Default(values.toMap())
					else -> empty()
				}


			@Suppress("UNCHECKED_CAST")
			override fun <Value : Any> get(key: GExecutorContextExtensionKey<out Value>): Value? =
				values[key] as Value?


			override fun <Value : Any> set(key: GExecutorContextExtensionKey<in Value>, value: Value?) {
				if (value != null)
					values[key] = value
				else
					values.remove(key)
			}


			override fun toString() =
				values.toString()
		}
	}


	private class Default(private val values: Map<GExecutorContextExtensionKey<*>, Any>) : GExecutorContextExtensionSet {

		@Suppress("UNCHECKED_CAST")
		override fun <Value : Any> get(key: GExecutorContextExtensionKey<out Value>): Value? =
			values[key] as Value?


		override fun isEmpty() =
			values.isEmpty()


		override fun toString() =
			values.toString()
	}


	private object Empty : GExecutorContextExtensionSet {

		override fun <Value : Any> get(key: GExecutorContextExtensionKey<out Value>): Nothing? =
			null


		override fun isEmpty() =
			true


		override fun toString() =
			"{}"
	}
}


public fun GExecutorContextExtensionSet.isNotEmpty(): Boolean =
	!isEmpty()
