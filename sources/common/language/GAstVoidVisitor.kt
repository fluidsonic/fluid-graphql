package io.fluidsonic.graphql


abstract class GAstVoidVisitor : GAstVisitor<Unit, Nothing?> {

	open fun visitNode(node: GAst) =
		Unit


	open fun visitArgument(argument: GArgument) =
		visitNode(argument)

	open fun visitArgumentDefinition(definition: GArgumentDefinition) =
		visitNode(definition)

	open fun visitBooleanValue(value: GValue.Boolean) =
		visitValue(value)

	open fun visitDefinition(definition: GDefinition) =
		visitNode(definition)

	open fun visitDirective(directive: GDirective) =
		visitNode(directive)

	open fun visitDirectiveDefinition(definition: GDirectiveDefinition) =
		visitTypeSystemDefinition(definition)

	open fun visitDocument(document: GDocument) =
		visitNode(document)

	open fun visitEnumType(type: GEnumType) =
		visitType(type)

	open fun visitEnumTypeExtension(extension: GEnumTypeExtension) =
		visitTypeExtension(extension)

	open fun visitEnumValue(value: GValue.Enum) =
		visitValue(value)

	open fun visitEnumValueDefinition(definition: GEnumValueDefinition) =
		visitNode(definition)

	open fun visitFieldDefinition(definition: GFieldDefinition) =
		visitNode(definition)

	open fun visitFieldSelection(selection: GFieldSelection) =
		visitSelection(selection)

	open fun visitFloatValue(value: GValue.Float) =
		visitValue(value)

	open fun visitFragmentDefinition(definition: GFragmentDefinition) =
		visitDefinition(definition)

	open fun visitFragmentSelection(selection: GFragmentSelection) =
		visitSelection(selection)

	open fun visitInlineFragmentSelection(selection: GInlineFragmentSelection) =
		visitSelection(selection)

	open fun visitInputObjectType(type: GInputObjectType) =
		visitType(type)

