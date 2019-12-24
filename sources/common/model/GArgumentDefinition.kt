package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#InputValueDefinition
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__InputValue-Type
class GArgumentDefinition(
	val name: String,
	val type: GType,
	val defaultValue: Optional<Any?>? = null,
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

		fun from(ast: GAst.ArgumentDefinition) =
			Unresolved(
				defaultValue = ast.defaultValue,
				description = ast.description?.value,
				directives = ast.directives.map { GDirective.from(it) },
				name = ast.name.value,
				type = GTypeRef.from(ast.type)
			)
	}


	class Unresolved(
		val name: String,
		val type: GTypeRef,
		val defaultValue: GAst.Value? = null, // FIXME no AST here
		val description: String? = null,
		val directives: List<GDirective> = emptyList()
	) {

		fun resolve(typeRegistry: GTypeRegistry): GArgumentDefinition {
			val type = typeRegistry.resolve(type)

			return GArgumentDefinition(
				defaultValue = defaultValue?.let { defaultValue ->
					Optional(GValueCoercer.default.coerceArgumentValue(
						value = defaultValue,
						type = type
					).value)
				},
				description = description,
				directives = directives,
				name = name,
				type = type
			)
		}


		companion object
	}
}
