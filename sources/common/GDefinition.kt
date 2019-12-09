package io.fluidsonic.graphql


sealed class GDefinition
sealed class GExecutableDefinition : GDefinition()
sealed class GTypeSystemDefinition : GDefinition()


class GFragmentDefinition(
	val name: String,
	val typeCondition: GTypeCondition,
	val selectionSet: List<GSelection>,
	val directives: List<GDirective> = emptyList()
) : GExecutableDefinition()


class GOperationDefinition(
	val type: GOperationType,
	val name: String? = null,
	val selectionSet: GSelectionSet,
	val directives: List<GDirective> = emptyList(),
	val variableDefinitions: List<GVariableDefinition> = emptyList()
) : GExecutableDefinition()
