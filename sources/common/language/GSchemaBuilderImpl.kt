package io.fluidsonic.graphql

import kotlin.jvm.*
import kotlin.reflect.*


// FIXME change to schemaDocument as schema will be needed within for directives & op type definitions
@JvmName("schemaWithoutContext")
@SchemaBuilderKeywordB
fun schema(configure: GSchemaBuilder<Nothing>.() -> Unit) =
	GSchemaBuilderImpl<Nothing>().apply(configure).build()


// FIXME change to schemaDocument as schema will be needed within for directives & op type definitions
@SchemaBuilderKeywordB
fun <Context : Any> schema(configure: GSchemaBuilder<Context>.() -> Unit) =
	GSchemaBuilderImpl<Context>().apply(configure).build()


internal class GSchemaBuilderImpl<out Environment : Any> : GSchemaBuilder<Environment>() {

	private val definitions = mutableListOf<GTypeSystemDefinition>()
	private var mutationType: GNamedTypeRef? = null
	private var queryType: GNamedTypeRef? = null
	private var subscriptionType: GNamedTypeRef? = null


	fun build(): GSchema {
		if (definitions.isEmpty())
			error("Cannot create an empty schema.")

		val definitions = definitions.toMutableList()
		if (mutationType !== null || queryType !== null || subscriptionType !== null)
			definitions.add(0, GSchemaDefinition(
				operationTypeDefinitions = listOfNotNull(
					queryType?.let { GOperationTypeDefinition(operationType = GOperationType.query, type = it) },
					mutationType?.let { GOperationTypeDefinition(operationType = GOperationType.mutation, type = it) },
					subscriptionType?.let { GOperationTypeDefinition(operationType = GOperationType.subscription, type = it) }
				)
			))

		return GSchema(
			document = GDocument(
				definitions = definitions
			)
		)
	}


	override fun Directive(name: String, configure: DirectiveDefinitionBuilder.() -> Unit) {
		definitions += DirectiveDefinitionBuilderImpl<Environment>(name = name).apply(configure).build()
	}


	override fun Enum(type: GNamedTypeRef, configure: EnumTypeDefinitionBuilder.() -> Unit) {
		definitions += EnumTypeDefinitionBuilderImpl<Environment>(name = type.name).apply(configure).build()
	}


	override fun InputObject(type: GNamedTypeRef, configure: InputObjectTypeDefinitionBuilder.() -> Unit) {
		definitions += InputObjectTypeDefinitionBuilderImpl<Environment>(name = type.name).apply(configure).build()
	}


	override fun Interface(type: GNamedTypeRef, configure: InterfaceTypeDefinitionBuilder.() -> Unit) {
		definitions += InterfaceTypeDefinitionBuilderImpl<Environment>(
			interfaces = emptyList(),
			name = type.name
		).apply(configure).build()
	}


	override fun Interface(named: Interfaces, configure: InterfaceTypeDefinitionBuilder.() -> Unit) {
		named as InterfacesImpl

		definitions += InterfaceTypeDefinitionBuilderImpl<Environment>(
			interfaces = named.interfaces,
			name = named.name
		).apply(configure).build()
	}


	override fun Mutation(type: GNamedTypeRef) {
		mutationType = type
	}


	override fun Mutation(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder<Environment, Unit>.() -> Unit) {
		mutationType = type

		Object(type, configure)
	}


	override fun <KotlinType : Any> Object(
		type: GNamedTypeRef,
		kotlinType: KClass<KotlinType>,
		configure: ObjectTypeDefinitionBuilder<Environment, KotlinType>.() -> Unit
	) {
		definitions += ObjectTypeDefinitionBuilderImpl<Environment, KotlinType>(
			interfaces = emptyList(),
			kotlinType = kotlinType,
			name = type.name
		).apply(configure).build()
	}


