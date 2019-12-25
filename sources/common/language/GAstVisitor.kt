package io.fluidsonic.graphql


interface GAstVisitor<out Result, in Data> {

	fun visitNode(node: GAst, data: Data): Result


	fun visitArgument(argument: GArgument, data: Data) =
		visitNode(argument, data)

	fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data) =
		visitNode(definition, data)

	fun visitBooleanValue(value: GValue.Boolean, data: Data) =
		visitValue(value, data)

	fun visitDefinition(definition: GDefinition, data: Data) =
		visitNode(definition, data)

	fun visitDirective(directive: GDirective, data: Data) =
		visitNode(directive, data)

	fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
		visitTypeSystemDefinition(definition, data)

	fun visitDocument(document: GDocument, data: Data) =
		visitNode(document, data)

	fun visitEnumType(type: GEnumType, data: Data) =
		visitType(type, data)

	fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
		visitTypeExtension(extension, data)

	fun visitEnumValue(value: GValue.Enum, data: Data) =
		visitValue(value, data)

	fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
		visitNode(definition, data)

	fun visitFieldDefinition(definition: GFieldDefinition, data: Data) =
		visitNode(definition, data)

	fun visitFieldSelection(selection: GFieldSelection, data: Data) =
		visitSelection(selection, data)

	fun visitFloatValue(value: GValue.Float, data: Data) =
		visitValue(value, data)

	fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) =
		visitDefinition(definition, data)

	fun visitFragmentSelection(selection: GFragmentSelection, data: Data) =
		visitSelection(selection, data)

	fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
		visitSelection(selection, data)

	fun visitInputObjectType(type: GInputObjectType, data: Data) =
		visitType(type, data)

	fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
		visitTypeExtension(extension, data)

	fun visitIntValue(value: GValue.Int, data: Data) =
		visitValue(value, data)

	fun visitInterfaceType(type: GInterfaceType, data: Data) =
		visitType(type, data)

	fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
		visitTypeExtension(extension, data)

	fun visitListValue(value: GValue.List, data: Data) =
		visitValue(value, data)

	fun visitListTypeRef(ref: GListTypeRef, data: Data) =
		visitTypeRef(ref, data)

	fun visitName(name: GName, data: Data) =
		visitNode(name, data)

	fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) =
		visitTypeRef(ref, data)

	fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
		visitTypeRef(ref, data)

	fun visitNullValue(value: GValue.Null, data: Data) =
		visitValue(value, data)

	fun visitObjectType(type: GObjectType, data: Data) =
		visitType(type, data)

	fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
		visitTypeExtension(extension, data)

	fun visitObjectValue(value: GValue.Object, data: Data) =
		visitValue(value, data)

	fun visitObjectValueField(field: GObjectValueField, data: Data) =
		visitNode(field, data)

	fun visitOperationDefinition(definition: GOperationDefinition, data: Data) =
		visitDefinition(definition, data)

	fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
		visitNode(definition, data)

	fun visitScalarType(type: GScalarType, data: Data) =
		visitType(type, data)

	fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
		visitTypeExtension(extension, data)

	fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) =
		visitTypeSystemDefinition(definition, data)

	fun visitSchemaExtension(extension: GSchemaExtensionDefinition, data: Data) =
		visitTypeSystemExtensionDefinition(extension, data)

	fun visitSelection(selection: GSelection, data: Data) =
		visitNode(selection, data)

	fun visitSelectionSet(set: GSelectionSet, data: Data) =
		visitNode(set, data)

	fun visitStringValue(value: GValue.String, data: Data) =
		visitValue(value, data)

	fun visitType(type: GType, data: Data) =
		visitTypeSystemDefinition(type, data)

	fun visitTypeExtension(extension: GTypeExtension, data: Data) =
		visitTypeSystemExtensionDefinition(extension, data)

	fun visitTypeRef(ref: GTypeRef, data: Data) =
		visitNode(ref, data)

	fun visitTypeSystemDefinition(definition: GTypeSystemDefinition, data: Data) =
		visitDefinition(definition, data)

	fun visitTypeSystemExtensionDefinition(definition: GTypeSystemExtensionDefinition, data: Data) =
		visitDefinition(definition, data)

	fun visitUnionType(type: GUnionType, data: Data) =
		visitType(type, data)

	fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
		visitTypeExtension(extension, data)

	fun visitValue(value: GValue, data: Data) =
		visitNode(value, data)

	fun visitVariableDefinition(value: GVariableDefinition, data: Data) =
		visitNode(value, data)

	fun visitVariableValue(value: GValue.Variable, data: Data) =
		visitValue(value, data)
}
