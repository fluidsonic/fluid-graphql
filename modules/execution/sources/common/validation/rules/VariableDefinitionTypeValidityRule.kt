package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object VariableDefinitionTypeValidityRule : ValidationRule.Singleton() {

	override fun onVariableDefinition(definition: GVariableDefinition, data: ValidationContext, visit: Visit) {
		val type = data.relatedType
			?: return // Unknown type.

		if (!type.isInputType())
			data.reportError(
				message = "Variable '$${definition.name}' cannot have output type '${type.name}'.",
				nodes = listOf(definition.type, type.underlyingNamedType.nameNode)
			)
	}
}
