package io.fluidsonic.graphql


typealias GNodeInputCoercer<Input> = GNodeInputCoercerContext.(input: Input) -> Any?
