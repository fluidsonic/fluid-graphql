package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#EnumValue
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__EnumValue-Type
class GEnumValueDefinition(
	val name: String,
	val description: String? = null,
	val directives: List<GDirective> = emptyList()
) {

	val deprecationReason: String?
	val isDeprecated: Boolean


	init {
		val deprecation = directives.firstOrNull { it.name == GSpecification.defaultDeprecatedDirective.name }

		deprecationReason = (deprecation?.arguments?.get("reason")?.value as? String)?.ifEmpty { null }
		isDeprecated = deprecation != null
	}


	override fun toString() =
		GWriter { writeEnumValueDefinition(this@GEnumValueDefinition) }


	companion object {

		fun from(ast: GAst.EnumValueDefinition) =
			GEnumValueDefinition(
				description = ast.description?.value,
				directives = ast.directives.map { GDirective.from(it) },
				name = ast.name.value
			)
	}
}
