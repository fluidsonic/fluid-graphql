package io.fluidsonic.graphql

import kotlin.reflect.*


interface GTypeRegistry {

	fun register(type: GNamedType)

	fun resolveOrNull(reference: GTypeRef): GType?


	class Autocreating(
		definitions: List<GNamedType.Unresolved>
	) : GTypeRegistry {

		private val definitions: Map<String, GNamedType.Unresolved>
		private val types = hashMapOf<GTypeRef, GType>()


		init {
			require(definitions.size <= 1 || definitions.mapTo(hashSetOf()) { it.name }.size == definitions.size) {
				"'definitions' must not contain multiple elements with the same name: $definitions"
			}

			this.definitions = definitions.associateBy { it.name }
		}


		override fun register(type: GNamedType) {
			check(types.put(GNamedTypeRef(type.name), type) == null) { "Cannot register multiple types with the same name: ${type.name}" }
		}


		override fun resolveOrNull(reference: GTypeRef): GType? {
			types[reference]?.let { return it }

			// Don't put new types into the map. They must self-register to allow for reference cycles.
			return when (reference) {
				is GNamedTypeRef -> when (reference.name) {
					GBooleanType.name -> GBooleanType
					GFloatType.name -> GFloatType
					GIDType.name -> GIDType
					GIntType.name -> GIntType
					GStringType.name -> GStringType
					else -> definitions[reference.name]?.resolve(this)
				}
				is GListTypeRef -> resolveOrNull(reference.elementType)?.let(::GListType)
				is GNonNullTypeRef -> resolveOrNull(reference.nullableType)?.let(::GNonNullType)
			}
		}
	}
}


fun GTypeRegistry.resolve(name: String) =
	resolveKind<GNamedType>(name)


fun GTypeRegistry.resolve(reference: GTypeRef) =
	resolveOrNull(reference)
		?: error("Cannot resolve type: $reference")


inline fun <reified Type : GNamedType> GTypeRegistry.resolveKind(name: String): Type =
	resolveKind(name, Type::class)


fun <Type : GNamedType> GTypeRegistry.resolveKind(name: String, kind: KClass<out Type>) =
	resolveKind(GNamedTypeRef(name), kind)


inline fun <reified Type : GNamedType> GTypeRegistry.resolveKind(reference: GNamedTypeRef): Type =
	resolveKind(reference, Type::class)


fun <Type : GNamedType> GTypeRegistry.resolveKind(reference: GNamedTypeRef, kind: KClass<out Type>): Type {
	val type = resolve(reference)

	val isValidType = when (kind) {
		GNamedType::class -> true

		GEnumType::class -> type is GEnumType
		GInputObjectType::class -> type is GInputObjectType
		GInterfaceType::class -> type is GInterfaceType
		GObjectType::class -> type is GObjectType
		GUnionType::class -> type is GUnionType

		GScalarType::class -> type is GScalarType
		GCustomScalarType::class -> type is GCustomScalarType
		GBooleanType::class -> type === GBooleanType
		GFloatType::class -> type === GFloatType
		GIntType::class -> type === GIntType
		GIDType::class -> type === GIDType
		GStringType::class -> type === GStringType

		else ->
			error("Unexpected required type kind: $kind")
	}

	require(isValidType) { "Required type of $kind but found type of ${type::class}" }

	@Suppress("UNCHECKED_CAST")
	return type as Type
}
