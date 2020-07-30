package io.fluidsonic.graphql


// FIXME Make interface?
public typealias GOutputCoercer<Output> = GOutputCoercerContext.(output: Output) -> Any
