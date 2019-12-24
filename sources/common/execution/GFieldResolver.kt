package io.fluidsonic.graphql


interface GFieldResolver<in Parent : Any> {

	fun Parent.resolve(context: Context): Any?


	companion object {

		fun <Parent : Any> of(resolver: Parent.(context: Context) -> Any?) =
			object : GFieldResolver<Parent> {

				override fun Parent.resolve(context: Context) =
					resolver(context)
			}
	}


	@SchemaBuilderDsl
	interface Context {

		val arguments: Map<String, Any?>
		val parentType: GNamedType
		val schema: GSchema
	}
}


// FIXME improve this

fun GFieldResolver.Context.booleanArgument(name: String) =
	arguments[name] as Boolean


fun GFieldResolver.Context.intArgument(name: String) =
	arguments[name] as Int


fun GFieldResolver.Context.stringArgument(name: String) =
	arguments[name] as String
