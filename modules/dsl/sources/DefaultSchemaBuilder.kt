package io.fluidsonic.graphql

import io.fluidsonic.graphql.GSchemaBuilder.*
import kotlin.jvm.*

// TODO Rework this into GraphQL* types.

internal class DefaultSchemaBuilder : GSchemaBuilder {

	private val definitions = mutableListOf<GTypeSystemDefinition>()
	private var mutationType: GNamedTypeRef? = null
	private var queryType: GNamedTypeRef? = null
	private var subscriptionType: GNamedTypeRef? = null


	// FIXME validate?
	fun build(): GSchema {
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
		definitions += DirectiveDefinitionBuilderImpl(name = name).apply(configure).build()
	}


	override fun Enum(type: GNamedTypeRef, configure: EnumTypeDefinitionBuilder.() -> Unit) {
		definitions += EnumTypeDefinitionBuilderImpl(name = type.name).apply(configure).build()
	}


	override fun InputObject(type: GNamedTypeRef, configure: InputObjectTypeDefinitionBuilder.() -> Unit) {
		definitions += InputObjectTypeDefinitionBuilderImpl(name = type.name).apply(configure).build()
	}


	override fun Interface(type: GNamedTypeRef, configure: InterfaceTypeDefinitionBuilder.() -> Unit) {
		definitions += InterfaceTypeDefinitionBuilderImpl(
			interfaces = emptyList(),
			name = type.name
		).apply(configure).build()
	}


	override fun Interface(named: Interfaces, configure: InterfaceTypeDefinitionBuilder.() -> Unit) {
		named as InterfacesImpl

		definitions += InterfaceTypeDefinitionBuilderImpl(
			interfaces = named.interfaces,
			name = named.name
		).apply(configure).build()
	}


	override fun Mutation(type: GNamedTypeRef) {
		mutationType = type
	}


	override fun Mutation(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		mutationType = type

		Object(type, configure)
	}


	override fun Object(
		type: GNamedTypeRef,
		configure: ObjectTypeDefinitionBuilder.() -> Unit,
	) {
		definitions += ObjectTypeDefinitionBuilderImpl(
			interfaces = emptyList(),
			name = type.name
		).apply(configure).build()
	}


	override fun Object(
		named: Interfaces,
		configure: ObjectTypeDefinitionBuilder.() -> Unit,
	) {
		named as InterfacesImpl

		definitions += ObjectTypeDefinitionBuilderImpl(
			interfaces = named.interfaces,
			name = named.name
		).apply(configure).build()
	}


	override fun Query(type: GNamedTypeRef) {
		queryType = type
	}


	override fun Query(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		queryType = type

		Object(type, configure)
	}


	override fun Scalar(type: GNamedTypeRef, configure: ScalarTypeDefinitionBuilder.() -> Unit) {
		definitions += ScalarTypeDefinitionBuilderImpl(name = type.name).apply(configure).build()
	}


	override fun Subscription(type: GNamedTypeRef) {
		subscriptionType = type
	}


	override fun Subscription(type: GNamedTypeRef, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
		subscriptionType = type

		Object(type, configure)
	}


	override fun Union(named: PossibleTypes, configure: UnionTypeDefinitionBuilder.() -> Unit) {
		@Suppress("UNCHECKED_CAST")
		definitions += (named as UnionTypeDefinitionBuilderImpl).apply(configure).build()
	}


	override fun GNamedTypeRef.implements(interfaceType: GNamedTypeRef): Interfaces =
		InterfacesImpl(
			interfaceType = interfaceType,
			name = name
		)


	override fun GNamedTypeRef.with(possibleType: GNamedTypeRef): PossibleTypes =
		UnionTypeDefinitionBuilderImpl(
			name = name,
			possibleType = possibleType
		)


