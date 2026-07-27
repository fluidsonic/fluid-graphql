package io.fluidsonic.graphql

// FIXME do we need to consider @skip and @include? if so, can we merge the code with Executor.collectFieldSelections?
// This is a port of graphql-js' OverlappingFieldsCanBeMergedRule.
// https://spec.graphql.org/draft/#sec-Field-Selection-Merging
internal class SelectionUnambiguityRule : ValidationRule() {

	private val cachedFieldsAndFragmentSpreads: MutableMap<GSelectionSet, FieldsAndFragmentSpreads> = mutableMapOf()
	private val comparedFieldsAndFragmentPairs = OrderedPairSet<FieldMap, String>()
	private val comparedFragmentPairs = PairSet()

	// Runs for every selection set, like upstream. Memoization — not a narrower hook — is what keeps this from
	// re-walking or double-reporting.
	override fun onSelectionSet(set: GSelectionSet, data: ValidationContext, visit: Visit) {
		val conflicts = findConflictsWithinSelectionSet(
			data = data,
			parentType = data.relatedParentType?.underlyingNamedType,
			selectionSet = set,
		)

		for (conflict in conflicts) {
			data.reportError(
				message = "Fields \"${conflict.responseName}\" conflict because ${conflict.reason.describe()}. " +
					"Use different aliases on the fields to fetch both if this was intentional.",
				nodes = conflict.fields1 + conflict.fields2,
			)
		}
	}

	private fun findConflictsWithinSelectionSet(data: ValidationContext, parentType: GNamedType?, selectionSet: GSelectionSet): List<Conflict> {
		val conflicts = mutableListOf<Conflict>()
		val (fieldMap, fragmentSpreads) = getFieldsAndFragmentSpreads(data = data, parentType = parentType, selectionSet = selectionSet)

		collectConflictsWithin(data = data, conflicts = conflicts, fieldMap = fieldMap)

		for (index1 in fragmentSpreads.indices) {
			collectConflictsBetweenFieldsAndFragment(data, conflicts, false, fieldMap, fragmentSpreads[index1])

			for (index2 in index1 + 1 until fragmentSpreads.size) {
				collectConflictsBetweenFragments(data, conflicts, false, fragmentSpreads[index1], fragmentSpreads[index2])
			}
		}

		return conflicts
	}

	private fun findConflictsBetweenSubSelectionSets(
		data: ValidationContext,
		areMutuallyExclusive: Boolean,
		parentType1: GNamedType?,
		selectionSet1: GSelectionSet,
		parentType2: GNamedType?,
		selectionSet2: GSelectionSet,
	): List<Conflict> {
		val conflicts = mutableListOf<Conflict>()
		val (fieldMap1, fragmentSpreads1) = getFieldsAndFragmentSpreads(data = data, parentType = parentType1, selectionSet = selectionSet1)
		val (fieldMap2, fragmentSpreads2) = getFieldsAndFragmentSpreads(data = data, parentType = parentType2, selectionSet = selectionSet2)

		collectConflictsBetween(data, conflicts, areMutuallyExclusive, fieldMap1, fieldMap2)

		for (fragmentSpread2 in fragmentSpreads2) {
			collectConflictsBetweenFieldsAndFragment(data, conflicts, areMutuallyExclusive, fieldMap1, fragmentSpread2)
		}

		for (fragmentSpread1 in fragmentSpreads1) {
			collectConflictsBetweenFieldsAndFragment(data, conflicts, areMutuallyExclusive, fieldMap2, fragmentSpread1)
		}

		for (fragmentSpread1 in fragmentSpreads1) {
			for (fragmentSpread2 in fragmentSpreads2) {
				collectConflictsBetweenFragments(data, conflicts, areMutuallyExclusive, fragmentSpread1, fragmentSpread2)
			}
		}

		return conflicts
	}

	private fun collectConflictsBetweenFieldsAndFragment(
		data: ValidationContext,
		conflicts: MutableList<Conflict>,
		areMutuallyExclusive: Boolean,
		fieldMap: FieldMap,
		fragmentSpread: FragmentSpread,
	) {
		if (comparedFieldsAndFragmentPairs.has(fieldMap, fragmentSpread.key, areMutuallyExclusive)) {
			return
		}

		comparedFieldsAndFragmentPairs.add(fieldMap, fragmentSpread.key, areMutuallyExclusive)

		// Cannot validate a selection that refers to a nonexistent fragment. A fragment whose fields we already
		// hold contributes nothing new either.
		val referenced = data.document.fragment(fragmentSpread.node.name)
			?.let { getReferencedFieldsAndFragmentSpreads(data = data, fragment = it) }
			?.takeIf { it.fieldMap !== fieldMap }
			?: return

		collectConflictsBetween(data, conflicts, areMutuallyExclusive, fieldMap, referenced.fieldMap)

		for (referencedFragmentSpread in referenced.fragmentSpreads) {
			collectConflictsBetweenFieldsAndFragment(data, conflicts, areMutuallyExclusive, fieldMap, referencedFragmentSpread)
		}
	}

