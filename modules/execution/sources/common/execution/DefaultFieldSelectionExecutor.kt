package io.fluidsonic.graphql


internal object DefaultFieldSelectionExecutor {

	// https://graphql.github.io/graphql-spec/June2018/#CompleteValue()
	private suspend fun complete(
		selections: List<GFieldSelection>,
		result: GResult<Any?>,
		type: GType,
		parentType: GObjectType,
		fieldDefinition: GFieldDefinition,
		path: GPath,
		context: DefaultExecutorContext
	): GResult<Any?> = result
		.flatMapValue { value ->
			when (value) {
				null -> when (type) {
					is GNonNullType -> error("Field '${parentType.name}.${fieldDefinition.name}' of type '${fieldDefinition.type}' resolved to null.")
					else -> GResult.success()
				}
				else -> when (type) {
					is GCompositeType -> {
						val childType = when (type) {
							is GAbstractType -> resolveAbstractType(
								abstractType = type,
								objectValue = value,
								context = context
							)

							is GInputObjectType ->
								error("Field '${parentType.name}.${fieldDefinition.name}' must have an output type but has input type '${type.name}'.")

							is GObjectType -> type
						}

						context.selectionSetExecutor.execute(
							selectionSet = mergeSelectionSets(selections),
							parent = value,
							parentType = childType,
							path = path,
							context = context
						).flatMapValue { childValue ->
							context.outputCoercer.coerceObjectValue(
								value = childValue,
								type = childType,
								parentType = parentType,
								field = fieldDefinition,
								context = context
							)
						}
					}

					is GLeafType ->
						context.outputCoercer.coerceLeafValue(
							value = value,
							type = type,
							parentType = parentType,
							field = fieldDefinition,
							context = context
						)

					is GListType ->
						when (value) {
							is Collection<*> -> value.mapIndexed { index, element ->
								complete(
									selections = selections,
									result = GResult.success(element),
									type = type.elementType,
									parentType = parentType,
									fieldDefinition = fieldDefinition,
									path = path.addIndex(index),
									context = context
								)
							}.flatten()

							else -> GResult.success(value)
						}

					is GNonNullType ->
						complete(
							selections = selections,
							result = GResult.success(value),
							type = type.nullableType,
							parentType = parentType,
							fieldDefinition = fieldDefinition,
							path = path,
							context = context
						)
				}
			}
		}
		.flatMapErrors { errors ->
			when (type) {
				is GNonNullType -> GResult.failure(errors)
				else -> GResult.success(value = null, errors = errors)
			}
		}


	// https://graphql.github.io/graphql-spec/June2018/#ExecuteField()
	suspend fun execute(
		selections: List<GFieldSelection>,
		parent: Any,
		parentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext
	): GResult<Any?> {
		require(selections.isNotEmpty()) { "'selections' must contain at least one selection." }

		val firstSelection = selections.first()
		if (GLanguage.isValidIntrospectionName(firstSelection.name))
			return executeIntrospection(
				selections = selections,
				originalParentType = parentType,
				path = path,
				context = context
			)

		val fieldDefinition = parentType.field(firstSelection.name)
			?: error("There is no field named '${firstSelection.name}' on type '${parentType.name}'.")
		val fieldType = context.schema.resolveType(fieldDefinition.type)
			?: error("Cannot resolve type '${fieldDefinition.type}' of field '${fieldDefinition.name}' in '${parentType.name}'.")

		return complete(
			selections = selections,
			result = resolve(
				parent = parent,
				parentType = parentType,
				fieldDefinition = fieldDefinition,
				selections = selections,
				path = path,
				context = context
			),
			type = fieldType,
			parentType = parentType,
			fieldDefinition = fieldDefinition,
			path = path,
			context = context
		)
	}


	private suspend fun executeIntrospection(
		selections: List<GFieldSelection>,
		originalParentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext
	): GResult<Any?> {
		require(selections.isNotEmpty()) { "'selections' must contain at least one selection." }

		val firstSelection = selections.first()

		val parent: Any
		val parentType: GObjectType

		val fieldDefinition = when (firstSelection.name) {
			Introspection.schemaField.name -> {
				parent = context.schema
				parentType = Introspection.schemaType

				Introspection.schemaField.takeIf { originalParentType == context.schema.queryType }
			}

			Introspection.typeField.name -> {
				parent = context.schema
				parentType = Introspection.schemaType

				Introspection.typeField.takeIf { originalParentType == context.schema.queryType }
			}

			Introspection.typenameField.name -> {
				parent = originalParentType
				parentType = Introspection.typeType

				Introspection.typenameField
			}

			else -> {
				parent = context.schema
				parentType = originalParentType

				null
			}
		} ?: error("There is no field named '${firstSelection.name}' on type '${originalParentType.name}'.")

		val fieldContext = context.copy(
			schema = Introspection.schema,
			root = context.schema,
			rootType = Introspection.schemaType
		)
		val fieldType = fieldContext.schema.resolveType(fieldDefinition.type)
			?: error("Cannot resolve type '${fieldDefinition.type}' of field '${fieldDefinition.name}' in '${originalParentType.name}'.")

		return complete(
			selections = selections,
			result = resolve(
				parent = parent,
				parentType = parentType,
				fieldDefinition = fieldDefinition,
				selections = selections,
				path = path,
				context = fieldContext
			),
			type = fieldType,
			parentType = parentType,
			fieldDefinition = fieldDefinition,
			path = path,
			context = fieldContext
		)
	}


	// https://graphql.github.io/graphql-spec/draft/#MergeSelectionSets()
	private fun mergeSelectionSets(fieldSelections: List<GFieldSelection>) =
		GSelectionSet(selections = fieldSelections.flatMap { it.selectionSet?.selections.orEmpty() })


	// https://graphql.github.io/graphql-spec/June2018/#ResolveFieldValue()
	private suspend fun resolve(
		parent: Any,
		parentType: GObjectType,
		fieldDefinition: GFieldDefinition,
		selections: List<GFieldSelection>,
		path: GPath,
		context: DefaultExecutorContext
	): GResult<Any?> =
		context.nodeInputCoercer.coerceArguments(
			node = selections.first(),
			definitions = fieldDefinition.argumentDefinitions,
			fieldSelectionPath = path,
			context = context
		).flatMapValue { argumentValues ->
			// FIXME how to improve type safety? can we use .kotlinType if available?
			val resolver = fieldDefinition.resolver as GFieldResolver<Any>?
				?: context.defaultFieldResolver
				?: error("No resolver registered for field '${parentType.name}.${fieldDefinition.name}' and no default resolver was specified.")

			GResult.catchErrors {
				resolver.resolveField(
					parent = parent,
					context = DefaultFieldResolverContext(
						arguments = argumentValues,
						parentType = parentType,
						field = fieldDefinition,
						executorContext = context
					)
				)
			}
			// FIXME add support for custom exception handler and don't handle by default
		}


	// https://graphql.github.io/graphql-spec/June2018/#ResolveAbstractType()
	private fun resolveAbstractType(
		abstractType: GAbstractType,
		objectValue: Any,
		context: DefaultExecutorContext
	) =
		// FIXME support default resolver
		context.schema.getPossibleTypes(abstractType)
			.firstOrNull { it.kotlinType?.isInstance(objectValue) ?: false } // FIXME
			?: error("Cannot resolve abstract type '${abstractType.name}' for Kotlin type '${objectValue::class.qualifiedOrSimpleName}': $objectValue")
}
