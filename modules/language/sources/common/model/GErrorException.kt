package io.fluidsonic.graphql


class GErrorException(val errors: List<GError>) : RuntimeException() {

	init {
		require(errors.isNotEmpty()) { "'errors' must contain at least one error." }
	}


	override val message: String
		get() = errors.joinToString(separator = "\n\n---\n\n") { it.describe() }
}
