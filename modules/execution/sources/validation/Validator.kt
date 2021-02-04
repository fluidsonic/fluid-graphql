package io.fluidsonic.graphql


internal class Validator(
	private val rules: List<ValidationRule.Provider>,
) {

	fun validate(document: GDocument, schema: GSchema): List<GError> {
		val context = ValidationContext(document, schema)

		// FIXME Why can't we just contextualize the entire visitor?
		document.accept(rules
			.map { it.provide().contextualize(context) }
			.parallelize())

		return context.errors
	}


	companion object {

		val default = Validator(rules = listOf(
			AnonymousOperationExclusivityRule,
			ArgumentExistenceRule,
			ArgumentRequirementRule,
			DirectiveExclusivityRule,
			DirectiveExistenceRule,
			DirectiveLocationValidityRule,
			DocumentExecutabilityRule,
			FieldSelectionExistenceRule,
			FieldSubselectionRule,
			FragmentCycleDetectionRule,
			FragmentDefinitionNameExclusivityRule,
			FragmentDefinitionUsageRule,
			FragmentSelectionExistenceRule,
			FragmentSelectionPossibilityRule,
			FragmentTypeConditionExistenceRule,
			FragmentTypeConditionValidityRule,
			ObjectFieldExistenceRule,
			ObjectFieldNameExclusivityRule,
			ObjectFieldRequirementRule,
			OperationDefinitionNameExclusivityRule,
			ScalarLeavesRule,
			SelectionUnambiguityRule,
			SubscriptionRootFieldExclusivityRule,
			ValueValidityRule,
			VariableDefinitionNameExclusivityRule,
			VariableDefinitionTypeValidityRule,
		))
	}
}
