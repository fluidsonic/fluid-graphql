package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Executable-Definitions
internal object DocumentExecutabilityRule : ValidationRule.Singleton() {

	override fun onDocument(document: GDocument, data: ValidationContext, visit: Visit) {
		if (document.definitions.none { it is GOperationDefinition })
			data.reportError("In order to be executable, the document must contain at least one operation definition.")

		val nonExecutableDefinitions = document.definitions.filterNot { it is GExecutableDefinition }
		if (nonExecutableDefinitions.isNotEmpty())
			data.reportError(
				message = "In order to be executable, the document must contain only executable definitions.",
				nodes = nonExecutableDefinitions
			)
	}
}
