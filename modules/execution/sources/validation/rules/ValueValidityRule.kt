package io.fluidsonic.graphql

// https://graphql.github.io/graphql-spec/draft/#sec-Values-of-Correct-Type
internal object ValueValidityRule : ValidationRule.Singleton() {

	override fun onArgument(argument: GArgument, data: ValidationContext, visit: Visit) {
		val argumentDefinition = data.relatedArgumentDefinition
			?: return // Cannot validate unknown argument.

		data.schema
			.validateValueForExecution(
				argument.value,
				type = null,
				typeRef = argumentDefinition.type,
				scalarLiteralCoercionOverride = ::attachedScalarLiteralCoercion,
			)
			.forEach { error ->
				data.reportError(error)
			}

		visit.skipChildren()
	}

	override fun onArgumentDefinition(definition: GArgumentDefinition, data: ValidationContext, visit: Visit) {
		val defaultValue = definition.defaultValue
			?: return // Nothing to validate.

		val type = data.relatedType
			?: return // Cannot validate argument of unknown type.

		data.schema
			.validateValueForExecution(defaultValue, type = type, typeRef = null, scalarLiteralCoercionOverride = ::attachedScalarLiteralCoercion)
			.forEach { error ->
				data.reportError(error)
			}

		visit.skipChildren()
	}

	override fun onVariableDefinition(definition: GVariableDefinition, data: ValidationContext, visit: Visit) {
		val defaultValue = definition.defaultValue
			?: return // Nothing to validate.

		val type = data.relatedType
			?: return // Cannot validate argument of unknown type.

		data.schema
			.validateValueForExecution(defaultValue, type = type, typeRef = null, scalarLiteralCoercionOverride = ::attachedScalarLiteralCoercion)
			.forEach { error ->
				data.reportError(error)
			}

		visit.skipChildren()
	}
}

// The validator must resolve the coercer the very same way `NodeInputConverter.coerceValueForScalar` does:
// a coercer attached to the type wins over the coercion the type performs itself, so validating without it
// would accept a literal that execution then rejects — or reject one it would have accepted.
private fun attachedScalarLiteralCoercion(type: GScalarType): ((value: GValue) -> Any?)? =
	type.inputLiteralCoercer?.let { coercer -> coercer::coerceInputLiteral }
