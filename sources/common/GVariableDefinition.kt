package io.fluidsonic.graphql


class GVariableDefinition(
	val name: String,
	val type: GTypeRef,
	val defaultValue: GValue? = null
)
