package io.fluidsonic.graphql

import io.fluidsonic.graphql.GSchemaBuilder.*
import kotlin.reflect.*


@SchemaBuilderKeywordB
fun schema(configure: GSchemaBuilder.() -> Unit) =
	GSchemaBuilderImpl().apply(configure).build()


internal class GSchemaBuilderImpl : GSchemaBuilder {

	private val directives = mutableListOf<GDirectiveDefinition.Unresolved>()
	private var mutationType: GNamedTypeRef? = null
	private var queryType: GNamedTypeRef? = null
	private var subscriptionType: GNamedTypeRef? = null
	private val types = mutableListOf<GNamedType.Unresolved>()


	fun build() =
		GSchema.Unresolved(
			types = types,
			queryType = queryType,
			mutationType = mutationType,
			subscriptionType = subscriptionType,
			directives = directives
		).resolve()


	override fun Directive(name: String, configure: DirectiveDefinitionBuilder.() -> Unit) {
		directives += DirectiveDefinitionBuilderImpl(name = name).apply(configure).build()
	}


	override fun Enum(type: GNamedTypeRef, configure: EnumTypeDefinitionBuilder.() -> Unit) {
		types += EnumTypeDefinitionBuilderImpl(name = type.name).apply(configure).build()
	}


	override fun InputObject(type: GNamedTypeRef, configure: InputObjectTypeDefinitionBuilder.() -> Unit) {
		types += InputObjectTypeDefinitionBuilderImpl(name = type.name).apply(configure).build()
	}


	override fun Interface(type: GNamedTypeRef, configure: InterfaceTypeDefinitionBuilder.() -> Unit) {
		types += InterfaceTypeDefinitionBuilderImpl(name = type.name).apply(configure).build()
	}


	override fun Mutation(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		mutationType = type

		Object(type, configure)
	}


	override fun Object(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		types += ObjectTypeDefinitionBuilderImpl(
			name = type.name,
			interfaceType = null
		).apply(configure).build()
	}


	override fun Object(named: InterfacesForObject, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		types += (named as ObjectTypeDefinitionBuilderImpl).apply(configure).build()
	}


	override fun Query(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		queryType = type

		Object(type, configure)
	}


	override fun Scalar(type: GNamedTypeRef, configure: ScalarTypeDefinitionBuilder.() -> Unit) {
		types += ScalarTypeDefinitionBuilderImpl(name = type.name).apply(configure).build()
	}


	override fun Subscription(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		subscriptionType = type

		Object(type, configure)
	}


	override fun Union(named: TypesForUnion, configure: UnionTypeDefinitionBuilder.() -> Unit) {
		types += (named as UnionTypeDefinitionBuilderImpl).apply(configure).build()
	}


