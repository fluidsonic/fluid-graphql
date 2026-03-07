package io.fluidsonic.graphql


/**
 * The result of a GraphQL operation that may succeed with a value or fail with errors.
 *
 * A [Success] carries a [Value] and an optional list of non-fatal errors.
 * A [Failure] has no value and at least one error.
 *
 * Use the companion factory functions [GResult.success] and [GResult.failure] to create instances.
 * Use extension functions like [mapValue], [flatMapValue], [ifErrors], and [flatten] to transform results.
 */
public sealed class GResult<out Value>(
	public val errors: List<GError>
) {

	abstract override fun equals(other: Any?): Boolean
	abstract override fun hashCode(): Int
	abstract override fun toString(): String


	/** Returns the value if this is a [Success], or `null` if this is a [Failure]. */
	public fun valueOrNull(): Value? = when (this) {
		is Success -> value
		is Failure -> null
	}


	/** Returns the value if this is a [Success], or throws a [GErrorException] if this is a [Failure]. */
	public fun valueOrThrow(): Value = when (this) {
		is Success -> value
		is Failure -> throw GErrorException(errors)
	}


	/** Returns the value only if this is a [Success] with no errors, otherwise `null`. */
	public fun valueWithoutErrorsOrNull(): Value? = when (this) {
		is Success -> if (errors.isEmpty()) value else null
		is Failure -> null
	}


	/** Returns the value if this is a [Success] with no errors, or throws a [GErrorException] otherwise. */
	public fun valueWithoutErrorsOrThrow(): Value = when (this) { // FIXME refactor
		is Success -> if (errors.isEmpty()) value else throw GErrorException(errors)
		is Failure -> throw GErrorException(errors)
	}


	public companion object {

		private val nullResult = Success(null)


		/**
		 * Runs [action] and wraps the result in a [Success], or catches any [GErrorException]
		 * thrown and returns it as a [Failure].
		 */
		public inline fun <Value> catchErrors(action: () -> Value): GResult<Value> =
			try {
				success(action())
			}
			catch (exception: GErrorException) {
				failure(exception.errors)
			}


		public fun failure(errors: List<GError>): GResult<Nothing> =
			Failure(errors)


		public fun failure(error: GError): GResult<Nothing> =
			Failure(error)


		public fun success(): GResult<Nothing?> =
			success(value = null)


		@Suppress("UNCHECKED_CAST")
		public fun <Value> success(value: Value, errors: List<GError> = emptyList()): GResult<Value> =
			when {
				(value as Any?) == null && errors.isEmpty() -> nullResult as Success<Value>
				else -> Success(value = value, errors = errors)
			}
	}


	@PublishedApi
	internal class Failure(
		errors: List<GError>
	) : GResult<Nothing>(errors = errors) {

		init {
			require(errors.isNotEmpty()) { "'errors' must not be empty in the failure case." }
		}


		constructor(error: GError) :
			this(listOf(error))


		override fun equals(other: Any?) =
			this === other || (other is Failure && errors == other.errors)


		override fun hashCode() =
			errors.hashCode()


		override fun toString() =
			"Failure($errors)"
	}


	@PublishedApi
	internal class Success<Value>(
		val value: Value,
		errors: List<GError> = emptyList()
	) : GResult<Value>(errors = errors) {

		init {
			require(value !is GResult<*>) { "A GResult cannot be the value of another GResult." }
		}


		override fun equals(other: Any?) =
			this === other || (other is Success<*> && value == other.value && errors == other.errors)


		override fun hashCode() =
			value.hashCode() xor errors.hashCode()


		override fun toString() = buildString {
			append("Success(")
			append(value)

			if (errors.isNotEmpty()) {
				append(", errors = ")
				append(errors)
			}

			append(")")
		}
	}
}


/**
 * Returns the value if there are no errors, or calls [action] with the error list otherwise.
 *
 * The [action] must not return normally — it must throw or call a non-local return.
 * This makes it convenient for early-exit patterns:
 * ```
 * val value = result.ifErrors { errors -> return GResult.failure(errors) }
 * ```
 */
public inline fun <Value> GResult<Value>.ifErrors(action: (result: List<GError>) -> Nothing): Value =
	when {
		errors.isNotEmpty() -> action(errors)
		else -> (this as GResult.Success).value
	}


/** If this is a [GResult.Failure], replaces it with the result of [action]; otherwise returns this unchanged. */
public inline fun <Value> GResult<Value>.flatMapErrors(action: (errors: List<GError>) -> GResult<Value>): GResult<Value> =
	when (this) {
		is GResult.Failure -> action(errors)
		is GResult.Success -> this
	}


/**
 * If this is a [GResult.Success], passes the value to [action] and returns its result,
 * merging any accumulated errors. If this is a [GResult.Failure], returns this unchanged.
 */
public inline fun <Value, TransformedValue> GResult<Value>.flatMapValue(action: (value: Value) -> GResult<TransformedValue>): GResult<TransformedValue> =
	when (this) {
		is GResult.Success -> {
			val transformed = action(value)
			when {
				errors.isNotEmpty() -> transformed.mapErrors { transformedErrors ->
					when {
						transformedErrors.isNotEmpty() -> errors + transformedErrors
						else -> errors
					}
				}

				else -> transformed
			}
		}

		is GResult.Failure -> this
	}


/** Transforms the error list of this result using [action], leaving the value unchanged. */
public inline fun <Value> GResult<Value>.mapErrors(action: (errors: List<GError>) -> List<GError>): GResult<Value> =
	when (this) {
		is GResult.Failure -> GResult.failure(action(errors))
		is GResult.Success -> GResult.success(value = value, errors = action(errors))
	}


/**
 * Transforms the value of a [GResult.Success] using [action], preserving errors.
 * If this is a [GResult.Failure], returns this unchanged.
 */
public inline fun <Value, TransformedValue> GResult<Value>.mapValue(action: (value: Value) -> TransformedValue): GResult<TransformedValue> =
	when (this) {
		is GResult.Success -> GResult.success(value = action(value), errors = errors)
		is GResult.Failure -> GResult.failure(errors)
	}


/**
 * Combines a collection of results into a single result.
 *
 * Collects all errors from every element. If any element is a [GResult.Failure], the combined
 * result is also a failure. Otherwise, returns a [GResult.Success] with all values as a list.
 */
public fun <Value> Iterable<GResult<Value>>.flatten(): GResult<List<Value>> {
	val errors = mutableListOf<GError>()
	val values = mutableListOf<Value>()
	var isFailure = false

	forEach { result ->
		errors += result.errors

		when (result) {
			is GResult.Success -> values += result.value
			is GResult.Failure -> isFailure = true
		}
	}

	return when (isFailure) {
		true -> GResult.failure(errors)
		false -> GResult.success(value = values, errors = errors)
	}
}


/**
 * Combines a map of results into a single result keyed by the same keys.
 *
 * Collects all errors from every entry. If any entry is a [GResult.Failure], the combined
 * result is also a failure. Otherwise, returns a [GResult.Success] with a map of all values.
 */
public fun <Key, Value> Map<Key, GResult<Value>>.flatten(): GResult<Map<Key, Value>> {
	val errors = mutableListOf<GError>()
	val values = mutableMapOf<Key, Value>()
	var isFailure = false

	forEach { (key, result) ->
		errors += result.errors

		when (result) {
			is GResult.Success -> values[key] = result.value
			is GResult.Failure -> isFailure = true
		}
	}

	return when (isFailure) {
		true -> GResult.failure(errors)
		false -> GResult.success(value = values, errors = errors)
	}
}
