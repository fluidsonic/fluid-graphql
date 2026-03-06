package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-All-Variable-Uses-Defined
internal class AllVariableUsesDefinedRule : ValidationRule() {

	private var definedVarNames = mutableSetOf<String>()

	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		definedVarNames = mutableSetOf()
		visit.visitChildren()
		val usages = collectVariableRefs(definition.selectionSet, data.document)
		for ((name, node) in usages) {
			if (name !in definedVarNames)
				data.reportError(
					message = "Variable '\$$name' is not defined.",
					nodes = listOf(node)
				)
		}
	}

	override fun onVariableDefinition(definition: GVariableDefinition, data: ValidationContext, visit: Visit) {
		definedVarNames += definition.name
	}

	companion object : Factory(::AllVariableUsesDefinedRule)
}
