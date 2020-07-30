package io.fluidsonic.graphql


// FIXME Make interface?
public typealias GNodeInputCoercer<Input> = GNodeInputCoercerContext.(input: Input) -> Any?
