package io.fluidsonic.graphql


@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("NOTHING_TO_INLINE")
internal inline fun UInt.toIntOrNull() =
	if (this <= Int.MAX_VALUE.toUInt()) toInt() else null
