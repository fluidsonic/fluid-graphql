package io.fluidsonic.graphql


class GPartialResult<out Value>(
	errors: List<GError>,
	val value: Value
) {

	val errors = errors.toList()


	override fun equals(other: Any?) =
		this === other || (other is GPartialResult<*> && errors == other.errors && value == other.value)


	override fun hashCode() =
		errors.hashCode() xor value.hashCode()


	override fun toString() =
		if (errors.isNotEmpty())
			"GResult(value = $value, errors = $errors)"
		else
			"GResult(value = $value)"


	companion object


	class Builder {

		@PublishedApi
		internal var errors: MutableList<GError>? = null


		fun collectError(error: GError): Nothing? {
			errors?.add(error)
				?: run { errors = mutableListOf(error) }

			return null
		}


		private fun collectErrors(errors: List<GError>) {
			if (errors.isEmpty())
				return

			this.errors?.addAll(errors)
				?: run { this.errors = errors.toMutableList() }
		}


		inline fun <Value> build(block: Builder.() -> Value): GPartialResult<Value> {
			val value = block()
			if (value is GResult<*> || value is GPartialResult<*>)
				error("Unexpected result type in GResult {} block.")

			return GPartialResult(errors = errors.orEmpty(), value = value)
		}


		fun <Value> GPartialResult<Value>.consumeFailure() =
			consumeErrors { failure ->
				collectErrors(failure.errors)
			}


		fun <Value> GResult<Value>.orNull() =
			when (this) {
				is GResult.Failure -> {
					collectErrors(errors)

					null
				}

				is GResult.Success ->
					value
			}


		inline fun <Value> GResult<Value>.mapSuccess(block: (value: Value) -> GResult<Value>): GResult<Value> =
			when (this) {
				is GResult.Failure -> this
				is GResult.Success -> block(value)
			}
	}
}


inline fun <Value> GPartialResult(block: GPartialResult.Builder.() -> Value) =
	GPartialResult.Builder().build(block)


inline fun <Value> GPartialResult<Value>.consumeErrors(block: (failure: GPartialResult<Value>) -> Unit): Value {
	if (errors.isNotEmpty())
		block(this)

	return value
}
