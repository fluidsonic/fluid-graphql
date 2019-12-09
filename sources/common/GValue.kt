package io.fluidsonic.graphql


sealed class GValue {

	sealed class Boolean(val value: kotlin.Boolean) : GValue() {

		object False : Boolean(false)
		object True : Boolean(true)


		companion object {

			operator fun invoke(value: kotlin.Boolean) =
				if (value) True else False
		}
	}


	class EnumValue(val value: kotlin.String) : GValue()
	class Float(val value: Double) : GValue()
	class Int(val value: kotlin.Int) : GValue()
	class List(val value: kotlin.collections.List<GValue>) : GValue()
	object Null : GValue()
	class String(val value: kotlin.String) : GValue()

	class Object(
		val value: Map<kotlin.String, GValue>
	) : GValue()

	class Variable(
		val name: kotlin.String
	) : GValue()


	companion object {

		fun from(value: Any?): GValue =
			when (value) {
				null -> Null
				is GValue -> value
				is kotlin.Boolean -> Boolean(value)
				is Double -> Float(value)
				is kotlin.Float -> Float(value.toDouble())
				is kotlin.Int -> Int(value)
				is kotlin.collections.List<*> -> List(value.map(::from))
				is kotlin.String -> String(value)
				is Map<*, *> -> Object(
					value
						.map { it.key as kotlin.String to from(it.value) }
						.toMap()
				)
				else -> error("Invalid GQL value type: ${value::class} = $value")
			}
	}
}
