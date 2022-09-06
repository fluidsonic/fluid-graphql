package io.fluidsonic.graphql

import kotlin.jvm.*
import kotlin.reflect.*

// TODO Rework this into GraphQL* types.

// The DSL color in IntelliJ IDEA depends on the hash code of the FQN of marker class.
// Therefore, we add a random letter to get the desired "DSL style number" (for now).

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class SchemaBuilderKeywordB

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class SchemaBuilderBuiltinTypeA

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class SchemaBuilderType

@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class SchemaBuilderDsl


@GraphQLMarker
@SchemaBuilderDsl
@Suppress("PropertyName")
public interface GSchemaBuilder {

	@SchemaBuilderType
	public val type: NamedTypeRefFactory
		get() = NamedTypeRefFactory

	@SchemaBuilderType
	public fun type(name: String): GNamedTypeRef =
		GTypeRef(name)

	@SchemaBuilderKeywordB
	public fun Directive(name: String, configure: DirectiveDefinitionBuilder.() -> Unit = noOp)

	@SchemaBuilderType
	public fun Enum(type: GNamedTypeRef, configure: EnumTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun InputObject(type: GNamedTypeRef, configure: InputObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Interface(type: GNamedTypeRef, configure: InterfaceTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Interface(named: Interfaces, configure: InterfaceTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Mutation(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Mutation(type(GLanguage.defaultMutationTypeName), configure)
	}

	@SchemaBuilderType
	public fun Mutation(type: GNamedTypeRef)

	@SchemaBuilderType
	public fun Mutation(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Object(named: Interfaces, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Object(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Query(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Query(type(GLanguage.defaultQueryTypeName), configure)
	}

	@SchemaBuilderType
	public fun Query(type: GNamedTypeRef)

	@SchemaBuilderType
	public fun Query(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Scalar(type: GNamedTypeRef, configure: ScalarTypeDefinitionBuilder.() -> Unit = noOp)

	@SchemaBuilderType
	public fun Subscription(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Subscription(type(GLanguage.defaultSubscriptionTypeName), configure)
	}

	@SchemaBuilderType
	public fun Subscription(type: GNamedTypeRef)

	@SchemaBuilderType
	public fun Subscription(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	@SchemaBuilderType
	public fun Union(named: PossibleTypes, configure: UnionTypeDefinitionBuilder.() -> Unit = noOp)

	@SchemaBuilderKeywordB
	public infix fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): Interfaces

	@SchemaBuilderKeywordB
	public infix fun GNamedTypeRef.with(possibleType: GNamedTypeRef): PossibleTypes


	public companion object {

		private val defaultDeprecatedReason = GLanguage.defaultDeprecatedDirective.argumentDefinition("reason")!!
			.defaultValue.let { it as GStringValue }
			.value
	}


	@SchemaBuilderDsl
	public interface ArgumentContainer {

		@SchemaBuilderKeywordB
		public fun argument(name: NameAndValue)

		@SchemaBuilderKeywordB
		public infix fun String.with(value: Value): NameAndValue


		public interface NameAndValue
	}


	@SchemaBuilderDsl
	public interface ArgumentDefinitionBuilder : NodeBuilder, DeprecationContainer, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	public interface ArgumentDefinitionContainer : TypeRefContainer, ValueContainer {

		@SchemaBuilderKeywordB
		public fun argument(name: NameAndType, configure: ArgumentDefinitionBuilder.() -> Unit = noOp)

		@SchemaBuilderKeywordB
		public fun argument(name: NameAndTypeAndDefault, configure: ArgumentDefinitionBuilder.() -> Unit = noOp)

		@SchemaBuilderKeywordB
		public infix fun String.of(type: GTypeRef): NameAndType


		public interface NameAndType {

			@SchemaBuilderKeywordB
			public infix fun default(default: Value): NameAndTypeAndDefault
		}

		public interface NameAndTypeAndDefault
	}


	@SchemaBuilderDsl
	public interface DeprecationContainer {

		@SchemaBuilderKeywordB
		public fun deprecated(reason: String? = defaultDeprecatedReason)
	}


	@SchemaBuilderDsl
	public interface DescriptionContainer {

		@SchemaBuilderKeywordB
		public fun description(text: String)
	}


	@SchemaBuilderDsl
	public interface DirectiveBuilder : NodeBuilder, ArgumentContainer, ValueContainer


	@SchemaBuilderDsl
	public interface DirectiveDefinitionBuilder : NodeBuilder, ArgumentDefinitionContainer, DescriptionContainer {

		@SchemaBuilderBuiltinTypeA
		public val ARGUMENT_DEFINITION: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val ENUM: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val ENUM_VALUE: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val FIELD: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val FIELD_DEFINITION: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val FRAGMENT_DEFINITION: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val FRAGMENT_SPREAD: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val INLINE_FRAGMENT: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val INPUT_FIELD_DEFINITION: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val INPUT_OBJECT: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val INTERFACE: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val MUTATION: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val OBJECT: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val QUERY: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val SCALAR: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val SCHEMA: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val SUBSCRIPTION: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val UNION: DirectiveLocation

		@SchemaBuilderBuiltinTypeA
		public val VARIABLE_DEFINITION: DirectiveLocation


		@SchemaBuilderKeywordB
		public fun on(any: DirectiveLocation)

		@SchemaBuilderKeywordB
		public fun on(any: DirectiveLocationSet)


		public interface DirectiveLocation {

			@SchemaBuilderKeywordB
			public infix fun or(other: DirectiveLocation): DirectiveLocationSet
		}


		public interface DirectiveLocationSet {

			@SchemaBuilderKeywordB
			public infix fun or(other: DirectiveLocation): DirectiveLocationSet
		}
	}


	@SchemaBuilderDsl
	public interface DirectiveContainer {

		@SchemaBuilderKeywordB
		public fun directive(name: String, configure: DirectiveBuilder.() -> Unit = noOp)
	}


	@SchemaBuilderDsl
	public interface EnumTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer {

		@SchemaBuilderKeywordB
		public fun value(name: String, configure: ValueBuilder.() -> Unit = noOp)


		@SchemaBuilderDsl
		public interface ValueBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer
	}


	@SchemaBuilderDsl
	public interface FieldDefinitionBuilder :
		NodeBuilder,
		ArgumentDefinitionContainer,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer


	@SchemaBuilderDsl
	public interface FieldDefinitionContainer : TypeRefContainer {

		@SchemaBuilderKeywordB
		public fun field(name: NameAndType, configure: FieldDefinitionBuilder.() -> Unit = noOp)

		@SchemaBuilderKeywordB
		public infix fun String.of(type: GTypeRef): NameAndType


		public interface NameAndType
	}


	@SchemaBuilderDsl
	public interface InputObjectTypeDefinitionBuilder : NodeBuilder, ArgumentDefinitionContainer, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	public interface InterfaceTypeDefinitionBuilder :
		NodeBuilder,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer


	public interface Interfaces {

		@SchemaBuilderKeywordB
		public infix fun and(type: GNamedTypeRef): Interfaces
	}


	public object NamedTypeRefFactory {

		@SchemaBuilderType
		public operator fun getValue(thisRef: Any?, property: KProperty<*>): GNamedTypeRef =
			GTypeRef(property.name)
	}


	@SchemaBuilderDsl
	public interface NodeBuilder {

		@SchemaBuilderKeywordB
		public fun <Value : Any> extension(key: GNodeExtensionKey<out Value>): Value?

		@SchemaBuilderKeywordB
		public fun <Value : Any> extension(key: GNodeExtensionKey<in Value>, value: Value)
	}


	@SchemaBuilderDsl
	public interface ObjectTypeDefinitionBuilder :
		NodeBuilder,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer


	public interface PossibleTypes {

		@SchemaBuilderKeywordB
		public infix fun or(type: GNamedTypeRef): PossibleTypes
	}


	@SchemaBuilderDsl
	public interface ScalarTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	@Suppress("PropertyName")
	public interface TypeRefContainer {

		@SchemaBuilderBuiltinTypeA
		public val Boolean: GTypeRef
			get() = GBooleanTypeRef

		@SchemaBuilderBuiltinTypeA
		public val Float: GTypeRef
			get() = GFloatTypeRef

		@SchemaBuilderBuiltinTypeA
		public val ID: GTypeRef
			get() = GIdTypeRef

		@SchemaBuilderBuiltinTypeA
		public val Int: GTypeRef
			get() = GIntTypeRef

		@SchemaBuilderBuiltinTypeA
		public val String: GTypeRef
			get() = GStringTypeRef

		@SchemaBuilderBuiltinTypeA
		public fun List(type: GTypeRef): GTypeRef


		public operator fun GTypeRef.not(): GNonNullTypeRef =
			if (this is GNonNullTypeRef)
				error("Cannot use '!' on a type that's already non-null")
			else
				GNonNullTypeRef(this)
	}


	@SchemaBuilderDsl
	public interface UnionTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer


	@SchemaBuilderDsl
	public interface Value {

		public fun toGValue(): GValue
	}


	@SchemaBuilderDsl
	public interface ValueContainer {

		public fun value(value: GValue): Value
	}
}


// FIXME add DSL for object values

@JvmName("floatValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.enum(value: String?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GEnumValue(value))
	}


@JvmName("nullValue")
@SchemaBuilderKeywordB
@Suppress("DEPRECATION_ERROR", "UNUSED_PARAMETER")
public fun GSchemaBuilder.ValueContainer.value(value: Nothing?): GSchemaBuilder.Value =
	value(GNullValue())


@JvmName("booleanValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: Boolean?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GBooleanValue(value))
	}


@JvmName("floatValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: Double?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GFloatValue(value))
	}


@JvmName("intValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: Int?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GIntValue(value))
	}


@JvmName("stringValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: String?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GStringValue(value))
	}


@JvmName("nullListValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: List<Nothing>?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GListValue(value.map { GNullValue() }))
	}


@JvmName("booleanListValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: List<Boolean>?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GListValue(value.map(::GBooleanValue)))
	}


@JvmName("floatListValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: List<Double>?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GListValue(value.map(::GFloatValue)))
	}


@JvmName("intListValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: List<Int>?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GListValue(value.map(::GIntValue)))
	}


@JvmName("stringListValue")
@SchemaBuilderKeywordB
public fun GSchemaBuilder.ValueContainer.value(value: List<String>?): GSchemaBuilder.Value =
	when (value) {
		null -> value(value)
		else -> value(GListValue(value.map(::GStringValue)))
	}


@SchemaBuilderKeywordB
@Suppress("UnusedReceiverParameter")
public fun GraphQL.schema(configure: GSchemaBuilder.() -> Unit): GSchema =
	DefaultSchemaBuilder().apply(configure).build()


// https://youtrack.jetbrains.com/issue/KT-40083
private val noOp: Any?.() -> Unit = {}
