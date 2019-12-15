package io.fluidsonic.graphql


class GFragmentDefinition(
	val name: String,
	val typeCondition: GNamedTypeRef,
	val selectionSet: GSelectionSet,
	val directives: List<GDirective> = emptyList()
) {

	companion object {

		// FIXME validation
		internal fun build(ast: AstNode.Definition.Fragment) =
			GFragmentDefinition(
				directives = ast.directives.map { GDirective.build(it) },
				name = ast.name.value,
				typeCondition = GNamedTypeRef.build(ast.typeCondition),
				selectionSet = GSelectionSet.build(ast.selectionSet)
			)
	}
}
