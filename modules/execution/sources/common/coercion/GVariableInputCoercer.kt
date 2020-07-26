package io.fluidsonic.graphql


typealias GVariableInputCoercer<Input> = GVariableInputCoercerContext.(input: Input) -> Any?
