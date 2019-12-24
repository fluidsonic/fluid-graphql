package io.fluidsonic.graphql


sealed class GResult<out Value> {

	abstract val errors: List<GError>
	abstract val value: Value?


	class Failure(errors: List<GError>) : GResult<Nothing>() {

		init {
			require(errors.isNotEmpty()) { "'errors' must not be empty." }
		}


		override val errors = errors.toList()


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


	companion object


	class Builder {

		@PublishedApi
		internal var errors: MutableList<GError>? = null


		fun collectError(error: GError): Nothing? {
			errors?.add(error)
				?: run { errors = mutableListOf(error) }

			return null
		}


		fun failWith(error: GError): Failure {
			collectError(error)

			return Failure(errors.orEmpty())
		}


		inline fun <Value> run(block: Builder.() -> Value): GResult<Value> {
			val value = block()

			return errors
				?.let(::Failure)
				?: Success(value)
		}


		fun <Value> GPartialResult<Value>.consumeFailure(): Value {
			this@Builder.errors?.addAll(errors)
				?: run { this@Builder.errors = errors.toMutableList() }

			return value
		}


		fun <Value> GResult<Value>.orNull(): Value? =
			when (this) {
				is Success -> value
				is Failure -> {
					this@Builder.errors?.addAll(errors)
						?: run { this@Builder.errors = errors.toMutableList() }

					null
				}
			}


		inline fun <Value> GPartialResult<Value>.or(block: (failure: Failure) -> Nothing): Value =
			when {
				errors.isEmpty() -> value
				else -> {
					consumeFailure()

					block(Failure(this@Builder.errors.orEmpty()))
				}
			}


		inline fun <Value> GResult<Value>.or(block: (failure: Failure) -> Nothing): Value =
			when (this) {
				is Success -> value
				is Failure -> {
					orNull()

					block(Failure(this@Builder.errors.orEmpty()))
				}
			}
	}
}


inline fun <Value> GResult(block: GResult.Builder.() -> Value) =
	GResult.Builder().run(block)


inline fun <Value> GResult<Value>.consumeFailure(block: (failure: GResult.Failure) -> Nothing): Value =
	when (this) {
		is GResult.Failure -> block(this)
		is GResult.Success -> value
	}


inline fun <Value> GResult<Value>.map(block: (value: Value) -> GResult<Value>): GResult<Value> =
	when (this) {
		is GResult.Failure -> this
		is GResult.Success -> block(value)
	}


inline fun <Value> GResult<Value>.or(block: (failure: GResult.Failure) -> Nothing): Value =
	when (this) {
		is GResult.Success -> value
		is GResult.Failure -> block(this)
	}
