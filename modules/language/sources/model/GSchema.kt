package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
public class GSchema internal constructor(
	public val directiveDefinitions: List<GDirectiveDefinition>,
	public val document: GDocument,
	queryType: GNamedTypeRef? = null,
	mutationType: GNamedTypeRef? = null,
	subscriptionType: GNamedTypeRef? = null,
	types: List<GNamedType>,
) {

	public val types: List<GNamedType> = types + GType.defaultTypes

	private val typesByName: Map<String, GNamedType> = this.types.associateByTo(hashMapOf()) { it.name }

	public val queryType: GObjectType? = queryType?.let { typesByName[it.name] as? GObjectType }
	public val mutationType: GObjectType? = mutationType?.let { typesByName[it.name] as? GObjectType }
	public val subscriptionType: GObjectType? = subscriptionType?.let { typesByName[it.name] as? GObjectType }

	private val possibleTypesByType: Map<String, List<GObjectType>> =
		types
			.filterIsInstance<GObjectType>()
			.filter { it.interfaces.isNotEmpty() }
			.flatMap { type -> type.interfaces.map { it.name to type } }
			.groupBy(keySelector = { it.first }, valueTransform = { it.second }) +
			types
				.filterIsInstance<GUnionType>()
				.associate { union -> union.name to union.possibleTypes.mapNotNull { typesByName[it.name] as? GObjectType } }


	public fun directiveDefinition(name: String): GDirectiveDefinition? =
		directiveDefinitions.firstOrNull { it.name == name }


	public fun getPossibleTypes(type: GCompositeType): List<GObjectType> =
		if (type is GObjectType) listOf(type)
		else possibleTypesByType[type.name].orEmpty()


	public fun resolveType(ref: GTypeRef): GType? =
		when (ref) {
			is GListTypeRef -> resolveType(ref.elementType)?.let { GListType(elementType = it) }
			is GNamedTypeRef -> resolveType(ref)
			is GNonNullTypeRef -> resolveType(ref.nullableRef)?.let { GNonNullType(nullableType = it) }
		}


	public fun resolveType(ref: GNamedTypeRef): GNamedType? =
		resolveType(ref.name)


	public fun resolveType(name: String): GNamedType? =
		typesByName[name]


	public fun rootTypeForOperationType(operationType: GOperationType): GObjectType? =
		when (operationType) {
			GOperationType.mutation -> mutationType
			GOperationType.query -> queryType
			GOperationType.subscription -> subscriptionType
		}


	override fun toString(): String =
		GDocument(
			definitions = document.definitions.filterIsInstance<GTypeSystemDefinition>()
		).toString()


	public fun validateValue(value: GValue, type: GType): List<GError> =
		validateValue(value = value, typeRef = null, type = type).orEmpty()


	public fun validateValue(value: GValue, typeRef: GTypeRef): List<GError> =
		validateValue(value = value, typeRef = typeRef, type = null).orEmpty()


	// FIXME not here
	@Suppress("NAME_SHADOWING")
	private fun validateValue(
		value: GValue,
		typeRef: GTypeRef?,
		type: GType?,
		fullyWrappedTypeRef: GTypeRef? = typeRef,
		errors: MutableList<GError>? = null,
	): List<GError>? {
		val type = type
			?: typeRef?.let { resolveType(it) } // FIXME resolve in execution context
			?: return null // We don't check types - only values.

		var errors = errors

		// no inline: https://youtrack.jetbrains.com/issue/KT-31371
		/* inline */ fun reportError(message: String? = null, nodeInsteadOfTypeRef: GNode? = null) {
			val message = message ?: run {
				val valueText = when (value) {
					is GListValue -> "a list value"
					is GObjectValue -> "an input object value"
					else -> "value '$value'"
				}

				"Type '${fullyWrappedTypeRef?.underlyingName ?: type.name}' does not allow $valueText."
			}

			(errors ?: mutableListOf<GError>().also { errors = it }).add(
				GError(
					message = message,
					nodes = listOfNotNull(value, nodeInsteadOfTypeRef ?: fullyWrappedTypeRef)
				)
			)
		}

		// We don't support variables here yet.
		if (value is GVariableRef)
			return null

		if (type is GNonNullType && value is GNullValue) {
			reportError()

			return errors
		}

		val isValidValue = when (val namedType = type.nullableType) {
			is GBooleanType ->
				when (value) {
					is GBooleanValue,
					is GNullValue,
					-> true

					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GObjectValue,
					is GStringValue,
					is GVariableRef,
					-> false
				}

			is GCustomScalarType ->
				// FIXME support conversion function
				when (value) {
					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GNullValue,
					is GObjectValue,
					is GStringValue,
					-> true

					is GVariableRef,
					-> false
				}

			is GEnumType ->
				when (value) {
					is GEnumValue ->
						namedType.value(value.name) !== null

					is GNullValue ->
						true

					is GBooleanValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GObjectValue,
					is GStringValue,
					is GVariableRef,
					-> false
				}

			is GFloatType ->
				when (value) {
					is GFloatValue,
					is GIntValue,
					is GNullValue,
					-> true

					is GBooleanValue,
					is GEnumValue,
					is GListValue,
					is GObjectValue,
					is GStringValue,
					is GVariableRef,
					-> false
				}

			is GIdType ->
				when (value) {
					is GIntValue,
					is GNullValue,
					is GStringValue,
					-> true

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GListValue,
					is GObjectValue,
					is GVariableRef,
					-> false
				}

			is GInputObjectType ->
				when (value) {
					is GObjectValue -> {
						for (argumentDefinition in namedType.argumentDefinitions)
							if (argumentDefinition.isRequired())
								if (value.argument(argumentDefinition.name) === null)
									reportError(
										message = "Required field '${argumentDefinition.name}' of type '${namedType.name}' is missing.",
										nodeInsteadOfTypeRef = argumentDefinition.nameNode
									)

						for (field in value.arguments) {
							val argumentDefinition = namedType.argumentDefinition(field.name)
							if (argumentDefinition !== null) {
								validateValue(
									value = field.value,
									typeRef = argumentDefinition.type,
									type = null,
									errors = errors ?: mutableListOf<GError>().also { errors = it }
								)
							}
							else
								reportError()
						}

						true
					}

					is GNullValue ->
						true

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GStringValue,
					is GVariableRef,
					-> false
				}

			is GIntType ->
				when (value) {
					is GIntValue,
					is GNullValue,
					-> true

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GListValue,
					is GObjectValue,
					is GStringValue,
					is GVariableRef,
					-> false
				}

			is GListType ->
				when (value) {
					is GListValue -> {
						for (element in value.elements)
							validateValue(
								value = element,
								typeRef = (typeRef?.nullableRef as GListTypeRef?)?.elementType,
								type = namedType.elementType,
								fullyWrappedTypeRef = fullyWrappedTypeRef,
								errors = errors ?: mutableListOf<GError>().also { errors = it }
							)

						true
					}

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GObjectValue,
					is GStringValue,
					-> {
						validateValue(
							value = value,
							typeRef = (typeRef?.nullableRef as GListTypeRef?)?.elementType,
							type = namedType.elementType,
							fullyWrappedTypeRef = fullyWrappedTypeRef,
							errors = errors ?: mutableListOf<GError>().also { errors = it }
						)

						true
					}

					is GNullValue ->
						true

					is GVariableRef ->
						false
				}

			is GNonNullType ->
				error("Impossible.")

			is GStringType ->
				when (value) {
					is GNullValue,
					is GStringValue,
					-> true

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GObjectValue,
					is GVariableRef,
					-> false
				}

			is GInterfaceType,
			is GObjectType,
			is GUnionType,
			->
				true // We don't check types - only values.
		}

		if (!isValidValue)
			reportError()

		return errors
	}


	public companion object {

		public fun parse(source: GDocumentSource.Parsable): GResult<GSchema> =
			GDocument.parse(source).mapValue(::GSchema)


		public fun parse(content: String, name: String = "<document>"): GResult<GSchema> =
			parse(GDocumentSource.of(content = content, name = name))
	}
}


