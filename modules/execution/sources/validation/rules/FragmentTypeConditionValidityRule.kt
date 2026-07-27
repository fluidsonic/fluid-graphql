package io.fluidsonic.graphql

// https://spec.graphql.org/draft/#sec-Fragments-on-Object-Interface-or-Union-Types
internal object FragmentTypeConditionValidityRule : ValidationRule.Singleton() {

	override fun onFragmentDefinition(definition: GFragmentDefinition, data: ValidationContext, visit: Visit) {
		val type = data.schema.resolveType(definition.typeCondition)
			?: return // Cannot validate type that doesn't exist.

		if (type is GCompositeType) {
			return // Type is valid.
		}

		data.reportError(
			message = "Fragment \"${definition.name}\" cannot condition on non composite type \"${type.name}\".",
			nodes = listOf(definition.typeCondition, type.nameNode),
		)
	}

	override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: ValidationContext, visit: Visit) {
		val typeCondition = selection.typeCondition
			?: return // Type condition is optional.

		val type = data.schema.resolveType(typeCondition)
			?: return // Cannot validate type that doesn't exist.

		if (type is GCompositeType) {
			return // Type is valid.
		}

		data.reportError(
			message = "Fragment cannot condition on non composite type \"${type.name}\".",
			nodes = listOf(typeCondition, type.nameNode),
		)
	}
}
