package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal object FragmentDefinitionNameExclusivityRule : ValidationRule {

	override fun validateDocument(document: GDocument, context: ValidationContext) {
		document.definitions
			.filterIsInstance<GFragmentDefinition>()
			.groupBy { it.name }
			.filter { (_, definitions) -> definitions.size > 1 }
			.forEach { (name, definitions) ->
				context.reportError(
					message = "The document must not contain multiple fragments with the same name '$name'.",
					nodes = definitions.map { it.nameNode }
				)
			}
	}
}
