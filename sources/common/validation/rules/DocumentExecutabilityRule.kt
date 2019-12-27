package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Executable-Definitions
internal object DocumentExecutabilityRule : ValidationRule {

	override fun validateDocument(document: GDocument, context: ValidationContext) {
		if (document.definitions.none { it is GOperationDefinition })
			context.reportError("In order to be executable, the document must contain at least one operation definition.")

		val nonExecutableDefinitions = document.definitions.filterNot { it is GExecutableDefinition }
		if (nonExecutableDefinitions.isNotEmpty())
			context.reportError(
				message = "In order to be executable, the document must contain only executable definitions.",
				nodes = nonExecutableDefinitions
			)
	}
}
