package io.fluidsonic.graphql


internal fun Long.toIntOrNull() =
	if (this in Int.MIN_VALUE.toLong() .. Int.MAX_VALUE.toLong()) toInt() else null
