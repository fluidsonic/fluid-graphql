package io.fluidsonic.graphql

import kotlin.internal.*
import kotlin.js.*


@GraphQLMarker
public sealed interface GraphQLValueListBuilder : GraphQLValueListBuilderScope {

	public fun build(): GListValue
}


@GraphQLMarker
public sealed interface GraphQLValueListBuilderScope : GraphQLValueContainerScope {

	@GraphQLMarker
	public fun add(value: Boolean) {
		add(GBooleanValue(value))
	}


	@GraphQLMarker
	public fun add(value: Boolean?) {
		add(value?.let(::GBooleanValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: Double) {
		add(GFloatValue(value))
	}


	@GraphQLMarker
	public fun add(value: Double?) {
		add(value?.let(::GFloatValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: Float) {
		add(GFloatValue(value))
	}


	@GraphQLMarker
	public fun add(value: Float?) {
		add(value?.let(::GFloatValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: Byte) {
		add(GIntValue(value.toInt()))
	}


	@GraphQLMarker
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun add(value: Byte?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: Int) {
		add(GIntValue(value))
	}


	@GraphQLMarker
	public fun add(value: Int?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: Nothing?) {
		add(GNullValue())
	}


	@GraphQLMarker
	public fun add(value: Short) {
		add(GIntValue(value.toInt()))
	}


	@GraphQLMarker
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public fun add(value: Short?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: String?) {
		add(value?.let(::GStringValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: UByte) {
		add(GIntValue(value.toInt()))
	}


	@GraphQLMarker
	public fun add(value: UByte?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public fun add(value: UShort) {
		add(GIntValue(value.toInt()))
	}


	@GraphQLMarker
	public fun add(value: UShort?) {
		add(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
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


@JsName("_GraphQLValueListBuilder")
public fun GraphQLValueListBuilder(): GraphQLValueListBuilder =
	GraphQLValueListBuilderImpl()


@GraphQLMarker
@Suppress("unused")
public inline fun GraphQLValueListBuilder.list(configure: GraphQLValueListBuilder.() -> Unit): GListValue =
	GraphQLValueListBuilder().apply(configure).build()


@GraphQLMarker
@Suppress("unused")
public inline fun GraphQLValueListBuilder.obj(configure: GraphQLArgumentsBuilder.() -> Unit): GObjectValue =
	GObjectValue(GraphQLArgumentsBuilder().apply(configure).build())
