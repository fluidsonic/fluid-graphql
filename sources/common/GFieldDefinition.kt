package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#FieldsDefinition
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Field-Type
class GFieldDefinition(
	val name: String,
	val type: GType,
	arguments: List<GArgumentDefinition> = emptyList(),
	description: String? = null,
	val directives: List<GDirective> = emptyList(),
	val isDeprecated: Boolean = false,
	deprecationReason: String? = null
) {

	val arguments: Map<String, GArgumentDefinition>
	val description = description?.ifEmpty { null }
	val deprecationReason = deprecationReason?.ifEmpty { null }


	init {
		require(arguments.size <= 1 || arguments.mapTo(hashSetOf()) { it.name }.size == arguments.size) {
			"'arguments' must not contain multiple elements with the same name: $arguments"
		}

		require(type.isOutputType()) { "'type' must be an output type: $type" }

		this.arguments = arguments.associateBy { it.name }
	}


	override fun toString() =
		GWriter { writeFieldDefinition(this@GFieldDefinition) }


	companion object


	class Unresolved(
		val name: String,
		val type: GTypeRef,
		val arguments: List<GArgumentDefinition.Unresolved>,
		val description: String? = null,
		val directives: List<GDirective> = emptyList(),
		val isDeprecated: Boolean = false,
		val deprecationReason: String? = null
	) {

		fun resolve(typeRegistry: GTypeRegistry) = GFieldDefinition(
			arguments = arguments.map { it.resolve(typeRegistry) },
			deprecationReason = deprecationReason,
			description = description,
			directives = directives,
			isDeprecated = isDeprecated,
			name = name,
			type = typeRegistry.resolve(type)
		)


		companion object
	}
}
