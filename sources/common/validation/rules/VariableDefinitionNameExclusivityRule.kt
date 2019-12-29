package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal object VariableDefinitionNameExclusivityRule : ValidationRule {

	override fun validateFragmentDefinition(definition: GFragmentDefinition, context: ValidationContext) {
		definition.variableDefinitions
			.groupBy { it.name }
			.filterNot { (_, variableDefinitions) ->
				// Unique.
				variableDefinitions.size == 1
			}
			.forEach { (name, variableDefinitions) ->
				context.reportError(
					message = "Fragment '${definition.name}' must not contain multiple variables with the same name '$$name'.",
					nodes = variableDefinitions.map { it.nameNode }
				)
			}
	}


	override fun validateOperationDefinition(definition: GOperationDefinition, context: ValidationContext) {
		definition.variableDefinitions
			.groupBy { it.name }
			.filterNot { (_, variableDefinitions) ->
				// Unique.
				variableDefinitions.size == 1
			}
			.forEach { (name, variableDefinitions) ->
				val operationName = definition.name?.let { " '$it'" } ?: ""

				context.reportError(
					message = "Operation$operationName must not contain multiple variables with the same name '$$name'.",
					nodes = variableDefinitions.map { it.nameNode }
				)
			}
	}
}
