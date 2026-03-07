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


/**
 * DSL builder for constructing a [GSchema].
 *
 * Use [GraphQL.schema] as the entry point. Inside the block, declare types and directives
 * using the provided builder functions:
 *
 * ```kotlin
 * val schema = GraphQL.schema {
 *     Query {
 *         "user" of type("User")
 *     }
 *     Object(type("User")) {
 *         "id" of !String
 *         "name" of String
 *     }
 * }
 * ```
 */
@GraphQLMarker
@SchemaBuilderDsl
@Suppress("PropertyName")
public interface GSchemaBuilder {

	/**
	 * Delegate provider that creates a [GNamedTypeRef] from the property name.
	 *
	 * ```kotlin
	 * val User by type
	 * Object(User) { ... }
	 * ```
	 */
	@SchemaBuilderType
	public val type: NamedTypeRefFactory
		get() = NamedTypeRefFactory

	/** Creates a named type reference by string name. */
	@SchemaBuilderType
	public fun type(name: String): GNamedTypeRef =
		GTypeRef(name)

	/** Defines a custom directive with the given [name]. */
	@SchemaBuilderKeywordB
	public fun Directive(name: String, configure: DirectiveDefinitionBuilder.() -> Unit = noOp)

	/** Defines an enum type. */
	@SchemaBuilderType
	public fun Enum(type: GNamedTypeRef, configure: EnumTypeDefinitionBuilder.() -> Unit)

	/** Defines an input object type. */
	@SchemaBuilderType
	public fun InputObject(type: GNamedTypeRef, configure: InputObjectTypeDefinitionBuilder.() -> Unit)

	/** Defines an interface type. */
	@SchemaBuilderType
	public fun Interface(type: GNamedTypeRef, configure: InterfaceTypeDefinitionBuilder.() -> Unit)

	/** Defines an interface type that implements other interfaces, specified via [implements]. */
	@SchemaBuilderType
	public fun Interface(named: Interfaces, configure: InterfaceTypeDefinitionBuilder.() -> Unit)

	/**
	 * Defines the mutation root type using the default name `Mutation`.
	 *
	 * Use [Mutation] with an explicit [GNamedTypeRef] to use a different name.
	 */
	@SchemaBuilderType
	public fun Mutation(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Mutation(type(GLanguage.defaultMutationTypeName), configure)
	}

	/** References an existing type as the mutation root without defining fields inline. */
	@SchemaBuilderType
	public fun Mutation(type: GNamedTypeRef)