	override fun <KotlinType : Any> Object(
		named: Interfaces,
		kotlinType: KClass<KotlinType>,
		configure: ObjectTypeDefinitionBuilder<Environment, KotlinType>.() -> Unit
	) {
		named as InterfacesImpl

		definitions += ObjectTypeDefinitionBuilderImpl<Environment, KotlinType>(
			interfaces = named.interfaces,
			kotlinType = kotlinType,
			name = named.name
		).apply(configure).build()
	}


	override fun Query(type: GNamedTypeRef) {
		queryType = type
	}


	override fun Query(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder<Environment, Unit>.() -> Unit) {
		queryType = type

		Object(type, configure)
	}


	override fun Scalar(type: GNamedTypeRef, configure: ScalarTypeDefinitionBuilder.() -> Unit) {
		definitions += ScalarTypeDefinitionBuilderImpl<Environment>(name = type.name).apply(configure).build()
	}


	override fun Subscription(type: GNamedTypeRef) {
		subscriptionType = type
	}


	override fun Subscription(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder<Environment, Unit>.() -> Unit) {
		subscriptionType = type

		Object(type, configure)
	}


	override fun Union(named: PossibleTypes, configure: UnionTypeDefinitionBuilder.() -> Unit) {
		@Suppress("UNCHECKED_CAST")
		definitions += (named as UnionTypeDefinitionBuilderImpl<Environment>).apply(configure).build()
	}


