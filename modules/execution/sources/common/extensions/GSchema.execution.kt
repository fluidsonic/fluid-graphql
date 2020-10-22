package io.fluidsonic.graphql


public fun GSchema.fieldDefinition(name: String, parentType: GNamedType): GFieldDefinition? =
	when (name) {
		Introspection.schemaField.name ->
			Introspection.schemaField.takeIf { parentType == queryType }

		Introspection.typeField.name ->
			Introspection.typeField.takeIf { parentType == queryType }

		Introspection.typenameField.name ->
			Introspection.typenameField

		else ->
			(parentType as? GNode.WithFieldDefinitions)?.fieldDefinition(name)
	}
