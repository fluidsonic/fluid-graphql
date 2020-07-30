package io.fluidsonic.graphql


public class GErrorException(public val errors: List<GError>) : RuntimeException() {

	init {
		require(errors.isNotEmpty()) { "'errors' must contain at least one error." }
	}


	public constructor(error: GError) :
		this(listOf(error))


	override val message: String
		get() = errors.joinToString(separator = "\n\n---\n\n") { it.describe() }
}
