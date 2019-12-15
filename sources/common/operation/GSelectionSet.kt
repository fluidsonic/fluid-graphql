package io.fluidsonic.graphql


class GSelectionSet(
	val selections: List<GSelection>
) {

	companion object {

		fun from(ast: GAst.SelectionSet) =
			GSelectionSet(
				selections = ast.selections.map { GSelection.from(it) }
			)
	}
}
