package io.fluidsonic.graphql

import kotlin.internal.*
import kotlin.js.*


// https://youtrack.jetbrains.com/issue/KT-52764/KJS-IR-Module-has-a-reference-to-symbol-caused-by-sealed-interface-with-private-inheritors
@GraphQLMarker
public /* sealed */ interface GraphQLArgumentsBuilder : GraphQLArgumentsBuilderScope {

	@GraphQLMarker
	public fun argument(argument: GArgument)


	@GraphQLMarker
	public override infix fun String.to(value: GValue) {
		argument(GArgument(name = this, value = value))
	}


	public fun build(): List<GArgument>
}


@GraphQLMarker
public /* sealed */ interface GraphQLArgumentsBuilderScope : GraphQLValueContainerScope {

	@GraphQLMarker
	public infix fun String.to(value: Boolean) {
		to(GBooleanValue(value))
	}


	@GraphQLMarker
	public infix fun String.to(value: Boolean?) {
		to(value?.let(::GBooleanValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: Byte) {
		to(GIntValue(value))
	}


	@GraphQLMarker
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public infix fun String.to(value: Byte?) {
		to(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: Double) {
		to(GFloatValue(value))
	}


	@GraphQLMarker
	public infix fun String.to(value: Double?) {
		to(value?.let(::GFloatValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: Float) {
		to(GFloatValue(value))
	}


	@GraphQLMarker
	public infix fun String.to(value: Float?) {
		to(value?.let(::GFloatValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: Int) {
		to(GIntValue(value))
	}


	@GraphQLMarker
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public infix fun String.to(value: Int?) {
		to(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: Nothing?) {
		to(GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: Short) {
		to(GIntValue(value))
	}


	@GraphQLMarker
	@LowPriorityInOverloadResolution // https://youtrack.jetbrains.com/issue/KT-645
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	public infix fun String.to(value: Short?) {
		to(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: UByte) {
		to(GIntValue(value))
	}


	@GraphQLMarker
	public infix fun String.to(value: UByte?) {
		to(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: UShort) {
		to(GIntValue(value))
	}


	@GraphQLMarker
	public infix fun String.to(value: UShort?) {
		to(value?.let(::GIntValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: String?) {
		to(value?.let(::GStringValue) ?: GNullValue())
	}


	@GraphQLMarker
	public infix fun String.to(value: GValue)


	@Deprecated(level = DeprecationLevel.ERROR, message = "Cannot use Kotlin's default 'to' in this context.")
	@GraphQLMarker
	@Suppress("DeprecatedCallableAddReplaceWith")
	public infix fun <A, B> A.to(that: B) {
		error("Cannot use Kotlin's default 'to' in this context.")
	}


	// TODO Move to extension and inline once we have context receivers.
	@GraphQLMarker
	public infix fun String.to(configure: GraphQLArgumentsBuilder.() -> Unit) {
		to(GObjectValue(GraphQLArgumentsBuilder().apply(configure).build()))
	}
}


private class GraphQLArgumentsBuilderImpl : GraphQLArgumentsBuilder {

	private val arguments = mutableListOf<GArgument>()


	override fun argument(argument: GArgument) {
		val name = argument.name

		check(GLanguage.isValidName(name)) { "Invalid argument name: $name" }
		check(arguments.none { it.name == name }) { "Cannot specify multiple arguments with the same name: $name" }

		arguments += argument
	}


	override fun build(): List<GArgument> =
		arguments.toList()
}


@JsName("_GraphQLArgumentsBuilder")
public fun GraphQLArgumentsBuilder(): GraphQLArgumentsBuilder =
	GraphQLArgumentsBuilderImpl()
