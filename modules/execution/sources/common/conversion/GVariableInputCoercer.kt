package io.fluidsonic.graphql


// FIXME call for List & null too
public fun interface GVariableInputCoercer<in Input> {

	public fun GVariableInputCoercerContext.coerceVariableInput(input: Input): Any?
}
