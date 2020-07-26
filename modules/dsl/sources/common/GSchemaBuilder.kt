package io.fluidsonic.graphql

import kotlin.reflect.*


// The DSL color in IntelliJ IDEA depends on the hash code of the FQN of marker class.
// Therefore we add a random letter to get the desired "DSL style number" (for now).

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
annotation class SchemaBuilderKeywordB

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
annotation class SchemaBuilderBuiltinTypeA

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
annotation class SchemaBuilderType

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
annotation class SchemaBuilderDsl


@GGraphDsl.Mark
@SchemaBuilderDsl
@Suppress("PropertyName")
interface GSchemaBuilder {

	@SchemaBuilderType
	val type: NamedTypeRefFactory
		get() = NamedTypeRefFactory

	@SchemaBuilderType
	fun type(name: String): GNamedTypeRef =
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
	fun Mutation(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Mutation(type(GLanguage.defaultMutationTypeName), configure)
	}

	@SchemaBuilderType
	fun Mutation(type: GNamedTypeRef)

	@SchemaBuilderType
	fun Mutation(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Object(named: Interfaces, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Object(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Query(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Query(type(GLanguage.defaultQueryTypeName), configure)
	}

	@SchemaBuilderType
	fun Query(type: GNamedTypeRef)

	@SchemaBuilderType
	fun Query(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Scalar(type: GNamedTypeRef, configure: ScalarTypeDefinitionBuilder.() -> Unit = {})

	@SchemaBuilderType
	fun Subscription(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Subscription(type(GLanguage.defaultSubscriptionTypeName), configure)
	}

	@SchemaBuilderType
	fun Subscription(type: GNamedTypeRef)

	@SchemaBuilderType
	fun Subscription(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	fun Union(named: PossibleTypes, configure: UnionTypeDefinitionBuilder.() -> Unit = {})

	@SchemaBuilderKeywordB
	infix fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): Interfaces

	@SchemaBuilderKeywordB
	infix fun GNamedTypeRef.with(possibleType: GNamedTypeRef): PossibleTypes


	companion object {

		private val defaultDeprecatedReason = GLanguage.defaultDeprecatedDirective.argumentDefinition("reason")!!
			.defaultValue.let { it as GStringValue }
			.value
	}


	@SchemaBuilderDsl
	interface ArgumentContainer {

		@SchemaBuilderKeywordB
		fun argument(name: NameAndValue)

		@SchemaBuilderKeywordB
		infix fun String.with(value: Any?): NameAndValue


		interface NameAndValue
	}


	@SchemaBuilderDsl
	interface ArgumentDefinitionBuilder : NodeBuilder, DeprecationContainer, DescriptionContainer, DirectiveContainer


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
		fun deprecated(reason: String? = defaultDeprecatedReason)
	}


	@SchemaBuilderDsl
	interface DescriptionContainer {

		@SchemaBuilderKeywordB
		fun description(text: String)
	}


	@SchemaBuilderDsl
	interface DirectiveBuilder : NodeBuilder, ArgumentContainer


	@SchemaBuilderDsl
	interface DirectiveDefinitionBuilder : NodeBuilder, ArgumentDefinitionContainer, DescriptionContainer {

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
	interface EnumTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer {

		@SchemaBuilderKeywordB
		fun value(name: String, configure: ValueBuilder.() -> Unit = {})


		@SchemaBuilderDsl
		interface ValueBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer
	}


	@SchemaBuilderDsl
	interface FieldDefinitionBuilder :
		NodeBuilder,
		ArgumentDefinitionContainer,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer


	@SchemaBuilderDsl
	interface FieldDefinitionContainer : TypeRefContainer {

		@SchemaBuilderKeywordB
		fun field(name: NameAndType, configure: FieldDefinitionBuilder.() -> Unit = {})

		@SchemaBuilderKeywordB
		infix fun String.of(type: GTypeRef): NameAndType


		interface NameAndType
	}


	@SchemaBuilderDsl
	interface InputObjectTypeDefinitionBuilder : NodeBuilder, ArgumentDefinitionContainer, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	interface InterfaceTypeDefinitionBuilder :
		NodeBuilder,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer


	interface Interfaces {

		@SchemaBuilderKeywordB
		infix fun and(type: GNamedTypeRef): Interfaces
	}


	object NamedTypeRefFactory {

		@SchemaBuilderType
		operator fun getValue(thisRef: Any?, property: KProperty<*>): GNamedTypeRef =
			GNamedTypeRef(property.name)
	}


	@SchemaBuilderDsl
	interface NodeBuilder {

		@SchemaBuilderKeywordB
		fun <Value : Any> extension(key: GNodeExtensionKey<out Value>): Value?

		@SchemaBuilderKeywordB
		fun <Value : Any> extension(key: GNodeExtensionKey<in Value>, value: Value)
	}


	@SchemaBuilderDsl
	interface ObjectTypeDefinitionBuilder :
		NodeBuilder,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer


	interface PossibleTypes {

		@SchemaBuilderKeywordB
		infix fun or(type: GNamedTypeRef): PossibleTypes
	}


	@SchemaBuilderDsl
	interface ScalarTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer


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
			get() = GIdTypeRef

		@SchemaBuilderBuiltinTypeA
		val Int: GTypeRef
			get() = GIntTypeRef

		@SchemaBuilderBuiltinTypeA
		val String: GTypeRef
			get() = GStringTypeRef

		@SchemaBuilderBuiltinTypeA
		fun List(type: GTypeRef): GTypeRef


		operator fun GTypeRef.not(): GNonNullTypeRef =
			if (this is GNonNullTypeRef)
				error("Cannot use '!' on a type that's already non-null")
			else
				GNonNullTypeRef(this)
	}


	@SchemaBuilderDsl
	interface UnionTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer
}
