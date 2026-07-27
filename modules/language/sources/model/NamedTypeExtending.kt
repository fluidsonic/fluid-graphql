package io.fluidsonic.graphql

/**
 * Returns this type definition extended by [typeExtension], or `null` if the extension's kind does not match
 * the kind of this definition.
 *
 * Directives and the members of composite kinds accumulate; a member whose name collides with an existing one
 * replaces it in place. See [mergeTypeExtensions] for how extensions of a whole document are merged.
 */
internal fun GNamedType.extendedBy(typeExtension: GTypeExtension): GNamedType? = when (typeExtension) {
	is GEnumTypeExtension -> (this as? GEnumType)?.extendedBy(typeExtension)
	is GInputObjectTypeExtension -> (this as? GInputObjectType)?.extendedBy(typeExtension)
	is GInterfaceTypeExtension -> (this as? GInterfaceType)?.extendedBy(typeExtension)
	is GObjectTypeExtension -> (this as? GObjectType)?.extendedBy(typeExtension)

	// The built-in scalars are `object`s of a sealed class and thus cannot be extended at all.
	is GScalarTypeExtension -> (this as? GCustomScalarType)?.extendedBy(typeExtension)

	is GUnionTypeExtension -> (this as? GUnionType)?.extendedBy(typeExtension)
}

private fun GCustomScalarType.extendedBy(typeExtension: GScalarTypeExtension): GCustomScalarType = GCustomScalarType(
	name = nameNode,
	description = descriptionNode,
	directives = directives + typeExtension.directives,
	origin = origin,
	extensions = nodeExtensionsAs(),
)

private fun GEnumType.extendedBy(typeExtension: GEnumTypeExtension): GEnumType = GEnumType(
	name = nameNode,
	values = values.replacingByName(typeExtension.values),
	description = descriptionNode,
	directives = directives + typeExtension.directives,
	origin = origin,
	extensions = nodeExtensionsAs(),
)

private fun GInputObjectType.extendedBy(typeExtension: GInputObjectTypeExtension): GInputObjectType = GInputObjectType(
	name = nameNode,
	argumentDefinitions = argumentDefinitions.replacingByName(typeExtension.argumentDefinitions),
	description = descriptionNode,
	directives = directives + typeExtension.directives,
	origin = origin,
	extensions = nodeExtensionsAs(),
)

private fun GInterfaceType.extendedBy(typeExtension: GInterfaceTypeExtension): GInterfaceType = GInterfaceType(
	name = nameNode,
	fieldDefinitions = fieldDefinitions.replacingByName(typeExtension.fieldDefinitions),
	interfaces = interfaces + typeExtension.interfaces,
	description = descriptionNode,
	directives = directives + typeExtension.directives,
	origin = origin,
	extensions = nodeExtensionsAs(),
)

private fun GObjectType.extendedBy(typeExtension: GObjectTypeExtension): GObjectType = GObjectType(
	name = nameNode,
	fieldDefinitions = fieldDefinitions.replacingByName(typeExtension.fieldDefinitions),
	interfaces = interfaces + typeExtension.interfaces,
	description = descriptionNode,
	directives = directives + typeExtension.directives,
	origin = origin,
	extensions = nodeExtensionsAs(),
)

private fun GUnionType.extendedBy(typeExtension: GUnionTypeExtension): GUnionType = GUnionType(
	name = nameNode,
	possibleTypes = possibleTypes + typeExtension.possibleTypes,
	description = descriptionNode,
	directives = directives + typeExtension.directives,
	origin = origin,
	extensions = nodeExtensionsAs(),
)
