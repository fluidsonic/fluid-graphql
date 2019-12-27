package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal class FragmentCycleDetectionRule : ValidationRule {

	private val path: MutableList<GFragmentSelection> = mutableListOf()
	private val pathIndexByName: MutableMap<String, Int> = mutableMapOf()
	private val visitedFragments: MutableSet<String> = mutableSetOf()


	override fun validateFragmentDefinition(definition: GFragmentDefinition, context: ValidationContext) {
		reportCycles(context = context, definition = definition)
	}


	private fun reportCycles(context: ValidationContext, definition: GFragmentDefinition) {
		if (!visitedFragments.add(definition.name))
			return

		val childSelection = definition.selectionSet.findImmediateFragmentSelections()
			.ifEmpty { return }

		pathIndexByName[definition.name] = path.size

		for (child in childSelection) {
			path += child

			val cycleIndex = pathIndexByName[child.name]
			if (cycleIndex == null) {
				val childDefinition = context.document.fragment(child.name)
				if (childDefinition !== null)
					reportCycles(context = context, definition = childDefinition)
			}
			else {
				val cyclePath = path.drop(cycleIndex).map { it.nameNode }
				val cycleText = cyclePath
					.dropLast(1)
					.ifEmpty { null }
					?.joinToString(separator = "' -> '") { it.value }
					?.let { " through '$it'" }
					.orEmpty()

				context.reportError(
					message = "Fragment '${child.name}' cannot recursively reference itself$cycleText.",
					nodes = cyclePath
				)
			}

			path.removeAt(path.size - 1)
		}

		pathIndexByName.remove(definition.name)
	}


	private fun GSelectionSet.findImmediateFragmentSelections(target: MutableList<GFragmentSelection> = mutableListOf()): List<GFragmentSelection> {
		for (selection in selections)
			when (selection) {
				is GFieldSelection -> selection.selectionSet?.findImmediateFragmentSelections(target = target)
				is GFragmentSelection -> target += selection
				is GInlineFragmentSelection -> selection.selectionSet.findImmediateFragmentSelections(target = target)
			}

		return target
	}
}
