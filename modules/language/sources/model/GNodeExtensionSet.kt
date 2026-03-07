package io.fluidsonic.graphql


/**
 * An immutable, type-safe container of arbitrary metadata attached to a [GNode].
 *
 * Retrieve values with the `[]` operator and a [GNodeExtensionKey]. Build instances using the
 * [invoke] factory or [Builder].
 *
 * The set is parameterised by the [Node] type so that extensions can be constrained to specific
 * node subtypes at the call site.
 *
 * @see GNodeExtensionKey
 */
public interface GNodeExtensionSet<out Node : GNode> {

	/** Returns the value associated with [key], or `null` if not present. */
	public operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

	public fun isEmpty(): Boolean

	override fun toString(): String


	public companion object {

		/** Creates a [GNodeExtensionSet] by building it with the given [action] on a [Builder]. */
		public inline operator fun <Node : GNode> invoke(action: Builder<Node>.() -> Unit): GNodeExtensionSet<Node> =
			Builder.default<Node>().apply(action).build()


		/** Returns an empty [GNodeExtensionSet]. */
		public fun <Node : GNode> empty(): GNodeExtensionSet<Node> =
			Empty
	}


	/** Mutable builder for constructing a [GNodeExtensionSet]. */
	public interface Builder<out Node : GNode> {

		public fun build(): GNodeExtensionSet<Node> // FIXME make private

		/** Returns the value associated with [key], or `null` if not set. */
		public operator fun <Value : Any> get(key: GNodeExtensionKey<out Value>): Value?

		/** Sets the value for [key], or removes it when [value] is `null`. */
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


/** Returns `true` if this set contains at least one entry. */
public fun GNodeExtensionSet<*>.isNotEmpty(): Boolean =
	!isEmpty()
