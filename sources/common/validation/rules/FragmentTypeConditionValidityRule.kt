package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Object-Spreads-In-Object-Scope
internal object FragmentTypeConditionValidityRule : ValidationRule {

	override fun validateFragmentDefinition(definition: GFragmentDefinition, context: ValidationContext) {
		val type = context.schema.resolveType(definition.typeCondition)
			?: return // Cannot validate type that doesn't exist.

		if (type is GCompositeType)
			return // Type is valid.

		context.reportError(
			message = "Fragment '${definition.name}' is specified on ${type.kind} '${type.name}' but must be specified on an Interface, Object or Union type.",
			nodes = listOf(definition.typeCondition, type.nameNode)
		)
	}


	override fun validateInlineFragmentSelection(selection: GInlineFragmentSelection, context: ValidationContext) {
		val typeCondition = selection.typeCondition
			?: return // Type condition is optional.

		val type = context.schema.resolveType(typeCondition)
			?: return // Cannot validate type that doesn't exist.

		if (type is GCompositeType)
			return // Type is valid.

		context.reportError(
			message = "Inline fragment is specified on ${type.kind} '${type.name}' but must be specified on an Interface, Object or Union type.",
			nodes = listOf(typeCondition, type.nameNode)
		)
	}
}
