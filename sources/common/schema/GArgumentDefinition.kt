package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#InputValueDefinition
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__InputValue-Type
class GArgumentDefinition(
	val name: String,
	val type: GType,
	val defaultValue: Any? = null,
	description: String? = null,
	val directives: List<GDirective> = emptyList()
) {

	val description = description?.ifEmpty { null }


	init {
		require(type.isInputType()) { "'type' must be an input type: $type" }
	}


	override fun toString() =
		GWriter { writeArgumentDefinition(this@GArgumentDefinition) }


	companion object {

		internal fun build(ast: AstNode.ArgumentDefinition) =
			Unresolved(
				defaultValue = ast.defaultValue, // FIXME
				description = ast.description?.value,
				directives = ast.directives.map { GDirective.build(it) },
				name = ast.name.value,
				type = GTypeRef.build(ast.type)
			)
	}


	class Unresolved(
		val name: String,
		val type: GTypeRef,
		val defaultValue: Any? = null,
		val description: String? = null,
		val directives: List<GDirective> = emptyList()
	) {

		fun resolve(typeRegistry: GTypeRegistry) = GArgumentDefinition(
			defaultValue = defaultValue,
			description = description,
			directives = directives,
			name = name,
			type = typeRegistry.resolve(type)
		)


		companion object
	}
}
