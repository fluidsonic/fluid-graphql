package io.fluidsonic.graphql

// https://graphql.github.io/graphql-spec/draft/#sec-All-Variable-Usages-Are-Allowed
internal class VariablesInAllowedPositionRule : ValidationRule() {

	// The usages in each fragment, by name, collected the first time an operation reaches the fragment.
	private val usagesByFragmentName = mutableMapOf<String, List<VariableUsage>>()

	// Like graphql-js, each operation is checked against its own variable definitions as the traversal leaves it, for
	// its own usages and those of every fragment it reaches. A fragment shared by several operations is therefore
	// checked once for each of them, and one that no operation reaches is not checked at all. Its usages are collected
	// in a traversal of their own because the fragment may be defined after the operation.
	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		visit.afterChildren {
			val varDefMap = definition.variableDefinitions.associateBy { it.name }
			val fragmentUsages = collectReferencedFragments(definition, data.document).flatMap { fragment ->
				usagesByFragmentName.getOrPut(fragment.name) { collectVariableUsages(fragment, data) }
			}

			for (usage in collectVariableUsages(definition, data) + fragmentUsages) {
				checkUsage(usage, varDefMap = varDefMap, data = data)
			}
		}
	}

	private fun checkUsage(usage: VariableUsage, varDefMap: Map<String, GVariableDefinition>, data: ValidationContext) {
		val ref = usage.ref
		val varDef = varDefMap[ref.name] ?: return
		val locationType = usage.locationType ?: return
		val varType = data.schema.resolveType(varDef.type) ?: return

		// A OneOf input object's *fields* must be nullable, but a *variable* supplying one must not be: a
		// nullable variable could carry `null` at runtime and break the "exactly one field, non-null"
		// invariant, and validation cannot see variable values. So the variable is rejected on its
		// nullability alone, whether or not a value is supplied. Upstream reports this from this same rule.
		// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects
		val parentType = usage.parentType
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

		if (!allowedVariableUsage(varType, varDef.defaultValue, locationType, usage.locationDefaultValue)) {
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

	private fun collectVariableUsages(definition: GExecutableDefinition, data: ValidationContext): List<VariableUsage> {
		val collector = VariableUsageCollector()
		definition.accept(
			collector.contextualize(
				VisitorContext(document = data.document, schema = data.schema, fieldDefinition = data.schema::fieldDefinition),
			),
		)

		return collector.usages
	}

	companion object : Factory(::VariablesInAllowedPositionRule)
}

// One variable reference together with what its position expects, as graphql-js `getVariableUsages` reports it.
private class VariableUsage(val ref: GVariableRef, val locationType: GType?, val locationDefaultValue: GValue?, val parentType: GType?)

private class VariableUsageCollector : Visitor.Hierarchical<Unit, VisitorContext>() {

	val usages = mutableListOf<VariableUsage>()

	override fun onAny(node: GNode, data: VisitorContext, visit: Visit) = Unit

	// graphql-js `getVariableUsages` does not look into variable definitions either.
	override fun onVariableDefinition(definition: GVariableDefinition, data: VisitorContext, visit: Visit) {
		visit.skipChildren()
	}

	override fun onVariableRef(ref: GVariableRef, data: VisitorContext, visit: Visit) {
		usages += VariableUsage(
			ref = ref,
			locationType = data.relatedType,
			// A list item never has a default value, even if the list it is part of has one.
			locationDefaultValue = data.relatedArgumentDefinition?.defaultValue?.takeIf { data.parentNode !is GListValue },
			parentType = data.relatedParentType,
		)
	}
}
