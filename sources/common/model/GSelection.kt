package io.fluidsonic.graphql


sealed class GSelection {

	abstract val directives: List<GDirective>


	override fun toString() =
		GWriter { writeSelection(this@GSelection) }


	companion object {

		fun from(ast: GAst.Selection): GSelection =
			when (ast) {
				is GAst.Selection.Field -> GFieldSelection.from(ast)
				is GAst.Selection.Fragment -> GFragmentSelection.from(ast)
				is GAst.Selection.InlineFragment -> GInlineFragmentSelection.from(ast)
			}
	}
}


class GFieldSelection(
	val name: String,
	val arguments: List<GArgument> = emptyList(),
	val alias: String? = null,
	override val directives: List<GDirective> = emptyList(),
	val selectionSet: GSelectionSet? = null
) : GSelection() {

	companion object {

		fun from(ast: GAst.Selection.Field) =
			GFieldSelection(
				alias = ast.alias?.value,
				arguments = ast.arguments.map { GArgument.from(it) },
				directives = ast.directives.map { GDirective.from(it) },
				name = ast.name.value,
				selectionSet = ast.selectionSet?.let { GSelectionSet.from(it) }
			)
	}
}


class GFragmentSelection(
	val name: String,
	override val directives: List<GDirective> = emptyList()
) : GSelection() {

	companion object {

		fun from(ast: GAst.Selection.Fragment) =
			GFragmentSelection(
				directives = ast.directives.map { GDirective.from(it) },
				name = ast.name.value
			)
	}
}


class GInlineFragmentSelection(
	val selectionSet: GSelectionSet,
	val typeCondition: GNamedTypeRef? = null,
	override val directives: List<GDirective> = emptyList()
) : GSelection() {

	companion object {

		fun from(ast: GAst.Selection.InlineFragment) =
			GInlineFragmentSelection(
				directives = ast.directives.map { GDirective.from(it) },
				selectionSet = GSelectionSet.from(ast.selectionSet),
				typeCondition = ast.typeCondition?.let { GNamedTypeRef.from(it) }
			)
	}
}
