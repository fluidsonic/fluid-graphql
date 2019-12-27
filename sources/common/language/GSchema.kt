package io.fluidsonic.graphql


// FIXME toString()
// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
class GSchema internal constructor(
	val directiveDefinitions: List<GDirectiveDefinition>,
	val document: GDocument,
	queryType: GNamedTypeRef? = null,
	mutationType: GNamedTypeRef? = null,
	subscriptionType: GNamedTypeRef? = null,
	types: List<GNamedType>
) {

	val types = types + GType.defaultTypes

	private val typesByName = this.types.associateBy { it.name }

	val queryType = queryType?.let { typesByName[it.name] as? GObjectType }
	val mutationType = mutationType?.let { typesByName[it.name] as? GObjectType }
	val subscriptionType = subscriptionType?.let { typesByName[it.name] as? GObjectType }

	private val possibleTypesByType: Map<String, List<GObjectType>> =
		types
			.filterIsInstance<GObjectType>()
			.filter { it.interfaces.isNotEmpty() }
			.flatMap { type -> type.interfaces.map { it.name to type } }
			.groupBy(keySelector = { it.first }, valueTransform = { it.second }) +
			types
				.filterIsInstance<GUnionType>()
				.associate { union -> union.name to union.possibleTypes.mapNotNull { typesByName[it.name] as? GObjectType } }


	fun directiveDefinition(name: String) =
		directiveDefinitions.firstOrNull { it.name == name }


	fun getFieldDefinition(
		type: GType,
		name: String
	) =
		when (name) {
			"__schema" ->
				if (type === queryType)
					GIntrospection.schemaField
				else
					null

			"__type" ->
				if (type === queryType)
					GIntrospection.typeField
				else
					null

			"__typename" ->
				GIntrospection.typenameField

			else ->
				(type as? GAst.WithFieldDefinitions)?.field(name)
		}


	fun getPossibleTypes(type: GCompositeType) =
		if (type is GObjectType) listOf(type)
		else possibleTypesByType[type.name].orEmpty()


	fun resolveType(ref: GTypeRef): GType? =
		when (ref) {
			is GListTypeRef -> resolveType(ref.elementType)?.let(::GListType)
			is GNamedTypeRef -> resolveType(ref)
			is GNonNullTypeRef -> resolveType(ref.nullableType)?.let(::GNonNullType)
		}


	fun resolveType(ref: GNamedTypeRef) =
		resolveType(ref.name)


	fun resolveType(name: String): GNamedType? =
		typesByName[name] ?: GIntrospection.schema.typesByName[name]


	inline fun <reified Type : GType> resolveTypeAs(ref: GTypeRef) =
		resolveType(ref) as? Type


	inline fun <reified Type : GNamedType> resolveTypeAs(ref: GNamedTypeRef) =
		resolveType(ref) as? Type


	inline fun <reified Type : GNamedType> resolveTypeAs(name: String) =
		resolveType(name) as? Type


	fun rootTypeForOperationType(operationType: GOperationType) =
		when (operationType) {
			GOperationType.mutation -> mutationType
			GOperationType.query -> queryType
			GOperationType.subscription -> subscriptionType
		}


	override fun toString() =
		GDocument(
			definitions = document.definitions.filterIsInstance<GTypeSystemDefinition>()
		).toString()


	fun validateValue(value: GValue, type: GType) =
		validateValue(value = value, typeRef = null, type = type).orEmpty()


	fun validateValue(value: GValue, typeRef: GTypeRef) =
		validateValue(value = value, typeRef = typeRef, type = null).orEmpty()


	@Suppress("NAME_SHADOWING")
	private fun validateValue(
		value: GValue,
		typeRef: GTypeRef?,
		type: GType?,
		fullyWrappedTypeRef: GTypeRef? = typeRef,
		errors: MutableList<GError>? = null
	): List<GError>? {
		val type = type
			?: typeRef?.let { resolveType(it) }
			?: return null // We don't check types - only values.

		var errors = errors

		// no inline: https://youtrack.jetbrains.com/issue/KT-31371
		/* inline */ fun reportError(message: String? = null, nodeInsteadOfTypeRef: GAst? = null) {
			val message = message ?: run {
				val valueText = when (value) {
					is GValue.List -> "a list value"
					is GValue.Object -> "an input object value"
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
		if (value is GValue.Variable)
			return null

		if (type is GNonNullType && value is GValue.Null) {
			reportError()

			return errors
		}

		val isValidValue = when (val namedType = type.nullableType) {
			is GBooleanType ->
				when (value) {
					is GValue.Boolean,
					is GValue.Null ->
						true

					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.List,
					is GValue.Object,
					is GValue.String,
					is GValue.Variable ->
						false
				}

			is GCustomScalarType ->
				// FIXME support conversion function
				when (value) {
					is GValue.Boolean,
					is GValue.Float,
					is GValue.Int,
					is GValue.Null,
					is GValue.String ->
						true

					is GValue.Enum,
					is GValue.List,
					is GValue.Object,
					is GValue.Variable ->
						false
				}

			is GEnumType ->
				when (value) {
					is GValue.Enum ->
						namedType.value(value.name) !== null

					is GValue.Null ->
						true

					is GValue.Boolean,
					is GValue.Float,
					is GValue.Int,
					is GValue.List,
					is GValue.Object,
					is GValue.String,
					is GValue.Variable ->
						false
				}

			is GFloatType ->
				when (value) {
					is GValue.Float,
					is GValue.Int,
					is GValue.Null ->
						true

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.List,
					is GValue.Object,
					is GValue.String,
					is GValue.Variable ->
						false
				}

			is GIDType ->
				when (value) {
					is GValue.Int,
					is GValue.Null,
					is GValue.String ->
						true

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.List,
					is GValue.Object,
					is GValue.Variable ->
						false
				}

			is GInputObjectType ->
				when (value) {
					is GValue.Object -> {
						for (argumentDefinition in namedType.argumentDefinitions)
							if (argumentDefinition.isRequired())
								if (value.field(argumentDefinition.name) === null)
									reportError(
										message = "Required field '${argumentDefinition.name}' of type '${namedType.name}' is missing.",
										nodeInsteadOfTypeRef = argumentDefinition.nameNode
									)

						for (field in value.fields) {
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

					is GValue.Null ->
						true

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.List,
					is GValue.String,
					is GValue.Variable ->
						false
				}

			is GIntType ->
				when (value) {
					is GValue.Int,
					is GValue.Null ->
						true

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.List,
					is GValue.Object,
					is GValue.String,
					is GValue.Variable ->
						false
				}

			is GListType ->
				when (value) {
					is GValue.List -> {
						for (element in value.elements)
							validateValue(
								value = element,
								typeRef = (typeRef?.nullable as GListTypeRef?)?.elementType,
								type = namedType.elementType,
								fullyWrappedTypeRef = fullyWrappedTypeRef,
								errors = errors ?: mutableListOf<GError>().also { errors = it }
							)

						true
					}

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.Object,
					is GValue.String -> {
						validateValue(
							value = value,
							typeRef = (typeRef?.nullable as GListTypeRef?)?.elementType,
							type = namedType.elementType,
							fullyWrappedTypeRef = fullyWrappedTypeRef,
							errors = errors ?: mutableListOf<GError>().also { errors = it }
						)

						true
					}

					is GValue.Null ->
						true

					is GValue.Variable ->
						false
				}

			is GNonNullType ->
				error("Impossible.")

			is GStringType ->
				when (value) {
					is GValue.Null,
					is GValue.String ->
						true

					is GValue.Boolean,
					is GValue.Enum,
					is GValue.Float,
					is GValue.Int,
					is GValue.List,
					is GValue.Object,
					is GValue.Variable ->
						false
				}

			is GInterfaceType,
			is GObjectType,
			is GUnionType ->
				true // We don't check types - only values.
		}

		if (!isValidValue)
			reportError()

		return errors
	}


	companion object {

		fun parse(source: GSource.Parsable) =
			GDocument.parse(source).schema


		fun parse(content: String, name: String = "<document>") =
			parse(GSource.of(content = content, name = name))
	}
}


fun GSchema(document: GDocument): GSchema? {
	val typeSystemDefinitions = document.definitions.filterIsInstance<GTypeSystemDefinition>()
		.ifEmpty { return null }

	val directiveDefinitions = typeSystemDefinitions.filterIsInstance<GDirectiveDefinition>().toMutableList()
	if (directiveDefinitions.none { it.name == "deprecated" })
		directiveDefinitions += GSpecification.defaultDeprecatedDirective
	if (directiveDefinitions.none { it.name == "include" })
		directiveDefinitions += GSpecification.defaultIncludeDirective
	if (directiveDefinitions.none { it.name == "skip" })
		directiveDefinitions += GSpecification.defaultSkipDirective

	val schemaDefinition = typeSystemDefinitions.filterIsInstance<GSchemaDefinition>()
		.singleOrNull() // FIXME
	val typeDefinitions = typeSystemDefinitions.filterIsInstance<GNamedType>()

	val mutationTypeRef: GNamedTypeRef?
	val queryTypeRef: GNamedTypeRef?
	val subscriptionTypeRef: GNamedTypeRef?

	if (schemaDefinition !== null) {
		mutationTypeRef = schemaDefinition.operationTypeDefinitions.firstOrNull { it.operationType == GOperationType.mutation }?.type
		queryTypeRef = schemaDefinition.operationTypeDefinitions.firstOrNull { it.operationType == GOperationType.query }?.type
		subscriptionTypeRef = schemaDefinition.operationTypeDefinitions.firstOrNull { it.operationType == GOperationType.subscription }?.type
	}
	else {
		mutationTypeRef = GTypeRef(GSpecification.defaultMutationTypeName)
		queryTypeRef = GTypeRef(GSpecification.defaultQueryTypeName)
		subscriptionTypeRef = GTypeRef(GSpecification.defaultSubscriptionTypeName)
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
