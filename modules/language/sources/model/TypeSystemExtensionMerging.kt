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
 * Builds the type system document that a schema assembled from [document] represents — that is, [document]
 * with every type extension already merged into the definition it extends.
 *
 * [mergedTypesByName] provides the merged type definitions, keyed by name. Type definitions of [document]
 * are substituted by their merged counterpart and appear in the order [document] declares them; extension
 * nodes contribute nothing of their own. Executable definitions are dropped.
 *
 * Only definitions [document] declares are emitted. The built-in scalars are therefore never printed even
 * though [mergedTypesByName] contains them, because a schema receives them from [GType.defaultTypes] rather
 * than from its document.
 */
internal fun mergedTypeSystemDocument(document: GDocument, mergedTypesByName: Map<String, GNamedType>): GDocument {
	val schemaDefinition = mergeSchemaExtensions(
		schemaDefinition = document.definitions.filterIsInstance<GSchemaDefinition>().firstOrNull(),
		schemaExtensions = document.definitions.filterIsInstance<GSchemaExtension>(),
	)

	val definitions = mutableListOf<GTypeSystemDefinition>()
	var hasWrittenSchemaDefinition = false

	for (definition in document.definitions) {
		when (definition) {
			is GDirectiveDefinition ->
				definitions += definition

			is GNamedType ->
				mergedTypesByName[definition.name]?.let { definitions += it }

			// The merged schema definition takes the place of whichever of the two comes first in the document.
			is GSchemaDefinition, is GSchemaExtension ->
				if (!hasWrittenSchemaDefinition && schemaDefinition !== null) {
					definitions += schemaDefinition
					hasWrittenSchemaDefinition = true
				}

			else ->
				Unit
		}
	}

	return GDocument(definitions = definitions)
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
