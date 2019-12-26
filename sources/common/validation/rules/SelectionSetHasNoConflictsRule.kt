package io.fluidsonic.graphql


// FIXME do we need to consider @skip and @include? if so, can we merge the code with Executor.collectFieldSelections?
// FIXME will give false-negatives if two fragments are in conflict, but are never possible at the same time
//       Maybe if there is a conflict run a more thorough check that validates all possible object types independently.
// https://graphql.github.io/graphql-spec/draft/#sec-Field-Selections-on-Objects-Interfaces-and-Unions-Types
internal object SelectionSetHasNoConflictsRule : ValidationRule {

	override fun validateSelectionSet(set: GSelectionSet, context: ValidationContext) {
		// We only care about top-level selection sets in operations and fragment definitions.
		// Nested selections will already be checked recursively through these.
		if (context.relatedParentSelectionSet !== null)
			return

		// Cannot validate a selection for an unknown type.
		val parentType = context.relatedParentType?.underlyingNamedType
			?: return

		findConflictsInSet(
			set = set.selections.withParentType(parentType),
			context = context
		)
	}


	// FIXME default values
	private fun argumentValuesAreEqual(selection1: GFieldSelection, selection2: GFieldSelection): Boolean {
		if (selection1.arguments.size != selection2.arguments.size)
			return false

		for (argument1 in selection1.arguments) {
			val argument2 = selection2.argumentsByName[argument1.name]
				?: return false

			if (argument1.value != argument2.value)
				return false
		}

		return true
	}


	private fun findConflictsForResponseName(
		responseName: String,
		fields: List<ResolvedField>,
		context: ValidationContext
	) {
		val firstField = fields.first()

		if (fields.size > 1) {
			val isPotentiallyCompatibleType = fields.fold(true) { compatible, otherField ->
				compatible && isPotentiallyCompatibleType(firstField.type, otherField.type)
			}
			if (!isPotentiallyCompatibleType)
				return context.reportError(
					message = "Field '$responseName' in '${firstField.parentType.name}' is selected in multiple locations but with incompatible types.",
					nodes = fields.flatMap { listOf(it.selection, it.definition.type) }
				)

			// Cannot validate a selection of a field of an invalid type.
			val potentiallyCompatibleType = firstField.type.underlyingNamedType
			if (!potentiallyCompatibleType.isOutputType())
				return

			var bothFieldsAreOfDistinctObjectType = false

			val incompatibleFields = fields.filterIndexed incompatible@{ index, otherField ->
				// No need to compare field against itself.
				if (index == 0)
					return@incompatible false

				// If both fields belong to different Object types they can never be part of the same response.
				bothFieldsAreOfDistinctObjectType = firstField.parentType !== otherField.parentType &&
					firstField.type is GObjectType &&
					otherField.type is GObjectType
				if (bothFieldsAreOfDistinctObjectType)
					return@incompatible false

				val fieldNameAndArgumentsAreIdentical = firstField.selection.name == otherField.selection.name &&
					argumentValuesAreEqual(firstField.selection, otherField.selection)
				if (fieldNameAndArgumentsAreIdentical)
					return@incompatible false

				true
			}

			if (incompatibleFields.isNotEmpty())
				context.reportError(
					message = "Field '$responseName' in '${firstField.parentType.name}' is selected in multiple locations " +
						"but selects different fields or with different arguments.",
					nodes = (listOf(firstField) + incompatibleFields).map { it.selection }
				)
			else if (!bothFieldsAreOfDistinctObjectType)
				findConflictsInSet(
					set = fields.flatMap { field ->
						val parentType = field.type.underlyingNamedType

						field.selection.selectionSet?.selections?.map { selection ->
							SelectionInfo(parentType = parentType, selection = selection)
						}.orEmpty()
					},
					context = context
				)
		}
		else {
			findConflictsInSet(
				set = fields.flatMap { field ->
					val parentType = field.type.underlyingNamedType

					field.selection.selectionSet?.selections?.map { selection ->
						SelectionInfo(parentType = parentType, selection = selection)
					}.orEmpty()
				},
				context = context
			)
		}
	}


