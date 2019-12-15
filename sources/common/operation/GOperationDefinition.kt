package io.fluidsonic.graphql


class GOperationDefinition(
	val type: GOperationType,
	val name: String? = null,
	val selectionSet: GSelectionSet,
	val directives: List<GDirective> = emptyList(),
	val variableDefinitions: List<GVariableDefinition> = emptyList()
) {

	companion object {

		fun from(ast: GAst.Definition.Operation) =
			GOperationDefinition(
				directives = ast.directives.map { GDirective.from(it) },
				name = ast.name?.value,
				selectionSet = GSelectionSet.from(ast.selectionSet),
				type = ast.type,
				variableDefinitions = ast.variableDefinitions.map { GVariableDefinition.from(it) }
			)
	}
}
