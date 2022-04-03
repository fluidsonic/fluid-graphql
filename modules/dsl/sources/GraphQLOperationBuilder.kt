package io.fluidsonic.graphql


@GraphQLMarker
public sealed interface GraphQLOperationBuilder :
	GraphQLOperationBuilderScope,
	GraphQLDirectivesContainer,
	GraphQLSelectionsContainer,
	GraphQLVariableContainer {

	public fun build(): GOperationDefinition
}


@GraphQLMarker
public sealed interface GraphQLOperationBuilderScope :
	GraphQLDirectivesContainerScope,
	GraphQLSelectionsContainerScope,
	GraphQLVariableContainerScope


@GraphQLMarker
internal class GraphQLOperationBuilderImpl(
	private val name: String?,
	private val type: GOperationType,
) : GraphQLOperationBuilder,
	GraphQLDirectivesContainerInternal,
	GraphQLSelectionsContainerInternal,
	GraphQLVariableContainerInternal {

	override var directives: List<GDirective>? = null
	override val selections: MutableList<GSelection> = mutableListOf()
	override val unusedVariableRefFactories: MutableList<GraphQLVariableContainer.RefFactory> = mutableListOf()
	override val variableDefinitions: MutableList<GVariableDefinition> = mutableListOf()


	init {
		check(name == null || GLanguage.isValidName(name)) { "Invalid operation name: $name" }
	}


	override fun build(): GOperationDefinition {
		super.finalize()

		check(selections.isNotEmpty()) { "Operation must contain at least one selection." }

		return GOperationDefinition(
			directives = directives.orEmpty(),
			name = name,
			selectionSet = GSelectionSet(selections = selections.toList()),
			type = type,
			variableDefinitions = variableDefinitions.toList(),
		)
	}
}


public fun GraphQLOperationBuilder(
	name: String? = null,
	type: GOperationType,
): GraphQLOperationBuilder =
	GraphQLOperationBuilderImpl(name = name, type = type)


@GraphQLMarker
@Suppress("unused")
public inline fun GraphQL.mutation(name: String? = null, configure: GraphQLOperationBuilderScope.() -> Unit): GOperationDefinition =
	GraphQLOperationBuilder(name = name, type = GOperationType.mutation).apply(configure).build()


@GraphQLMarker
@Suppress("unused")
public inline fun GraphQL.query(name: String? = null, configure: GraphQLOperationBuilderScope.() -> Unit): GOperationDefinition =
	GraphQLOperationBuilder(name = name, type = GOperationType.query).apply(configure).build()


@GraphQLMarker
@Suppress("unused")
public inline fun GraphQL.subscription(name: String? = null, configure: GraphQLOperationBuilderScope.() -> Unit): GOperationDefinition =
	GraphQLOperationBuilder(name = name, type = GOperationType.subscription).apply(configure).build()