	private fun findConflictsInSet(
		set: List<SelectionInfo>,
		context: ValidationContext
	) {
		val fieldsByResponseName = set.groupByResponseName(context = context)

		for ((responseName, fields) in fieldsByResponseName)
			findConflictsForResponseName(
				responseName = responseName,
				fields = fields,
				context = context
			)
	}


	private fun GSelection.groupByResponseName(
		parentType: GNamedType,
		result: MutableMap<String, MutableList<ResolvedField>>,
		visitedFragments: MutableSet<String>,
		context: ValidationContext
	) {
		when (this) {
			is GFieldSelection -> {
				// Cannot validate a selection of a non-existent field.
				val fieldDefinition = context.schema.getFieldDefinition(type = parentType, name = name)
					?: return

				// Cannot validate a selection of a field of an unknown type.
				val fieldType = context.schema.resolveType(fieldDefinition.type)
					?: return

				result.getOrPut(alias ?: name) { mutableListOf() } += ResolvedField(
					definition = fieldDefinition,
					parentType = parentType,
					selection = this,
					type = fieldType
				)
			}

			is GFragmentSelection -> {
				// Cannot validate a selection that refers to a non-existent fragment.
				val fragment = context.document.fragmentsByName[name] ?: return

				// Cannot validate a selection that refers to a fragment on an unknown, invalid or incompatible type.
				val fragmentType = context.schema.resolveType(fragment.typeCondition)
					?.takeIf { it.isOutputType() }
					?: return

				fragment.selectionSet.selections
					.withParentType(parentType = fragmentType)
					.groupByResponseName(
						result = result,
						visitedFragments = visitedFragments,
						context = context
					)
			}

			is GInlineFragmentSelection -> {
				val fragmentType = typeCondition?.let { typeCondition ->
					// Cannot validate a selection that refers to a fragment on an unknown, invalid or incompatible type.
					context.schema.resolveType(typeCondition)
						?.takeIf { it.isOutputType() }
						?: return
				} ?: parentType

				selectionSet.selections
					.withParentType(parentType = fragmentType)
					.groupByResponseName(
						result = result,
						visitedFragments = visitedFragments,
						context = context
					)
			}
		}
	}


	private fun isPotentiallyCompatibleType(a: GType, b: GType): Boolean {
		if (a === b)
			return true

		return when (a) {
			is GListType ->
				if (b is GListType) isPotentiallyCompatibleType(a.elementType, b.elementType)
				else false

			is GNonNullType ->
				if (b is GNonNullType) isPotentiallyCompatibleType(a.nullableType, b.nullableType)
				else false

			is GEnumType,
			is GInterfaceType,
			is GInputObjectType,
			is GObjectType,
			is GScalarType,
			is GUnionType ->
				false
		}
	}


	private fun List<GSelection>.withParentType(parentType: GNamedType) =
		map { SelectionInfo(parentType = parentType, selection = it) }


	private fun List<SelectionInfo>.groupByResponseName(
		context: ValidationContext
	): Map<String, List<ResolvedField>> {
		val result = mutableMapOf<String, MutableList<ResolvedField>>()

		groupByResponseName(
			result = result,
			visitedFragments = mutableSetOf(),
			context = context
		)

		return result
	}


	private fun List<SelectionInfo>.groupByResponseName(
		context: ValidationContext,
		result: MutableMap<String, MutableList<ResolvedField>>,
		visitedFragments: MutableSet<String>
	) {
		for ((parentType, selection) in this)
			selection.groupByResponseName(
				context = context,
				parentType = parentType,
				result = result,
				visitedFragments = visitedFragments
			)
	}


	private class ResolvedField(
		val definition: GFieldDefinition,
		val parentType: GNamedType,
		val selection: GFieldSelection,
		val type: GType
	)


	private data class SelectionInfo(
		val parentType: GNamedType,
		val selection: GSelection
	)
}
