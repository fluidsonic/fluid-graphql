package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-All-Variables-Used
internal class AllVariablesUsedRule : ValidationRule() {

	private var variableDefs = mutableListOf<GVariableDefinition>()

	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		variableDefs = mutableListOf()
		visit.visitChildren()
		val usedNames = collectVariableRefs(definition.selectionSet, data.document).map { it.first }.toSet()
		variableDefs.filter { it.name !in usedNames }
			.forEach { varDef ->
				data.reportError(
					message = "Variable '\$${varDef.name}' is defined but never used.",
					nodes = listOf(varDef.nameNode)
				)
			}
	}

	override fun onVariableDefinition(definition: GVariableDefinition, data: ValidationContext, visit: Visit) {
		variableDefs += definition
	}

	companion object : Factory(::AllVariablesUsedRule)
}
