package io.fluidsonic.graphql


// FIXME fluid-stdlib
internal inline fun <Result> Boolean.thenTake(block: () -> Result) =
	if (this) block() else null
