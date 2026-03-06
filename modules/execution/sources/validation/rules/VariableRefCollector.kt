package io.fluidsonic.graphql


internal fun collectVariableRefs(
	selectionSet: GSelectionSet,
	document: GDocument,
	visitedFragments: MutableSet<String> = mutableSetOf(),
): List<Pair<String, GVariableRef>> {
	val result = mutableListOf<Pair<String, GVariableRef>>()
	for (selection in selectionSet.selections) {
		when (selection) {
			is GFieldSelection -> {
				for (arg in selection.arguments)
					collectFromValue(arg.value, result)
				selection.selectionSet?.let { result += collectVariableRefs(it, document, visitedFragments) }
			}
			is GFragmentSelection -> {
				if (!visitedFragments.add(selection.name)) continue
				val frag = document.fragment(selection.name) ?: continue
				result += collectVariableRefs(frag.selectionSet, document, visitedFragments)
			}
			is GInlineFragmentSelection ->
				result += collectVariableRefs(selection.selectionSet, document, visitedFragments)
		}
	}
	return result
}


private fun collectFromValue(value: GValue, result: MutableList<Pair<String, GVariableRef>>) {
	when (value) {
		is GVariableRef -> result += value.name to value
		is GListValue -> value.elements.forEach { collectFromValue(it, result) }
		is GObjectValue -> value.arguments.forEach { collectFromValue(it.value, result) }
		else -> {}
	}
}
