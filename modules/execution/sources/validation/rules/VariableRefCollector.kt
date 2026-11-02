package io.fluidsonic.graphql

// Every variable reference the operation uses, in document order, including those in directive arguments and in the
// fragments it transitively spreads.
// https://spec.graphql.org/draft/#sec-All-Variables-Used
internal fun collectVariableRefs(operation: GOperationDefinition, document: GDocument): List<Pair<String, GVariableRef>> {
	val result = mutableListOf<Pair<String, GVariableRef>>()
	collectFromDirectives(operation.directives, result)
	result += collectVariableRefs(operation.selectionSet, document, visitedFragments = mutableSetOf())
	return result
}

private fun collectVariableRefs(selectionSet: GSelectionSet, document: GDocument, visitedFragments: MutableSet<String>): List<Pair<String, GVariableRef>> {
	val result = mutableListOf<Pair<String, GVariableRef>>()
	for (selection in selectionSet.selections) {
		when (selection) {
			is GFieldSelection -> {
				for (arg in selection.arguments) {
					collectFromValue(arg.value, result)
				}
				collectFromDirectives(selection.directives, result)
				selection.selectionSet?.let { result += collectVariableRefs(it, document, visitedFragments) }
			}
			is GFragmentSelection -> {
				// The spread's own directives are used even if the fragment was visited before or is undefined.
				collectFromDirectives(selection.directives, result)
				if (!visitedFragments.add(selection.name)) continue
				val frag = document.fragment(selection.name) ?: continue
				collectFromDirectives(frag.directives, result)
				result += collectVariableRefs(frag.selectionSet, document, visitedFragments)
			}
			is GInlineFragmentSelection -> {
				collectFromDirectives(selection.directives, result)
				result += collectVariableRefs(selection.selectionSet, document, visitedFragments)
			}
		}
	}
	return result
}

private fun collectFromDirectives(directives: List<GDirective>, result: MutableList<Pair<String, GVariableRef>>) {
	for (directive in directives) {
		for (arg in directive.arguments) {
			collectFromValue(arg.value, result)
		}
	}
}

private fun collectFromValue(value: GValue, result: MutableList<Pair<String, GVariableRef>>) {
	when (value) {
		is GVariableRef -> result += value.name to value
		is GListValue -> value.elements.forEach { collectFromValue(it, result) }
		is GObjectValue -> value.arguments.forEach { collectFromValue(it.value, result) }
		else -> {}
	}
}