	companion object {

		@JvmName("GValueOfNull")
		private fun GValue(@Suppress("UNUSED_PARAMETER") value: Nothing?): GNullValue =
			GNullValue()


		@JvmName("GValueOfBoolean")
		private fun GValue(value: Boolean?): GValue = when (value) {
			null -> GValue(value)
			else -> GBooleanValue(value)
		}


		@JvmName("GValueOfFloat")
		private fun GValue(value: Double?): GValue = when (value) {
			null -> GValue(value)
			else -> GFloatValue(value)
		}


		@JvmName("GValueOfInt")
		private fun GValue(value: Int?): GValue = when (value) {
			null -> GValue(value)
			else -> GIntValue(value)
		}


		@JvmName("GValueOfString")
		private fun GValue(value: String?): GValue = when (value) {
			null -> GValue(value)
			else -> GStringValue(value)
		}


		@JvmName("GValueOfNullList")
		private fun GValue(value: List<Nothing?>?): GValue = when (value) {
			null -> GValue(value)
			else -> GListValue(value.map(::GValue))
		}


		@JvmName("GValueOfBooleanList")
		private fun GValue(value: List<Boolean?>?): GValue = when (value) {
			null -> GValue(value)
			else -> GListValue(value.map(::GValue))
		}


		@JvmName("GValueOfFloatList")
		private fun GValue(value: List<Double?>?): GValue = when (value) {
			null -> GValue(value)
			else -> GListValue(value.map(::GValue))
		}


		@JvmName("GValueOfIntList")
		private fun GValue(value: List<Int?>?): GValue = when (value) {
			null -> GValue(value)
			else -> GListValue(value.map(::GValue))
		}


		@JvmName("GValueOfStringList")
		private fun GValue(value: List<String?>?): GValue = when (value) {
			null -> GValue(value)
			else -> GListValue(value.map(::GValue))
		}
	}


	private class ArgumentBuilderImpl(
		var name: String,
		var value: GValue,
	) : ArgumentContainer.NameAndValue {

		fun build() = GArgument(
			name = name,
			value = value
		)
	}


	private class ArgumentDefinitionBuilderImpl(
		val name: String,
		val type: GTypeRef,
		val definitionType: ArgumentDefinitionType,
		var defaultValue: GValue? = null,
	) : ContainerImpl<GArgumentDefinition>(),
		ArgumentDefinitionBuilder,
		ArgumentDefinitionContainer.NameAndType,
		ArgumentDefinitionContainer.NameAndTypeAndDefault {

		@Suppress("UNCHECKED_CAST")
		fun build(): GArgumentDefinition = when (definitionType) {
			ArgumentDefinitionType.directiveDefinition ->
				GDirectiveArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					type = type,
					extensions = extensions as GNodeExtensionSet<GDirectiveArgumentDefinition>
				)

			ArgumentDefinitionType.fieldDefinition ->
				GFieldArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					type = type,
					extensions = extensions as GNodeExtensionSet<GFieldArgumentDefinition>
				)

