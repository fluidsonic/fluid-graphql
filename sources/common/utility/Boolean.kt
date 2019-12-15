package io.fluidsonic.graphql


internal inline fun <Result> Boolean.thenTake(block: () -> Result) =
	if (this) block() else null
