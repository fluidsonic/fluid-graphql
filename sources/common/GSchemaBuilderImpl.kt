package io.fluidsonic.graphql

import io.fluidsonic.graphql.GQLInput.Argument
import io.fluidsonic.graphql.GQLInput.Directive
import io.fluidsonic.graphql.GQLInput.DirectiveDefinition
import io.fluidsonic.graphql.GQLInput.EnumValue
import io.fluidsonic.graphql.GQLInput.Field
import io.fluidsonic.graphql.GQLInput.InputValue
import io.fluidsonic.graphql.SchemaBuilder.*


@SchemaBuilderKeywordB
fun schema(configure: SchemaBuilder.() -> Unit) =
	SchemaBuilderImpl().apply(configure).build()


internal class SchemaBuilderImpl : SchemaBuilder {

	private val directives = mutableListOf<DirectiveDefinition>()
	private var mutationType: String? = null
	private var queryType: String? = null
	private var subscriptionType: String? = null
	private val types = mutableListOf<GQLInput.Type>()


	fun build() =
		GSchema.of(GQLInput.Schema(
			types = types,
			queryType = queryType,
			mutationType = mutationType,
			subscriptionType = subscriptionType,
			directives = directives
		))


	override fun Directive(name: String, configure: DirectiveDefinitionBuilder.() -> Unit) {
		directives += DirectiveDefinitionBuilderImpl(name = name).apply(configure).build()
	}


	override fun Enum(type: GTypeRef, configure: EnumBuilder.() -> Unit) {
		types += EnumBuilderImpl(name = type.requireName()).apply(configure).build()
	}


	override fun InputObject(type: GTypeRef, configure: InputObjectBuilder.() -> Unit) {
		types += InputObjectBuilderImpl(name = type.requireName()).apply(configure).build()
	}


	override fun Interface(type: GTypeRef, configure: InterfaceBuilder.() -> Unit) {
		types += InterfaceBuilderImpl(name = type.requireName()).apply(configure).build()
	}


	override fun Mutation(type: GTypeRef, configure: ObjectBuilder.() -> Unit) {
		mutationType = type.requireName()

		Object(type, configure)
	}


	override fun Object(type: GTypeRef, configure: ObjectBuilder.() -> Unit) {
		types += ObjectBuilderImpl(
			name = type.requireName(),
			interfaceType = null
		).apply(configure).build()
	}


	override fun Object(named: InterfacesForObject, configure: ObjectBuilder.() -> Unit) {
		types += (named as ObjectBuilderImpl).apply(configure).build()
	}


	override fun Query(type: GTypeRef, configure: ObjectBuilder.() -> Unit) {
		queryType = type.requireName()

		Object(type, configure)
	}


	override fun Scalar(type: GTypeRef, configure: ScalarBuilder.() -> Unit) {
		types += ScalarBuilderImpl(name = type.requireName()).apply(configure).build()
	}


	override fun Subscription(type: GTypeRef, configure: ObjectBuilder.() -> Unit) {
		subscriptionType = type.requireName()

		Object(type, configure)
	}


	override fun Union(named: PossibleTypesForUnion, configure: UnionBuilder.() -> Unit) {
		types += (named as UnionBuilderImpl).apply(configure).build()
	}


	override fun GTypeRef.implements(interfaceType: GTypeRef): InterfacesForObject =
		ObjectBuilderImpl(
			name = requireName(),
			interfaceType = interfaceType
		)


	override fun GTypeRef.with(possibleType: GTypeRef): PossibleTypesForUnion =
		UnionBuilderImpl(
			name = requireName(),
			possibleType = possibleType
		)


	companion object {

		fun GTypeRef.requireName() =
			(this as GNamedTypeRef).name
	}


