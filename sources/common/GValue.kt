package io.fluidsonic.graphql


sealed class GValue {

	override fun toString() =
		GWriter { writeValue(this@GValue) }


	companion object {

		fun from(value: Any?): GValue =
			when (value) {
				null -> GNullValue
				is GValue -> value
				is Boolean -> GBooleanValue(value)
				is Double -> GFloatValue(value)
				is Float -> GFloatValue(value.toDouble())
				is GError -> from(mapOf(
					"message" to value.message
				))
				is Int -> GIntValue(value)
				is List<*> -> GListValue(value.map(::from))
				is String -> GStringValue(value)
				is Map<*, *> -> GObjectValue(
					value
						.map { it.key as String to from(it.value) }
						.toMap()
				)
				else -> error("Invalid GQL value type: ${value::class} = $value")
			}
	}
}


sealed class GBooleanValue(val value: Boolean) : GValue() {

	object False : GBooleanValue(false)
	object True : GBooleanValue(true)


	companion object {

		operator fun invoke(value: Boolean) =
			if (value) True else False
	}
}


class GEnumValue(val value: String) : GValue() {

	override fun equals(other: Any?) =
		this === other || (other is GEnumValue && value == other.value)


	override fun hashCode() =
		value.hashCode()
}


class GFloatValue(val value: Double) : GValue() {

	override fun equals(other: Any?) =
		this === other || (other is GFloatValue && value == other.value)


	override fun hashCode() =
		value.hashCode()
}


class GIntValue(val value: Int) : GValue() {

	override fun equals(other: Any?) =
		this === other || (other is GIntValue && value == other.value)


	override fun hashCode() =
		value.hashCode()
}


class GListValue(val value: List<GValue>) : GValue() {

	override fun equals(other: Any?) =
		this === other || (other is GListValue && value == other.value)


	override fun hashCode() =
		value.hashCode()
}


object GNullValue : GValue()


class GObjectValue(val value: Map<String, GValue>) : GValue() {

	// TODO does equality depend on entry order?
	override fun equals(other: Any?) =
		this === other || (other is GObjectValue && value == other.value)


	override fun hashCode() =
		value.hashCode()
}


class GStringValue(val value: String) : GValue() {

	override fun equals(other: Any?) =
		this === other || (other is GStringValue && value == other.value)


	override fun hashCode() =
		value.hashCode()
}


class GVariableReference(val name: String) : GValue() {

	override fun equals(other: Any?) =
		this === other || (other is GVariableReference && name == other.name)


	override fun hashCode() =
		name.hashCode()
}
