package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Values-of-Correct-Type
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object ValueValidityRule : ValidationRule.Singleton() {

	override fun onArgument(argument: GArgument, data: ValidationContext, visit: Visit) {
		val argumentDefinition = data.relatedArgumentDefinition
			?: return // Cannot validate unknown argument.

		data.schema.validateValue(argument.value, typeRef = argumentDefinition.type).forEach { error ->
			data.reportError(error)
		}
	}


	override fun onArgumentDefinition(definition: GArgumentDefinition, data: ValidationContext, visit: Visit) {
		val defaultValue = definition.defaultValue
			?: return // Nothing to validate.

		val type = data.relatedType
			?: return // Cannot validate argument of unknown type.

		data.schema.validateValue(defaultValue, type = type).forEach { error ->
			data.reportError(error)
		}
	}


	override fun onVariableDefinition(definition: GVariableDefinition, data: ValidationContext, visit: Visit) {
		val defaultValue = definition.defaultValue
			?: return // Nothing to validate.

		val type = data.relatedType
			?: return // Cannot validate argument of unknown type.

		data.schema.validateValue(defaultValue, type = type).forEach { error ->
			data.reportError(error)
		}
	}
}
