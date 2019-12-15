package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#FieldsDefinition
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Field-Type
class GFieldDefinition(
	val name: String,
	val type: GType,
	arguments: List<GArgumentDefinition> = emptyList(),
	description: String? = null,
	val directives: List<GDirective> = emptyList(),
	val resolver: GFieldResolver<*>? = null
) {

	val arguments: Map<String, GArgumentDefinition>
	val deprecationReason: String?
	val description = description?.ifEmpty { null }
	val isDeprecated: Boolean


	init {
		require(arguments.size <= 1 || arguments.mapTo(hashSetOf()) { it.name }.size == arguments.size) {
			"'arguments' must not contain multiple elements with the same name: $arguments"
		}

		require(type.isOutputType()) { "'type' must be an output type: $type" }

		this.arguments = arguments.associateBy { it.name }

		val deprecation = directives.firstOrNull { it.name == GSpecification.defaultDeprecatedDirective.name }

		deprecationReason = (deprecation?.arguments?.get("reason")?.value as? String)?.ifEmpty { null }
		isDeprecated = deprecation != null
	}


	override fun toString() =
		GWriter { writeFieldDefinition(this@GFieldDefinition) }


	companion object {

		internal fun build(ast: AstNode.FieldDefinition) =
			Unresolved(
				arguments = ast.arguments.map { GArgumentDefinition.build(it) },
				description = ast.description?.value,
				directives = ast.directives.map { GDirective.build(it) },
				name = ast.name.value,
				type = GTypeRef.build(ast.type)
			)
	}


	class Unresolved(
		val name: String,
		val type: GTypeRef,
		val arguments: List<GArgumentDefinition.Unresolved>,
		val description: String? = null,
		val directives: List<GDirective> = emptyList(),
		val resolver: GFieldResolver<*>? = null
	) {

		fun resolve(typeRegistry: GTypeRegistry) = GFieldDefinition(
			arguments = arguments.map { it.resolve(typeRegistry) },
			description = description,
			directives = directives,
			name = name,
			resolver = resolver,
			type = typeRegistry.resolve(type)
		)


		companion object
	}
}
