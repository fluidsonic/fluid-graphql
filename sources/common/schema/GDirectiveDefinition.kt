package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Directives
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Directive-Type
class GDirectiveDefinition(
	val name: String,
	val locations: List<GDirectiveLocation>,
	arguments: List<GArgumentDefinition> = emptyList(),
	description: String? = null
) {

	val arguments: Map<String, GArgumentDefinition>
	val description = description?.ifEmpty { null }


	init {
		require(arguments.size <= 1 || arguments.mapTo(hashSetOf()) { it.name }.size == arguments.size) {
			"'arguments' must not contain multiple elements with the same name: $arguments"
		}

		this.arguments = arguments.associateBy { it.name }
	}


	override fun toString() =
		GWriter { writeDirectiveDefinition(this@GDirectiveDefinition) }


	companion object {

		internal fun build(ast: AstNode.Definition.TypeSystem.Directive) =
			Unresolved(
				arguments = ast.arguments.map { GArgumentDefinition.build(it) },
				description = ast.description?.value,
				locations = ast.locations.map { GDirectiveLocation.build(it) },
				name = ast.name.value
			)
	}


	class Unresolved(
		val name: String,
		val locations: List<GDirectiveLocation>,
		val arguments: List<GArgumentDefinition.Unresolved> = emptyList(),
		val description: String? = null
	) {

		fun resolve(typeRegistry: GTypeRegistry) = GDirectiveDefinition(
			arguments = arguments.map { it.resolve(typeRegistry) },
			description = description,
			locations = locations,
			name = name
		)

		companion object
	}
}