	/** Defines the mutation root type with the given [type] name. */
	@SchemaBuilderType
	public fun Mutation(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	/** Defines an object type that implements interfaces, specified via [implements]. */
	@SchemaBuilderType
	public fun Object(named: Interfaces, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	/** Defines an object type. */
	@SchemaBuilderType
	public fun Object(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	/**
	 * Defines the query root type using the default name `Query`.
	 *
	 * Use [Query] with an explicit [GNamedTypeRef] to use a different name.
	 */
	@SchemaBuilderType
	public fun Query(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Query(type(GLanguage.defaultQueryTypeName), configure)
	}

	/** References an existing type as the query root without defining fields inline. */
	@SchemaBuilderType
	public fun Query(type: GNamedTypeRef)

	/** Defines the query root type with the given [type] name. */
	@SchemaBuilderType
	public fun Query(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	/** Defines a custom scalar type. */
	@SchemaBuilderType
	public fun Scalar(type: GNamedTypeRef, configure: ScalarTypeDefinitionBuilder.() -> Unit = noOp)

	/**
	 * Defines the subscription root type using the default name `Subscription`.
	 *
	 * Use [Subscription] with an explicit [GNamedTypeRef] to use a different name.
	 */
	@SchemaBuilderType
	public fun Subscription(configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		Subscription(type(GLanguage.defaultSubscriptionTypeName), configure)
	}

	/** References an existing type as the subscription root without defining fields inline. */
	@SchemaBuilderType
	public fun Subscription(type: GNamedTypeRef)

	/** Defines the subscription root type with the given [type] name. */
	@SchemaBuilderType
	public fun Subscription(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit)

	/**
	 * Defines a union type.
	 *
	 * Use the [with]/[or] DSL to declare the possible types:
	 * `type("SearchResult") with type("Post") or type("User")`
	 */
	@SchemaBuilderType
	public fun Union(named: PossibleTypes, configure: UnionTypeDefinitionBuilder.() -> Unit = noOp)

	/**
	 * Declares that a type implements an interface, producing an [Interfaces] value for use
	 * with [Object] or [Interface].
	 *
	 * ```kotlin
	 * Object(type("Cat") implements type("Animal")) { ... }
	 * ```
	 */
	@SchemaBuilderKeywordB
	public infix fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): Interfaces

	/**
	 * Begins a [PossibleTypes] declaration for use with [Union].
	 *
	 * ```kotlin
	 * Union(type("SearchResult") with type("Post") or type("User"))
	 * ```
	 */
	@SchemaBuilderKeywordB
	public infix fun GNamedTypeRef.with(possibleType: GNamedTypeRef): PossibleTypes


	public companion object {

		private val defaultDeprecatedReason = GLanguage.defaultDeprecatedDirective.argumentDefinition("reason")!!
			.defaultValue.let { it as GStringValue }
			.value
	}


	/** Builder scope for providing argument values inside a directive application. */
	@SchemaBuilderDsl
	public interface ArgumentContainer {

		/** Adds an argument described by the given [name]/value pair. */
		@SchemaBuilderKeywordB
		public fun argument(name: NameAndValue)

		/** Creates a name/value pair for use with [argument]. */
		@SchemaBuilderKeywordB
		public infix fun String.with(value: Value): NameAndValue


		public interface NameAndValue
	}


	/** Builder for a single input argument definition on a field or directive. */
	@SchemaBuilderDsl
	public interface ArgumentDefinitionBuilder : NodeBuilder, DeprecationContainer, DescriptionContainer, DirectiveContainer


	/**
	 * DSL scope for declaring argument definitions on a field or directive.
	 *
	 * Use `"name" of Type` to declare an argument without a default value, and chain
	 * `.default(...)` to provide one:
	 *
	 * ```kotlin
	 * argument("limit" of Int)
	 * argument("status" of type("Status") default value("ACTIVE"))
	 * ```
	 */
	@SchemaBuilderDsl
	public interface ArgumentDefinitionContainer : TypeRefContainer, ValueContainer {

		/** Declares an argument with a name and type. */
		@SchemaBuilderKeywordB
		public fun argument(name: NameAndType, configure: ArgumentDefinitionBuilder.() -> Unit = noOp)

		/** Declares an argument with a name, type, and default value. */
		@SchemaBuilderKeywordB
		public fun argument(name: NameAndTypeAndDefault, configure: ArgumentDefinitionBuilder.() -> Unit = noOp)

		/** Creates a name/type pair for use with [argument]. */
		@SchemaBuilderKeywordB
		public infix fun String.of(type: GTypeRef): NameAndType


		public interface NameAndType {

			/** Provides a default value, producing a [NameAndTypeAndDefault]. */
			@SchemaBuilderKeywordB
			public infix fun default(default: Value): NameAndTypeAndDefault
		}

		public interface NameAndTypeAndDefault
	}


	/** DSL scope for marking a schema element as deprecated. */
	@SchemaBuilderDsl
	public interface DeprecationContainer {

		/**
		 * Marks this element as deprecated with an optional [reason].
		 *
		 * When [reason] is `null`, no reason text is included. Defaults to the standard
		 * GraphQL deprecation message.
		 */
		@SchemaBuilderKeywordB
		public fun deprecated(reason: String? = defaultDeprecatedReason)
	}


	/** DSL scope for adding a description to a schema element. */
	@SchemaBuilderDsl
	public interface DescriptionContainer {

		/** Sets the description for this schema element. */
		@SchemaBuilderKeywordB
		public fun description(text: String)
	}


	/** Builder for a single directive application, allowing argument values to be set. */
	@SchemaBuilderDsl
	public interface DirectiveBuilder : NodeBuilder, ArgumentContainer, ValueContainer


	/**
	 * Builder for a custom directive definition.
	 *
	 * Use the [on] function with one or more [DirectiveLocation] values to specify where the
	 * directive may be applied:
	 *
	 * ```kotlin
	 * Directive("auth") {
	 *     on(FIELD_DEFINITION or OBJECT)
	 * }
	 * ```
	 */
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


		/** Declares that the directive may appear at the given [DirectiveLocation]. */
		@SchemaBuilderKeywordB
		public fun on(any: DirectiveLocation)

		/** Declares that the directive may appear at any of the given locations. */
		@SchemaBuilderKeywordB
		public fun on(any: DirectiveLocationSet)


		/** A single valid location for a directive definition. */
		public interface DirectiveLocation {

			/** Combines this location with [other] into a [DirectiveLocationSet]. */
			@SchemaBuilderKeywordB
			public infix fun or(other: DirectiveLocation): DirectiveLocationSet
		}


		/** A set of valid locations for a directive definition. */
		public interface DirectiveLocationSet {

			/** Adds [other] to this location set. */
			@SchemaBuilderKeywordB
			public infix fun or(other: DirectiveLocation): DirectiveLocationSet
		}
	}


	/** DSL scope for applying directives to a schema element. */
	@SchemaBuilderDsl
	public interface DirectiveContainer {

		/** Applies the directive with the given [name] to this schema element. */
		@SchemaBuilderKeywordB
		public fun directive(name: String, configure: DirectiveBuilder.() -> Unit = noOp)
	}


	/** Builder for an enum type definition. */
	@SchemaBuilderDsl
	public interface EnumTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer {

		/** Declares an enum value with the given [name]. */
		@SchemaBuilderKeywordB
		public fun value(name: String, configure: ValueBuilder.() -> Unit = noOp)


		/** Builder for a single enum value. */
		@SchemaBuilderDsl
		public interface ValueBuilder : DeprecationContainer, DescriptionContainer, DirectiveContainer
	}


	/** Builder for a field definition on an object or interface type. */
	@SchemaBuilderDsl
	public interface FieldDefinitionBuilder :
		NodeBuilder,
		ArgumentDefinitionContainer,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer


	/**
	 * DSL scope for declaring fields on an object or interface type.
	 *
	 * Use `"fieldName" of Type` to declare a field:
	 *
	 * ```kotlin
	 * field("id" of !String)
	 * field("items" of List(!type("Item"))) {
	 *     argument("limit" of Int)
	 * }
	 * ```
	 */
	@SchemaBuilderDsl
	public interface FieldDefinitionContainer : TypeRefContainer {

		/** Declares a field with the given name and type. */
		@SchemaBuilderKeywordB
		public fun field(name: NameAndType, configure: FieldDefinitionBuilder.() -> Unit = noOp)

		/** Creates a name/type pair for use with [field]. */
		@SchemaBuilderKeywordB
		public infix fun String.of(type: GTypeRef): NameAndType


		public interface NameAndType
	}


	/** Builder for an input object type definition. Fields are declared via [ArgumentDefinitionContainer]. */
	@SchemaBuilderDsl
	public interface InputObjectTypeDefinitionBuilder : NodeBuilder, ArgumentDefinitionContainer, DescriptionContainer, DirectiveContainer


	/** Builder for an interface type definition. */
	@SchemaBuilderDsl
	public interface InterfaceTypeDefinitionBuilder :
		NodeBuilder,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer


	/**
	 * Represents a type and one or more interfaces it implements.
	 *
	 * Constructed via [implements] and extended with [and]:
	 * `type("Dog") implements type("Animal") and type("Pet")`
	 */
	public interface Interfaces {

		/** Adds another interface to the list. */
		@SchemaBuilderKeywordB
		public infix fun and(type: GNamedTypeRef): Interfaces
	}


	/**
	 * Delegate provider that derives a [GNamedTypeRef] from the delegating property's name.
	 *
	 * Accessed via the [type] property: `val User by type`
	 */
	public object NamedTypeRefFactory {

		@SchemaBuilderType
		public operator fun getValue(thisRef: Any?, property: KProperty<*>): GNamedTypeRef =
			GTypeRef(property.name)
	}


	/** Provides access to AST node extension data during schema construction. */
	@SchemaBuilderDsl
	public interface NodeBuilder {

		/** Reads a custom extension value associated with [key] from this AST node. */
		@SchemaBuilderKeywordB
		public fun <Value : Any> extension(key: GNodeExtensionKey<out Value>): Value?

		/** Attaches a custom extension [value] associated with [key] to this AST node. */
		@SchemaBuilderKeywordB
		public fun <Value : Any> extension(key: GNodeExtensionKey<in Value>, value: Value)
	}


	/** Builder for an object type definition. */
	@SchemaBuilderDsl
	public interface ObjectTypeDefinitionBuilder :
		NodeBuilder,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		FieldDefinitionContainer


	/**
	 * Represents a union name and its possible member types.
	 *
	 * Constructed via [with] and extended with [or]:
	 * `type("SearchResult") with type("Post") or type("User")`
	 */
	public interface PossibleTypes {

		/** Adds another possible type to the union. */
		@SchemaBuilderKeywordB
		public infix fun or(type: GNamedTypeRef): PossibleTypes
	}


	/** Builder for a scalar type definition. */
	@SchemaBuilderDsl
	public interface ScalarTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer


	/**
	 * Provides built-in GraphQL scalar type references and list/non-null constructors.
	 *
	 * Use `!Type` to make a type non-null, and `List(Type)` to wrap it in a list.
	 *
	 * ```kotlin
	 * "id" of !String
	 * "tags" of List(!String)
	 * ```
	 */
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


		/**
		 * Wraps a type reference as non-null.
		 *
		 * Throws if already non-null.
		 */
		public operator fun GTypeRef.not(): GNonNullTypeRef =
			if (this is GNonNullTypeRef)
				error("Cannot use '!' on a type that's already non-null")
			else
				GNonNullTypeRef(this)
	}


	/** Builder for a union type definition. */
	@SchemaBuilderDsl
	public interface UnionTypeDefinitionBuilder : NodeBuilder, DescriptionContainer, DirectiveContainer


	/** A wrapped [GValue] for use as an argument or default value in the schema DSL. */
	@SchemaBuilderDsl
	public interface Value {

		/** Returns the underlying [GValue]. */
		public fun toGValue(): GValue
	}


	/** DSL scope for producing [Value] wrappers from raw [GValue] nodes. */
	@SchemaBuilderDsl
	public interface ValueContainer {

		/** Wraps the given [GValue] as a [Value] for use in argument or default value positions. */
		public fun value(value: GValue): Value
	}
}


// FIXME add DSL for object values

/** Creates a [GSchemaBuilder.Value] for the given enum value name, or null. */
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


/**
 * Builds a [GSchema] using the [GSchemaBuilder] DSL.
 *
 * ```kotlin
 * val schema = GraphQL.schema {
 *     Query {
 *         "hello" of String
 *     }
 * }
 * ```
 */
@SchemaBuilderKeywordB
@Suppress("UnusedReceiverParameter")
public fun GraphQL.schema(configure: GSchemaBuilder.() -> Unit): GSchema =
	DefaultSchemaBuilder().apply(configure).build()


// https://youtrack.jetbrains.com/issue/KT-40083
private val noOp: Any?.() -> Unit = {}
