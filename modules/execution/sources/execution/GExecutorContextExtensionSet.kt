package io.fluidsonic.graphql

import io.fluidsonic.graphql.GExecutorContextExtensionSet.*


/**
 * An immutable set of typed extension values attached to a GraphQL execution context.
 *
 * Pass extensions when calling [GExecutor.execute] to provide per-request data (e.g. authentication
 * context, tracing info) that resolvers and coercers can read via [GExecutorContext.extensions].
 *
 * Build an instance with the [invoke] operator:
 * ```kotlin
 * val ext = GExecutorContextExtensionSet {
 *     set(MyKey, myValue)
 * }
 * ```
 */
public interface GExecutorContextExtensionSet {

	/** Returns the value associated with [key], or `null` if not present. */
	public operator fun <Value : Any> get(key: GExecutorContextExtensionKey<out Value>): Value?

	public fun isEmpty(): Boolean

	override fun toString(): String


	public companion object {

		/** Creates a [GExecutorContextExtensionSet] by applying [action] to a [Builder]. */
		public inline operator fun invoke(action: Builder.() -> Unit): GExecutorContextExtensionSet =
			Builder.default().apply(action).build()


		/** Returns an empty [GExecutorContextExtensionSet]. */
		public fun empty(): GExecutorContextExtensionSet =
			Empty
	}


	/** Mutable builder for constructing a [GExecutorContextExtensionSet]. */
	public interface Builder {

		public fun build(): GExecutorContextExtensionSet // FIXME make private

		/** Returns the value associated with [key], or `null` if not present. */
		public operator fun <Value : Any> get(key: GExecutorContextExtensionKey<out Value>): Value?

		/** Associates [value] with [key], or removes the association if [value] is `null`. */
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
