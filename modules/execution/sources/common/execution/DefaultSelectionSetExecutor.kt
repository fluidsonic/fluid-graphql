package io.fluidsonic.graphql


internal object DefaultSelectionSetExecutor {

	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	private fun collectFieldSelections(
		selectionSet: GSelectionSet,
		parentType: GObjectType,
		context: DefaultExecutorContext,
		path: GPath,
		fieldSelectionsByResponseKey: MutableMap<String, MutableList<GFieldSelection>>,
		visitedFragments: MutableSet<String>
	): GResult<Map<String, List<GFieldSelection>>> {
		selectionSet.selections
			.filter { selection ->
				selection.isIncluded(fieldSelectionPath = path, context = context)
					.ifErrors { return GResult.failure(it) }
			}
			.forEach { selection ->
				collectFieldSelections(
					selection = selection,
					path = path,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragmentNames = visitedFragments,
					parentType = parentType,
					context = context
				).ifErrors { return GResult.failure(it) }
			}

		return GResult.success(fieldSelectionsByResponseKey)
	}


	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	private fun collectFieldSelections(
		selection: GSelection,
		parentType: GObjectType,
		context: DefaultExecutorContext,
		path: GPath,
		fieldSelectionsByResponseKey: MutableMap<String, MutableList<GFieldSelection>>,
		visitedFragmentNames: MutableSet<String>
	): GResult<Nothing?> {
		when (selection) {
			is GFieldSelection -> {
				fieldSelectionsByResponseKey.getOrPut(selection.alias ?: selection.name) { mutableListOf() } += selection

				return GResult.success()
			}

			is GFragmentSelection -> {
				val fragmentName = selection.name
				if (!visitedFragmentNames.add(fragmentName))
					return GResult.success()

				val fragment = context.document.fragment(fragmentName)
					?: error("A fragment with name '$fragmentName' is referenced but not defined.")

				val fragmentType = context.schema.resolveType(fragment.typeCondition)
					?: error("Cannot resolve type '${fragment.typeCondition}' in condition of fragment '$fragmentName'.")

				if (!doesFragmentTypeApply(fragmentType, to = parentType))
					return GResult.success()

				return collectFieldSelections(
					selectionSet = fragment.selectionSet,
					path = path,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragments = visitedFragmentNames,
					parentType = parentType,
					context = context
				).mapValue { null }
			}

			is GInlineFragmentSelection -> {
				val fragmentTypeCondition = selection.typeCondition
				if (fragmentTypeCondition !== null) {
					val fragmentType = context.schema.resolveType(fragmentTypeCondition)
						?: error("Cannot resolve type '${fragmentTypeCondition}' in condition of inline fragment.")

					if (!doesFragmentTypeApply(fragmentType, to = parentType))
						return GResult.success()
				}

				return collectFieldSelections(
					selectionSet = selection.selectionSet,
					path = path,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragments = visitedFragmentNames,
					parentType = parentType,
					context = context
				).mapValue { null }
			}
		}
	}


	// https://graphql.github.io/graphql-spec/June2018/#DoesFragmentTypeApply()
	private fun doesFragmentTypeApply(fragmentType: GType, to: GObjectType) =
		to.isSubtypeOf(fragmentType)


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteSelectionSet()
	suspend fun execute(
		selectionSet: GSelectionSet,
		parent: Any,
		parentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext
	): GResult<Map<String, Any?>> =
		collectFieldSelections(
			selectionSet = selectionSet,
			parentType = parentType,
			context = context,
			path = path,
			fieldSelectionsByResponseKey = mutableMapOf(),
			visitedFragments = mutableSetOf()
		).flatMapValue { fieldSelections ->
			fieldSelections
				.mapValues { (_, fieldSelections) ->
					context.fieldSelectionExecutor.execute(
						selections = fieldSelections,
						parent = parent,
						parentType = parentType,
						path = path.addName(fieldSelections.first().name),
						context = context
					)
				}
				.flatten()
		}


	private fun GNode.WithDirectives.getDirectiveValues(
		definition: GDirectiveDefinition,
		fieldSelectionPath: GPath,
		context: DefaultExecutorContext
	): GResult<Map<String, Any?>?> =
		directive(definition.name)
			?.let { directive ->
				context.nodeInputConverter.convertArguments(
					node = directive,
					definitions = definition.argumentDefinitions,
					fieldSelectionPath = fieldSelectionPath,
					context = context
				)
			}
			?: GResult.success()


	// FIXME improve type casting
	private fun GSelection.isIncluded(fieldSelectionPath: GPath, context: DefaultExecutorContext): GResult<Boolean> {
		val skip = getDirectiveValues(GLanguage.defaultSkipDirective, fieldSelectionPath = fieldSelectionPath, context = context)
			.ifErrors { return GResult.failure(it) }
			.let { it?.get("if") as Boolean? ?: false }
		if (skip)
			return GResult.success(false)

		val include = getDirectiveValues(GLanguage.defaultIncludeDirective, fieldSelectionPath = fieldSelectionPath, context = context)
			.ifErrors { return GResult.failure(it) }
			.let { it?.get("if") as Boolean? ?: false }
		if (!include)
			return GResult.success(true)

		return GResult.success(true)
	}
}
