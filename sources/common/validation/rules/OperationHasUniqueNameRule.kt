package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Operation-Name-Uniqueness
internal object OperationHasUniqueNameRule : ValidationRule {

	override fun validateDocument(document: GDocument, context: ValidationContext) {
		document.definitions
			.filterIsInstance<GOperationDefinition>()
			.filter { it.name !== null }
			.groupBy { it.name!! }
			.filter { (_, operations) -> operations.size > 1 }
			.forEach { (name, operations) ->
				context.reportError(
					message = "The document must not contain multiple operations with the same name '$name'.",
					nodes = operations
				)
			}
	}
}