	override fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): Interfaces =
		InterfacesImpl(
			interfaceType = interfaceType,
			name = name
		)


	override fun GNamedTypeRef.with(possibleType: GNamedTypeRef): PossibleTypes =
		UnionTypeDefinitionBuilderImpl<Environment>(
			name = name,
			possibleType = possibleType
		)


	private class ArgumentBuilderImpl(
		var name: String,
		var value: GValue
	) : ArgumentContainer.NameAndValue {

		fun build() = GArgument(
			name = name,
			value = value
		)
	}


	private class ArgumentDefinitionBuilderImpl<out Environment : Any>(
		val name: String,
		val type: GTypeRef,
		val definitionType: ArgumentDefinitionType,
		var defaultValue: GValue? = null
	) : ContainerImpl<Environment>(),
		ArgumentDefinitionBuilder,
		ArgumentDefinitionContainer.NameAndType,
		ArgumentDefinitionContainer.NameAndTypeAndDefault {

		fun build() = when (definitionType) {
			ArgumentDefinitionType.directiveDefinition ->
				GDirectiveArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					type = type
				)

			ArgumentDefinitionType.fieldDefinition ->
				GFieldArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					type = type
				)

			ArgumentDefinitionType.inputField ->
				GInputObjectArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					type = type
				)
		}


		override fun default(default: Any?) = apply {
			defaultValue =
				if (default === null) GNullValue.withoutOrigin
				else GValue.of(default) ?: error("Value is not a valid GraphQL value: $default (${default::class})")
		}
	}


	private enum class ArgumentDefinitionType {

		directiveDefinition,
		fieldDefinition,
		inputField
	}


	private open class ContainerImpl<out Environment : Any> :
		ArgumentContainer,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		TypeRefContainer {

		protected val argumentDefinitions = mutableListOf<GArgumentDefinition>()
		protected val arguments = mutableListOf<GArgument>()
		protected var description: String? = null
		protected val directives = mutableListOf<GDirective>()


		override fun argument(name: ArgumentContainer.NameAndValue) {
			arguments += (name as ArgumentBuilderImpl).build()
		}


		fun argument(name: ArgumentDefinitionContainer.NameAndType, configure: ArgumentDefinitionBuilder.() -> Unit) {
			@Suppress("UNCHECKED_CAST")
			argumentDefinitions += (name as ArgumentDefinitionBuilderImpl<Environment>).apply(configure).build()
		}


		fun argument(name: ArgumentDefinitionContainer.NameAndTypeAndDefault, configure: ArgumentDefinitionBuilder.() -> Unit) {
			@Suppress("UNCHECKED_CAST")
			argumentDefinitions += (name as ArgumentDefinitionBuilderImpl<Environment>).apply(configure).build()
		}


		// FIXME what if custom @deprecated is provided?
		override fun deprecated(reason: String?) {
			require(directives.none { it.name == GSpecification.defaultDeprecatedDirective.name }) {
				"Cannot use deprecate() multiple times on the same element"
			}

			directives += GDirective(
				name = GSpecification.defaultDeprecatedDirective.name,
				arguments = listOf(
					GArgument(name = "reason", value = reason?.let { GStringValue(it) } ?: GNullValue.withoutOrigin) // FIXME null vs not-specified
				)
			)
		}


		override fun description(text: String) {
			description = text.ifEmpty { null }
		}


		override fun directive(name: String, configure: DirectiveBuilder.() -> Unit) {
			directives += DirectiveBuilderImpl<Environment>(name = name).apply(configure).build()
		}


		fun String.ofArgumentDefinitionType(type: GTypeRef, definitionType: ArgumentDefinitionType) =
			ArgumentDefinitionBuilderImpl<Environment>(name = this, type = type, definitionType = definitionType)


		fun String.ofFieldDefinitionType(type: GTypeRef) =
			FieldDefinitionBuilderImpl<Any, Environment>(name = this, type = type)


		override fun String.with(value: Any?) =
			ArgumentBuilderImpl(
				name = this,
				value = (
					if (value === null) GNullValue.withoutOrigin
					else GValue.of(value) ?: error("Value is not a valid GraphQL value: $value (${value::class})")
					)
			)


		override fun List(type: GTypeRef) =
			GListTypeRef(type)
	}


	private class DirectiveBuilderImpl<out Environment : Any>(
		val name: String
	) : ContainerImpl<Environment>(),
		DirectiveBuilder {

		fun build() = GDirective(
			arguments = arguments,
			name = name
		)
	}


	private class DirectiveDefinitionBuilderImpl<out Environment : Any>(
		val name: String
	) : ContainerImpl<Environment>(),
		DirectiveDefinitionBuilder {

		private var locations = emptySet<GDirectiveLocation>()


		@Suppress("UNCHECKED_CAST")
		fun build() = GDirectiveDefinition(
			argumentDefinitions = argumentDefinitions as List<GDirectiveArgumentDefinition>,
			description = description,
			locations = locations,
			name = name
		)

		override val ARGUMENT_DEFINITION get() = Companion.ARGUMENT_DEFINITION
		override val ENUM get() = Companion.ENUM
		override val ENUM_VALUE get() = Companion.ENUM_VALUE
		override val FIELD get() = Companion.FIELD
		override val FIELD_DEFINITION get() = Companion.FIELD_DEFINITION
		override val FRAGMENT_DEFINITION get() = Companion.FRAGMENT_DEFINITION
		override val FRAGMENT_SPREAD get() = Companion.FRAGMENT_SPREAD
		override val INLINE_FRAGMENT get() = Companion.INLINE_FRAGMENT
		override val INPUT_FIELD_DEFINITION get() = Companion.INPUT_FIELD_DEFINITION
		override val INPUT_OBJECT get() = Companion.INPUT_OBJECT
		override val INTERFACE get() = Companion.INTERFACE
		override val MUTATION get() = Companion.MUTATION
		override val OBJECT get() = Companion.OBJECT
		override val QUERY get() = Companion.QUERY
		override val SCALAR get() = Companion.SCALAR
		override val SCHEMA get() = Companion.SCHEMA
		override val SUBSCRIPTION get() = Companion.SUBSCRIPTION
		override val UNION get() = Companion.UNION
		override val VARIABLE_DEFINITION get() = Companion.VARIABLE_DEFINITION


		override fun on(any: DirectiveDefinitionBuilder.DirectiveLocation) {
			locations = (any as DirectiveLocationSetImpl).locations
		}


		override fun on(any: DirectiveDefinitionBuilder.DirectiveLocationSet) {
			locations = (any as DirectiveLocationSetImpl).locations
		}


		override fun String.of(type: GTypeRef) =
			ofArgumentDefinitionType(type, definitionType = ArgumentDefinitionType.directiveDefinition)


		class DirectiveLocationSetImpl(val locations: Set<GDirectiveLocation>) :
			DirectiveDefinitionBuilder.DirectiveLocation,
			DirectiveDefinitionBuilder.DirectiveLocationSet {

			override fun or(other: DirectiveDefinitionBuilder.DirectiveLocation) =
				DirectiveLocationSetImpl(locations + (other as DirectiveLocationSetImpl).locations)
		}


		companion object {

			val ARGUMENT_DEFINITION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.ARGUMENT_DEFINITION))
			val ENUM = DirectiveLocationSetImpl(setOf(GDirectiveLocation.ENUM))
			val ENUM_VALUE = DirectiveLocationSetImpl(setOf(GDirectiveLocation.ENUM_VALUE))
			val FIELD = DirectiveLocationSetImpl(setOf(GDirectiveLocation.FIELD))
			val FIELD_DEFINITION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.FIELD_DEFINITION))
			val FRAGMENT_DEFINITION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.FRAGMENT_DEFINITION))
			val FRAGMENT_SPREAD = DirectiveLocationSetImpl(setOf(GDirectiveLocation.FRAGMENT_SPREAD))
			val INLINE_FRAGMENT = DirectiveLocationSetImpl(setOf(GDirectiveLocation.INLINE_FRAGMENT))
			val INPUT_FIELD_DEFINITION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.INPUT_FIELD_DEFINITION))
			val INPUT_OBJECT = DirectiveLocationSetImpl(setOf(GDirectiveLocation.INPUT_OBJECT))
			val INTERFACE = DirectiveLocationSetImpl(setOf(GDirectiveLocation.INTERFACE))
			val MUTATION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.MUTATION))
			val OBJECT = DirectiveLocationSetImpl(setOf(GDirectiveLocation.OBJECT))
			val QUERY = DirectiveLocationSetImpl(setOf(GDirectiveLocation.QUERY))
			val SCALAR = DirectiveLocationSetImpl(setOf(GDirectiveLocation.SCALAR))
			val SCHEMA = DirectiveLocationSetImpl(setOf(GDirectiveLocation.SCHEMA))
			val SUBSCRIPTION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.SUBSCRIPTION))
			val UNION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.UNION))
			val VARIABLE_DEFINITION = DirectiveLocationSetImpl(setOf(GDirectiveLocation.VARIABLE_DEFINITION))
		}
	}


	private class EnumTypeDefinitionBuilderImpl<out Environment : Any>(
		val name: String
	) : ContainerImpl<Environment>(),
		EnumTypeDefinitionBuilder {

		private val values = mutableListOf<GEnumValueDefinition>()


		fun build() = GEnumType(
			description = description,
			directives = directives,
			name = name,
			values = values
		)


		override fun value(name: String, configure: EnumTypeDefinitionBuilder.ValueBuilder.() -> Unit) {
			values += ValueBuilderImpl<Environment>(name = name).apply(configure).build()
		}


		private class ValueBuilderImpl<out Environment : Any>(
			val name: String
		) : ContainerImpl<Environment>(),
			EnumTypeDefinitionBuilder.ValueBuilder {

			fun build() = GEnumValueDefinition(
				description = description,
				directives = directives,
				name = name
			)
		}
	}


	private class FieldDefinitionBuilderImpl<out Environment : Any, out ParentKotlinType : Any>(
		var name: String,
		var type: GTypeRef
	) : ContainerImpl<Environment>(),
		FieldDefinitionBuilder.Resolvable<Environment, ParentKotlinType>,
		FieldDefinitionContainer.NameAndType {

		private var resolver: GFieldResolver<Environment, ParentKotlinType>? = null


		@Suppress("UNCHECKED_CAST")
		fun build() = GFieldDefinition(
			argumentDefinitions = argumentDefinitions as List<GFieldArgumentDefinition>,
			description = description,
			directives = directives,
			name = name,
			resolver = resolver,
			type = type
		)


		override fun <Result> resolve(resolver: suspend GFieldResolverContext<Environment>.(context: ParentKotlinType) -> Result) {
			this.resolver = GFieldResolver(resolver)
		}


		override fun String.of(type: GTypeRef) =
			ofArgumentDefinitionType(type, definitionType = ArgumentDefinitionType.fieldDefinition)
	}


	private class InputObjectTypeDefinitionBuilderImpl<out Environment : Any>(
		val name: String
	) : ContainerImpl<Environment>(),
		InputObjectTypeDefinitionBuilder {

		@Suppress("UNCHECKED_CAST")
		fun build() = GInputObjectType(
			argumentDefinitions = argumentDefinitions as List<GInputObjectArgumentDefinition>,
			description = description,
			directives = directives,
			name = name
		)


		override fun String.of(type: GTypeRef) =
			ofArgumentDefinitionType(type, definitionType = ArgumentDefinitionType.inputField)
	}


	private class InterfaceTypeDefinitionBuilderImpl<out Environment : Any>(
		val name: String,
		val interfaces: List<GNamedTypeRef>
	) : ContainerImpl<Environment>(),
		InterfaceTypeDefinitionBuilder {

		private val fieldDefinitions = mutableListOf<GFieldDefinition>()


		fun build() = GInterfaceType(
			description = description,
			directives = directives,
			fields = fieldDefinitions,
			interfaces = interfaces,
			name = name
		)


		@Suppress("UNCHECKED_CAST")
		override fun field(name: FieldDefinitionContainer.NameAndType, configure: FieldDefinitionBuilder.() -> Unit) {
			fieldDefinitions += (name as FieldDefinitionBuilderImpl<Unit, Environment>).apply(configure).build()
		}


		override fun String.of(type: GTypeRef) =
			ofFieldDefinitionType(type)
	}


	private class InterfacesImpl(
		val name: String,
		interfaceType: GNamedTypeRef?
	) : Interfaces {

		val interfaces = interfaceType?.let { mutableListOf(it) } ?: mutableListOf()


		override fun and(type: GNamedTypeRef) = apply {
			interfaces += type
		}
	}


	private class ObjectTypeDefinitionBuilderImpl<out Environment : Any, out KotlinType : Any>(
		val name: String,
		val interfaces: List<GNamedTypeRef>,
		val kotlinType: KClass<out KotlinType>?
	) : ContainerImpl<Environment>(),
		ObjectTypeDefinitionBuilder<Environment, KotlinType> {

		private val fieldDefinitions = mutableListOf<GFieldDefinition>()


		fun build() = GObjectType(
			description = description,
			directives = directives,
			fields = fieldDefinitions,
			interfaces = interfaces,
			kotlinType = kotlinType,
			name = name
		)


		@Suppress("UNCHECKED_CAST")
		override fun field(
			name: FieldDefinitionContainer.NameAndType,
			configure: FieldDefinitionBuilder.Resolvable<Environment, KotlinType>.() -> Unit
		) {
			fieldDefinitions += (name as FieldDefinitionBuilderImpl<Environment, KotlinType>).apply(configure).build()
		}


		override fun String.of(type: GTypeRef) =
			ofFieldDefinitionType(type)
	}


	private class ScalarTypeDefinitionBuilderImpl<out Environment : Any>(
		val name: String
	) : ContainerImpl<Environment>(),
		ScalarTypeDefinitionBuilder {

		fun build() = GCustomScalarType(
			description = description,
			directives = directives,
			name = name
		)
	}


	private class UnionTypeDefinitionBuilderImpl<out Environment : Any>(
		val name: String,
		possibleType: GNamedTypeRef
	) : ContainerImpl<Environment>(),
		PossibleTypes,
		UnionTypeDefinitionBuilder {

		private val possibleTypes = mutableListOf(possibleType)


		fun build() = GUnionType(
			description = description,
			directives = directives,
			name = name,
			possibleTypes = possibleTypes
		)


		override fun or(type: GNamedTypeRef) = apply {
			possibleTypes += type
		}
	}
}
