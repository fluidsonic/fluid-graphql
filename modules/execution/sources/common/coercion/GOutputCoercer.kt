package io.fluidsonic.graphql


// FIXME Support null as return value?
typealias GOutputCoercer<Output> = GOutputCoercerContext.(output: Output) -> Any
