package io.fluidsonic.graphql

import io.fluidsonic.graphql.GraphQLTypeContainerScope.*
import kotlin.properties.*
import kotlin.reflect.*


/**
 * Base scope providing built-in GraphQL type references for the document/operation builder DSL.
 *
 * Provides scalar type references ([Boolean], [Int], [Float], [String], [ID]), list and
 * non-null constructors, and a [type] helper for named types.
 */
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


	/** Wraps [type] in a [GListTypeRef]. */
	@GraphQLMarker
	public fun List(type: GTypeRef): GTypeRef =
		GListTypeRef(type)

	/** Wraps a type named [type] in a [GListTypeRef]. */
	@GraphQLMarker
	public fun List(type: String): GTypeRef =
		GListTypeRef(type(type))

	/** Creates a [GNamedTypeRef] for the type with the given [name]. */
	@GraphQLMarker
	public fun type(name: String): GNamedTypeRef {
		check(GLanguage.isValidTypeName(name)) { "Invalid type name: $name" }

		return GNamedTypeRef(name)
	}

	/**
	 * Wraps this type reference as non-null.
	 *
	 * Throws if the type is already non-null.
	 */
	@GraphQLMarker
	public operator fun GTypeRef.not(): GNonNullTypeRef =
		when (this) {
			is GNonNullTypeRef -> error("Cannot use '!' on a type that's already non-null.")
			else -> GNonNullTypeRef(this)
		}

	/**
	 * Creates a non-null reference to the type with this name.
	 *
	 * Equivalent to `!type(name)`.
	 */
	@GraphQLMarker
	public operator fun String.not(): GNonNullTypeRef =
		GNonNullTypeRef(type(this))


	/**
	 * Delegate provider that derives a [GNamedTypeRef] from the delegating property's name.
	 *
	 * Accessed via the [type] extension property: `val User by type`
	 */
	@GraphQLMarker
	public object RefFactory : PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, GNamedTypeRef>> {

		override fun provideDelegate(thisRef: Nothing?, property: KProperty<*>): ReadOnlyProperty<Nothing?, GNamedTypeRef> {
			check(GLanguage.isValidTypeName(property.name)) { "Invalid type name: ${property.name}" }
			val ref = GNamedTypeRef(property.name)

			return ReadOnlyProperty { _, _ -> ref }
		}
	}
}


/**
 * Delegate provider that creates a [GNamedTypeRef] from the property name.
 *
 * ```kotlin
 * val User by type
 * ```
 */
@GraphQLMarker
@Suppress("UnusedReceiverParameter")
public val GraphQLTypeContainerScope.type: RefFactory
	get() = RefFactory
