package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal object FragmentTypeConditionExistenceRule : ValidationRule {

	override fun validateFragmentDefinition(definition: GFragmentDefinition, context: ValidationContext) {
		if (context.schema.resolveType(definition.typeCondition) !== null)
			return // Type exists.

		context.reportError(
			message = "A fragment must be specified on a type that exist in the schema.",
			nodes = listOf(definition.typeCondition)
		)
	}


	override fun validateInlineFragmentSelection(selection: GInlineFragmentSelection, context: ValidationContext) {
		val typeCondition = selection.typeCondition
			?: return // Type condition is optional.

		if (context.schema.resolveType(typeCondition) !== null)
			return // Type exists.

		context.reportError(
			message = "A fragment spread must be specified on a type that exist in the schema.",
			nodes = listOf(typeCondition)
		)
	}
}
