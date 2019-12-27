package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal object VariableDefinitionTypeValidityRule : ValidationRule {

	override fun validateVariableDefinition(definition: GVariableDefinition, context: ValidationContext) {
		val type = context.relatedType
			?: return // Unknown type.

		if (!type.isInputType())
			context.reportError(
				message = "Variable '$${definition.name}' cannot have output type '${type.name}'.",
				nodes = listOf(definition.type, type.underlyingNamedType.nameNode)
			)
	}
}
