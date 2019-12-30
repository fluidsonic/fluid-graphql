package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal object VariableDefinitionNameExclusivityRule : ValidationRule.Singleton() {

	override fun onFragmentDefinition(definition: GFragmentDefinition, data: ValidationContext, visit: Visit) {
		definition.variableDefinitions
			.groupBy { it.name }
			.filterNot { (_, variableDefinitions) ->
				// Unique.
				variableDefinitions.size == 1
			}
			.forEach { (name, variableDefinitions) ->
				data.reportError(
					message = "Fragment '${definition.name}' must not contain multiple variables with the same name '$$name'.",
					nodes = variableDefinitions.map { it.nameNode }
				)
			}
	}


	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		definition.variableDefinitions
			.groupBy { it.name }
			.filterNot { (_, variableDefinitions) ->
				// Unique.
				variableDefinitions.size == 1
			}
			.forEach { (name, variableDefinitions) ->
				val operationName = definition.name?.let { " '$it'" } ?: ""

				data.reportError(
					message = "Operation$operationName must not contain multiple variables with the same name '$$name'.",
					nodes = variableDefinitions.map { it.nameNode }
				)
			}
	}
}