			ArgumentDefinitionType.inputField ->
				GInputObjectArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					type = type,
					extensions = extensions as GNodeExtensionSet<GInputObjectArgumentDefinition>
				)
		}


		override fun default(default: Value) = apply {
			defaultValue = default.toGValue()
		}
	}


	private enum class ArgumentDefinitionType {

		directiveDefinition,
		fieldDefinition,
		inputField
	}


	private open class ContainerImpl<Node : GNode> :
		ArgumentContainer,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		NodeBuilder,
		TypeRefContainer,
		ValueContainer {

		private var extensionSetBuilder: GNodeExtensionSet.Builder<Node>? = null

		protected val argumentDefinitions = mutableListOf<GArgumentDefinition>()
		protected val arguments = mutableListOf<GArgument>()
		protected var description: String? = null
		protected val directives = mutableListOf<GDirective>()


		override fun argument(name: ArgumentContainer.NameAndValue) {
			arguments += (name as ArgumentBuilderImpl).build()
		}


		fun argument(name: ArgumentDefinitionContainer.NameAndType, configure: ArgumentDefinitionBuilder.() -> Unit) {
			@Suppress("UNCHECKED_CAST")
			argumentDefinitions += (name as ArgumentDefinitionBuilderImpl).apply(configure).build()
		}


		fun argument(name: ArgumentDefinitionContainer.NameAndTypeAndDefault, configure: ArgumentDefinitionBuilder.() -> Unit) {
			@Suppress("UNCHECKED_CAST")
			argumentDefinitions += (name as ArgumentDefinitionBuilderImpl).apply(configure).build()
		}


		override fun deprecated(reason: String?) {
			require(directives.none { it.name == GLanguage.defaultDeprecatedDirective.name }) {
				"Cannot use deprecate() multiple times on the same element"
			}

			directives += GDirective(
				name = GLanguage.defaultDeprecatedDirective.name,
				arguments = listOf(
					GArgument(name = "reason", value = reason?.let(::GStringValue) ?: GNullValue())
				)
			)
		}


		override fun description(text: String) {
			description = text.ifEmpty { null }
		}


		override fun directive(name: String, configure: DirectiveBuilder.() -> Unit) {
			directives += DirectiveBuilderImpl(name = name).apply(configure).build()
		}


		override fun <Value : Any> extension(key: GNodeExtensionKey<out Value>) =
			extensionSetBuilder?.get(key)


		override fun <Value : Any> extension(key: GNodeExtensionKey<in Value>, value: Value) {
			val builder = extensionSetBuilder
				?: GNodeExtensionSet.Builder.default<Node>().also { extensionSetBuilder = it }

			builder[key] = value
		}


		val extensions
			get() = extensionSetBuilder?.build() ?: GNodeExtensionSet.empty()


		override fun value(value: GValue) =
			ValueImpl(value)


		fun String.ofArgumentDefinitionType(type: GTypeRef, definitionType: ArgumentDefinitionType) =
			ArgumentDefinitionBuilderImpl(name = this, type = type, definitionType = definitionType)


		fun String.ofFieldDefinitionType(type: GTypeRef) =
			FieldDefinitionBuilderImpl(name = this, type = type)


		override fun String.with(value: Value) =
			ArgumentBuilderImpl(name = this, value = value.toGValue())


		override fun List(type: GTypeRef) =
			GListTypeRef(type)
	}


	private class DirectiveBuilderImpl(
		val name: String,
	) : ContainerImpl<GDirective>(),
		DirectiveBuilder {

		fun build() = GDirective(
			arguments = arguments,
			name = name,
			extensions = extensions
		)
	}


	private class DirectiveDefinitionBuilderImpl(
		val name: String,
	) : ContainerImpl<GDirectiveDefinition>(),
		DirectiveDefinitionBuilder {

		private var locations = emptySet<GDirectiveLocation>()


		@Suppress("UNCHECKED_CAST")
		fun build() = GDirectiveDefinition(
			argumentDefinitions = argumentDefinitions as List<GDirectiveArgumentDefinition>,
			description = description,
			locations = locations,
			name = name,
			extensions = extensions
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


	private class EnumTypeDefinitionBuilderImpl(
		val name: String,
	) : ContainerImpl<GEnumType>(),
		EnumTypeDefinitionBuilder {

		private val values = mutableListOf<GEnumValueDefinition>()


		fun build() = GEnumType(
			description = description,
			directives = directives,
			name = name,
			values = values,
			extensions = extensions
		)


		override fun value(name: String, configure: EnumTypeDefinitionBuilder.ValueBuilder.() -> Unit) {
			values += ValueBuilderImpl(name = name).apply(configure).build()
		}


		private class ValueBuilderImpl(
			val name: String,
		) : ContainerImpl<GEnumValueDefinition>(),
			EnumTypeDefinitionBuilder.ValueBuilder {

			fun build() = GEnumValueDefinition(
				description = description,
				directives = directives,
				name = name,
				extensions = extensions
			)
		}
	}


	private class FieldDefinitionBuilderImpl(
		var name: String,
		var type: GTypeRef,
	) : ContainerImpl<GFieldDefinition>(),
		FieldDefinitionBuilder,
		FieldDefinitionContainer.NameAndType {

		@Suppress("UNCHECKED_CAST")
		fun build() = GFieldDefinition(
			argumentDefinitions = argumentDefinitions as List<GFieldArgumentDefinition>,
			description = description,
			directives = directives,
			name = name,
			type = type,
			extensions = extensions
		)


		override fun String.of(type: GTypeRef) =
			ofArgumentDefinitionType(type, definitionType = ArgumentDefinitionType.fieldDefinition)
	}


	private class InputObjectTypeDefinitionBuilderImpl(
		val name: String,
	) : ContainerImpl<GInputObjectType>(),
		InputObjectTypeDefinitionBuilder {

		@Suppress("UNCHECKED_CAST")
		fun build() = GInputObjectType(
			argumentDefinitions = argumentDefinitions as List<GInputObjectArgumentDefinition>,
			description = description,
			directives = directives,
			name = name,
			extensions = extensions
		)


		override fun String.of(type: GTypeRef) =
			ofArgumentDefinitionType(type, definitionType = ArgumentDefinitionType.inputField)
	}


	private class InterfaceTypeDefinitionBuilderImpl(
		val name: String,
		val interfaces: List<GNamedTypeRef>,
	) : ContainerImpl<GInterfaceType>(),
		InterfaceTypeDefinitionBuilder {

		private val fieldDefinitions = mutableListOf<GFieldDefinition>()


		fun build() = GInterfaceType(
			description = description,
			directives = directives,
			fieldDefinitions = fieldDefinitions,
			interfaces = interfaces,
			name = name,
			extensions = extensions
		)


		@Suppress("UNCHECKED_CAST")
		override fun field(name: FieldDefinitionContainer.NameAndType, configure: FieldDefinitionBuilder.() -> Unit) {
			fieldDefinitions += (name as FieldDefinitionBuilderImpl).apply(configure).build()
		}


		override fun String.of(type: GTypeRef) =
			ofFieldDefinitionType(type)
	}


	private class InterfacesImpl(
		val name: String,
		interfaceType: GNamedTypeRef?,
	) : Interfaces {

		val interfaces = interfaceType?.let { mutableListOf(it) } ?: mutableListOf()


		override fun and(type: GNamedTypeRef) = apply {
			interfaces += type
		}
	}


	private class ObjectTypeDefinitionBuilderImpl(
		val name: String,
		val interfaces: List<GNamedTypeRef>,
	) : ContainerImpl<GObjectType>(),
		ObjectTypeDefinitionBuilder {

		private val fieldDefinitions = mutableListOf<GFieldDefinition>()


		fun build() = GObjectType(
			description = description,
			directives = directives,
			fieldDefinitions = fieldDefinitions,
			interfaces = interfaces,
			name = name,
			extensions = extensions
		)


		@Suppress("UNCHECKED_CAST")
		override fun field(name: FieldDefinitionContainer.NameAndType, configure: FieldDefinitionBuilder.() -> Unit) {
			fieldDefinitions += (name as FieldDefinitionBuilderImpl).apply(configure).build()
		}


		override fun String.of(type: GTypeRef) =
			ofFieldDefinitionType(type)
	}


	private class ScalarTypeDefinitionBuilderImpl(
		val name: String,
	) : ContainerImpl<GCustomScalarType>(),
		ScalarTypeDefinitionBuilder {

		fun build() = GCustomScalarType(
			description = description,
			directives = directives,
			name = name,
			extensions = extensions
		)
	}


	private class UnionTypeDefinitionBuilderImpl(
		val name: String,
		possibleType: GNamedTypeRef,
	) : ContainerImpl<GUnionType>(),
		PossibleTypes,
		UnionTypeDefinitionBuilder {

		private val possibleTypes = mutableListOf(possibleType)


		fun build() = GUnionType(
			description = description,
			directives = directives,
			name = name,
			possibleTypes = possibleTypes,
			extensions = extensions
		)


		override fun or(type: GNamedTypeRef) = apply {
			possibleTypes += type
		}
	}


	private class ValueImpl(
		val value: GValue,
	) : Value {

		override fun toGValue() =
			value
	}
}
