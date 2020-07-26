package io.fluidsonic.graphql

import kotlin.jvm.*
import kotlin.reflect.*


// FIXME Add DSL for setting input & output coercers.
object GSchemaBuilderExecutionExtensions {

	class FieldDefinitionBuilder<ParentKotlinType : Any> internal constructor(
		delegate: GSchemaBuilder.FieldDefinitionBuilder
	) : GSchemaBuilder.FieldDefinitionBuilder by delegate {

		@SchemaBuilderKeywordB
		fun <Result> resolve(
			resolver: suspend GFieldResolverContext<Any>.(context: ParentKotlinType) -> Result // FIXME env
		) {
			require(extension(FieldDefinitionResolverExtensionKey) == null) { "Only one resolver can be provided." }

			extension(FieldDefinitionResolverExtensionKey, GFieldResolver(resolver))
		}
	}


	class ObjectTypeDefinitionBuilder<KotlinType : Any> internal constructor(
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
		fun field(
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
fun <Result> GSchemaBuilder.FieldDefinitionBuilder.resolve(
	resolver: suspend GFieldResolverContext<Any>.(context: Any) -> Result // FIXME env
) {
	require(extension(FieldDefinitionResolverExtensionKey) == null) { "Only one resolver can be provided." }

	extension(FieldDefinitionResolverExtensionKey, GFieldResolver(resolver))
}


@SchemaBuilderType
fun <KotlinType : Any> GSchemaBuilder.Object(
	named: GSchemaBuilder.Interfaces,
	kotlinType: KClass<KotlinType>,
	configure: GSchemaBuilderExecutionExtensions.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(named = named) {
		GSchemaBuilderExecutionExtensions.ObjectTypeDefinitionBuilder<KotlinType>(delegate = this).apply(configure)

		extension(ObjectKotlinTypeNodeExtensionKey, kotlinType)
	}
}


@SchemaBuilderType
fun <KotlinType : Any> GSchemaBuilder.Object(
	type: GNamedTypeRef,
	kotlinType: KClass<KotlinType>,
	configure: GSchemaBuilderExecutionExtensions.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(type = type) {
		GSchemaBuilderExecutionExtensions.ObjectTypeDefinitionBuilder<KotlinType>(delegate = this).apply(configure)

		extension(ObjectKotlinTypeNodeExtensionKey, kotlinType)
	}
}


@SchemaBuilderType
inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	type: GNamedTypeRef,
	noinline configure: GSchemaBuilderExecutionExtensions.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(type = type, kotlinType = KotlinType::class, configure = configure)
}


@SchemaBuilderType
inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	named: GSchemaBuilder.Interfaces,
	noinline configure: GSchemaBuilderExecutionExtensions.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(named = named, kotlinType = KotlinType::class, configure = configure)
}
