package io.fluidsonic.graphql


interface GFieldResolver<in Environment : Any, in ParentKotlinType : Any> {

	suspend fun Context<Environment>.resolve(parent: ParentKotlinType): Any?


	companion object {

		fun <Environment : Any, ParentKotlinType : Any> of(
			resolver: suspend Context<Environment>.(parent: ParentKotlinType) -> Any?
		) =
			object : GFieldResolver<Environment, ParentKotlinType> {

				override suspend fun Context<Environment>.resolve(parent: ParentKotlinType) =
					resolver(parent)
			}
	}


	@SchemaBuilderDsl
	interface Context<out Environment : Any> {

		val arguments: Map<String, Any?>
		val environment: Environment
		val parentType: GNamedType
		val schema: GSchema
	}
}


// FIXME improve this

fun GFieldResolver.Context<*>.booleanArgument(name: String) =
	arguments[name] as Boolean


fun GFieldResolver.Context<*>.intArgument(name: String) =
	arguments[name] as Int


fun GFieldResolver.Context<*>.stringArgument(name: String) =
	arguments[name] as String
