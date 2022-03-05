package io.fluidsonic.graphql


internal object TypeResolver {

	fun resolveType(schema: GSchema, ref: GTypeRef, includeIntrospection: Boolean = true): GType? =
		when (ref) {
			is GListTypeRef -> resolveType(schema, ref.elementType, includeIntrospection)?.let { GListType(elementType = it) }
			is GNamedTypeRef -> resolveType(schema, ref, includeIntrospection)
			is GNonNullTypeRef -> resolveType(schema, ref.nullableRef, includeIntrospection)?.let { GNonNullType(nullableType = it) }
		}


	fun resolveType(schema: GSchema, ref: GNamedTypeRef, includeIntrospection: Boolean = true): GNamedType? =
		resolveType(schema, ref.name, includeIntrospection)


	fun resolveType(schema: GSchema, name: String, includeIntrospection: Boolean = true): GNamedType? =
		schema.resolveType(name) ?: when (includeIntrospection) {
			true -> Introspection.schema.resolveType(name)
			false -> null
		}


	inline fun <reified Type : GType> resolveTypeAs(schema: GSchema, ref: GTypeRef, includeIntrospection: Boolean = true): Type? =
		resolveType(schema, ref, includeIntrospection) as? Type


	inline fun <reified Type : GNamedType> resolveTypeAs(schema: GSchema, ref: GNamedTypeRef, includeIntrospection: Boolean = true): Type? =
		resolveType(schema, ref, includeIntrospection) as? Type


	inline fun <reified Type : GNamedType> resolveTypeAs(schema: GSchema, name: String, includeIntrospection: Boolean = true): Type? =
		resolveType(schema, name, includeIntrospection) as? Type
}
