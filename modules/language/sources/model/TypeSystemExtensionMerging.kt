package io.fluidsonic.graphql

/**
 * Merges the given [typeExtensions] into the [typeDefinitions] they extend and returns the resulting
 * type definitions in the order in which [typeDefinitions] lists them.
 *
 * Members contributed by an extension are appended in document order. When a contributed member has the
 * same name as an existing one it replaces that member *in place*, so the extended definition keeps its
 * original member order. Two extensions of the same definition both apply.
 *
 * An extension of a definition that this document does not contain — including an extension of a built-in
 * scalar — is silently ignored, as is an extension whose kind does not match the extended definition's kind.
 * Reporting those as errors is the job of SDL validation, not of schema assembly.
 */
internal fun mergeTypeExtensions(typeDefinitions: List<GNamedType>, typeExtensions: List<GTypeExtension>): List<GNamedType> {
	if (typeExtensions.isEmpty()) {
		return typeDefinitions
	}

	val types = typeDefinitions.toMutableList()

	for (typeExtension in typeExtensions) {
		val index = types.indexOfFirst { it.name == typeExtension.name }
		val extendedType = types.getOrNull(index)?.extendedBy(typeExtension)
		if (extendedType !== null) {
			types[index] = extendedType
		}
	}

	return types
}

/**
 * Merges the given [schemaExtensions] into [schemaDefinition] and returns the resulting schema definition,
 * or `null` if the document contains neither.
 *
 * Directives are appended in document order. An operation type contributed by an extension replaces the one
 * of the same operation *in place*.
 */
internal fun mergeSchemaExtensions(schemaDefinition: GSchemaDefinition?, schemaExtensions: List<GSchemaExtension>): GSchemaDefinition? {
	if (schemaExtensions.isEmpty()) {
		return schemaDefinition
	}

	return GSchemaDefinition(
		operationTypeDefinitions = schemaExtensions.fold(schemaDefinition?.operationTypeDefinitions.orEmpty()) { definitions, schemaExtension ->
			definitions.replacingOperationTypes(schemaExtension.operationTypeDefinitions)
		},
		descriptionNode = schemaDefinition?.descriptionNode,
		directives = schemaDefinition?.directives.orEmpty() + schemaExtensions.flatMap { it.directives },
		origin = schemaDefinition?.origin,
		extensions = schemaDefinition?.nodeExtensionsAs() ?: GNodeExtensionSet.empty(),
	)
}

/**
 * Returns this node's extension set typed for [Node].
 *
 * [GNode.extensions] is erased to `GNodeExtensionSet<GNode>` by [GNode]'s covariant type parameter, so
 * re-typing it for a specific node type is the only way to carry a node's extensions over into a copy of
 * that node. The set is read-only, so the cast can never fail at runtime.
 */
@Suppress("UNCHECKED_CAST")
internal fun <Node : GNode> GNode.nodeExtensionsAs(): GNodeExtensionSet<Node> = extensions as GNodeExtensionSet<Node>

/**
 * Returns this list with every element of [additions] either replacing the same-named element in place or,
 * if there is none, appended at the end.
 */
internal fun <Definition : GNode.WithName> List<Definition>.replacingByName(additions: List<Definition>): List<Definition> {
	if (additions.isEmpty()) {
		return this
	}

	val definitions = toMutableList()
	for (addition in additions) {
		val index = definitions.indexOfFirst { it.name == addition.name }
		if (index >= 0) {
			definitions[index] = addition
		} else {
			definitions += addition
		}
	}

	return definitions
}

/**
 * Returns this list with every element of [additions] either replacing the element for the same operation
 * type in place or, if there is none, appended at the end.
 */
private fun List<GOperationTypeDefinition>.replacingOperationTypes(additions: List<GOperationTypeDefinition>): List<GOperationTypeDefinition> {
	if (additions.isEmpty()) {
		return this
	}

	val definitions = toMutableList()
	for (addition in additions) {
		val index = definitions.indexOfFirst { it.operationType == addition.operationType }
		if (index >= 0) {
			definitions[index] = addition
		} else {
			definitions += addition
		}
	}

	return definitions
}
