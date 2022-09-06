package io.fluidsonic.graphql


@GraphQLMarker
public /* sealed */ interface GraphQLFieldSelectionBuilder :
	GraphQLFieldSelectionBuilderScope,
	GraphQLArgumentsContainer,
	GraphQLDirectivesContainer,
	GraphQLSelectionsContainer {

	public fun build(): GFieldSelection
}


@GraphQLMarker
public /* sealed */ interface GraphQLFieldSelectionBuilderScope :
	GraphQLArgumentsContainerScope,
	GraphQLDirectivesContainerScope,
	GraphQLSelectionsContainerScope


private class GraphQLFieldSelectionBuilderImpl(
	private val alias: String?,
	private val name: String,
) : GraphQLFieldSelectionBuilder, GraphQLArgumentsContainerInternal, GraphQLDirectivesContainerInternal, GraphQLSelectionsContainerInternal {

	override var arguments: List<GArgument>? = null
	override var directives: List<GDirective>? = null
	override val selections = mutableListOf<GSelection>()


	init {
		check(GLanguage.isValidName(name)) { "Invalid field name: $name" }
		check(alias == null || GLanguage.isValidName(alias)) { "Invalid field selection alias: $alias" }
	}


	override fun build(): GFieldSelection =
		GFieldSelection(
			alias = alias,
			arguments = arguments.orEmpty(),
			directives = directives.orEmpty(),
			name = name,
			selectionSet = selections.ifEmpty { null }?.let { GSelectionSet(selections = it.toList()) },
		)
}


public fun GraphQLFieldSelectionBuilder(
	name: String,
	alias: String? = null,
): GraphQLFieldSelectionBuilder =
	GraphQLFieldSelectionBuilderImpl(alias = alias, name = name)
