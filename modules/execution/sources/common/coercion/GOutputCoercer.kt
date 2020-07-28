package io.fluidsonic.graphql


// FIXME Support null as return value?
// FIXME Make interface?
typealias GOutputCoercer<Output> = GOutputCoercerContext.(output: Output) -> Any
