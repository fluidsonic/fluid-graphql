package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-All-Variable-Usages-Are-Allowed
internal class VariablesInAllowedPositionRule : ValidationRule() {

	private var varDefMap = mutableMapOf<String, GVariableDefinition>()

	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		varDefMap = mutableMapOf()
		visit.visitChildren()
	}

	override fun onVariableDefinition(definition: GVariableDefinition, data: ValidationContext, visit: Visit) {
		varDefMap[definition.name] = definition
	}

	override fun onVariableRef(ref: GVariableRef, data: ValidationContext, visit: Visit) {
		val varDef = varDefMap[ref.name] ?: return
		val locationType = data.relatedType ?: return
		val varType = data.schema.resolveType(varDef.type) ?: return

		if (!allowedVariableUsage(varType, varDef.defaultValue, locationType, data.relatedArgumentDefinition?.defaultValue)) {
			data.reportError(
				message = "Variable '\$${ref.name}' of type '${varType.name}' cannot be used as an argument of type '${locationType.name}'.",
				nodes = listOf(varDef, ref)
			)
		}
	}

	private fun allowedVariableUsage(
		varType: GType,
		varDefault: GValue?,
		locationType: GType,
		locationDefault: GValue?,
	): Boolean {
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
