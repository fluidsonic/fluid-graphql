package io.fluidsonic.graphql


// FIXME Make interface?
typealias GNodeInputCoercer<Input> = GNodeInputCoercerContext.(input: Input) -> Any?
