package io.fluidsonic.graphql


internal fun ULong.toIntOrNull() =
	if (this <= Int.MAX_VALUE.toULong()) toInt() else null
