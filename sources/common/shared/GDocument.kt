package io.fluidsonic.graphql


class GDocument(
	fragments: List<GFragmentDefinition> = emptyList(),
	val operations: List<GOperationDefinition> = emptyList(),
	val schema: GSchema? = null
) {

	val fragments: Map<String, GFragmentDefinition>


	init {
		require(fragments.size <= 1 || fragments.mapTo(hashSetOf()) { it.name }.size == fragments.size) {
			"'fragments' must not contain multiple elements with the same name: $fragments"
		}

		this.fragments = fragments.associateBy { it.name }
	}


	companion object {

		// FIXME validation
		// FIXME extensions
		internal fun build(ast: AstNode.Document) =
			GDocument(
				fragments = ast.definitions
					.filterIsInstance<AstNode.Definition.Fragment>()
					.map { GFragmentDefinition.build(it) },
				operations = ast.definitions
					.filterIsInstance<AstNode.Definition.Operation>()
					.map { GOperationDefinition.build(it) },
				schema = ast.definitions
					.filterIsInstance<AstNode.Definition.TypeSystem>()
					.ifEmpty { null }
					?.let { GSchema.build(it) }
			)


		fun parse(source: String) =
			build(Parser.parse(source))
	}
}
