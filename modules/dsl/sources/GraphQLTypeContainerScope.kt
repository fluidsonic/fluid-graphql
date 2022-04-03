package io.fluidsonic.graphql

import io.fluidsonic.graphql.GraphQLTypeContainerScope.*
import kotlin.properties.*
import kotlin.reflect.*


@GraphQLMarker
@Suppress("PropertyName")
public sealed interface GraphQLTypeContainerScope {

	@GraphQLMarker
	public val Boolean: GNamedTypeRef
		get() = GBooleanTypeRef

	@GraphQLMarker
	public val Float: GNamedTypeRef
		get() = GFloatTypeRef

	@GraphQLMarker
	public val ID: GNamedTypeRef
		get() = GIdTypeRef

	@GraphQLMarker
	public val Int: GNamedTypeRef
		get() = GIntTypeRef

	@GraphQLMarker
	public val String: GNamedTypeRef
		get() = GStringTypeRef


	@GraphQLMarker
	public fun List(type: GTypeRef): GTypeRef =
		GListTypeRef(type)


	@GraphQLMarker
	public fun List(type: String): GTypeRef =
		GListTypeRef(type(type))


	@GraphQLMarker
	public fun type(name: String): GNamedTypeRef {
		check(GLanguage.isValidTypeName(name)) { "Invalid type name: $name" }

		return GNamedTypeRef(name)
	}


	@GraphQLMarker
	public operator fun GTypeRef.not(): GNonNullTypeRef =
		when (this) {
			is GNonNullTypeRef -> error("Cannot use '!' on a type that's already non-null.")
			else -> GNonNullTypeRef(this)
		}


	@GraphQLMarker
	public operator fun String.not(): GNonNullTypeRef =
		GNonNullTypeRef(type(this))


	@GraphQLMarker
	public object RefFactory : PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, GNamedTypeRef>> {

		override fun provideDelegate(thisRef: Nothing?, property: KProperty<*>): ReadOnlyProperty<Nothing?, GNamedTypeRef> {
			check(GLanguage.isValidTypeName(property.name)) { "Invalid type name: ${property.name}" }
			val ref = GNamedTypeRef(property.name)

			return ReadOnlyProperty { _, _ -> ref }
		}
	}
}


@GraphQLMarker
@Suppress("unused")
public val GraphQLTypeContainerScope.type: RefFactory
	get() = RefFactory
