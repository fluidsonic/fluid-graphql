package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Input-Object-Required-Fields
internal object ObjectFieldRequirementRule : ValidationRule {

	override fun validateValue(value: GValue, context: ValidationContext) {
		if (value !is GValue.Object)
			return // Irrelevant.

		val type = context.relatedType as? GInputObjectType
			?: return // Cannot validate unknown or incorrect type.

		val missingArguments = type.argumentDefinitions
			.filter { it.isRequired() && value.field(it.name) === null }
			.ifEmpty { return } // All required fields are provided.

		val missingArgumentsText =
			if (missingArguments.size == 1) "field '${missingArguments.first().name}'"
			else "fields '${missingArguments.joinToString(separator = "', '", lastSeparator = "' and '", transform = { it.name })}'"

		context.reportError(
			message = "Value for Input type '${type.name}' is missing required $missingArgumentsText.",
			nodes = listOf(value) + missingArguments.map { it.nameNode }
		)
	}
}
