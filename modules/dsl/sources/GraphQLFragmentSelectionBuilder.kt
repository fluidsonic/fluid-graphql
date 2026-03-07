package io.fluidsonic.graphql


/**
 * Builder for a [GFragmentSelection] (a `...FragmentName` spread) within a selection set.
 */
@GraphQLMarker
public sealed interface GraphQLFragmentSelectionBuilder :
	GraphQLFragmentSelectionBuilderScope,
	GraphQLDirectivesContainer {

	/** Builds and returns the [GFragmentSelection]. */
	public fun build(): GFragmentSelection
}


/** Scope interface for [GraphQLFragmentSelectionBuilder]. */
@GraphQLMarker
public sealed interface GraphQLFragmentSelectionBuilderScope : GraphQLDirectivesContainerScope


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


/** Creates a new [GraphQLFragmentSelectionBuilder] for the fragment with the given [name]. */
public fun GraphQLFragmentSelectionBuilder(name: String): GraphQLFragmentSelectionBuilder =
	GraphQLFragmentSelectionBuilderImpl(name = name)
