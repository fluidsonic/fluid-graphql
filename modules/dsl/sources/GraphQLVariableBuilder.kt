package io.fluidsonic.graphql

import kotlin.internal.*


@GraphQLMarker
public /* sealed */ interface GraphQLVariableBuilder : GraphQLVariableBuilderScope, GraphQLDirectivesContainer {

	public fun build(): GVariableDefinition
}


@GraphQLMarker
public /* sealed */ interface GraphQLVariableBuilderScope : GraphQLValueContainerScope, GraphQLDirectivesContainerScope {

	@GraphQLMarker
	public fun default(value: Boolean) {
		default(GBooleanValue(value))
	}


	@GraphQLMarker
	public fun default(value: Boolean?) {
		default(value?.let(::GBooleanValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: Byte) {
		default(GIntValue(value))
	}


	@GraphQLMarker
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun default(value: Byte?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: Double) {
		default(GFloatValue(value))
	}


	@GraphQLMarker
	public fun default(value: Double?) {
		default(value?.let(::GFloatValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: Float) {
		default(GFloatValue(value))
	}


	@GraphQLMarker
	public fun default(value: Float?) {
		default(value?.let(::GFloatValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: Int) {
		default(GIntValue(value))
	}


	@GraphQLMarker
	public fun default(value: Int?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: Nothing?) {
		default(GNullValue())
	}


	@GraphQLMarker
	public fun default(value: Short) {
		default(GIntValue(value))
	}


	@GraphQLMarker
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun default(value: Short?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: String?) {
		default(value?.let(::GStringValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: UByte) {
		default(GIntValue(value))
	}


	@GraphQLMarker
	public fun default(value: UByte?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: UShort) {
		default(GIntValue(value))
	}


	@GraphQLMarker
	public fun default(value: UShort?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun default(value: GValue)


	// TODO Move to extension and inline once we have context receivers.
	@GraphQLMarker
	public fun default(configure: GraphQLArgumentsBuilder.() -> Unit) {
		default(GObjectValue(GraphQLArgumentsBuilder().apply(configure).build()))
	}
}


private class GraphQLVariableBuilderImpl(
	private val name: String,
	private val type: GTypeRef,
) : GraphQLVariableBuilder, GraphQLDirectivesContainerInternal {

	private var defaultValue: GValue? = null
	override var directives: List<GDirective>? = null


	init {
		check(GLanguage.isValidTypeName(name)) { "Invalid variable name: $name" }
	}


	override fun build(): GVariableDefinition =
		GVariableDefinition(
			defaultValue = defaultValue,
			directives = directives.orEmpty(),
			name = name,
			type = type,
		)


	override fun default(value: GValue) {
		check(defaultValue == null) { "Cannot set multiple default values." }

		// TODO Check lists and objects recursively.
		check(value !is GVariableRef) { "Cannot use a variable reference as default value for a variable." }

		defaultValue = value
	}
}


public fun GraphQLVariableBuilder(name: String, type: GTypeRef): GraphQLVariableBuilder =
	GraphQLVariableBuilderImpl(name = name, type = type)
