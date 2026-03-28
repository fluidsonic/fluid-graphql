package io.fluidsonic.graphql


/** A GraphQL integer value literal. */
public class GIntValue(
	public val value: Int,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GIntValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.INT


	override fun equals(other: Any?): Boolean =
		this === other || (other is GIntValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GIntValue &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		value.hashCode()


	override fun unwrap(): Int =
		value


	public companion object
}


/** Creates a [GIntValue] from a [Byte]. */
public fun GIntValue(value: Byte): GIntValue =
	GIntValue(value.toInt())


/** Creates a [GIntValue] without a source origin. */
public fun GIntValue(value: Int): GIntValue =
	GIntValue(value = value, origin = null)


/** Creates a [GIntValue] from a [UByte]. */
public fun GIntValue(value: UByte): GIntValue =
	GIntValue(value.toInt())


/** Creates a [GIntValue] from a [Short]. */
public fun GIntValue(value: Short): GIntValue =
	GIntValue(value.toInt())


/** Creates a [GIntValue] from a [UShort]. */
public fun GIntValue(value: UShort): GIntValue =
	GIntValue(value.toInt())
