package io.fluidsonic.graphql


interface GVariableInputCoercionContext<Environment : Any> : GCoercionContext<Environment> {

	val coercion: GVariableInputCoercion<Environment>
	val path: GPath.Builder
}


fun GVariableInputCoercionContext<*>.error(message: String): Nothing =
	throw GError(
		message = message,
		path = path.snapshot() // FIXME add node? remove path for vars?
	)
