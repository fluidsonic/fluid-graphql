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
interface SchemaBuilder {

	@SchemaBuilderType
	val type
		get() = TypeRefFactory

	@SchemaBuilderKeywordB
	fun Directive(name: String, configure: DirectiveDefinitionBuilder.() -> Unit = {})

	@SchemaBuilderType
	fun Enum(type: GTypeRef, configure: EnumBuilder.() -> Unit)

	@SchemaBuilderType
	fun InputObject(type: GTypeRef, configure: InputObjectBuilder.() -> Unit)

	@SchemaBuilderType
	fun Interface(type: GTypeRef, configure: InterfaceBuilder.() -> Unit)

	@SchemaBuilderType
	fun Mutation(configure: ObjectBuilder.() -> Unit) =
		Mutation(type(GSpecification.defaultMutationTypeName), configure)

	@SchemaBuilderType
	fun Mutation(type: GTypeRef, configure: ObjectBuilder.() -> Unit)

	@SchemaBuilderType
	fun Object(type: GTypeRef, configure: ObjectBuilder.() -> Unit)

	@SchemaBuilderType
	fun Object(named: InterfacesForObject, configure: ObjectBuilder.() -> Unit)

	@SchemaBuilderType
	fun Query(configure: ObjectBuilder.() -> Unit) =
		Query(type(GSpecification.defaultQueryTypeName), configure)

	@SchemaBuilderType
	fun Query(type: GTypeRef, configure: ObjectBuilder.() -> Unit)

	@SchemaBuilderType
	fun Scalar(type: GTypeRef, configure: ScalarBuilder.() -> Unit = {})

	@SchemaBuilderType
	fun Subscription(configure: ObjectBuilder.() -> Unit) =
		Subscription(type(GSpecification.defaultSubscriptionTypeName), configure)

	@SchemaBuilderType
	fun Subscription(type: GTypeRef, configure: ObjectBuilder.() -> Unit)

	@SchemaBuilderType
	fun Union(named: PossibleTypesForUnion, configure: UnionBuilder.() -> Unit = {})

	@SchemaBuilderKeywordB
	infix fun GTypeRef.implements(interfaceType: GTypeRef): InterfacesForObject

	@SchemaBuilderKeywordB
	infix fun GTypeRef.with(possibleType: GTypeRef): PossibleTypesForUnion


	companion object


	@SchemaBuilderDsl
	interface ArgumentContainer : ValueContainer {

		@SchemaBuilderKeywordB
		infix fun String.with(value: Any?)
	}


	@SchemaBuilderDsl
	interface DeprecationContainer {

		@SchemaBuilderKeywordB
		fun deprecated(reason: String = "")
	}


	@SchemaBuilderDsl
	interface DescriptionContainer {

		@SchemaBuilderKeywordB
		fun description(text: String)
	}


	@SchemaBuilderDsl
	interface DirectiveBuilder : ArgumentContainer


	@SchemaBuilderDsl
	interface DirectiveDefinitionBuilder : InputValueContainer, DescriptionContainer {

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
	interface EnumBuilder : DescriptionContainer, DirectiveContainer {

		operator fun String.invoke(configure: ValueBuilder.() -> Unit): DanglingValue
		operator fun String.unaryMinus()


		interface DanglingValue {

			operator fun unaryMinus()
		}


		@SchemaBuilderDsl
		interface ValueBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer
	}


	@SchemaBuilderDsl
	interface FieldBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer, InputValueContainer, ValueContainer


	@SchemaBuilderDsl
	interface FieldContainer : TypeRefContainer {

		@SchemaBuilderKeywordB
		infix fun String.of(type: DanglingType)

		@SchemaBuilderKeywordB
		infix fun String.of(type: GTypeRef)

		@SchemaBuilderBuiltinTypeA
		operator fun GTypeRef.invoke(configure: FieldBuilder.() -> Unit): DanglingType

		// GTypeRef.invoke() won't work for List(…) {} because Kotlin's global 'List' function takes precedence
		@SchemaBuilderBuiltinTypeA
		fun List(type: GTypeRef, configure: FieldBuilder.() -> Unit) =
			List(type)(configure)


		interface DanglingType
	}


	@SchemaBuilderDsl
	interface InputObjectBuilder : DescriptionContainer, DirectiveContainer, InputValueContainer


	@SchemaBuilderDsl
	interface InputValueBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	interface InputValueContainer : TypeRefContainer {

		@SchemaBuilderKeywordB
		infix fun String.of(type: DanglingType)

		@SchemaBuilderKeywordB
		infix fun String.of(type: GTypeRef): InputValue

		operator fun Boolean.invoke(configure: InputValueBuilder.() -> Unit): DanglingDefault

		operator fun Double.invoke(configure: InputValueBuilder.() -> Unit): DanglingDefault

		operator fun Float.invoke(configure: InputValueBuilder.() -> Unit): DanglingDefault

		operator fun Int.invoke(configure: InputValueBuilder.() -> Unit): DanglingDefault

		operator fun String.invoke(configure: InputValueBuilder.() -> Unit): DanglingDefault

		@SchemaBuilderBuiltinTypeA
		operator fun GTypeRef.invoke(configure: InputValueBuilder.() -> Unit): DanglingType

		operator fun GValue.invoke(configure: InputValueBuilder.() -> Unit): DanglingDefault

		// I hate that function invocation takes precedence over infix operators!
		@SchemaBuilderKeywordB
		fun enumValue(name: String, configure: InputValueBuilder.() -> Unit) =
			GEnumValue(name)(configure)

		// GTypeRef.invoke() won't work for List(…) {} because Kotlin's global 'List' function takes precedence
		@SchemaBuilderBuiltinTypeA
		fun List(type: GTypeRef, configure: InputValueBuilder.() -> Unit) =
			List(type)(configure)


		interface InputValue {

			@SchemaBuilderKeywordB
			infix fun default(default: Boolean): InputValue

			@SchemaBuilderKeywordB
			infix fun default(default: Double): InputValue

			@SchemaBuilderKeywordB
			infix fun default(default: Float): InputValue

			@SchemaBuilderKeywordB
			infix fun default(default: Int): InputValue

			@SchemaBuilderKeywordB
			infix fun default(default: Nothing?): InputValue

			@SchemaBuilderKeywordB
			infix fun default(default: String): InputValue

			@SchemaBuilderKeywordB
			infix fun default(default: GValue): InputValue

			@SchemaBuilderKeywordB
			infix fun default(default: DanglingDefault): InputValue

			operator fun invoke(configure: InputValueBuilder.() -> Unit)
		}


		interface DanglingDefault
		interface DanglingType
	}


	@SchemaBuilderDsl
	interface InterfaceBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer, FieldContainer


	interface InterfacesForObject {

		@SchemaBuilderKeywordB
		infix fun and(type: GTypeRef): InterfacesForObject
	}


	interface PossibleTypesForUnion {

		@SchemaBuilderKeywordB
		infix fun or(type: GTypeRef): PossibleTypesForUnion
	}


	@SchemaBuilderDsl
	interface ScalarBuilder : DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	interface ObjectBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer, FieldContainer


	object TypeRefFactory {

		@SchemaBuilderType
		operator fun invoke(name: String): GTypeRef =
			GNamedTypeRef(name)


		@SchemaBuilderType
		operator fun getValue(thisRef: Any?, property: KProperty<*>): GTypeRef =
			invoke(property.name)
	}


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
				nonNull()
	}


	@SchemaBuilderDsl
	interface UnionBuilder : DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	interface ValueContainer {

		@SchemaBuilderKeywordB
		fun enumValue(name: String): GEnumValue
	}
}
