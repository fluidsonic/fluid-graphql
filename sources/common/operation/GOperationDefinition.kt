package io.fluidsonic.graphql


class GOperationDefinition(
	val type: GOperationType,
	val name: String? = null,
	val selectionSet: GSelectionSet,
	val directives: List<GDirective> = emptyList(),
	val variableDefinitions: List<GVariableDefinition> = emptyList()
) {

	companion object {

		internal fun build(ast: AstNode.Definition.Operation) =
			GOperationDefinition(
				directives = ast.directives.map { GDirective.build(it) },
				name = ast.name?.value,
				selectionSet = GSelectionSet.build(ast.selectionSet),
				type = ast.type,
				variableDefinitions = ast.variableDefinitions.map { GVariableDefinition.build(it) }
			)
	}
}
