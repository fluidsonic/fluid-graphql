package io.fluidsonic.graphql

import kotlin.jvm.*
import kotlin.reflect.*


// FIXME Add DSL for setting input & output coercers.
public object GDslForExecution {

	public class FieldDefinitionBuilder<ParentKotlinType : Any> internal constructor(
		delegate: GSchemaBuilder.FieldDefinitionBuilder
	) : GSchemaBuilder.FieldDefinitionBuilder by delegate {

		@SchemaBuilderKeywordB
		public fun <Result> resolve(
			resolver: suspend GFieldResolverContext.(context: ParentKotlinType) -> Result
		) {
			require(extension(FieldDefinitionResolverExtensionKey) == null) { "Only one resolver can be provided." }

			extension(FieldDefinitionResolverExtensionKey, GFieldResolver(resolver))
		}
	}


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


		@JvmName("fieldWithType")
		@SchemaBuilderKeywordB
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


@SchemaBuilderKeywordB
public fun <Result> GSchemaBuilder.FieldDefinitionBuilder.resolve(
	resolver: suspend GFieldResolverContext.(context: Any) -> Result
) {
	require(extension(FieldDefinitionResolverExtensionKey) == null) { "Only one resolver can be provided." }

	extension(FieldDefinitionResolverExtensionKey, GFieldResolver(resolver))
}


@SchemaBuilderType
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


@SchemaBuilderType
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


@SchemaBuilderType
public inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	type: GNamedTypeRef,
	noinline configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(type = type, kotlinType = KotlinType::class, configure = configure)
}


@SchemaBuilderType
public inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	named: GSchemaBuilder.Interfaces,
	noinline configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(named = named, kotlinType = KotlinType::class, configure = configure)
}


@SchemaBuilderKeywordB
public fun <Result> GSchemaBuilder.ScalarTypeDefinitionBuilder.coerceNodeInput(
	coercer: GNodeInputCoercerContext.(input: GValue) -> Result
) {
	require(extension(LeafTypeNodeInputCoercerExtensionKey) == null) { "Only one node input coercer can be provided." }

	extension(LeafTypeNodeInputCoercerExtensionKey, GNodeInputCoercer(coercer))
}


@SchemaBuilderKeywordB
public fun <Result : Any> GSchemaBuilder.ScalarTypeDefinitionBuilder.coerceOutput(
	coercer: GOutputCoercerContext.(input: Any) -> Result
) {
	require(extension(LeafTypeOutputCoercerExtensionKey) == null) { "Only one output coercer can be provided." }

	extension(LeafTypeOutputCoercerExtensionKey, GOutputCoercer(coercer))
}


@SchemaBuilderKeywordB
public fun <Result> GSchemaBuilder.ScalarTypeDefinitionBuilder.coerceVariableInput(
	coercer: GVariableInputCoercerContext.(input: Any) -> Result
) {
	require(extension(LeafTypeVariableInputCoercerExtensionKey) == null) { "Only one variable input coercer can be provided." }

	extension(LeafTypeVariableInputCoercerExtensionKey, GVariableInputCoercer(coercer))
}