public fun GSchema(
	document: GDocument,
	supportOptional: Boolean = false,
): GSchema {
	val typeSystemDefinitions = document.definitions.filterIsInstance<GTypeSystemDefinition>()

	val directiveDefinitions = typeSystemDefinitions.filterIsInstance<GDirectiveDefinition>().toMutableList()
	if (directiveDefinitions.none { it.name == "deprecated" })
		directiveDefinitions += GLanguage.defaultDeprecatedDirective
	if (directiveDefinitions.none { it.name == "include" })
		directiveDefinitions += GLanguage.defaultIncludeDirective
	if (directiveDefinitions.none { it.name == "skip" })
		directiveDefinitions += GLanguage.defaultSkipDirective

	if (supportOptional && directiveDefinitions.none { it.name == "optional" })
		directiveDefinitions += GLanguage.defaultOptionalDirective

	val schemaDefinition = typeSystemDefinitions.filterIsInstance<GSchemaDefinition>()
		.singleOrNull() // FIXME
	val typeDefinitions = typeSystemDefinitions.filterIsInstance<GNamedType>()

	val mutationTypeRef: GNamedTypeRef?
	val queryTypeRef: GNamedTypeRef?
	val subscriptionTypeRef: GNamedTypeRef?

	if (schemaDefinition !== null) {
		mutationTypeRef = schemaDefinition.operationTypeDefinition(GOperationType.mutation)?.type
		queryTypeRef = schemaDefinition.operationTypeDefinition(GOperationType.query)?.type
		subscriptionTypeRef = schemaDefinition.operationTypeDefinition(GOperationType.subscription)?.type
	}
	else {
		mutationTypeRef = GTypeRef(GLanguage.defaultMutationTypeName)
		queryTypeRef = GTypeRef(GLanguage.defaultQueryTypeName)
		subscriptionTypeRef = GTypeRef(GLanguage.defaultSubscriptionTypeName)
	}

	return GSchema(
		directiveDefinitions = directiveDefinitions,
		document = document,
		mutationType = mutationTypeRef,
		queryType = queryTypeRef,
		subscriptionType = subscriptionTypeRef,
		types = typeDefinitions
	)
}
