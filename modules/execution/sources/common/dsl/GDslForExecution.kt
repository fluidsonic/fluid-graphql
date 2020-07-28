package io.fluidsonic.graphql

import kotlin.jvm.*
import kotlin.reflect.*


// FIXME Add DSL for setting input & output coercers.
object GDslForExecution {

	class FieldDefinitionBuilder<ParentKotlinType : Any> internal constructor(
		delegate: GSchemaBuilder.FieldDefinitionBuilder
	) : GSchemaBuilder.FieldDefinitionBuilder by delegate {

		@SchemaBuilderKeywordB
		fun <Result> resolve(
			resolver: suspend GFieldResolverContext.(context: ParentKotlinType) -> Result
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
	resolver: suspend GFieldResolverContext.(context: Any) -> Result
) {
	require(extension(FieldDefinitionResolverExtensionKey) == null) { "Only one resolver can be provided." }

	extension(FieldDefinitionResolverExtensionKey, GFieldResolver(resolver))
}


@SchemaBuilderType
fun <KotlinType : Any> GSchemaBuilder.Object(
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
fun <KotlinType : Any> GSchemaBuilder.Object(
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
inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	type: GNamedTypeRef,
	noinline configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(type = type, kotlinType = KotlinType::class, configure = configure)
}


@SchemaBuilderType
inline fun <reified KotlinType : Any> GSchemaBuilder.Object(
	named: GSchemaBuilder.Interfaces,
	noinline configure: GDslForExecution.ObjectTypeDefinitionBuilder<KotlinType>.() -> Unit
) {
	Object(named = named, kotlinType = KotlinType::class, configure = configure)
}
