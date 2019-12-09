package io.fluidsonic.graphql


sealed class GSelection {

	abstract val directives: List<GDirective>


	override fun toString() =
		GWriter { writeSelection(this@GSelection) }
}


class GFieldSelection(
	val name: String,
	val arguments: List<GArgument> = emptyList(),
	val alias: String? = null,
	override val directives: List<GDirective> = emptyList(),
	val selectionSet: List<GSelection> = emptyList()
) : GSelection()


class GFragmentSpread(
	val name: String,
	override val directives: List<GDirective> = emptyList()
) : GSelection()


class GInlineFragment(
	val selectionSet: List<GSelection>,
	val typeCondition: GTypeCondition? = null,
	override val directives: List<GDirective> = emptyList()
) : GSelection()
