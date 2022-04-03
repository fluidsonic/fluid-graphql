package io.fluidsonic.graphql


public class GDocument(
	public val definitions: List<GDefinition>,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GDocument> = GNodeExtensionSet.empty(),
) : GNode(
	extensions = extensions,
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GDocument &&
				definitions.equalsNode(other.definitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public fun fragment(name: String): GFragmentDefinition? {
		for (definition in definitions)
			if (definition is GFragmentDefinition && definition.name == name)
				return definition

		return null
	}


	public fun operation(name: String?): GOperationDefinition? {
		for (definition in definitions)
			if (definition is GOperationDefinition && definition.name == name)
				return definition

		return null
	}


	public companion object {

		public fun parse(source: GDocumentSource.Parsable): GResult<GDocument> =
			Parser.parseDocument(source)


		public fun parse(content: String, name: String = "<document>"): GResult<GDocument> =
			parse(GDocumentSource.of(content = content, name = name))
	}
}
