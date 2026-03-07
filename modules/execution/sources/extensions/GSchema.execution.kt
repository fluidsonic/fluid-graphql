package io.fluidsonic.graphql


/**
 * Returns the [GFieldDefinition] for [name] on [parentType], including built-in introspection fields
 * (`__schema`, `__type`, `__typename`).
 *
 * Returns `null` if [parentType] has no field with that name.
 */
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
