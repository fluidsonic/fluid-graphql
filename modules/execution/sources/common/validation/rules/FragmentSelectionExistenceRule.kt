package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-spread-target-defined
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object FragmentSelectionExistenceRule : ValidationRule.Singleton() {

	override fun onFragmentSelection(selection: GFragmentSelection, data: ValidationContext, visit: Visit) {
		val definition = data.relatedFragmentDefinition
		if (definition !== null)
			return // Fragment exists.

		data.reportError(
			message = "Fragment '${selection.name}' does not exist.",
			nodes = listOf(selection.nameNode)
		)
	}
}