	private open class ContainerImpl :
		ArgumentContainer,
		DeprecationContainer,
		DescriptionContainer,
		DirectiveContainer,
		TypeRefContainer {

		private val fieldBuilders = mutableListOf<FieldBuilderImpl>()
		private val inputValueBuilders = mutableListOf<InputValueBuilderImpl>()

		protected val arguments = mutableListOf<Argument>()
		protected var deprecationReason: String? = null
		protected var description: String? = null
		protected val directives = mutableListOf<Directive>()
		protected var isDeprecated = false


		val fields
			get() = fieldBuilders.map { it.build() }


		val inputValues
			get() = inputValueBuilders.map { it.build() }


		override fun deprecated(reason: String) {
			deprecationReason = reason.ifEmpty { null }
			isDeprecated = true
		}


		override fun description(text: String) {
			this.description = text.ifEmpty { null }
		}


		override fun directive(name: String, configure: DirectiveBuilder.() -> Unit) {
			this.directives += DirectiveBuilderImpl(name = name).apply(configure).build()
		}


		override fun enumValue(name: String) =
			GValue.EnumValue(name)


		fun Boolean.invoke(configure: InputValueBuilder.() -> Unit) =
			InputValueBuilderImpl(defaultValue = GValue.from(this)).apply(configure)


		fun Double.invoke(configure: InputValueBuilder.() -> Unit) =
			InputValueBuilderImpl(defaultValue = GValue.from(this)).apply(configure)


		fun Float.invoke(configure: InputValueBuilder.() -> Unit) =
			InputValueBuilderImpl(defaultValue = GValue.from(this)).apply(configure)


		fun Int.invoke(configure: InputValueBuilder.() -> Unit) =
			InputValueBuilderImpl(defaultValue = GValue.from(this)).apply(configure)


		fun String.invoke(configure: InputValueBuilder.() -> Unit) =
			InputValueBuilderImpl(defaultValue = GValue.from(this)).apply(configure)


		fun GValue.invoke(configure: InputValueBuilder.() -> Unit) =
			InputValueBuilderImpl(defaultValue = this).apply(configure)


		fun String.of(type: FieldContainer.DanglingType) {
			type as FieldBuilderImpl

			type.name = this
		}


		fun String.of(type: InputValueContainer.DanglingType) {
			type as InputValueBuilderImpl

			type.name = this
		}


		fun String.ofFieldType(type: GTypeRef) {
			fieldBuilders += FieldBuilderImpl(name = this, type = type)
		}


		fun String.ofInputType(type: GTypeRef) =
			InputValueBuilderImpl(name = this, type = type)
				.also { inputValueBuilders += it }


		override fun String.with(value: Any?) {
			arguments += Argument(name = this, value = GValue.from(value))
		}


		fun GTypeRef.invoke(configure: FieldBuilder.() -> Unit) =
			FieldBuilderImpl(type = this).apply(configure)
				.also { fieldBuilders += it }


		fun GTypeRef.invoke(configure: InputValueBuilder.() -> Unit) =
			InputValueBuilderImpl(type = this).apply(configure)
				.also { inputValueBuilders += it }


		override fun List(type: GTypeRef) =
			GListTypeRef(type)
	}


