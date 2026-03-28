package io.fluidsonic.graphql


/** A GraphQL `null` value literal. */
public class GNullValue(
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GNullValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.NULL


	override fun equals(other: Any?): Boolean =
		this === other || other is GNullValue


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GNullValue &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		0


	override fun unwrap(): Nothing? =
		null


	public companion object {

		/** A shared [GNullValue] instance with no source origin. */
		public val withoutOrigin: GNullValue = GNullValue(origin = null)
	}
}


/** Returns a shared [GNullValue] instance without a source origin. */
public fun GNullValue(): GNullValue =
	GNullValue.withoutOrigin
