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
	fun Enum(type: GNamedTypeRef, configure: EnumTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun InputObject(type: GNamedTypeRef, configure: InputObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Interface(type: GNamedTypeRef, configure: InterfaceTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Interface(named: Interfaces, configure: InterfaceTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Mutation(configure: ObjectTypeDefinitionBuilder<Unit>.() -> Unit) =
		Mutation(type(GSpecification.defaultMutationTypeName), configure)

	@SchemaBuilderType
	fun Mutation(type: GNamedTypeRef)

	@SchemaBuilderType
	fun Mutation(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder<Unit>.() -> Unit)

	@SchemaBuilderType
	fun Object(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder<Any>.() -> Unit) =
		Object(type = type, kotlinType = Any::class, configure = configure)

	@SchemaBuilderType
	fun <T : Any> Object(type: GNamedTypeRef, kotlinType: KClass<T>, configure: ObjectTypeDefinitionBuilder<T>.() -> Unit)

	@SchemaBuilderType
	fun Object(named: Interfaces, configure: ObjectTypeDefinitionBuilder<Any>.() -> Unit) =
		Object(named = named, kotlinType = Any::class, configure = configure)

	@SchemaBuilderType
	fun <T : Any> Object(named: Interfaces, kotlinType: KClass<T>, configure: ObjectTypeDefinitionBuilder<T>.() -> Unit)

	@SchemaBuilderType
	fun Query(configure: ObjectTypeDefinitionBuilder<Unit>.() -> Unit) =
		Query(type(GSpecification.defaultQueryTypeName), configure)

	@SchemaBuilderType
	fun Query(type: GNamedTypeRef)

	@SchemaBuilderType
	fun Query(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder<Unit>.() -> Unit)

	@SchemaBuilderType
	fun Scalar(type: GNamedTypeRef, configure: ScalarTypeDefinitionBuilder.() -> Unit = {})

	@SchemaBuilderType
	fun Subscription(configure: ObjectTypeDefinitionBuilder<Unit>.() -> Unit) =
		Subscription(type(GSpecification.defaultSubscriptionTypeName), configure)

	@SchemaBuilderType
	fun Subscription(type: GNamedTypeRef)

	@SchemaBuilderType
	fun Subscription(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder<Unit>.() -> Unit)

	@SchemaBuilderType
	fun Union(named: PossibleTypes, configure: UnionTypeDefinitionBuilder.() -> Unit = {})

	@SchemaBuilderKeywordB
	infix fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): Interfaces

	@SchemaBuilderKeywordB
	infix fun GNamedTypeRef.with(possibleType: GNamedTypeRef): PossibleTypes


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
	interface EnumTypeDefinitionBuilder : DescriptionContainer, DirectiveContainer {

		@SchemaBuilderKeywordB
		fun value(name: String, configure: ValueBuilder.() -> Unit = {})


		@SchemaBuilderDsl
		interface ValueBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer
	}


	@SchemaBuilderDsl
	interface FieldDefinitionBuilder : ArgumentDefinitionContainer, DeprecationContainer, DescriptionContainer, DirectiveContainer {

		@SchemaBuilderDsl
		interface Resolvable<out Parent : Any> : FieldDefinitionBuilder {

			@SchemaBuilderKeywordB
			fun resolve(resolver: GFieldResolver.Context.(parent: Parent) -> Any?)
		}
	}


	@SchemaBuilderDsl
	interface FieldDefinitionContainer : TypeRefContainer {

		@SchemaBuilderKeywordB
		infix fun String.of(type: GTypeRef): NameAndType


		interface NameAndType


		@SchemaBuilderDsl
		interface Resolvable<out Parent : Any> : FieldDefinitionContainer {

			@SchemaBuilderKeywordB
			fun field(name: NameAndType, configure: FieldDefinitionBuilder.Resolvable<Parent>.() -> Unit = {})
		}


		@SchemaBuilderDsl
		interface Unresolvable : FieldDefinitionContainer {

			@SchemaBuilderKeywordB
			fun field(name: NameAndType, configure: FieldDefinitionBuilder.() -> Unit = {})
		}
	}


	@SchemaBuilderDsl
	interface InputObjectTypeDefinitionBuilder : ArgumentDefinitionContainer, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	interface InterfaceTypeDefinitionBuilder :
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer.Unresolvable


	interface Interfaces {

		@SchemaBuilderKeywordB
		infix fun and(type: GNamedTypeRef): Interfaces
	}


	object NamedTypeRefFactory {

		@SchemaBuilderType
		operator fun getValue(thisRef: Any?, property: KProperty<*>) =
			GNamedTypeRef(property.name)
	}


	@SchemaBuilderDsl
	interface ObjectTypeDefinitionBuilder<out T : Any> :
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer.Resolvable<T>


	interface PossibleTypes {

		@SchemaBuilderKeywordB
		infix fun or(type: GNamedTypeRef): PossibleTypes
	}


	@SchemaBuilderDsl
	interface ScalarTypeDefinitionBuilder : DescriptionContainer, DirectiveContainer


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
	interface UnionTypeDefinitionBuilder : DescriptionContainer, DirectiveContainer
}


@SchemaBuilderType
inline fun <reified T : Any> GSchemaBuilder.Object(type: GNamedTypeRef, noinline configure: GSchemaBuilder.ObjectTypeDefinitionBuilder<T>.() -> Unit) =
	Object(type = type, kotlinType = T::class, configure = configure)


@SchemaBuilderType
inline fun <reified T : Any> GSchemaBuilder.Object(named: GSchemaBuilder.Interfaces, noinline configure: GSchemaBuilder.ObjectTypeDefinitionBuilder<T>.() -> Unit) =
	Object(named = named, kotlinType = T::class, configure = configure)
