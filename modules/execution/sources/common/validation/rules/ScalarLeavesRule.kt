package io.fluidsonic.graphql


// http://spec.graphql.org/draft/#sec-Leaf-Field-Selections
internal object ScalarLeavesRule : ValidationRule.Singleton() {

	override fun onFieldSelection(selection: GFieldSelection, data: ValidationContext, visit: Visit) {
		val type = data.relatedType?.underlyingNamedType ?: return
		val selectionSet = selection.selectionSet

		when (type) {
			is GLeafType ->
				if (selectionSet != null) {
					data.reportError(
						message = "Field '${selection.name}' must not have a selection since ${type.kind} type '${type.name}' has no subfields.",
						nodes = listOf(selectionSet)
					)
				}

			else -> {
				if (selectionSet == null)
					data.reportError(
						message = "Field '${selection.name}' of ${type.kind} type '${type.name}' must have a selection of subfields. " +
							"Did you mean '${selection.name} { … }'?",
						nodes = listOf(selection)
					)
			}
		}
	}
}
