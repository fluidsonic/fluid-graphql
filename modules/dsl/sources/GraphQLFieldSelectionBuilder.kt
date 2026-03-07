package io.fluidsonic.graphql


/**
 * Builder for a [GFieldSelection] within a selection set.
 *
 * Provides arguments, directives, and nested sub-selection capabilities.
 */
@GraphQLMarker
public sealed interface GraphQLFieldSelectionBuilder :
	GraphQLFieldSelectionBuilderScope,
	GraphQLArgumentsContainer,
	GraphQLDirectivesContainer,
	GraphQLSelectionsContainer {

	/** Builds and returns the [GFieldSelection]. */
	public fun build(): GFieldSelection
}


/** Scope interface for [GraphQLFieldSelectionBuilder]. */
@GraphQLMarker
public sealed interface GraphQLFieldSelectionBuilderScope :
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


/**
 * Creates a new [GraphQLFieldSelectionBuilder] for the field named [name], with an optional [alias].
 */
public fun GraphQLFieldSelectionBuilder(
	name: String,
	alias: String? = null,
): GraphQLFieldSelectionBuilder =
	GraphQLFieldSelectionBuilderImpl(alias = alias, name = name)
