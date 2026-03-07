package io.fluidsonic.graphql


/**
 * A [RuntimeException] that carries one or more [GError]s.
 *
 * Thrown by [GResult.valueOrThrow], [GResult.valueWithoutErrorsOrThrow], and [GError.throwException].
 * Use [toFailure] to convert back to a [GResult.Failure].
 */
public class GErrorException(public val errors: List<GError>) : RuntimeException() {

	init {
		require(errors.isNotEmpty()) { "'errors' must contain at least one error." }
	}


	public constructor(error: GError) :
		this(listOf(error))


	override val message: String
		get() = errors.joinToString(separator = "\n\n---\n\n") { it.describe() }


	/** Converts this exception into a [GResult.Failure] containing the same errors. */
	public fun toFailure(): GResult<Nothing> =
		GResult.failure(errors)
}
