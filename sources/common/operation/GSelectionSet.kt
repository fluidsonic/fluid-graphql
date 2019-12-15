package io.fluidsonic.graphql


class GSelectionSet(
	val selections: List<GSelection>
) {

	companion object {

		internal fun build(ast: AstNode.SelectionSet) =
			GSelectionSet(
				selections = ast.selections.map { GSelection.build(it) }
			)
	}
}
