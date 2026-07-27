package io.fluidsonic.graphql

/**
 * Runs a set of validation rules over a document in one shared traversal.
 *
 * @param rules The rules to run. Exposed so that a test can assert every registered rule is actually covered.
 */
internal class Validator(val rules: List<ValidationRule.Provider>) {

	/**
	 * Validates [document] against [schema] and returns every violation found.
	 *
	 * Stops validating once [maxErrors] errors have been found and appends one terminal error saying so, for a total
	 * of `maxErrors + 1` errors. A document with many violations is rejected either way, and a client cannot act on
	 * thousands of errors — so the remaining work is wasted.
	 *
	 * @param maxErrors How many errors to report before validation is abandoned. Must not be negative.
	 */
	fun validate(document: GDocument, schema: GSchema, maxErrors: Int = defaultMaxErrors): List<GError> {
		val context = ValidationContext(document = document, schema = schema, maxErrors = maxErrors)

		return context.collectingErrors {
			document.accept(visitor = rules.map { it.provide() }.parallelizeContextualized(), data = context)
		}
	}

	companion object {

		/** How many errors [validate] reports before abandoning validation, matching graphql-js' `maxErrors`. */
		const val defaultMaxErrors = 100

		val default = Validator(
			rules = listOf(
				AllVariableUsesDefinedRule,
				AllVariablesUsedRule,
				AnonymousOperationExclusivityRule,
				ArgumentExistenceRule,
				ArgumentRequirementRule,
				ArgumentUniquenessRule,
				DirectiveExclusivityRule,
				DirectiveExistenceRule,
				DirectiveLocationValidityRule,
				DocumentExecutabilityRule,
				FieldSelectionExistenceRule,
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
				VariablesInAllowedPositionRule,
			),
		)
	}
}
