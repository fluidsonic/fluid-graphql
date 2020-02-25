package io.fluidsonic.graphql


interface GNodeInputCoercionContext<Environment : Any> : GCoercionContext<Environment> {

	val coercion: GNodeInputCoercion<Environment>
	val path: GPath.Builder
	val variableValues: Map<String, Any?>
}


fun GNodeInputCoercionContext<*>.error(message: String): Nothing =
	throw GError(
		message = message,
		path = path.snapshot() // FIXME add node?
	)
