package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
class GSchema private constructor(
	val directives: List<GDirectiveDefinition>,
	val mutationType: GObjectType?,
	val queryType: GObjectType?,
	val subscriptionType: GObjectType?,
	val types: Map<String, GNamedType>
) {

	fun resolveType(ref: GTypeRef): GType? {
		TODO()
	}


	fun resolveType(ref: GNamedTypeRef) =
		types[ref.name]


	fun resolveType(name: String) =
		resolveType(GNamedTypeRef(name))


	fun rootTypeForOperationType(operationType: GOperationType) =
		when (operationType) {
			GOperationType.mutation -> mutationType
			GOperationType.query -> queryType
			GOperationType.subscription -> subscriptionType
		}


	val rootTypeNamesFollowCommonConvention
		get() = (queryType == null || queryType.name == GSpecification.defaultQueryTypeName) &&
			(mutationType == null || mutationType.name == GSpecification.defaultMutationTypeName) &&
			(subscriptionType == null || subscriptionType.name == GSpecification.defaultSubscriptionTypeName)


	override fun toString() =
		GWriter { writeSchema(this@GSchema) }


	companion object


	class Unresolved(
		val types: List<GNamedType.Unresolved>,
		val queryType: GNamedTypeRef? = null,
		val mutationType: GNamedTypeRef? = null,
		val subscriptionType: GNamedTypeRef? = null,
		val directives: List<GDirectiveDefinition.Unresolved> = emptyList()
	) {

		fun resolve(): GSchema {
			// FIXME validate first

			val typeRegistry = GTypeRegistry.Autocreating(definitions = types)
			val types = types
				.map { typeRegistry.resolve(it.name) }
				.associateBy { it.name }

			val queryType = queryType
				?: GNamedTypeRef(GSpecification.defaultQueryTypeName).takeIf { types.containsKey(it.name) }

			return GSchema(
				directives = directives.map { it.resolve(typeRegistry) },
				mutationType = mutationType?.let { typeRegistry.resolveKind<GObjectType>(it) },
				queryType = queryType?.let { typeRegistry.resolveKind<GObjectType>(it) },
				subscriptionType = subscriptionType?.let { typeRegistry.resolveKind<GObjectType>(it) },
				types = types // FIXME add builtins?
			)
		}


		companion object
	}
}
