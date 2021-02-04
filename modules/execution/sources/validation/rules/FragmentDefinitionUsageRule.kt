package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal class FragmentDefinitionUsageRule : ValidationRule() {

	private val fragmentDefinitions = mutableListOf<GFragmentDefinition>()
	private val operationDefinitions = mutableListOf<GOperationDefinition>()


	override fun onDocument(document: GDocument, data: ValidationContext, visit: Visit) {
		visit.visitChildren()

		val referencedFragmentNames = mutableSetOf<String>()

		for (operationDefinition in operationDefinitions)
			collectReferencedFragmentNames(
				document = data.document,
				set = operationDefinition.selectionSet,
				target = referencedFragmentNames
			)

		fragmentDefinitions
			.filterNot { referencedFragmentNames.contains(it.name) }
			.groupBy { it.name }
			.values
			.map { it.first() }
			.forEach { fragment ->
				data.reportError(
					message = "Fragment '${fragment.name}' is not used by any operation.",
					nodes = listOf(fragment.nameNode)
				)
			}
	}


	override fun onFragmentDefinition(definition: GFragmentDefinition, data: ValidationContext, visit: Visit) {
		fragmentDefinitions += definition
	}


	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		operationDefinitions += definition
	}


	private fun collectReferencedFragmentNames(document: GDocument, set: GSelectionSet, target: MutableSet<String>) {
		for (selection in set.selections)
			when (selection) {
				is GFieldSelection ->
					selection.selectionSet?.let { subset ->
						collectReferencedFragmentNames(
							document = document,
							set = subset,
							target = target
						)
					}

				is GFragmentSelection -> {
					if (!target.add(selection.name))
						return

					val definition = document.fragment(selection.name)
						?: return // Cannot visit nonexistent fragment.

					collectReferencedFragmentNames(
						document = document,
						set = definition.selectionSet,
						target = target
					)
				}

				is GInlineFragmentSelection ->
					collectReferencedFragmentNames(
						document = document,
						set = selection.selectionSet,
						target = target
					)
			}
	}


	companion object : Factory(::FragmentDefinitionUsageRule)
}
