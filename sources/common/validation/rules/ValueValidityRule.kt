package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Values-of-Correct-Type
internal object ValueValidityRule : ValidationRule {

	override fun validateArgument(argument: GArgument, context: ValidationContext) {
		val argumentDefinition = context.relatedArgumentDefinition
			?: return // Cannot validate unknown argument.

		context.schema.validateValue(argument.value, typeRef = argumentDefinition.type).forEach { error ->
			context.reportError(error)
		}
	}


	override fun validateArgumentDefinition(definition: GArgumentDefinition, context: ValidationContext) {
		val defaultValue = definition.defaultValue
			?: return // Nothing to validate.

		val type = context.relatedType
			?: return // Cannot validate argument of unknown type.

		context.schema.validateValue(defaultValue, type = type).forEach { error ->
			context.reportError(error)
		}
	}


	override fun validateVariableDefinition(definition: GVariableDefinition, context: ValidationContext) {
		val defaultValue = definition.defaultValue
			?: return // Nothing to validate.

		val type = context.relatedType
			?: return // Cannot validate argument of unknown type.

		context.schema.validateValue(defaultValue, type = type).forEach { error ->
			context.reportError(error)
		}
	}
}