	private fun collectConflictsBetweenFragments(
		data: ValidationContext,
		conflicts: MutableList<Conflict>,
		areMutuallyExclusive: Boolean,
		fragmentSpread1: FragmentSpread,
		fragmentSpread2: FragmentSpread,
	) {
		if (fragmentSpread1.key == fragmentSpread2.key ||
			comparedFragmentPairs.has(fragmentSpread1.key, fragmentSpread2.key, areMutuallyExclusive)
		) {
			return
		}

		comparedFragmentPairs.add(fragmentSpread1.key, fragmentSpread2.key, areMutuallyExclusive)

		// Cannot validate a selection that refers to a nonexistent fragment.
		val fragment1 = data.document.fragment(fragmentSpread1.node.name)
		val fragment2 = data.document.fragment(fragmentSpread2.node.name)
		if (fragment1 === null || fragment2 === null) {
			return
		}

		val (fieldMap1, referencedFragmentSpreads1) = getReferencedFieldsAndFragmentSpreads(data = data, fragment = fragment1)
		val (fieldMap2, referencedFragmentSpreads2) = getReferencedFieldsAndFragmentSpreads(data = data, fragment = fragment2)

		collectConflictsBetween(data, conflicts, areMutuallyExclusive, fieldMap1, fieldMap2)

		for (referencedFragmentSpread2 in referencedFragmentSpreads2) {
			collectConflictsBetweenFragments(data, conflicts, areMutuallyExclusive, fragmentSpread1, referencedFragmentSpread2)
		}

		for (referencedFragmentSpread1 in referencedFragmentSpreads1) {
			collectConflictsBetweenFragments(data, conflicts, areMutuallyExclusive, referencedFragmentSpread1, fragmentSpread2)
		}
	}

	private fun collectConflictsWithin(data: ValidationContext, conflicts: MutableList<Conflict>, fieldMap: FieldMap) {
		for ((responseName, fields) in fieldMap.fieldsByResponseName) {
			for (index1 in fields.indices) {
				for (index2 in index1 + 1 until fields.size) {
					findConflict(
						data = data,
						parentFieldsAreMutuallyExclusive = false,
						responseName = responseName,
						field1 = fields[index1],
						field2 = fields[index2],
					)?.let { conflicts += it }
				}
			}
		}
	}

	private fun collectConflictsBetween(
		data: ValidationContext,
		conflicts: MutableList<Conflict>,
		parentFieldsAreMutuallyExclusive: Boolean,
		fieldMap1: FieldMap,
		fieldMap2: FieldMap,
	) {
		for ((responseName, fields1) in fieldMap1.fieldsByResponseName) {
			val fields2 = fieldMap2.fieldsByResponseName[responseName] ?: continue

			for (field1 in fields1) {
				for (field2 in fields2) {
					findConflict(
						data = data,
						parentFieldsAreMutuallyExclusive = parentFieldsAreMutuallyExclusive,
						responseName = responseName,
						field1 = field1,
						field2 = field2,
					)?.let { conflicts += it }
				}
			}
		}
	}

