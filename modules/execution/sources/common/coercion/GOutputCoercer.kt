package io.fluidsonic.graphql


// FIXME Make interface?
typealias GOutputCoercer<Output> = GOutputCoercerContext.(output: Output) -> Any
