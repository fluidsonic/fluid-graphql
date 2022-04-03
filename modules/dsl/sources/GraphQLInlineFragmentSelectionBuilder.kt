package io.fluidsonic.graphql


@GraphQLMarker
public sealed interface GraphQLInlineFragmentSelectionBuilder :
	GraphQLInlineFragmentSelectionBuilderScope,
	GraphQLDirectivesContainer,
	GraphQLSelectionsContainer {

	public fun build(): GInlineFragmentSelection
}


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


public fun GraphQLInlineFragmentSelectionBuilder(type: GNamedTypeRef): GraphQLInlineFragmentSelectionBuilder =
	GraphQLInlineFragmentSelectionBuilderImpl(type = type)
