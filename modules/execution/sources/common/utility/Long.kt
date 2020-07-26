package io.fluidsonic.graphql


@Suppress("NOTHING_TO_INLINE")
inline fun Long.toIntOrNull() =
	if (this in Int.MIN_VALUE.toLong() .. Int.MAX_VALUE.toLong()) toInt() else null
