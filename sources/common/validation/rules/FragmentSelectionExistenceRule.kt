package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-spread-target-defined
internal object FragmentSelectionExistenceRule : ValidationRule {

	override fun validateFragmentSelection(selection: GFragmentSelection, context: ValidationContext) {
		val definition = context.relatedFragmentDefinition
		if (definition !== null)
			return // Fragment exists.

		context.reportError(
			message = "Fragment '${selection.name}' does not exist.",
			nodes = listOf(selection.nameNode)
		)
	}
}
