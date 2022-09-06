package io.fluidsonic.graphql


@GraphQLMarker
public /* sealed */ interface GraphQLFragmentSelectionBuilder :
	GraphQLFragmentSelectionBuilderScope,
	GraphQLDirectivesContainer {

	public fun build(): GFragmentSelection
}


@GraphQLMarker
public /* sealed */ interface GraphQLFragmentSelectionBuilderScope : GraphQLDirectivesContainerScope


private class GraphQLFragmentSelectionBuilderImpl(
	private val name: String,
) : GraphQLFragmentSelectionBuilder, GraphQLDirectivesContainerInternal {

	override var directives: List<GDirective>? = null


	init {
		check(GLanguage.isValidFragmentName(name)) { "Invalid fragment name: $name" }
	}


	override fun build(): GFragmentSelection =
		GFragmentSelection(
			directives = directives.orEmpty(),
			name = name,
		)
}


public fun GraphQLFragmentSelectionBuilder(name: String): GraphQLFragmentSelectionBuilder =
	GraphQLFragmentSelectionBuilderImpl(name = name)
