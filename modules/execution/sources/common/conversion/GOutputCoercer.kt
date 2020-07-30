package io.fluidsonic.graphql


public fun interface GOutputCoercer<in Output : Any> {

	public fun GOutputCoercerContext.coerceOutput(output: Output): Any
}
