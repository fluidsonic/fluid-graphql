package io.fluidsonic.graphql


/**
 * Builder for a [GFragmentDefinition].
 *
 * Provides selections, variables, and directives. Use via [GraphQLFragmentDefinitionContainerScope.fragment].
 */
@GraphQLMarker
public sealed interface GraphQLFragmentDefinitionBuilder :
	GraphQLFragmentDefinitionBuilderScope,
	GraphQLDirectivesContainer,
	GraphQLSelectionsContainer,
	GraphQLVariableContainer {

	/** Builds and returns the [GFragmentDefinition]. */
	public fun build(): GFragmentDefinition
}


/** Scope interface for [GraphQLFragmentDefinitionBuilder]. */
@GraphQLMarker
public sealed interface GraphQLFragmentDefinitionBuilderScope :
	GraphQLDirectivesContainerScope,
	GraphQLSelectionsContainerScope,
	GraphQLVariableContainerScope


private class GraphQLFragmentDefinitionBuilderImpl(
	private val name: String,
	private val typeCondition: GNamedTypeRef,
) : GraphQLFragmentDefinitionBuilder,
	GraphQLDirectivesContainerInternal,
	GraphQLSelectionsContainerInternal,
	GraphQLVariableContainerInternal {

	override var directives: List<GDirective>? = null
	override val selections: MutableList<GSelection> = mutableListOf()
	override val unusedVariableRefFactories: MutableList<GraphQLVariableContainer.RefFactory> = mutableListOf()
	override val variableDefinitions: MutableList<GVariableDefinition> = mutableListOf()


	init {
		check(GLanguage.isValidFragmentName(name)) { "Invalid fragment name: $name" }
	}


	override fun build(): GFragmentDefinition {
		super.finalize()

		check(selections.isNotEmpty()) { "Fragment definition must contain at least one selection." }

		return GFragmentDefinition(
			directives = directives.orEmpty(),
			name = name,
			selectionSet = GSelectionSet(selections = selections.toList()),
			typeCondition = typeCondition,
			variableDefinitions = variableDefinitions.toList(),
		)
	}
}


/**
 * Creates a new [GraphQLFragmentDefinitionBuilder] for a fragment with the given [name] and
 * [typeCondition].
 */
public fun GraphQLFragmentDefinitionBuilder(name: String, typeCondition: GNamedTypeRef): GraphQLFragmentDefinitionBuilder =
	GraphQLFragmentDefinitionBuilderImpl(name = name, typeCondition = typeCondition)
