package io.fluidsonic.graphql

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal object DefaultSelectionSetExecutor {

	// https://graphql.github.io/graphql-spec/June2018/#CollectFields()
	private fun collectFieldSelections(
		selectionSet: GSelectionSet,
		parentType: GObjectType,
		context: DefaultExecutorContext,
		path: GPath,
		fieldSelectionsByResponseKey: MutableMap<String, MutableList<GFieldSelection>>,
		visitedFragments: MutableSet<String>,
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
					context = context,
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
		visitedFragmentNames: MutableSet<String>,
	): GResult<Nothing?> {
		when (selection) {
			is GFieldSelection -> {
				fieldSelectionsByResponseKey.getOrPut(selection.alias ?: selection.name, ::mutableListOf)
					.add(selection)

				return GResult.success()
			}

			is GFragmentSelection -> {
				val fragmentName = selection.name
				if (!visitedFragmentNames.add(fragmentName)) {
					return GResult.success()
				}

				// A spread of an undefined fragment is skipped, mirroring graphql-js `collectFields`,
				// which continues when `exeContext.fragments[fragName]` is absent.
				val fragment = context.document.fragment(fragmentName)
					?: return GResult.success()

				// An unresolvable type condition never applies, so the fragment is skipped — exactly as for an
				// inline fragment below. graphql-js routes both through `doesFragmentConditionMatch`, which
				// returns false when `typeFromAST` yields no type, and draws no distinction between the two.
				val fragmentType = TypeResolver.resolveType(context.schema, fragment.typeCondition)
					?: return GResult.success()

				if (!doesFragmentTypeApply(fragmentType, to = parentType)) {
					return GResult.success()
				}

				return collectFieldSelections(
					selectionSet = fragment.selectionSet,
					path = path,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragments = visitedFragmentNames,
					parentType = parentType,
					context = context,
				).mapValue { null }
			}

			is GInlineFragmentSelection -> {
				val fragmentTypeCondition = selection.typeCondition
				if (fragmentTypeCondition !== null) {
					// An unresolvable type condition never applies, so the fragment is skipped. graphql-js
					// `doesFragmentConditionMatch` returns false when `typeFromAST` yields no type.
					val fragmentType = TypeResolver.resolveType(context.schema, fragmentTypeCondition)
						?: return GResult.success()

					if (!doesFragmentTypeApply(fragmentType, to = parentType)) {
						return GResult.success()
					}
				}

				return collectFieldSelections(
					selectionSet = selection.selectionSet,
					path = path,
					fieldSelectionsByResponseKey = fieldSelectionsByResponseKey,
					visitedFragments = visitedFragmentNames,
					parentType = parentType,
					context = context,
				).mapValue { null }
			}
		}
	}

	// https://graphql.github.io/graphql-spec/June2018/#DoesFragmentTypeApply()
	private fun doesFragmentTypeApply(fragmentType: GType, to: GObjectType) = to.isSubtypeOf(fragmentType)

	// https://graphql.github.io/graphql-spec/June2018/#ExecuteSelectionSet()
	suspend fun execute(
		selectionSet: GSelectionSet,
		parent: Any,
		parentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext,
	): GResult<Map<String, Any?>> = try {
		executeInParallel(selectionSet = selectionSet, parent = parent, parentType = parentType, path = path, context = context)
	} catch (exception: GErrorException) {
		GResult.failure(exception.errors)
	}

	private suspend fun executeInParallel(
		selectionSet: GSelectionSet,
		parent: Any,
		parentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext,
	): GResult<Map<String, Any?>> = collectFieldSelections(
		selectionSet = selectionSet,
		parentType = parentType,
		context = context,
		path = path,
		fieldSelectionsByResponseKey = mutableMapOf(),
		visitedFragments = mutableSetOf(),
	).flatMapValue { fieldSelections ->
		coroutineScope {
			fieldSelections
				.map { (key, selections) ->
					key to async {
						context.fieldSelectionExecutor.execute(
							selections = selections,
							parent = parent,
							parentType = parentType,
							path = path.addName(selections.first().name),
							context = context,
						)
					}
				}
				.map { (key, deferred) -> key to deferred.await() }
				.toMap()
				.filterValues { result -> !result.isAbsent() }
				.flatten()
		}
	}

	suspend fun executeSerially(
		selectionSet: GSelectionSet,
		parent: Any,
		parentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext,
	): GResult<Map<String, Any?>> = try {
		executeInSeries(selectionSet = selectionSet, parent = parent, parentType = parentType, path = path, context = context)
	} catch (exception: GErrorException) {
		GResult.failure(exception.errors)
	}

	private suspend fun executeInSeries(
		selectionSet: GSelectionSet,
		parent: Any,
		parentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext,
	): GResult<Map<String, Any?>> = collectFieldSelections(
		selectionSet = selectionSet,
		parentType = parentType,
		context = context,
		path = path,
		fieldSelectionsByResponseKey = mutableMapOf(),
		visitedFragments = mutableSetOf(),
	).flatMapValue { fieldSelections ->
		fieldSelections
			.mapValues { (_, fieldSelections) ->
				context.fieldSelectionExecutor.execute(
					selections = fieldSelections,
					parent = parent,
					parentType = parentType,
					path = path.addName(fieldSelections.first().name),
					context = context,
				)
			}
			.filterValues { result -> !result.isAbsent() }
			.flatten()
	}

	/** Whether the field was skipped entirely and so must not appear in the response at all. */
	private fun GResult<Any?>.isAbsent() = valueOrNull() === NoValue

	private fun GNode.WithDirectives.getDirectiveValues(
		definition: GDirectiveDefinition,
		fieldSelectionPath: GPath,
		context: DefaultExecutorContext,
	): GResult<Map<String, Any?>?> = directive(definition.name)
		?.let { directive ->
			context.nodeInputConverter.convertArguments(
				node = directive,
				definitions = definition.argumentDefinitions,
				fieldSelectionPath = fieldSelectionPath,
				context = context,
			)
		}
		?: GResult.success()

	// FIXME improve type casting
	private fun GSelection.isIncluded(fieldSelectionPath: GPath, context: DefaultExecutorContext): GResult<Boolean> {
		val skip = getDirectiveValues(GLanguage.defaultSkipDirective, fieldSelectionPath = fieldSelectionPath, context = context)
			.ifErrors { return GResult.failure(it) }
			.let { it?.get("if") as Boolean? ?: false }
		if (skip) {
			return GResult.success(false)
		}

		val include = getDirectiveValues(GLanguage.defaultIncludeDirective, fieldSelectionPath = fieldSelectionPath, context = context)
			.ifErrors { return GResult.failure(it) }
			.let { it?.get("if") as Boolean? ?: true }
		if (!include) {
			return GResult.success(false)
		}

		return GResult.success(true)
	}
}
