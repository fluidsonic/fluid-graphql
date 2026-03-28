package io.fluidsonic.graphql

import kotlin.internal.*


/**
 * Builder for a [GListValue].
 *
 * Use [add] to append values and [build] to produce the final list.
 */
@GraphQLMarker
public sealed interface GraphQLValueListBuilder : GraphQLValueListBuilderScope {

	/** Builds and returns the [GListValue]. */
	public fun build(): GListValue
}


/**
 * Scope interface for [GraphQLValueListBuilder].
 *
 * Provides typed [add] overloads for all supported GraphQL scalar types and raw [GValue] nodes.
 */
@GraphQLMarker
public sealed interface GraphQLValueListBuilderScope : GraphQLValueContainerScope {

	/** Appends a [Boolean] value to the list. */
	public fun add(value: Boolean) {
		add(GBooleanValue(value))
	}


	/** Appends a nullable [Boolean] value to the list. */
	public fun add(value: Boolean?) {
		add(value?.let(::GBooleanValue) ?: GNullValue())
	}


	/** Appends a [Double] value to the list (coerced to GraphQL Float). */
	public fun add(value: Double) {
		add(GFloatValue(value))
	}


	/** Appends a nullable [Double] value to the list (coerced to GraphQL Float). */
	public fun add(value: Double?) {
		add(value?.let(::GFloatValue) ?: GNullValue())
	}


	/** Appends a [Float] value to the list (coerced to GraphQL Float). */
	public fun add(value: Float) {
		add(GFloatValue(value))
	}


	/** Appends a nullable [Float] value to the list (coerced to GraphQL Float). */
	public fun add(value: Float?) {
		add(value?.let(::GFloatValue) ?: GNullValue())
	}


	/** Appends a [Byte] value to the list (coerced to GraphQL Int). */
	public fun add(value: Byte) {
		add(GIntValue(value.toInt()))
	}


	/** Appends a nullable [Byte] value to the list (coerced to GraphQL Int). */
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun add(value: Byte?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Appends an [Int] value to the list. */
	public fun add(value: Int) {
		add(GIntValue(value))
	}


	/** Appends a nullable [Int] value to the list. */
	public fun add(value: Int?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Appends a null value to the list. */
	public fun add(value: Nothing?) {
		add(GNullValue())
	}


	/** Appends a [Short] value to the list (coerced to GraphQL Int). */
	public fun add(value: Short) {
		add(GIntValue(value.toInt()))
	}


	/** Appends a nullable [Short] value to the list (coerced to GraphQL Int). */
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun add(value: Short?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Appends a nullable [String] value to the list. */
	public fun add(value: String?) {
		add(value?.let(::GStringValue) ?: GNullValue())
	}


	/** Appends a [UByte] value to the list (coerced to GraphQL Int). */
	public fun add(value: UByte) {
		add(GIntValue(value.toInt()))
	}


	/** Appends a nullable [UByte] value to the list (coerced to GraphQL Int). */
	public fun add(value: UByte?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Appends a [UShort] value to the list (coerced to GraphQL Int). */
	public fun add(value: UShort) {
		add(GIntValue(value.toInt()))
	}


	/** Appends a nullable [UShort] value to the list (coerced to GraphQL Int). */
	public fun add(value: UShort?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	/** Appends a raw [GValue] to the list. */
	public fun add(value: GValue)
}


private class GraphQLValueListBuilderImpl : GraphQLValueListBuilder {

	private val values = mutableListOf<GValue>()


	override fun add(value: GValue) {
		values += value
	}


	override fun build(): GListValue =
		GListValue(values.toList())
}


/** Creates a new [GraphQLValueListBuilder]. */
public fun GraphQLValueListBuilder(): GraphQLValueListBuilder =
	GraphQLValueListBuilderImpl()


/** Creates a nested [GListValue] inside a list builder. */
@Suppress("UnusedReceiverParameter")
public inline fun GraphQLValueListBuilder.list(configure: GraphQLValueListBuilder.() -> Unit): GListValue =
	GraphQLValueListBuilder().apply(configure).build()


/** Creates a [GObjectValue] inside a list builder using the [GraphQLArgumentsBuilder] DSL. */
@Suppress("UnusedReceiverParameter")
public inline fun GraphQLValueListBuilder.obj(configure: GraphQLArgumentsBuilder.() -> Unit): GObjectValue =
	GObjectValue(GraphQLArgumentsBuilder().apply(configure).build())
