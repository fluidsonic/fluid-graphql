package io.fluidsonic.graphql


/**
 * Builder for a [GInlineFragmentSelection] (`... on TypeName { ... }`) within a selection set.
 */
@GraphQLMarker
public sealed interface GraphQLInlineFragmentSelectionBuilder :
	GraphQLInlineFragmentSelectionBuilderScope,
	GraphQLDirectivesContainer,
	GraphQLSelectionsContainer {

	/** Builds and returns the [GInlineFragmentSelection]. */
	public fun build(): GInlineFragmentSelection
}


/** Scope interface for [GraphQLInlineFragmentSelectionBuilder]. */
@GraphQLMarker
public sealed interface GraphQLInlineFragmentSelectionBuilderScope :
	GraphQLDirectivesContainerScope,
	GraphQLSelectionsContainerScope


private class GraphQLInlineFragmentSelectionBuilderImpl(
	private val type: GNamedTypeRef,
) : GraphQLInlineFragmentSelectionBuilder,
	GraphQLDirectivesContainerInternal,
	GraphQLSelectionsContainerInternal {

	override var directives: List<GDirective>? = null
	override val selections = mutableListOf<GSelection>()


	init {
		check(GLanguage.isValidName(type.name)) { "Invalid type name in inline fragment condition: $type" }
	}


	override fun build(): GInlineFragmentSelection {
		check(selections.isNotEmpty()) { "Inline fragment must contain at least one selection." }

		return GInlineFragmentSelection(
			directives = directives.orEmpty(),
			typeCondition = type,
			selectionSet = GSelectionSet(selections = selections.toList()),
		)
	}
}


/** Creates a new [GraphQLInlineFragmentSelectionBuilder] for the given type condition [type]. */
public fun GraphQLInlineFragmentSelectionBuilder(type: GNamedTypeRef): GraphQLInlineFragmentSelectionBuilder =
	GraphQLInlineFragmentSelectionBuilderImpl(type = type)
