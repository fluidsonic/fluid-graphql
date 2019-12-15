package io.fluidsonic.graphql


sealed class GSelection {

	abstract val directives: List<GDirective>


	override fun toString() =
		GWriter { writeSelection(this@GSelection) }


	companion object {

		internal fun build(ast: AstNode.Selection): GSelection =
			when (ast) {
				is AstNode.Selection.Field -> GFieldSelection.build(ast)
				is AstNode.Selection.FragmentSpread -> GFragmentSpreadSelection.build(ast)
				is AstNode.Selection.InlineFragment -> GInlineFragmentSelection.build(ast)
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

		internal fun build(ast: AstNode.Selection.Field) =
			GFieldSelection(
				alias = ast.alias?.value,
				arguments = ast.arguments.map { GArgument.build(it) },
				directives = ast.directives.map { GDirective.build(it) },
				name = ast.name.value,
				selectionSet = ast.selectionSet?.let { GSelectionSet.build(it) }
			)
	}
}


class GFragmentSpreadSelection(
	val name: String,
	override val directives: List<GDirective> = emptyList()
) : GSelection() {

	companion object {

		internal fun build(ast: AstNode.Selection.FragmentSpread) =
			GFragmentSpreadSelection(
				directives = ast.directives.map { GDirective.build(it) },
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

		internal fun build(ast: AstNode.Selection.InlineFragment) =
			GInlineFragmentSelection(
				directives = ast.directives.map { GDirective.build(it) },
				selectionSet = GSelectionSet.build(ast.selectionSet),
				typeCondition = ast.typeCondition?.let { GNamedTypeRef.build(it) }
			)
	}
}
