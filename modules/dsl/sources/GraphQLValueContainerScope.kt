package io.fluidsonic.graphql


/**
 * Base scope for DSL contexts that can produce GraphQL values.
 *
 * Provides helpers for creating enum values and variable references.
 */
@GraphQLMarker
public sealed interface GraphQLValueContainerScope {

	/** Creates a [GEnumValue] for the given enum value [name]. */
	public fun enum(name: String): GEnumValue {
		check(GLanguage.isValidEnumValue(name)) { "Invalid enum value: $name" }

		return GEnumValue(name)
	}


	/**
	 * Creates a [GVariableRef] referencing a variable with the given [name].
	 *
	 * Note: unlike [GraphQLVariableContainerScope.variable], this does not declare a new
	 * variable — it only creates a reference by name.
	 */
	public fun variable(name: String): GVariableRef {
		check(GLanguage.isValidName(name)) { "Invalid variable name: $name" }

		return GVariableRef(name)
	}
}


/** Builds a [GListValue] using the [GraphQLValueListBuilder] DSL. */
@Suppress("UnusedReceiverParameter")
public inline fun GraphQLValueContainerScope.list(configure: GraphQLValueListBuilder.() -> Unit): GListValue =
	GraphQLValueListBuilder().apply(configure).build()


/** Builds a [GObjectValue] using the [GraphQLArgumentsBuilder] DSL. */
@Suppress("UnusedReceiverParameter")
public inline fun GraphQLValueContainerScope.obj(configure: GraphQLArgumentsBuilder.() -> Unit): GObjectValue =
	GObjectValue(GraphQLArgumentsBuilder().apply(configure).build())
