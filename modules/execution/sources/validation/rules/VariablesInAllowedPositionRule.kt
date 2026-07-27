package io.fluidsonic.graphql

// https://graphql.github.io/graphql-spec/draft/#sec-All-Variable-Usages-Are-Allowed
internal class VariablesInAllowedPositionRule : ValidationRule() {

	private var varDefMap = mutableMapOf<String, GVariableDefinition>()

	// `onVariableRef` relies on the operation's own definitions only, and a variable reference can never precede
	// them, so this needs nothing but a clean map before the operation's subtree is traversed.
	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		varDefMap = mutableMapOf()
	}

	override fun onVariableDefinition(definition: GVariableDefinition, data: ValidationContext, visit: Visit) {
		varDefMap[definition.name] = definition
	}

	override fun onVariableRef(ref: GVariableRef, data: ValidationContext, visit: Visit) {
		val varDef = varDefMap[ref.name] ?: return
		val locationType = data.relatedType ?: return
		val varType = data.schema.resolveType(varDef.type) ?: return

		// A OneOf input object's *fields* must be nullable, but a *variable* supplying one must not be: a
		// nullable variable could carry `null` at runtime and break the "exactly one field, non-null"
		// invariant, and validation cannot see variable values. So the variable is rejected on its
		// nullability alone, whether or not a value is supplied. Upstream reports this from this same rule.
		// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects
		val parentType = data.relatedParentType
		if (
			parentType is GInputObjectType &&
			parentType.directive(GLanguage.defaultOneOfDirective.name) !== null &&
			varType !is GNonNullType
		) {
			data.reportError(
				message = "Variable \"\$${ref.name}\" is of type \"${varDef.type}\" but must be non-nullable " +
					"to be used for OneOf Input Object \"${parentType.name}\".",
				nodes = listOf(varDef, ref),
			)

			return
		}

		if (!allowedVariableUsage(varType, varDef.defaultValue, locationType, data.relatedArgumentDefinition?.defaultValue)) {
			data.reportError(
				message = "Variable '\$${ref.name}' of type '${varType.name}' cannot be used as an argument of type '${locationType.name}'.",
				nodes = listOf(varDef, ref),
			)
		}
	}

	private fun allowedVariableUsage(varType: GType, varDefault: GValue?, locationType: GType, locationDefault: GValue?): Boolean {
		if (locationType is GNonNullType && varType !is GNonNullType) {
			val hasNonNullVarDefault = varDefault != null && varDefault !is GNullValue
			val hasLocationDefault = locationDefault != null
			if (!hasNonNullVarDefault && !hasLocationDefault) return false
			return varType.isSubtypeOf(locationType.nullableType)
		}
		return varType.isSubtypeOf(locationType)
	}

	companion object : Factory(::VariablesInAllowedPositionRule)
}
