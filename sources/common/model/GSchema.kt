package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
class GSchema(
	types: List<GNamedType>,
	queryType: GNamedTypeRef? = null,
	mutationType: GNamedTypeRef? = null,
	subscriptionType: GNamedTypeRef? = null,
	val directives: List<GDirectiveDefinition> = emptyList()
) {

	val types = types + GType.defaultTypes
	val directivesByName = directives.associateBy { it.name }
	val typesByName = this.types.associateBy { it.name }

	val queryType = queryType?.let { typesByName[it.name] as? GObjectType }
	val queryTypeRef = queryType
	val mutationType = mutationType?.let { typesByName[it.name] as? GObjectType }
	val mutationTypeRef = mutationType
	val subscriptionType = subscriptionType?.let { typesByName[it.name] as? GObjectType }
	val subscriptionTypeRef = subscriptionType

	private val possibleTypesByType: Map<String, List<GObjectType>> =
		types
			.filterIsInstance<GObjectType>()
			.filter { it.interfaces.isNotEmpty() }
			.flatMap { type -> type.interfaces.map { it.name to type } }
			.groupBy(keySelector = { it.first }, valueTransform = { it.second }) +
			types
				.filterIsInstance<GUnionType>()
				.associate { union -> union.name to union.possibleTypes.mapNotNull { typesByName[it.name] as? GObjectType } }


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
				(type as? GType.WithFields)?.fieldsByName?.get(name)
		}


	fun getPossibleTypes(type: GAbstractType) =
		possibleTypesByType[type.name].orEmpty()


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


	companion object
}


fun GSchema(definitions: List<GTypeSystemDefinition>): GSchema {
	val directiveDefinitions = definitions.filterIsInstance<GDirectiveDefinition>()
	val schemaDefinitions = definitions.filterIsInstance<GSchemaDefinition>()
		.singleOrNull() // FIXME
	val typeDefinitions = definitions.filterIsInstance<GNamedType>()

	val mutationType = schemaDefinitions?.operationTypes
		?.firstOrNull { it.operation == GOperationType.mutation }
		?.type

	val queryType = schemaDefinitions?.operationTypes
		?.firstOrNull { it.operation == GOperationType.query }
		?.type

	val subscriptionType = schemaDefinitions?.operationTypes
		?.firstOrNull { it.operation == GOperationType.subscription }
		?.type

	return GSchema(
		directives = directiveDefinitions,
		mutationType = mutationType,
		queryType = queryType,
		subscriptionType = subscriptionType,
		types = typeDefinitions
	)
}
