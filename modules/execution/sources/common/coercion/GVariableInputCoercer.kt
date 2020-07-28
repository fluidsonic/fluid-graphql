package io.fluidsonic.graphql


// FIXME Make interface?
typealias GVariableInputCoercer<Input> = GVariableInputCoercerContext.(input: Input) -> Any?
