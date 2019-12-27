package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Lone-Anonymous-Operation
internal object AnonymousOperationExclusivityRule : ValidationRule {

	override fun validateDocument(document: GDocument, context: ValidationContext) {
		val operations = document.definitions.filterIsInstance<GOperationDefinition>()
		if (operations.size <= 1)
			return

		val anonymousOperations = operations.filter { it.name === null }
		if (anonymousOperations.isEmpty())
			return

		context.reportError(
			message = "The document must not contain more than one operation if it contains an anonymous operation.",
			nodes = anonymousOperations
		)
	}
}
