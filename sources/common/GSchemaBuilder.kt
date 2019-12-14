package io.fluidsonic.graphql

import kotlin.reflect.*


// The DSL color in IntelliJ IDEA depends on the hash code of the FQN of marker class.
// Therefor we add a random letter to get the desired "DSL style number" (for now).

@DslMarker
annotation class SchemaBuilderKeywordB

@DslMarker
annotation class SchemaBuilderBuiltinTypeA

@DslMarker
annotation class SchemaBuilderType

@DslMarker
annotation class SchemaBuilderDsl


@SchemaBuilderDsl
@Suppress("PropertyName")
interface GSchemaBuilder {

	@SchemaBuilderType
	val type
		get() = NamedTypeRefFactory

	@SchemaBuilderType
	fun type(name: String) =
		GNamedTypeRef(name)

	@SchemaBuilderKeywordB
	fun Directive(name: String, configure: DirectiveDefinitionBuilder.() -> Unit = {})

	@SchemaBuilderType
	fun Enum(type: GNamedTypeRef, configure: EnumDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun InputObject(type: GNamedTypeRef, configure: InputObjectDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Interface(type: GNamedTypeRef, configure: InterfaceDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Mutation(configure: ObjectDefinitionBuilder.() -> Unit) =
		Mutation(type(GSpecification.defaultMutationTypeName), configure)

	@SchemaBuilderType
	fun Mutation(type: GNamedTypeRef, configure: ObjectDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Object(type: GNamedTypeRef, configure: ObjectDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Object(named: InterfacesForObject, configure: ObjectDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Query(configure: ObjectDefinitionBuilder.() -> Unit) =
		Query(type(GSpecification.defaultQueryTypeName), configure)

	@SchemaBuilderType
	fun Query(type: GNamedTypeRef, configure: ObjectDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Scalar(type: GNamedTypeRef, configure: ScalarBuilder.() -> Unit = {})

	@SchemaBuilderType
	fun Subscription(configure: ObjectDefinitionBuilder.() -> Unit) =
		Subscription(type(GSpecification.defaultSubscriptionTypeName), configure)

	@SchemaBuilderType
	fun Subscription(type: GNamedTypeRef, configure: ObjectDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Union(named: PossibleTypesForUnion, configure: UnionDefinitionBuilder.() -> Unit = {})

	@SchemaBuilderKeywordB
	infix fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): InterfacesForObject

	@SchemaBuilderKeywordB
	infix fun GNamedTypeRef.with(possibleType: GNamedTypeRef): PossibleTypesForUnion


	companion object


	@SchemaBuilderDsl
	interface ArgumentContainer {

		@SchemaBuilderKeywordB
		fun argument(name: NameAndValue)

		@SchemaBuilderKeywordB
		infix fun String.with(value: Any?): NameAndValue


		interface NameAndValue
	}


	@SchemaBuilderDsl
	interface ArgumentDefinitionBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	interface ArgumentDefinitionContainer : TypeRefContainer {

		@SchemaBuilderKeywordB
		fun argument(name: NameAndType, configure: ArgumentDefinitionBuilder.() -> Unit = {})

		@SchemaBuilderKeywordB
		fun argument(name: NameAndTypeAndDefault, configure: ArgumentDefinitionBuilder.() -> Unit = {})

		@SchemaBuilderKeywordB
		infix fun String.of(type: GTypeRef): NameAndType


		interface NameAndType {

			@SchemaBuilderKeywordB
			infix fun default(default: Any?): NameAndTypeAndDefault
		}

		interface NameAndTypeAndDefault
	}


	@SchemaBuilderDsl
	interface DeprecationContainer {

		@SchemaBuilderKeywordB
		fun deprecated(reason: String? = null)
	}


	@SchemaBuilderDsl
	interface DescriptionContainer {

		@SchemaBuilderKeywordB
		fun description(text: String)
	}


	@SchemaBuilderDsl
	interface DirectiveBuilder : ArgumentContainer


	@SchemaBuilderDsl
	interface DirectiveDefinitionBuilder : ArgumentDefinitionContainer, DescriptionContainer {

		@SchemaBuilderBuiltinTypeA
		val ARGUMENT_DEFINITION: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val ENUM: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val ENUM_VALUE: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val FIELD: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val FIELD_DEFINITION: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val FRAGMENT_DEFINITION: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val FRAGMENT_SPREAD: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val INLINE_FRAGMENT: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val INPUT_FIELD_DEFINITION: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val INPUT_OBJECT: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val INTERFACE: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val MUTATION: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val OBJECT: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val QUERY: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val SCALAR: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val SCHEMA: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val SUBSCRIPTION: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val UNION: DirectiveLocation
		@SchemaBuilderBuiltinTypeA
		val VARIABLE_DEFINITION: DirectiveLocation


		@SchemaBuilderKeywordB
		fun on(any: DirectiveLocation)

		@SchemaBuilderKeywordB
		fun on(any: DirectiveLocationSet)


		interface DirectiveLocation {

			@SchemaBuilderKeywordB
			infix fun or(other: DirectiveLocation): DirectiveLocationSet
		}


		interface DirectiveLocationSet {

			@SchemaBuilderKeywordB
			infix fun or(other: DirectiveLocation): DirectiveLocationSet
		}
	}


	@SchemaBuilderDsl
	interface DirectiveContainer {

		@SchemaBuilderKeywordB
		fun directive(name: String, configure: DirectiveBuilder.() -> Unit = {})
	}


	@SchemaBuilderDsl
	interface EnumDefinitionBuilder : DescriptionContainer, DirectiveContainer {

		@SchemaBuilderKeywordB
		fun value(name: String, configure: ValueBuilder.() -> Unit = {})


		@SchemaBuilderDsl
		interface ValueBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer
	}


	@SchemaBuilderDsl
	interface FieldDefinitionBuilder : ArgumentDefinitionContainer, DeprecationContainer, DescriptionContainer, DirectiveContainer {

		@SchemaBuilderKeywordB
		fun <Parent : Any> resolve(parentClass: KClass<out Parent>, resolver: GFieldResolver.Context.(parent: Parent) -> Any?)
	}


	@SchemaBuilderDsl
	interface FieldDefinitionContainer : TypeRefContainer {

		@SchemaBuilderKeywordB
		fun field(name: NameAndType, configure: FieldDefinitionBuilder.() -> Unit = {})

		@SchemaBuilderKeywordB
		infix fun String.of(type: GTypeRef): NameAndType


		interface NameAndType
	}


	@SchemaBuilderDsl
	interface InputObjectDefinitionBuilder : ArgumentDefinitionContainer, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	interface InterfaceDefinitionBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer, FieldDefinitionContainer


	interface InterfacesForObject {

		@SchemaBuilderKeywordB
		infix fun and(type: GNamedTypeRef): InterfacesForObject
	}


	object NamedTypeRefFactory {

		@SchemaBuilderType
		operator fun getValue(thisRef: Any?, property: KProperty<*>) =
			GNamedTypeRef(property.name)
	}


	interface PossibleTypesForUnion {

		@SchemaBuilderKeywordB
		infix fun or(type: GNamedTypeRef): PossibleTypesForUnion
	}


	@SchemaBuilderDsl
	interface ObjectDefinitionBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer, FieldDefinitionContainer


	@SchemaBuilderDsl
	interface ScalarBuilder : DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	@Suppress("PropertyName")
	interface TypeRefContainer {

		@SchemaBuilderBuiltinTypeA
		val Boolean: GTypeRef
			get() = GBooleanTypeRef

		@SchemaBuilderBuiltinTypeA
		val Float: GTypeRef
			get() = GFloatTypeRef

		@SchemaBuilderBuiltinTypeA
		val ID: GTypeRef
			get() = GIDTypeRef

		@SchemaBuilderBuiltinTypeA
		val Int: GTypeRef
			get() = GIntTypeRef

		@SchemaBuilderBuiltinTypeA
		val String: GTypeRef
			get() = GStringTypeRef

		@SchemaBuilderBuiltinTypeA
		fun List(type: GTypeRef): GTypeRef


		operator fun GTypeRef.not() =
			if (this is GNonNullTypeRef)
				error("Cannot use '!' on a type that's already non-null")
			else
				GNonNullTypeRef(this)
	}


	@SchemaBuilderDsl
	interface UnionDefinitionBuilder : DescriptionContainer, DirectiveContainer
}


@SchemaBuilderKeywordB
inline fun <reified Parent : Any> GSchemaBuilder.FieldDefinitionBuilder.resolve(noinline resolver: GFieldResolver.Context.(parent: Parent) -> Any?) =
	resolve(parentClass = Parent::class, resolver = resolver)
