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
		fun from(ast: GAst.Document) =
			GDocument(
				fragments = ast.definitions
					.filterIsInstance<GAst.Definition.Fragment>()
					.map { GFragmentDefinition.from(it) },
				operations = ast.definitions
					.filterIsInstance<GAst.Definition.Operation>()
					.map { GOperationDefinition.from(it) },
				schema = ast.definitions
					.filterIsInstance<GAst.Definition.TypeSystem>()
					.ifEmpty { null }
					?.let { GSchema.from(it) }
			)


		fun parse(source: GSource.Parsable) =
			from(GAst.parseDocument(source))


		fun parse(content: String, name: String = "<document>") =
			parse(GSource.of(content = content, name = name))
	}
}
