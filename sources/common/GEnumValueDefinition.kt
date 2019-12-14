package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#EnumValue
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__EnumValue-Type
class GEnumValueDefinition(
	val name: String,
	val description: String? = null,
	val directives: List<GDirective> = emptyList(),
	val isDeprecated: Boolean = false,
	deprecationReason: String? = null
) {

	val deprecationReason = deprecationReason?.ifEmpty { null }


	override fun toString() =
		GWriter { writeEnumValueDefinition(this@GEnumValueDefinition) }


	companion object
}
