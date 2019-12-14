package io.fluidsonic.graphql


interface GFieldResolver<in Parent : Any> {

	fun Context.resolve(parent: Parent): Any?


	companion object {

		fun <Parent : Any> of(resolver: Context.(parent: Parent) -> Any?) =
			object : GFieldResolver<Parent> {

				override fun Context.resolve(parent: Parent) =
					resolver(parent)
			}
	}


	@SchemaBuilderDsl
	interface Context {

		val arguments: Map<String, Any>
		val parentType: GNamedType
		val schema: GSchema
	}
}


fun GFieldResolver.Context.booleanArgument(name: String) =
	arguments[name] as Boolean


fun GFieldResolver.Context.stringArgument(name: String) =
	arguments[name] as String
