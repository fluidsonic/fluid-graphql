package io.fluidsonic.graphql


sealed class GResult<out Value> {

	abstract val errors: List<GError>
	abstract val value: Value?


	inline fun onFailure(block: (failure: Failure) -> Nothing): Value =
		when (this) {
			is Failure -> block(this)
			is Success -> value
		}


	class Failure(override val errors: List<GError>) : GResult<Nothing>() {

		constructor(error: GError)
			: this(listOf(error))


		override fun equals(other: Any?) =
			this === other || (other is Failure && errors == other.errors)


		override fun hashCode() =
			errors.hashCode()


		override fun toString() =
			"Failure($errors)"


		@Deprecated(level = DeprecationLevel.HIDDEN, message = "useless")
		override val value
			get() = null
	}


	class Success<Value>(override val value: Value) : GResult<Value>() {

		override fun equals(other: Any?) =
			this === other || (other is Success<*> && value == other.value)


		@Deprecated(level = DeprecationLevel.HIDDEN, message = "useless")
		override val errors =
			emptyList<Nothing>()


		override fun hashCode() =
			value.hashCode()


		override fun toString() =
			"Success($value)"
	}


	companion object {

		inline fun <Value> collect(block: (addError: (message: String) -> Unit) -> Value): GResult<Value> {
			val errors = mutableListOf<GError>()
			val value = block { errors += GError(it) }

			return if (errors.isNotEmpty())
				Failure(errors)
			else
				Success(value)
		}
	}
}
