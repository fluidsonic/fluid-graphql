package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Object-Spreads-In-Object-Scope
internal object FragmentTypeConditionValidityRule : ValidationRule.Singleton() {

	override fun onFragmentDefinition(definition: GFragmentDefinition, data: ValidationContext, visit: Visit) {
		val type = data.schema.resolveType(definition.typeCondition)
			?: return // Cannot validate type that doesn't exist.

		if (type is GCompositeType)
			return // Type is valid.

		data.reportError(
			message = "Fragment '${definition.name}' is specified on ${type.kind} '${type.name}' but must be specified on an interface, object or union type.",
			nodes = listOf(definition.typeCondition, type.nameNode)
		)
	}


	override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: ValidationContext, visit: Visit) {
		val typeCondition = selection.typeCondition
			?: return // Type condition is optional.

		val type = data.schema.resolveType(typeCondition)
			?: return // Cannot validate type that doesn't exist.

		if (type is GCompositeType)
			return // Type is valid.

		data.reportError(
			message = "Inline fragment is specified on ${type.kind} '${type.name}' but must be specified on an interface, object or union type.",
			nodes = listOf(typeCondition, type.nameNode)
		)
	}
}
