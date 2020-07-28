package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal object FragmentDefinitionNameExclusivityRule : ValidationRule.Singleton() {

	override fun onDocument(document: GDocument, data: ValidationContext, visit: Visit) {
		document.definitions
			.filterIsInstance<GFragmentDefinition>()
			.groupBy { it.name }
			.filter { (_, definitions) -> definitions.size > 1 }
			.forEach { (name, definitions) ->
				data.reportError(
					message = "The document must not contain multiple fragments with the same name '$name'.",
					nodes = definitions.map { it.nameNode }
				)
			}
	}
}
