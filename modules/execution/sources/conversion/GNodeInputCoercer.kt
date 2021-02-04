package io.fluidsonic.graphql


// FIXME call for List & null too
public fun interface GNodeInputCoercer<in Input> {

	public fun GNodeInputCoercerContext.coerceNodeInput(input: Input): Any?
}
