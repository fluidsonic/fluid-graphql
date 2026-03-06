package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Single-root-field
internal object SubscriptionRootFieldExclusivityRule : ValidationRule.Singleton() {

	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		if (definition.type != GOperationType.subscription)
			return

		val fields = collectFields(definition.selectionSet, data.document, mutableSetOf())

		if (fields.size != 1 || fields.keys.first().startsWith("__")) {
			data.reportError(
				message = "Subscription operations must have exactly one root field.",
				nodes = fields.values.flatten()
			)
		}
	}

	private fun collectFields(
		selectionSet: GSelectionSet,
		document: GDocument,
		visitedFragments: MutableSet<String>,
	): Map<String, List<GFieldSelection>> {
		val fields = mutableMapOf<String, MutableList<GFieldSelection>>()
		for (selection in selectionSet.selections) {
			when (selection) {
				is GFieldSelection ->
					fields.getOrPut(selection.alias ?: selection.name, ::mutableListOf).add(selection)
				is GFragmentSelection -> {
					if (!visitedFragments.add(selection.name)) continue
					val fragment = document.fragment(selection.name) ?: continue
					collectFields(fragment.selectionSet, document, visitedFragments).forEach { (k, v) ->
						fields.getOrPut(k, ::mutableListOf).addAll(v)
					}
				}
				is GInlineFragmentSelection ->
					collectFields(selection.selectionSet, document, visitedFragments).forEach { (k, v) ->
						fields.getOrPut(k, ::mutableListOf).addAll(v)
					}
			}
		}
		return fields
	}
}