	override fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): InterfacesForObject =
		ObjectTypeDefinitionBuilderImpl(
			name = name,
			interfaceType = interfaceType
		)


	override fun GNamedTypeRef.with(possibleType: GNamedTypeRef): TypesForUnion =
		UnionTypeDefinitionBuilderImpl(
			name = name,
			possibleType = possibleType
		)


	private class ArgumentBuilderImpl(
		var name: String,
		var value: Any?
	) : ArgumentContainer.NameAndValue {

		fun build() =
			GArgument(
				name = name,
				value = value
			)
	}


	private class ArgumentDefinitionBuilderImpl(
		var name: String,
		var type: GTypeRef,
		var defaultValue: Any? = null
	) : ContainerImpl(),
		ArgumentDefinitionBuilder,
		ArgumentDefinitionContainer.NameAndType,
		ArgumentDefinitionContainer.NameAndTypeAndDefault {

		fun build() =
			GArgumentDefinition.Unresolved(
				name = name,
				type = type,
				defaultValue = defaultValue,
				description = description,
				directives = directives
			)


		override fun default(default: Any?) = apply {
			// FIXME typecheck

			defaultValue = default ?: GNullValue
		}
	}


	private open class ContainerImpl :
		ArgumentContainer,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		TypeRefContainer {


		protected val argumentDefinitions = mutableListOf<GArgumentDefinition.Unresolved>()
		protected val arguments = mutableListOf<GArgument>()
		protected var description: String? = null
		protected val directives = mutableListOf<GDirective>()
		protected val fieldDefinitions = mutableListOf<GFieldDefinition.Unresolved>()


		override fun argument(name: ArgumentContainer.NameAndValue) {
			arguments += (name as ArgumentBuilderImpl).build()
		}


		fun argument(name: ArgumentDefinitionContainer.NameAndType, configure: ArgumentDefinitionBuilder.() -> Unit) {
			argumentDefinitions += (name as ArgumentDefinitionBuilderImpl).apply(configure).build()
		}


		fun argument(name: ArgumentDefinitionContainer.NameAndTypeAndDefault, configure: ArgumentDefinitionBuilder.() -> Unit) {
			argumentDefinitions += (name as ArgumentDefinitionBuilderImpl).apply(configure).build()
		}


		// FIXME what if custom @deprecated is provided?
		override fun deprecated(reason: String?) {
			require(directives.none { it.name == GSpecification.defaultDeprecatedDirective.name }) {
				"Cannot use deprecate() multiple times on the same element"
			}

			directives += GDirective(
				name = GSpecification.defaultDeprecatedDirective.name,
				arguments = listOf(
					GArgument(name = "reason", value = reason) // FIXME null vs not-specified
				)
			)
		}


		override fun description(text: String) {
			description = text.ifEmpty { null }
		}


		override fun directive(name: String, configure: DirectiveBuilder.() -> Unit) {
			directives += DirectiveBuilderImpl(name = name).apply(configure).build()
		}


		fun field(name: FieldDefinitionContainer.NameAndType, configure: FieldDefinitionBuilder.() -> Unit) {
			fieldDefinitions += (name as FieldDefinitionBuilderImpl).apply(configure).build()
		}


		fun String.ofArgumentDefinitionType(type: GTypeRef) =
			ArgumentDefinitionBuilderImpl(name = this, type = type)


		fun String.ofFieldDefinitionType(type: GTypeRef) =
			FieldDefinitionBuilderImpl(name = this, type = type)


		override fun String.with(value: Any?) =
			ArgumentBuilderImpl(name = this, value = value)


		override fun List(type: GTypeRef) =
			GListTypeRef(type)
	}


	private class DirectiveBuilderImpl(
		val name: String
	) : ContainerImpl(),
		DirectiveBuilder {

		fun build() =
			GDirective(
				name = name,
				arguments = arguments
			)
	}


	private class DirectiveDefinitionBuilderImpl(
		val name: String
	) : ContainerImpl(),
		DirectiveDefinitionBuilder {

		private var locations = emptyList<GDirectiveLocation>()


		fun build() =
			GDirectiveDefinition.Unresolved(
				name = name,
				arguments = argumentDefinitions,
				locations = locations,
				description = description
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
			ofArgumentDefinitionType(type)


		class DirectiveLocationSetImpl(val locations: List<GDirectiveLocation>) :
			DirectiveDefinitionBuilder.DirectiveLocation,
			DirectiveDefinitionBuilder.DirectiveLocationSet {

			override fun or(other: DirectiveDefinitionBuilder.DirectiveLocation) =
				DirectiveLocationSetImpl(locations + (other as DirectiveLocationSetImpl).locations)
		}


		companion object {

			val ARGUMENT_DEFINITION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.ARGUMENT_DEFINITION))
			val ENUM = DirectiveLocationSetImpl(listOf(GDirectiveLocation.ENUM))
			val ENUM_VALUE = DirectiveLocationSetImpl(listOf(GDirectiveLocation.ENUM_VALUE))
			val FIELD = DirectiveLocationSetImpl(listOf(GDirectiveLocation.FIELD))
			val FIELD_DEFINITION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.FIELD_DEFINITION))
			val FRAGMENT_DEFINITION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.FRAGMENT_DEFINITION))
			val FRAGMENT_SPREAD = DirectiveLocationSetImpl(listOf(GDirectiveLocation.FRAGMENT_SPREAD))
			val INLINE_FRAGMENT = DirectiveLocationSetImpl(listOf(GDirectiveLocation.INLINE_FRAGMENT))
			val INPUT_FIELD_DEFINITION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.INPUT_FIELD_DEFINITION))
			val INPUT_OBJECT = DirectiveLocationSetImpl(listOf(GDirectiveLocation.INPUT_OBJECT))
			val INTERFACE = DirectiveLocationSetImpl(listOf(GDirectiveLocation.INTERFACE))
			val MUTATION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.MUTATION))
			val OBJECT = DirectiveLocationSetImpl(listOf(GDirectiveLocation.OBJECT))
			val QUERY = DirectiveLocationSetImpl(listOf(GDirectiveLocation.QUERY))
			val SCALAR = DirectiveLocationSetImpl(listOf(GDirectiveLocation.SCALAR))
			val SCHEMA = DirectiveLocationSetImpl(listOf(GDirectiveLocation.SCHEMA))
			val SUBSCRIPTION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.SUBSCRIPTION))
			val UNION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.UNION))
			val VARIABLE_DEFINITION = DirectiveLocationSetImpl(listOf(GDirectiveLocation.VARIABLE_DEFINITION))
		}
	}


	private class EnumTypeDefinitionBuilderImpl(
		val name: String
	) : ContainerImpl(),
		EnumTypeDefinitionBuilder {

		private val values = mutableListOf<GEnumValueDefinition>()


		fun build() =
			GEnumType.Unresolved(
				name = name,
				values = values,
				description = description,
				directives = directives
			)


		override fun value(name: String, configure: EnumTypeDefinitionBuilder.ValueBuilder.() -> Unit) {
			values += ValueBuilderImpl(name = name).apply(configure).build()
		}


		private class ValueBuilderImpl(
			val name: String
		) : ContainerImpl(),
			EnumTypeDefinitionBuilder.ValueBuilder {

			fun build() = GEnumValueDefinition(
				name = name,
				description = description,
				directives = directives
			)
		}
	}


	private class FieldDefinitionBuilderImpl(
		var name: String,
		var type: GTypeRef
	) : ContainerImpl(),
		FieldDefinitionBuilder,
		FieldDefinitionContainer.NameAndType {

		private var resolver: GFieldResolver<*>? = null


		fun build() =
			GFieldDefinition.Unresolved(
				name = name,
				type = type,
				arguments = argumentDefinitions,
				description = description,
				directives = directives,
				resolver = resolver
			)


		override fun <Parent : Any> resolve(parentClass: KClass<out Parent>, resolver: GFieldResolver.Context.(parent: Parent) -> Any?) {
			this.resolver = GFieldResolver.of(resolver)
		}


		override fun String.of(type: GTypeRef) =
			ofArgumentDefinitionType(type)
	}


	private class InputObjectTypeDefinitionBuilderImpl(
		val name: String
	) : ContainerImpl(),
		InputObjectTypeDefinitionBuilder {

		fun build() =
			GInputObjectType.Unresolved(
				name = name,
				arguments = argumentDefinitions,
				description = description,
				directives = directives
			)


		override fun String.of(type: GTypeRef) =
			ofArgumentDefinitionType(type)
	}


	private class InterfaceTypeDefinitionBuilderImpl(
		val name: String
	) : ContainerImpl(),
		InterfaceTypeDefinitionBuilder {

		fun build() =
			GInterfaceType.Unresolved(
				name = name,
				fields = fieldDefinitions,
				description = description,
				directives = directives
			)


		override fun String.of(type: GTypeRef) =
			ofFieldDefinitionType(type)
	}


	private class ObjectTypeDefinitionBuilderImpl(
		val name: String,
		val interfaceType: GNamedTypeRef?
	) : ContainerImpl(),
		ObjectTypeDefinitionBuilder,
		InterfacesForObject {

		private val interfaces = interfaceType?.let { mutableListOf(it) } ?: mutableListOf()


		fun build() =
			GObjectType.Unresolved(
				name = name,
				fields = fieldDefinitions,
				interfaces = interfaces,
				description = description,
				directives = directives
			)


		override fun and(type: GNamedTypeRef) = apply {
			interfaces += type
		}


		override fun String.of(type: GTypeRef) =
			ofFieldDefinitionType(type)
	}


	private class ScalarTypeDefinitionBuilderImpl(
		val name: String
	) : ContainerImpl(),
		ScalarTypeDefinitionBuilder {

		fun build() =
			GCustomScalarType.Unresolved(
				name = name,
				description = description,
				directives = directives
			)
	}


	private class UnionTypeDefinitionBuilderImpl(
		val name: String,
		possibleType: GNamedTypeRef
	) : ContainerImpl(),
		TypesForUnion,
		UnionTypeDefinitionBuilder {

		private val types = mutableListOf(possibleType)


		fun build() =
			GUnionType.Unresolved(
				name = name,
				description = description,
				directives = directives,
				types = types
			)


		override fun or(type: GNamedTypeRef) = apply {
			types += type
		}
	}
}