	private class DirectiveBuilderImpl(
		val name: String
	) : ContainerImpl(),
		DirectiveBuilder {

		fun build() =
			Directive(
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
			DirectiveDefinition(
				name = name,
				arguments = inputValues,
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


		override fun on(any: DirectiveDefinitionBuilder.DirectiveLocation) {
			locations = (any as DirectiveLocationSetImpl).locations
		}


		override fun on(any: DirectiveDefinitionBuilder.DirectiveLocationSet) {
			locations = (any as DirectiveLocationSetImpl).locations
		}


		override fun String.of(type: GTypeRef) =
			ofInputType(type)


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
		}
	}


	private class EnumBuilderImpl(
		val name: String
	) : ContainerImpl(),
		EnumBuilder {

		private val values = mutableListOf<EnumValue>()


		fun build() =
			GQLInput.Type.Enum(
				name = name,
				values = values,
				description = description,
				directives = directives
			)


		override fun String.invoke(configure: EnumBuilder.ValueBuilder.() -> Unit) =
			ValueBuilderImpl(name = this).apply(configure)


		override fun String.unaryMinus() {
			values += EnumValue(name = this)
		}


		private inner class ValueBuilderImpl(
			val name: String
		) : ContainerImpl(),
			EnumBuilder.DanglingValue,
			EnumBuilder.ValueBuilder {

			override fun unaryMinus() {
				this@EnumBuilderImpl.values += EnumValue(
					name = name,
					description = description,
					directives = directives,
					isDeprecated = isDeprecated,
					deprecationReason = deprecationReason
				)
			}
		}
	}


	private class FieldBuilderImpl(
		var name: String? = null,
		var type: GTypeRef? = null
	) : ContainerImpl(),
		FieldBuilder,
		FieldContainer.DanglingType {

		fun build() =
			Field(
				name = name ?: error("Name of field is missing"),
				type = type ?: error("GTypeRef of field is missing"),
				args = inputValues,
				description = description,
				directives = directives,
				isDeprecated = isDeprecated,
				deprecationReason = deprecationReason
			)


		override fun String.of(type: GTypeRef) =
			ofInputType(type)
	}


	private class InputValueBuilderImpl(
		var name: String? = null,
		var type: GTypeRef? = null,
		var defaultValue: GValue? = null
	) : ContainerImpl(),
		InputValueBuilder,
		InputValueContainer.DanglingDefault,
		InputValueContainer.DanglingType,
		InputValueContainer.InputValue {

		fun build() =
			InputValue(
				name = name ?: error("Name of input value is missing"),
				type = type ?: error("GTypeRef of input value is missing"),
				defaultValue = defaultValue,
				description = description,
				directives = directives
			)


		override fun default(default: Boolean) = apply {
			defaultValue = GValue.from(default)
		}


		override fun default(default: Double) = apply {
			defaultValue = GValue.from(default)
		}


		override fun default(default: Float) = apply {
			defaultValue = GValue.from(default)
		}


		override fun default(default: Int) = apply {
			defaultValue = GValue.from(default)
		}


		override fun default(default: Nothing?) = apply {
			defaultValue = GValue.from(default)
		}


		override fun default(default: String) = apply {
			defaultValue = GValue.from(default)
		}


		override fun default(default: GValue) = apply {
			defaultValue = default
		}


		override fun default(default: InputValueContainer.DanglingDefault) = apply {
			defaultValue = (default as InputValueBuilderImpl).defaultValue
		}


		override fun invoke(configure: InputValueBuilder.() -> Unit) {
			configure()
		}
	}


	private class InputObjectBuilderImpl(
		val name: String
	) : ContainerImpl(),
		InputObjectBuilder {

		fun build() =
			GQLInput.Type.InputObject(
				name = name,
				fields = inputValues,
				description = description,
				directives = directives
			)


		override fun String.of(type: GTypeRef) =
			ofInputType(type)
	}


	private class InterfaceBuilderImpl(
		val name: String
	) : ContainerImpl(),
		InterfaceBuilder {

		fun build() =
			GQLInput.Type.Interface(
				name = name,
				fields = fields,
				description = description,
				directives = directives
			)


		override fun String.of(type: GTypeRef) =
			ofFieldType(type)
	}


	private class ObjectBuilderImpl(
		val name: String,
		val interfaceType: GTypeRef?
	) : ContainerImpl(),
		ObjectBuilder,
		InterfacesForObject {

		private val interfaces = interfaceType?.let { mutableListOf(it) } ?: mutableListOf()


		fun build() =
			GQLInput.Type.Object(
				name = name,
				fields = fields,
				interfaces = interfaces,
				description = description,
				directives = directives
			)


		override fun and(type: GTypeRef) = apply {
			interfaces += type
		}


		override fun String.of(type: GTypeRef) =
			ofFieldType(type)
	}


	private class ScalarBuilderImpl(
		val name: String
	) : ContainerImpl(),
		ScalarBuilder {

		fun build() =
			GQLInput.Type.Scalar(
				name = name,
				description = description,
				directives = directives
			)
	}


	private class UnionBuilderImpl(
		val name: String,
		possibleType: GTypeRef
	) : ContainerImpl(),
		PossibleTypesForUnion,
		UnionBuilder {

		private val possibleTypes = mutableListOf(possibleType)


		fun build() =
			GQLInput.Type.Union(
				name = name,
				description = description,
				directives = directives,
				possibleTypes = possibleTypes
			)


		override fun or(type: GTypeRef) = apply {
			possibleTypes += type
		}
	}
}
