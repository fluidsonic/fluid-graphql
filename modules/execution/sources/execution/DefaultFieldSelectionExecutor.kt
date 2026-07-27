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
		context: DefaultExecutorContext,
	): GResult<Any?> = result
		.flatMapValue { value ->
			when (value) {
				null -> when (type) {
					// A field error, not a crash: the enclosing `flatMapErrors` propagates the null upwards
					// until it reaches a nullable position. https://spec.graphql.org/draft/#sec-Handling-Field-Errors
					is GNonNullType -> GResult.failure(
						GError(
							message = "Field '${parentType.name}.${fieldDefinition.name}' of type '${fieldDefinition.type}' resolved to null.",
							path = path,
							nodes = listOfNotNull(selections.firstOrNull()?.nameNode),
						),
					)

					else -> GResult.success()
				}

				else -> when (type) {
					is GCompositeType -> {
						val childType = when (type) {
							is GAbstractType -> resolveAbstractType(
								abstractType = type,
								objectValue = value,
								context = context,
							)

							is GObjectType -> type
						}

						context.selectionSetExecutor.execute(
							selectionSet = mergeSelectionSets(selections),
							parent = value,
							parentType = childType,
							path = path,
							context = context,
						).flatMapValue { childValue ->
							convertOutput(
								value = childValue,
								type = childType,
								parentType = parentType,
								path = path,
								fieldDefinition = fieldDefinition,
								context = context,
							)
						}
					}

					// A broken schema, not something a client can provoke.
					is GInputObjectType ->
						error("Field '${parentType.name}.${fieldDefinition.name}' must have an output type but has input type '${type.name}'.")

					is GLeafType ->
						convertOutput(
							value = value,
							type = type,
							parentType = parentType,
							path = path,
							fieldDefinition = fieldDefinition,
							context = context,
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
									context = context,
								)
							}.flatten()

							else -> GResult.success(value)
						}

					is GNonNullType -> {
						val completed = complete(
							selections = selections,
							result = GResult.success(value),
							type = type.nullableType,
							parentType = parentType,
							fieldDefinition = fieldDefinition,
							path = path,
							context = context,
						)

						// The unwrapped frame answers a field error of its own — a failed output coercion, or a
						// non-null child that nullified itself — by becoming null, because the type it was given
						// is nullable. This position is not, so that null cannot stand here and the errors have
						// to keep propagating outwards instead of stopping one level too early.
						// https://spec.graphql.org/draft/#sec-Handling-Field-Errors
						when {
							completed.valueOrNull() === null && completed.errors.isNotEmpty() -> GResult.failure(completed.errors)
							else -> completed
						}
					}
				}
			}
		}
		.flatMapErrors { errors ->
			when (type) {
				is GNonNullType -> GResult.failure(errors)
				else -> GResult.success(value = null, errors = errors)
			}
		}

	private fun convertOutput(
		value: Any,
		type: GType,
		parentType: GObjectType,
		path: GPath,
		fieldDefinition: GFieldDefinition,
		context: DefaultExecutorContext,
	): GResult<Any> = context.outputConverter.convertOutput(
		value = value,
		type = type,
		fieldDefinition = fieldDefinition,
		parentType = parentType,
		path = path,
		context = context,
	)

	// https://graphql.github.io/graphql-spec/June2018/#ExecuteField()
	suspend fun execute(selections: List<GFieldSelection>, parent: Any, parentType: GObjectType, path: GPath, context: DefaultExecutorContext): GResult<Any?> =
		try {
			executeField(selections = selections, parent = parent, parentType = parentType, path = path, context = context)
		} catch (exception: GErrorException) {
			// Errors raised for conditions that cannot occur in a validated document must still honour the
			// GResult contract rather than escaping as a raw exception.
			GResult.failure(exception.errors)
		}

	private suspend fun executeField(
		selections: List<GFieldSelection>,
		parent: Any,
		parentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext,
	): GResult<Any?> {
		// An error can occur only if this function was called directly with an empty selection list.
		require(selections.isNotEmpty()) { "'selections' must contain at least one selection." }

		val firstSelection = selections.first()
		if (GLanguage.isValidIntrospectionName(firstSelection.name)) {
			return executeIntrospection(
				selections = selections,
				originalParentType = parentType,
				path = path,
				context = context,
			)
		}

		// A field that the type does not define is skipped entirely, leaving its response key absent
		// rather than present-and-null. This mirrors graphql-js, which returns from `executeField`
		// when `schema.getField` finds no definition.
		val fieldDefinition = parentType.fieldDefinition(firstSelection.name)

		return when (fieldDefinition) {
			null -> GResult.success(NoValue)
			else -> {
				// An error can occur only if the schema wasn't validated.
				val fieldType = context.schema.resolveType(fieldDefinition.type)
					?: error("Cannot resolve type '${fieldDefinition.type}' of field '${fieldDefinition.name}' in '${parentType.name}'.")

				complete(
					selections = selections,
					result = resolveFieldValue(
						parent = parent,
						parentType = parentType,
						fieldDefinition = fieldDefinition,
						selections = selections,
						path = path,
						context = context,
					),
					type = fieldType,
					parentType = parentType,
					fieldDefinition = fieldDefinition,
					path = path,
					context = context,
				)
			}
		}
	}

	private suspend fun executeIntrospection(
		selections: List<GFieldSelection>,
		originalParentType: GObjectType,
		path: GPath,
		context: DefaultExecutorContext,
	): GResult<Any?> {
		require(selections.isNotEmpty()) { "'selections' must contain at least one selection." }

		val firstSelection = selections.first()

		val parent: Any
		val parentType: GObjectType

		val fieldDefinition = when (firstSelection.name) {
			Introspection.schemaField.name -> {
				parent = context.schema
				parentType = Introspection.schemaType(context.schema)

				Introspection.schemaField.takeIf { originalParentType == context.schema.queryType }
			}

			Introspection.typeField.name -> {
				parent = context.schema
				parentType = Introspection.schemaType(context.schema)

				Introspection.typeField.takeIf { originalParentType == context.schema.queryType }
			}

			Introspection.typenameField.name -> {
				parent = originalParentType
				parentType = Introspection.typeType(context.schema)

				Introspection.typenameField
			}

			else -> {
				parent = context.schema
				parentType = originalParentType

				null
			}
		} ?: return GResult.success(NoValue) // An unknown introspection field is skipped, same as any other unknown field.

		// No schema is swapped in here: the introspection types are members of `context.schema` itself.
		val fieldType = context.schema.resolveType(fieldDefinition.type)
			?: error("Cannot resolve type '${fieldDefinition.type}' of field '${fieldDefinition.name}' in '${originalParentType.name}'.")

		return complete(
			selections = selections,
			result = resolveFieldValue(
				parent = parent,
				parentType = parentType,
				fieldDefinition = fieldDefinition,
				selections = selections,
				path = path,
				context = context,
			),
			type = fieldType,
			parentType = parentType,
			fieldDefinition = fieldDefinition,
			path = path,
			context = context,
		)
	}

	// https://graphql.github.io/graphql-spec/draft/#MergeSelectionSets()
	private fun mergeSelectionSets(fieldSelections: List<GFieldSelection>) =
		GSelectionSet(selections = fieldSelections.flatMap { it.selectionSet?.selections.orEmpty() })

	// https://graphql.github.io/graphql-spec/June2018/#ResolveAbstractType()
	private fun resolveAbstractType(abstractType: GAbstractType, objectValue: Any, context: DefaultExecutorContext) = // FIXME support default resolver
		context.schema.getPossibleTypes(abstractType)
			.firstOrNull { it.kotlinType?.isInstance(objectValue) ?: false } // FIXME
			?: GError(
				message = "Cannot resolve abstract type '${abstractType.name}' for Kotlin type " +
					"'${objectValue::class.qualifiedName}': $objectValue",
			).throwException()

	// https://graphql.github.io/graphql-spec/June2018/#ResolveFieldValue()
	private suspend fun resolveFieldValue(
		parent: Any,
		parentType: GObjectType,
		fieldDefinition: GFieldDefinition,
		selections: List<GFieldSelection>,
		path: GPath,
		context: DefaultExecutorContext,
	): GResult<Any?> = context.nodeInputConverter.convertArguments(
		node = selections.first(),
		definitions = fieldDefinition.argumentDefinitions,
		fieldSelectionPath = path,
		context = context,
	).flatMapValue { argumentValues ->
		val resolverContext = DefaultFieldResolverContext(
			arguments = argumentValues,
			execution = context,
			fieldDefinition = fieldDefinition,
			parent = parent,
			parentType = parentType,
			path = path,
		)

		GResult.catchErrors {
			when (val resolver = context.fieldResolver) {
				null -> resolverContext.next()
				else ->
					context.withExceptionHandler(origin = { GExceptionOrigin.FieldResolver(resolver = resolver, context = resolverContext) }) {
						with(resolver) {
							resolverContext.resolveField(parent = parent)
						}
					}
			}
		}
	}
}
