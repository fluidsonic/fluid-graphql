package io.fluidsonic.graphql

import kotlin.js.*

// TODO Remove redundant 'arguments {}' and allow specifying arguments directly.

@GraphQLMarker
public /* sealed */ interface GraphQLDirectiveBuilder : GraphQLDirectiveBuilderScope, GraphQLArgumentsContainer {

	public fun build(): GDirective
}


@GraphQLMarker
public /* sealed */ interface GraphQLDirectiveBuilderScope : GraphQLValueContainerScope, GraphQLArgumentsContainerScope


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


@JsName("_GraphQLDirectiveBuilder")
public fun GraphQLDirectiveBuilder(name: String): GraphQLDirectiveBuilder =
	GraphQLDirectiveBuilderImpl(name = name)