	private fun findConflict(
		data: ValidationContext,
		parentFieldsAreMutuallyExclusive: Boolean,
		responseName: String,
		field1: ResolvedField,
		field2: ResolvedField,
	): Conflict? {
		val node1 = field1.selection
		val node2 = field2.selection
		val type1 = field1.type
		val type2 = field2.type
		val selectionSet1 = node1.selectionSet
		val selectionSet2 = node2.selectionSet

		// Fields of two distinct object types are never part of the same response, so their names and arguments
		// need not match — but their types still have to be mergeable, as do their sub-selections.
		val areMutuallyExclusive = parentFieldsAreMutuallyExclusive ||
			(field1.parentType !== field2.parentType && field1.parentType is GObjectType && field2.parentType is GObjectType)

		return when {
			!areMutuallyExclusive && node1.name != node2.name -> Conflict(
				responseName = responseName,
				reason = SimpleConflictReason("\"${node1.name}\" and \"${node2.name}\" are different fields"),
				fields1 = listOf(node1),
				fields2 = listOf(node2),
			)

			!areMutuallyExclusive && !argumentValuesAreEqual(node1, node2) -> Conflict(
				responseName = responseName,
				reason = SimpleConflictReason("they have differing arguments"),
				fields1 = listOf(node1),
				fields2 = listOf(node2),
			)

			type1 !== null && type2 !== null && doTypesConflict(type1, type2) -> Conflict(
				responseName = responseName,
				reason = SimpleConflictReason("they return conflicting types \"${type1.name}\" and \"${type2.name}\""),
				fields1 = listOf(node1),
				fields2 = listOf(node2),
			)

			selectionSet1 !== null && selectionSet2 !== null -> subfieldConflicts(
				conflicts = findConflictsBetweenSubSelectionSets(
					data = data,
					areMutuallyExclusive = areMutuallyExclusive,
					parentType1 = type1?.underlyingNamedType,
					selectionSet1 = selectionSet1,
					parentType2 = type2?.underlyingNamedType,
					selectionSet2 = selectionSet2,
				),
				responseName = responseName,
				node1 = node1,
				node2 = node2,
			)

			else -> null
		}
	}

	private fun getFieldsAndFragmentSpreads(data: ValidationContext, parentType: GNamedType?, selectionSet: GSelectionSet): FieldsAndFragmentSpreads {
		cachedFieldsAndFragmentSpreads[selectionSet]?.let { return it }

		val fieldMap = FieldMap()
		val fragmentSpreads = mutableMapOf<String, FragmentSpread>()
		collectFieldsAndFragmentSpreads(
			data = data,
			parentType = parentType,
			selectionSet = selectionSet,
			fieldMap = fieldMap,
			fragmentSpreads = fragmentSpreads,
		)

		val result = FieldsAndFragmentSpreads(fieldMap = fieldMap, fragmentSpreads = fragmentSpreads.values.toList())
		cachedFieldsAndFragmentSpreads[selectionSet] = result

		return result
	}

	private fun getReferencedFieldsAndFragmentSpreads(data: ValidationContext, fragment: GFragmentDefinition): FieldsAndFragmentSpreads {
		cachedFieldsAndFragmentSpreads[fragment.selectionSet]?.let { return it }

		return getFieldsAndFragmentSpreads(
			data = data,
			parentType = data.schema.resolveType(fragment.typeCondition),
			selectionSet = fragment.selectionSet,
		)
	}

	// Fragment spreads are collected but deliberately not expanded here. That keeps collection non-recursive
	// across fragments, which is what makes fragment cycles harmless.
	private fun collectFieldsAndFragmentSpreads(
		data: ValidationContext,
		parentType: GNamedType?,
		selectionSet: GSelectionSet,
		fieldMap: FieldMap,
		fragmentSpreads: MutableMap<String, FragmentSpread>,
	) {
		for (selection in selectionSet.selections) {
			when (selection) {
				is GFieldSelection -> {
					// FIXME Will this work for introspection queries?
					val definition = (parentType as? GNode.WithFieldDefinitions)?.fieldDefinition(selection.name)

					fieldMap.fieldsByResponseName.getOrPut(selection.alias ?: selection.name) { mutableListOf() } += ResolvedField(
						parentType = parentType,
						selection = selection,
						type = definition?.type?.let { data.schema.resolveType(it) },
					)
				}

				// fluid's fragment spreads have no arguments, so the fragment name alone identifies a spread.
				is GFragmentSelection ->
					fragmentSpreads[selection.name] = FragmentSpread(key = selection.name, node = selection)

				is GInlineFragmentSelection -> collectFieldsAndFragmentSpreads(
					data = data,
					parentType = selection.typeCondition?.let { data.schema.resolveType(it) } ?: parentType,
					selectionSet = selection.selectionSet,
					fieldMap = fieldMap,
					fragmentSpreads = fragmentSpreads,
				)
			}
		}
	}

	companion object : Factory(::SelectionUnambiguityRule)
}

private fun subfieldConflicts(conflicts: List<Conflict>, responseName: String, node1: GFieldSelection, node2: GFieldSelection): Conflict? {
	if (conflicts.isEmpty()) {
		return null
	}

	return Conflict(
		responseName = responseName,
		reason = NestedConflictReason(conflicts.map { it.responseName to it.reason }),
		fields1 = listOf<GNode>(node1) + conflicts.flatMap { it.fields1 },
		fields2 = listOf<GNode>(node2) + conflicts.flatMap { it.fields2 },
	)
}

