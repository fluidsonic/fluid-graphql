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


	companion object {

		// https://graphql.github.io/graphql-spec/June2018/#IsInputType()
		fun isInputType(type: GType): Boolean =
			when (type) {
				is GWrappingType -> isInputType(type.ofType)
				is GScalarType, is GEnumType, is GInputObjectType -> true
				else -> false
			}


		// https://graphql.github.io/graphql-spec/June2018/#IsOutputType()
		fun isOutputType(type: GType): Boolean =
			when (type) {
				is GWrappingType -> isOutputType(type.ofType)
				is GScalarType, is GObjectType, is GInterfaceType, is GUnionType, is GEnumType -> true
				else -> false
			}


		fun of(input: GQLInput.Schema): GSchema {
			val typeInputByName = input.types.associateByTo(hashMapOf()) { it.name } // FIXME check dups
			val typeByReference = hashMapOf<GTypeRef, GType>()

			val typeFactory = object : TypeFactory {

				override fun get(name: String, kind: GType.Kind?) =
					if (kind != null)
						get(GNamedTypeRef(name), kind) as GNamedType
					else
						get(GNamedTypeRef(name)) as GNamedType


				override fun get(reference: GTypeRef): GType {
					typeByReference[reference]?.let { return it }

					// Don't put, just return new type! Types must self-register or else we risk race conditions
					return when (reference) {
						is GNamedTypeRef -> {
							when (reference.name) {
								"Boolean" -> GBooleanType
								"Float" -> GFloatType
								"ID" -> GIDType
								"Int" -> GIntType
								"String" -> GStringType
								else -> {
									val typeInput = typeInputByName[reference.name]
										?: error("no such type: ${reference.name}") // FIXME

									when (typeInput) {
										is GQLInput.Type.Enum -> GEnumType(typeFactory = this, input = typeInput)
										is GQLInput.Type.InputObject -> GInputObjectType(typeFactory = this, input = typeInput)
										is GQLInput.Type.Interface -> GInterfaceType(typeFactory = this, input = typeInput)
										is GQLInput.Type.Object -> GObjectType(typeFactory = this, input = typeInput)
										is GQLInput.Type.Scalar -> GCustomScalarType(typeFactory = this, input = typeInput)
										is GQLInput.Type.Union -> GUnionType(typeFactory = this, input = typeInput)
									}
								}
							}
						}

						is GListTypeRef -> GListType(typeFactory = this, input = reference)
						is GNonNullTypeRef -> GNonNullType(typeFactory = this, input = reference)
					}
				}


				override fun get(reference: GTypeRef, kind: GType.Kind): GType {
					val type = get(reference)
					if (type.kind !== kind) {
						error("Expected type $type to be of kind $kind but is ${type.kind}")
					}

					return type
				}


				override fun getObjectsImplementingInterface(name: String) =
					input.types
						.filterIsInstance<GQLInput.Type.Object>()
						.filter { objectInput -> objectInput.interfaces.any { (it as GNamedTypeRef).name == name } }
						.map { get(it.name) as GObjectType }


				private fun kindOfInput(type: GQLInput.Type) =
					when (type) {
						is GQLInput.Type.Enum -> GType.Kind.ENUM
						is GQLInput.Type.InputObject -> GType.Kind.INPUT_OBJECT
						is GQLInput.Type.Interface -> GType.Kind.INTERFACE
						is GQLInput.Type.Object -> GType.Kind.OBJECT
						is GQLInput.Type.Scalar -> GType.Kind.SCALAR
						is GQLInput.Type.Union -> GType.Kind.UNION
					}


				override fun register(type: GType) {
					typeByReference[referenceForType(type)] = type
				}


				private fun referenceForType(type: GType): GTypeRef =
					when (type) {
						is GNamedType ->
							GNamedTypeRef(type.name)

						is GWrappingType ->
							when (type) {
								is GListType -> GListTypeRef(referenceForType(type.ofType))
								is GNonNullType -> GNonNullTypeRef(referenceForType(type.ofType))
							}
					}
			}

			val types = input.types
				.map { typeFactory.get(it.name) }
				.associateBy { it.name }

			val queryTypeName = input.queryType ?: GSpecification.defaultQueryTypeName

			// FIXME validate
			return GSchema(
				directives = input.directives.map { GDirectiveDefinition(typeFactory = typeFactory, input = it) },
				mutationType = input.mutationType?.let { typeFactory.get(it, GType.Kind.OBJECT) as GObjectType },
				queryType = typeFactory.get(queryTypeName, GType.Kind.OBJECT) as GObjectType,
				subscriptionType = input.subscriptionType?.let { typeFactory.get(it, GType.Kind.OBJECT) as GObjectType },
				types = types // FIXME builtins
			)
		}


//			private fun requireOperationType(typeName: String, types: KList<Type>, operationName: String): Object {
//				val type = types.firstOrNull { it.name == queryType }
//			}
	}
}
