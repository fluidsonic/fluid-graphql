package io.fluidsonic.graphql

// Every variable reference the operation uses, including those in directive arguments: first the operation's own in
// document order, then those of each fragment it reaches, in the order of `collectReferencedFragments`. That is the
// order of graphql-js `getRecursiveVariableUsages`.
// https://spec.graphql.org/draft/#sec-All-Variables-Used
internal fun collectVariableRefs(operation: GOperationDefinition, document: GDocument): List<Pair<String, GVariableRef>> {
	val result = mutableListOf<Pair<String, GVariableRef>>()
	collectFromDirectives(operation.directives, result)
	collectFromSelectionSet(operation.selectionSet, result)

	for (fragment in collectReferencedFragments(operation, document)) {
		collectFromDirectives(fragment.directives, result)
		collectFromSelectionSet(fragment.selectionSet, result)
	}

	return result
}

// The fragments the operation spreads, directly or transitively, each once and in the order of graphql-js
// `getRecursivelyReferencedFragments`: a selection set's own spreads come before those nested in it, and nested
// selection sets are visited last in, first out. Spreads of undefined fragments are skipped.
internal fun collectReferencedFragments(operation: GOperationDefinition, document: GDocument): List<GFragmentDefinition> {
	val fragments = mutableListOf<GFragmentDefinition>()
	val collectedNames = mutableSetOf<String>()
	val selectionSetsToVisit = mutableListOf(operation.selectionSet)

	while (selectionSetsToVisit.isNotEmpty()) {
		for (spread in collectFragmentSpreads(selectionSetsToVisit.removeAt(selectionSetsToVisit.size - 1))) {
			if (!collectedNames.add(spread.name)) continue
			val fragment = document.fragment(spread.name) ?: continue
			fragments += fragment
			selectionSetsToVisit += fragment.selectionSet
		}
	}

	return fragments
}

// graphql-js `getFragmentSpreads`
private fun collectFragmentSpreads(selectionSet: GSelectionSet): List<GFragmentSelection> {
	val spreads = mutableListOf<GFragmentSelection>()
	val selectionSetsToVisit = mutableListOf(selectionSet)

	while (selectionSetsToVisit.isNotEmpty()) {
		for (selection in selectionSetsToVisit.removeAt(selectionSetsToVisit.size - 1).selections) {
			when (selection) {
				is GFieldSelection -> selection.selectionSet?.let { selectionSetsToVisit += it }
				is GFragmentSelection -> spreads += selection
				is GInlineFragmentSelection -> selectionSetsToVisit += selection.selectionSet
			}
		}
	}

	return spreads
}

// A spread's own directives are part of the selection set, but the fragment it spreads is not.
private fun collectFromSelectionSet(selectionSet: GSelectionSet, result: MutableList<Pair<String, GVariableRef>>) {
	for (selection in selectionSet.selections) {
		when (selection) {
			is GFieldSelection -> {
				for (arg in selection.arguments) {
					collectFromValue(arg.value, result)
				}
				collectFromDirectives(selection.directives, result)
				selection.selectionSet?.let { collectFromSelectionSet(it, result) }
			}
			is GFragmentSelection ->
				collectFromDirectives(selection.directives, result)
			is GInlineFragmentSelection -> {
				collectFromDirectives(selection.directives, result)
				collectFromSelectionSet(selection.selectionSet, result)
			}
		}
	}
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
