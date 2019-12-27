package io.fluidsonic.graphql


internal object ArgumentRequirementRule : ValidationRule {

	override fun validateDirective(directive: GDirective, context: ValidationContext) {
		val definition = context.relatedDirectiveDefinition
			?: return // Cannot validate undefined directive.

		val missingArguments = definition.argumentDefinitions
			.filter { it.isRequired() && directive.argument(it.name) === null }
			.ifEmpty { return }

		val missingArgumentsText =
			if (missingArguments.size == 1) "argument '${missingArguments.first().name}'"
			else "arguments '${missingArguments.joinToString(separator = "', '", lastSeparator = "' and '", transform = { it.name })}'"

		context.reportError(
			message = "Directive '@${directive.name}' is missing required $missingArgumentsText.",
			nodes = listOf(directive.nameNode) + missingArguments.map { it.nameNode }
		)
	}


	override fun validateFieldSelection(selection: GFieldSelection, context: ValidationContext) {
		val fieldDefinition = context.relatedFieldDefinition
			?: return // Cannot validate undefined fields.

		val missingArguments = fieldDefinition.argumentDefinitions
			.filter { it.isRequired() && selection.argument(it.name) === null }
			.ifEmpty { return }

		val missingArgumentsText =
			if (missingArguments.size == 1) "argument '${missingArguments.first().name}'"
			else "arguments '${missingArguments.joinToString(separator = "', '", lastSeparator = "' and '", transform = { it.name })}'"

		context.reportError(
			message = "Selection of field '${fieldDefinition.name}' is missing required $missingArgumentsText.",
			nodes = listOf(selection.nameNode) + missingArguments.map { it.nameNode }
		)
	}
}