	open fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension) =
		visitTypeExtension(extension)

	open fun visitIntValue(value: GValue.Int) =
		visitValue(value)

	open fun visitInterfaceType(type: GInterfaceType) =
		visitType(type)

	open fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension) =
		visitTypeExtension(extension)

	open fun visitListValue(value: GValue.List) =
		visitValue(value)

	open fun visitListTypeRef(ref: GListTypeRef) =
		visitTypeRef(ref)

	open fun visitName(name: GName) =
		visitNode(name)

	open fun visitNamedTypeRef(ref: GNamedTypeRef) =
		visitTypeRef(ref)

	open fun visitNonNullTypeRef(ref: GNonNullTypeRef) =
		visitTypeRef(ref)

	open fun visitNullValue(value: GValue.Null) =
		visitValue(value)

	open fun visitObjectType(type: GObjectType) =
		visitType(type)

	open fun visitObjectTypeExtension(extension: GObjectTypeExtension) =
		visitTypeExtension(extension)

	open fun visitObjectValue(value: GValue.Object) =
		visitValue(value)

	open fun visitObjectValueField(value: GObjectValueField) =
		visitNode(value)

	open fun visitOperationDefinition(definition: GOperationDefinition) =
		visitDefinition(definition)

	open fun visitOperationTypeDefinition(definition: GOperationTypeDefinition) =
		visitNode(definition)

	open fun visitScalarType(type: GScalarType) =
		visitType(type)

	open fun visitScalarTypeExtension(extension: GScalarTypeExtension) =
		visitTypeExtension(extension)

	open fun visitSchemaDefinition(definition: GSchemaDefinition) =
		visitTypeSystemDefinition(definition)

	open fun visitSchemaExtension(extension: GSchemaExtensionDefinition) =
		visitTypeSystemExtension(extension)

	open fun visitSelection(selection: GSelection) =
		visitNode(selection)

	open fun visitSelectionSet(set: GSelectionSet) =
		visitNode(set)

	open fun visitStringValue(value: GValue.String) =
		visitValue(value)

	open fun visitType(type: GType) =
		visitTypeSystemDefinition(type)

	open fun visitTypeExtension(extension: GTypeExtension) =
		visitTypeSystemExtension(extension)

	open fun visitTypeRef(ref: GTypeRef) =
		visitNode(ref)

	open fun visitTypeSystemDefinition(definition: GTypeSystemDefinition) =
		visitDefinition(definition)

	open fun visitTypeSystemExtension(extension: GTypeSystemExtensionDefinition) =
		visitDefinition(extension)

	open fun visitUnionType(type: GUnionType) =
		visitType(type)

	open fun visitUnionTypeExtension(extension: GUnionTypeExtension) =
		visitTypeExtension(extension)

	open fun visitValue(value: GValue) =
		visitNode(value)

	open fun visitVariableDefinition(value: GVariableDefinition) =
		visitNode(value)

	open fun visitVariableValue(value: GValue.Variable) =
		visitValue(value)


	final override fun visitNode(node: GAst, data: Nothing?) =
		visitNode(node)


	final override fun visitArgument(argument: GArgument, data: Nothing?) =
		visitArgument(argument)

	final override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Nothing?) =
		visitArgumentDefinition(definition)

	final override fun visitBooleanValue(value: GValue.Boolean, data: Nothing?) =
		visitBooleanValue(value)

	final override fun visitDefinition(definition: GDefinition, data: Nothing?) =
		visitDefinition(definition)

	final override fun visitDirective(directive: GDirective, data: Nothing?) =
		visitDirective(directive)

	final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?) =
		visitDirectiveDefinition(definition)

	final override fun visitDocument(document: GDocument, data: Nothing?) =
		visitDocument(document)

	final override fun visitEnumType(type: GEnumType, data: Nothing?) =
		visitEnumType(type)

	final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?) =
		visitEnumTypeExtension(extension)

	final override fun visitEnumValue(value: GValue.Enum, data: Nothing?) =
		visitEnumValue(value)

	final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?) =
		visitEnumValueDefinition(definition)

	final override fun visitFieldDefinition(definition: GFieldDefinition, data: Nothing?) =
		visitFieldDefinition(definition)

	final override fun visitFieldSelection(selection: GFieldSelection, data: Nothing?) =
		visitFieldSelection(selection)

	final override fun visitFloatValue(value: GValue.Float, data: Nothing?) =
		visitFloatValue(value)

	final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Nothing?) =
		visitFragmentDefinition(definition)

	final override fun visitFragmentSelection(selection: GFragmentSelection, data: Nothing?) =
		visitFragmentSelection(selection)

	final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?) =
		visitInlineFragmentSelection(selection)

	final override fun visitInputObjectType(type: GInputObjectType, data: Nothing?) =
		visitInputObjectType(type)

	final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?) =
		visitInputObjectTypeExtension(extension)

	final override fun visitIntValue(value: GValue.Int, data: Nothing?) =
		visitIntValue(value)

	final override fun visitInterfaceType(type: GInterfaceType, data: Nothing?) =
		visitInterfaceType(type)

	final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?) =
		visitInterfaceTypeExtension(extension)

	final override fun visitListValue(value: GValue.List, data: Nothing?) =
		visitListValue(value)

	final override fun visitListTypeRef(ref: GListTypeRef, data: Nothing?) =
		visitListTypeRef(ref)

	final override fun visitName(name: GName, data: Nothing?) =
		visitName(name)

	final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Nothing?) =
		visitNamedTypeRef(ref)

	final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?) =
		visitNonNullTypeRef(ref)

	final override fun visitNullValue(value: GValue.Null, data: Nothing?) =
		visitNullValue(value)

	final override fun visitObjectType(type: GObjectType, data: Nothing?) =
		visitObjectType(type)

	final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?) =
		visitObjectTypeExtension(extension)

	final override fun visitObjectValue(value: GValue.Object, data: Nothing?) =
		visitObjectValue(value)

	final override fun visitObjectValueField(field: GObjectValueField, data: Nothing?) =
		visitObjectValueField(field)

	final override fun visitOperationDefinition(definition: GOperationDefinition, data: Nothing?) =
		visitOperationDefinition(definition)

	final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?) =
		visitOperationTypeDefinition(definition)

	final override fun visitScalarType(type: GScalarType, data: Nothing?) =
		visitScalarType(type)

	final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?) =
		visitScalarTypeExtension(extension)

	final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Nothing?) =
		visitSchemaDefinition(definition)

	final override fun visitSchemaExtension(extension: GSchemaExtensionDefinition, data: Nothing?) =
		visitSchemaExtension(extension)

	final override fun visitSelection(selection: GSelection, data: Nothing?) =
		visitSelection(selection)

	final override fun visitSelectionSet(set: GSelectionSet, data: Nothing?) =
		visitSelectionSet(set)

	final override fun visitStringValue(value: GValue.String, data: Nothing?) =
		visitStringValue(value)

	final override fun visitType(type: GType, data: Nothing?) =
		visitType(type)

	final override fun visitTypeExtension(extension: GTypeExtension, data: Nothing?) =
		visitTypeExtension(extension)

	final override fun visitTypeRef(ref: GTypeRef, data: Nothing?) =
		visitTypeRef(ref)

	final override fun visitTypeSystemDefinition(definition: GTypeSystemDefinition, data: Nothing?) =
		visitTypeSystemDefinition(definition)

	final override fun visitTypeSystemExtensionDefinition(definition: GTypeSystemExtensionDefinition, data: Nothing?) =
		visitTypeSystemExtension(definition)

	final override fun visitUnionType(type: GUnionType, data: Nothing?) =
		visitUnionType(type)

	final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?) =
		visitUnionTypeExtension(extension)

	final override fun visitValue(value: GValue, data: Nothing?) =
		visitValue(value)

	final override fun visitVariableDefinition(value: GVariableDefinition, data: Nothing?) =
		visitVariableDefinition(value)

	final override fun visitVariableValue(value: GValue.Variable, data: Nothing?) =
		visitVariableValue(value)
}
