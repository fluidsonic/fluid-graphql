package io.fluidsonic.graphql

import kotlin.internal.*


/**
 * Builder for a single [GVariableDefinition] in an operation or fragment.
 */
@GraphQLMarker
public sealed interface GraphQLVariableBuilder : GraphQLVariableBuilderScope, GraphQLDirectivesContainer {

	/** Builds and returns the [GVariableDefinition]. */
	public fun build(): GVariableDefinition
}


/**
 * Scope interface for [GraphQLVariableBuilder].
 *
 * Provides typed [default] overloads for setting the variable's default value.
 */
@GraphQLMarker
public sealed interface GraphQLVariableBuilderScope : GraphQLValueContainerScope, GraphQLDirectivesContainerScope {

	/** Sets the default value to a [Boolean]. */
	public fun default(value: Boolean) {
		default(GBooleanValue(value))
	}


	/** Sets the default value to a nullable [Boolean]. */
	public fun default(value: Boolean?) {
		default(value?.let(::GBooleanValue) ?: GNullValue())
	}


	/** Sets the default value to a [Byte] (coerced to GraphQL Int). */
	public fun default(value: Byte) {
		default(GIntValue(value))
	}


	/** Sets the default value to a nullable [Byte] (coerced to GraphQL Int). */
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun default(value: Byte?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Sets the default value to a [Double] (coerced to GraphQL Float). */
	public fun default(value: Double) {
		default(GFloatValue(value))
	}


	/** Sets the default value to a nullable [Double] (coerced to GraphQL Float). */
	public fun default(value: Double?) {
		default(value?.let(::GFloatValue) ?: GNullValue())
	}


	/** Sets the default value to a [Float] (coerced to GraphQL Float). */
	public fun default(value: Float) {
		default(GFloatValue(value))
	}


	/** Sets the default value to a nullable [Float] (coerced to GraphQL Float). */
	public fun default(value: Float?) {
		default(value?.let(::GFloatValue) ?: GNullValue())
	}


	/** Sets the default value to an [Int]. */
	public fun default(value: Int) {
		default(GIntValue(value))
	}


	/** Sets the default value to a nullable [Int]. */
	public fun default(value: Int?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Sets the default value to null. */
	public fun default(value: Nothing?) {
		default(GNullValue())
	}


	/** Sets the default value to a [Short] (coerced to GraphQL Int). */
	public fun default(value: Short) {
		default(GIntValue(value))
	}


	/** Sets the default value to a nullable [Short] (coerced to GraphQL Int). */
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun default(value: Short?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Sets the default value to a nullable [String]. */
	public fun default(value: String?) {
		default(value?.let(::GStringValue) ?: GNullValue())
	}


	/** Sets the default value to a [UByte] (coerced to GraphQL Int). */
	public fun default(value: UByte) {
		default(GIntValue(value))
	}


	/** Sets the default value to a nullable [UByte] (coerced to GraphQL Int). */
	public fun default(value: UByte?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Sets the default value to a [UShort] (coerced to GraphQL Int). */
	public fun default(value: UShort) {
		default(GIntValue(value))
	}


	/** Sets the default value to a nullable [UShort] (coerced to GraphQL Int). */
	public fun default(value: UShort?) {
		default(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Sets the default value for the variable using a raw [GValue]. */
	public fun default(value: GValue)


	// TODO Move to extension and inline once we have context receivers.
	/** Sets the default value to an object value built with the [GraphQLArgumentsBuilder] DSL. */
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


/** Creates a new [GraphQLVariableBuilder] for a variable with the given [name] and [type]. */
public fun GraphQLVariableBuilder(name: String, type: GTypeRef): GraphQLVariableBuilder =
	GraphQLVariableBuilderImpl(name = name, type = type)
