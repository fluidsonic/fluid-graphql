package io.fluidsonic.graphql


internal fun UInt.toIntOrNull() =
	if (this <= Int.MAX_VALUE.toUInt()) toInt() else null
