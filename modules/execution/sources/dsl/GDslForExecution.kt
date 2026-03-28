package io.fluidsonic.graphql

import kotlin.jvm.*
import kotlin.reflect.*


// FIXME Add DSL for setting input & output coercers.
/**
 * DSL extensions for the schema builder that add execution-specific configuration.
 *
 * Use the typed [Object] overloads on [GSchemaBuilder] to associate a Kotlin type with a
 * GraphQL object type, enabling type-safe field resolvers via [FieldDefinitionBuilder.resolve].
 */
public object GDslForExecution {

	/**
	 * Field definition builder that knows the Kotlin [ParentKotlinType] of the enclosing object.
	 *
	 * Obtained inside a typed [GSchemaBuilder.Object] block. Use [resolve] to attach a resolver.
	 */
	public class FieldDefinitionBuilder<ParentKotlinType : Any> internal constructor(
		delegate: GSchemaBuilder.FieldDefinitionBuilder
	) : GSchemaBuilder.FieldDefinitionBuilder by delegate {

		/** Attaches a field resolver that receives the parent object as [ParentKotlinType]. */
		public fun <Result> resolve(
			resolver: suspend GFieldResolverContext.(context: ParentKotlinType) -> Result
		) {
			require(extension(FieldDefinitionResolverExtensionKey) == null) { "Only one resolver can be provided." }

			extension(FieldDefinitionResolverExtensionKey, GFieldResolver(resolver))
		}
	}


	/**
	 * Object type definition builder that knows the Kotlin [KotlinType] mapped to this GraphQL object.
	 *
	 * Obtained inside a typed [GSchemaBuilder.Object] block. Use [field] to define fields with
	 * type-safe resolvers via [FieldDefinitionBuilder].
	 */
	public class ObjectTypeDefinitionBuilder<KotlinType : Any> internal constructor(
		private val delegate: GSchemaBuilder.ObjectTypeDefinitionBuilder
	) : GSchemaBuilder.ObjectTypeDefinitionBuilder by delegate {

		@Deprecated(message = "Use field() with generic builder.", level = DeprecationLevel.HIDDEN)
		override fun field(
			name: GSchemaBuilder.FieldDefinitionContainer.NameAndType,
			configure: GSchemaBuilder.FieldDefinitionBuilder.() -> Unit
		) {
			delegate.field(name, configure)
		}


		/** Defines a field with a type-safe resolver that receives the parent as [KotlinType]. */
		@JvmName("fieldWithType")
		public fun field(
			name: GSchemaBuilder.FieldDefinitionContainer.NameAndType,
			configure: FieldDefinitionBuilder<KotlinType>.() -> Unit
		) {
			delegate.field(name) {
				FieldDefinitionBuilder<KotlinType>(delegate = this).apply(configure)
			}
		}
	}
}


/**
 * Attaches a field resolver to this field definition.
 *
 * The [resolver] lambda receives the parent object as an untyped `Any`.
 * For a type-safe alternative, use [GDslForExecution.FieldDefinitionBuilder.resolve] inside
 * a typed [GSchemaBuilder.Object] block.
 */
public fun <Result> GSchemaBuilder.FieldDefinitionBuilder.resolve(
	resolver: suspend GFieldResolverContext.(context: Any) -> Result
) {
	require(extension(FieldDefinitionResolverExtensionKey) == null) { "Only one resolver can be provided." }

	extension(FieldDefinitionResolverExtensionKey, GFieldResolver(resolver))
}


/**
 * Defines a GraphQL object type associated with the Kotlin class [kotlinType].
 *
 * Inside [configure], use [GDslForExecution.ObjectTypeDefinitionBuilder.field] to define fields
 * with type-safe resolvers that receive instances of [KotlinType] as the parent.
 */
public fun <KotlinType : Any> GSchemaBuilder.Object(
	named: GSchemaBuilder.Interfaces,
	kotlinType: KClass<KotlinType>,
	configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(named = named) {
		GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>(delegate = this).apply(configure)

		extension(ObjectKotlinTypeNodeExtensionKey, kotlinType)
	}
}


/**
 * Defines a GraphQL object type named by [type] and associated with the Kotlin class [kotlinType].
 *
 * Inside [configure], use [GDslForExecution.ObjectTypeDefinitionBuilder.field] to define fields
 * with type-safe resolvers that receive instances of [KotlinType] as the parent.
 */
public fun <KotlinType : Any> GSchemaBuilder.Object(
	type: GNamedTypeRef,
	kotlinType: KClass<KotlinType>,
	configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(type = type) {
		GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>(delegate = this).apply(configure)

		extension(ObjectKotlinTypeNodeExtensionKey, kotlinType)
	}
}


/** Defines a GraphQL object type named by [type], with [KotlinType] inferred via reified type parameter. */
public inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	type: GNamedTypeRef,
	noinline configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(type = type, kotlinType = KotlinType::class, configure = configure)
}


/** Defines a GraphQL object type with interfaces, with [KotlinType] inferred via reified type parameter. */
public inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	named: GSchemaBuilder.Interfaces,
	noinline configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(named = named, kotlinType = KotlinType::class, configure = configure)
}


/**
 * Attaches an inline input coercer to this scalar type definition.
 *
 * The [coercer] is called when a value for this scalar is provided inline in a document (not via a variable).
 */
public fun <Result> GSchemaBuilder.ScalarTypeDefinitionBuilder.coerceNodeInput(
	coercer: GNodeInputCoercerContext.(input: GValue) -> Result
) {
	require(extension(LeafTypeNodeInputCoercerExtensionKey) == null) { "Only one node input coercer can be provided." }

	extension(LeafTypeNodeInputCoercerExtensionKey, GNodeInputCoercer(coercer))
}


/**
 * Attaches an output coercer to this scalar type definition.
 *
 * The [coercer] is called when a resolver returns a value of this scalar type, converting it
 * to a GraphQL-serializable form.
 */
public fun <Result : Any> GSchemaBuilder.ScalarTypeDefinitionBuilder.coerceOutput(
	coercer: GOutputCoercerContext.(input: Any) -> Result
) {
	require(extension(LeafTypeOutputCoercerExtensionKey) == null) { "Only one output coercer can be provided." }

	extension(LeafTypeOutputCoercerExtensionKey, GOutputCoercer(coercer))
}


/**
 * Attaches a variable input coercer to this scalar type definition.
 *
 * The [coercer] is called when a value for this scalar is provided as a variable.
 */
public fun <Result> GSchemaBuilder.ScalarTypeDefinitionBuilder.coerceVariableInput(
	coercer: GVariableInputCoercerContext.(input: Any) -> Result
) {
	require(extension(LeafTypeVariableInputCoercerExtensionKey) == null) { "Only one variable input coercer can be provided." }

	extension(LeafTypeVariableInputCoercerExtensionKey, GVariableInputCoercer(coercer))
}
