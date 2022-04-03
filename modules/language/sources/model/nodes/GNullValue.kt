package io.fluidsonic.graphql

import kotlin.js.*


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

		public val withoutOrigin: GNullValue = GNullValue(origin = null)
	}
}


@JsName("_GNullValue")
public fun GNullValue(): GNullValue =
	GNullValue.withoutOrigin
