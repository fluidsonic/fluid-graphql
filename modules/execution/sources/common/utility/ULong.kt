package io.fluidsonic.graphql


@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("NOTHING_TO_INLINE")
inline fun ULong.toIntOrNull() =
	if (this <= Int.MAX_VALUE.toULong()) toInt() else null