// FIXME default values
private fun argumentValuesAreEqual(selection1: GFieldSelection, selection2: GFieldSelection): Boolean {
	if (selection1.arguments.size != selection2.arguments.size) {
		return false
	}

	return selection1.arguments.all { argument1 ->
		val argument2 = selection2.argument(argument1.name)

		argument2 !== null && stringifyValue(argument1.value) == stringifyValue(argument2.value)
	}
}

/** Returns whether [type1] and [type2] can never describe the same value — note the inverted polarity. */
private fun doTypesConflict(type1: GType, type2: GType): Boolean = when {
	type1 is GListType -> if (type2 is GListType) doTypesConflict(type1.elementType, type2.elementType) else true
	type2 is GListType -> true
	type1 is GNonNullType -> if (type2 is GNonNullType) doTypesConflict(type1.nullableType, type2.nullableType) else true
	type2 is GNonNullType -> true
	type1 is GLeafType || type2 is GLeafType -> type1 !== type2
	else -> false
}

/**
 * Renders [value] in a form where two values that differ only in the order of input object fields
 * produce the same string, so that they compare as equal.
 */
private fun stringifyValue(value: GValue): String = sortValueNode(value).toString()

/** Returns [value] with the fields of all nested input object values sorted by name. */
private fun sortValueNode(value: GValue): GValue = when (value) {
	is GListValue -> GListValue(elements = value.elements.map(::sortValueNode), origin = value.origin)
	is GObjectValue -> GObjectValue(
		arguments = value.arguments
			.sortedBy { it.name }
			.map { GArgument(name = it.nameNode, value = sortValueNode(it.value), origin = it.origin) },
		origin = value.origin,
	)

	else -> value
}

/** One reported conflict: the fields that conflict, and why. */
private class Conflict(val responseName: String, val reason: ConflictReason, val fields1: List<GNode>, val fields2: List<GNode>)

private sealed interface ConflictReason {

	fun describe(): String
}

private class SimpleConflictReason(private val message: String) : ConflictReason {

	override fun describe(): String = message
}

private class NestedConflictReason(private val reasons: List<Pair<String, ConflictReason>>) : ConflictReason {

	override fun describe(): String = reasons.joinToString(separator = " and ") { (responseName, reason) ->
		"subfields \"$responseName\" conflict because ${reason.describe()}"
	}
}

/**
 * Fields of one selection set, grouped by response name.
 *
 * Deliberately does not override [equals]: memoization and the pair sets below rely on identity, matching
 * graphql-js which keys those on the `Map` instance.
 */
private class FieldMap {

	val fieldsByResponseName: MutableMap<String, MutableList<ResolvedField>> = mutableMapOf()
}

private data class FieldsAndFragmentSpreads(val fieldMap: FieldMap, val fragmentSpreads: List<FragmentSpread>)

private class FragmentSpread(val key: String, val node: GFragmentSelection)

private class ResolvedField(val parentType: GNamedType?, val selection: GFieldSelection, val type: GType?)

/**
 * Records ordered `(a, b)` pairs together with the exclusivity flag they were compared under.
 *
 * [has] reports a pair as present when it was compared under the same flag, or — when asked with
 * [weaklyPresent] — under any flag. Comparing a pair non-exclusively subsumes the exclusive comparison,
 * but not the other way around.
 */
private class OrderedPairSet<A : Any, B : Any> {

	private val data: MutableMap<A, MutableMap<B, Boolean>> = mutableMapOf()

	fun has(a: A, b: B, weaklyPresent: Boolean): Boolean {
		val storedValue = data[a]?.get(b) ?: return false

		return weaklyPresent || !storedValue
	}

	fun add(a: A, b: B, weaklyPresent: Boolean) {
		data.getOrPut(a) { mutableMapOf() }[b] = weaklyPresent
	}
}

/** [OrderedPairSet] that ignores the order of the two keys. */
private class PairSet {

	private val orderedPairSet = OrderedPairSet<String, String>()

	fun has(a: String, b: String, weaklyPresent: Boolean): Boolean =
		if (a < b) orderedPairSet.has(a, b, weaklyPresent) else orderedPairSet.has(b, a, weaklyPresent)

	fun add(a: String, b: String, weaklyPresent: Boolean) {
		if (a < b) orderedPairSet.add(a, b, weaklyPresent) else orderedPairSet.add(b, a, weaklyPresent)
	}
}
