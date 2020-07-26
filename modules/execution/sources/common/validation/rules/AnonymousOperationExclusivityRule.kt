package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Lone-Anonymous-Operation
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object AnonymousOperationExclusivityRule : ValidationRule.Singleton() {

	override fun onDocument(document: GDocument, data: ValidationContext, visit: Visit) {
		val operations = document.definitions.filterIsInstance<GOperationDefinition>()
		if (operations.size <= 1)
			return

		val anonymousOperations = operations.filter { it.name === null }
		if (anonymousOperations.isEmpty())
			return

		data.reportError(
			message = "The document must not contain more than one operation if it contains an anonymous operation.",
			nodes = anonymousOperations
		)
	}
}
