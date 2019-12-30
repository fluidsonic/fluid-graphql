package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-Name-Uniqueness
internal object FragmentTypeConditionExistenceRule : ValidationRule.Singleton() {

	override fun onFragmentDefinition(definition: GFragmentDefinition, data: ValidationContext, visit: Visit) {
		if (data.schema.resolveType(definition.typeCondition) !== null)
			return // Type exists.

		data.reportError(
			message = "A fragment must be specified on a type that exist in the schema.",
			nodes = listOf(definition.typeCondition)
		)
	}


	override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: ValidationContext, visit: Visit) {
		val typeCondition = selection.typeCondition
			?: return // Type condition is optional.

		if (data.schema.resolveType(typeCondition) !== null)
			return // Type exists.

		data.reportError(
			message = "A fragment spread must be specified on a type that exist in the schema.",
			nodes = listOf(typeCondition)
		)
	}
}
