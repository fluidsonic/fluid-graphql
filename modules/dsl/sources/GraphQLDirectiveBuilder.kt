package io.fluidsonic.graphql

// TODO Remove redundant 'arguments {}' and allow specifying arguments directly.

/**
 * Builder for a single [GDirective] application.
 *
 * Arguments are added via the [arguments] DSL from [GraphQLArgumentsContainer].
 */
@GraphQLMarker
public sealed interface GraphQLDirectiveBuilder : GraphQLDirectiveBuilderScope, GraphQLArgumentsContainer {

	/** Builds and returns the [GDirective]. */
	public fun build(): GDirective
}


/** Scope interface for [GraphQLDirectiveBuilder]. */
@GraphQLMarker
public sealed interface GraphQLDirectiveBuilderScope : GraphQLValueContainerScope, GraphQLArgumentsContainerScope


private class GraphQLDirectiveBuilderImpl(
	private val name: String,
) : GraphQLDirectiveBuilder, GraphQLArgumentsContainerInternal {

	private val directive = mutableListOf<GDirective>()

	override var arguments: List<GArgument>? = null


	init {
		check(GLanguage.isValidName(name)) { "Invalid directive name: $name" }
	}


	override fun build(): GDirective =
		GDirective(
			arguments = arguments.orEmpty(),
			name = name,
		)
}


/** Creates a new [GraphQLDirectiveBuilder] for a directive with the given [name]. */
public fun GraphQLDirectiveBuilder(name: String): GraphQLDirectiveBuilder =
	GraphQLDirectiveBuilderImpl(name = name)
