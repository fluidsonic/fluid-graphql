package io.fluidsonic.graphql


internal object ArgumentRequirementRule : ValidationRule.Singleton() {

	override fun onDirective(directive: GDirective, data: ValidationContext, visit: Visit) {
		val definition = data.relatedDirectiveDefinition
			?: return // Cannot validate undefined directive.

		val missingArguments = definition.argumentDefinitions
			.filter { it.isRequired() && directive.argument(it.name) === null }
			.ifEmpty { return }

		val missingArgumentsText =
			if (missingArguments.size == 1) "argument '${missingArguments.first().name}'"
			else "arguments '${missingArguments.joinToString(separator = "', '", lastSeparator = "' and '", transform = { it.name })}'"

		data.reportError(
			message = "Directive '@${directive.name}' is missing required $missingArgumentsText.",
			nodes = listOf(directive.nameNode) + missingArguments.map { it.nameNode }
		)
	}


	override fun onFieldSelection(selection: GFieldSelection, data: ValidationContext, visit: Visit) {
		val fieldDefinition = data.relatedFieldDefinition
			?: return // Cannot validate undefined fields.

		val missingArguments = fieldDefinition.argumentDefinitions
			.filter { it.isRequired() && selection.argument(it.name) === null }
			.ifEmpty { return }

		val missingArgumentsText =
			if (missingArguments.size == 1) "argument '${missingArguments.first().name}'"
			else "arguments '${missingArguments.joinToString(separator = "', '", lastSeparator = "' and '", transform = { it.name })}'"

		data.reportError(
			message = "Selection of field '${fieldDefinition.name}' is missing required $missingArgumentsText.",
			nodes = listOf(selection.nameNode) + missingArguments.map { it.nameNode }
		)
	}
}
