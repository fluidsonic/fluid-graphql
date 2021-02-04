package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Operation-Name-Uniqueness
internal object OperationDefinitionNameExclusivityRule : ValidationRule.Singleton() {

	override fun onDocument(document: GDocument, data: ValidationContext, visit: Visit) {
		document.definitions
			.filterIsInstance<GOperationDefinition>()
			.filter { it.name !== null }
			.groupBy { it.name!! }
			.filter { (_, operations) -> operations.size > 1 }
			.forEach { (name, operations) ->
				data.reportError(
					message = "The document must not contain multiple operations with the same name '$name'.",
					nodes = operations.map { it.nameNode!! }
				)
			}
	}
}
