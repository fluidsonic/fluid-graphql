package io.fluidsonic.graphql


// FIXME Make interface?
public typealias GVariableInputCoercer<Input> = GVariableInputCoercerContext.(input: Input) -> Any?
