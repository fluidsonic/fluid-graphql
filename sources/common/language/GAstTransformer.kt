package io.fluidsonic.graphql


// TODO It may be okay to widen the transformation result type for some node types
// FIXME pretty useless unless we glue all changes together
abstract class GAstTransformer<in Data> : GAstVisitor<GAst, Data> {

	open fun <Node : GAst> transformNode(node: Node, data: Data) =
		node


	open fun transformArgument(argument: GArgument, data: Data) =
		transformNode(argument, data)

	open fun transformArgumentDefinition(definition: GArgumentDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformBooleanValue(value: GValue.Boolean, data: Data) =
		transformValue(value, data)

	open fun <Node : GDefinition> transformDefinition(definition: Node, data: Data) =
		transformNode(definition, data)

	open fun transformDirective(directive: GDirective, data: Data) =
		transformNode(directive, data)

	open fun transformDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
		transformTypeSystemDefinition(definition, data)

	open fun transformDocument(document: GDocument, data: Data) =
		transformNode(document, data)

	open fun transformEnumType(type: GEnumType, data: Data) =
		transformType(type, data)

	open fun transformEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformEnumValue(value: GValue.Enum, data: Data) =
		transformValue(value, data)

	open fun transformEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformFieldDefinition(definition: GFieldDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformFieldSelection(selection: GFieldSelection, data: Data) =
		transformSelection(selection, data)

	open fun transformFloatValue(value: GValue.Float, data: Data) =
		transformValue(value, data)

	open fun transformFragmentDefinition(definition: GFragmentDefinition, data: Data) =
		transformDefinition(definition, data)

	open fun transformFragmentSelection(selection: GFragmentSelection, data: Data) =
		transformSelection(selection, data)

	open fun transformInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
		transformSelection(selection, data)

	open fun transformInputObjectType(type: GInputObjectType, data: Data) =
		transformType(type, data)

	open fun transformInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformIntValue(value: GValue.Int, data: Data) =
		transformValue(value, data)

	open fun transformInterfaceType(type: GInterfaceType, data: Data) =
		transformType(type, data)

	open fun transformInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformListValue(value: GValue.List, data: Data) =
		transformValue(value, data)

	open fun transformListTypeRef(ref: GListTypeRef, data: Data) =
		transformTypeRef(ref, data)

	open fun transformName(name: GName, data: Data) =
		transformNode(name, data)

	open fun transformNamedTypeRef(ref: GNamedTypeRef, data: Data) =
		transformTypeRef(ref, data)

	open fun transformNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
		transformTypeRef(ref, data)

	open fun transformNullValue(value: GValue.Null, data: Data) =
		transformValue(value, data)

	open fun transformObjectType(type: GObjectType, data: Data) =
		transformType(type, data)

	open fun transformObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformObjectValue(value: GValue.Object, data: Data) =
		transformValue(value, data)

	open fun transformObjectValueField(field: GObjectValueField, data: Data) =
		transformNode(field, data)

	open fun transformOperationDefinition(definition: GOperationDefinition, data: Data) =
		transformDefinition(definition, data)

	open fun transformOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformScalarType(type: GScalarType, data: Data) =
		transformType(type, data)

	open fun transformScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformSchemaDefinition(definition: GSchemaDefinition, data: Data) =
		transformTypeSystemDefinition(definition, data)

	open fun transformSchemaExtension(extension: GSchemaExtensionDefinition, data: Data) =
		transformTypeSystemExtensionDefinition(extension, data)

	open fun <Node : GSelection> transformSelection(selection: Node, data: Data) =
		transformNode(selection, data)

	open fun transformSelectionSet(set: GSelectionSet, data: Data) =
		transformNode(set, data)

	open fun transformStringValue(value: GValue.String, data: Data) =
		transformValue(value, data)

	open fun <Node : GType> transformType(type: Node, data: Data) =
		transformTypeSystemDefinition(type, data)

	open fun <Node : GTypeExtension> transformTypeExtension(extension: Node, data: Data) =
		transformTypeSystemExtensionDefinition(extension, data)

	open fun <Node : GTypeRef> transformTypeRef(reference: Node, data: Data) =
		transformNode(reference, data)

	open fun <Node : GTypeSystemDefinition> transformTypeSystemDefinition(definition: Node, data: Data) =
		transformDefinition(definition, data)

	open fun <Node : GTypeSystemExtensionDefinition> transformTypeSystemExtensionDefinition(definition: Node, data: Data) =
		transformDefinition(definition, data)

	open fun transformUnionType(type: GUnionType, data: Data) =
		transformType(type, data)

	open fun transformUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
		transformTypeExtension(extension, data)

	open fun <Node : GValue> transformValue(value: Node, data: Data) =
		transformNode(value, data)

	open fun transformVariableDefinition(value: GVariableDefinition, data: Data) =
		transformNode(value, data)

	open fun transformVariableValue(value: GValue.Variable, data: Data) =
		transformValue(value, data)


	final override fun visitNode(node: GAst, data: Data) =
		transformNode(node, data)


	final override fun visitArgument(argument: GArgument, data: Data) =
		transformArgument(argument, data)

	final override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data) =
		transformArgumentDefinition(definition, data)

	final override fun visitBooleanValue(value: GValue.Boolean, data: Data) =
		transformBooleanValue(value, data)

	final override fun visitDefinition(definition: GDefinition, data: Data) =
		transformDefinition(definition, data)

	final override fun visitDirective(directive: GDirective, data: Data) =
		transformDirective(directive, data)

	final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
		transformDirectiveDefinition(definition, data)

	final override fun visitDocument(document: GDocument, data: Data) =
		transformDocument(document, data)

	final override fun visitEnumType(type: GEnumType, data: Data) =
		transformEnumType(type, data)

	final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
		transformEnumTypeExtension(extension, data)

	final override fun visitEnumValue(value: GValue.Enum, data: Data) =
		transformEnumValue(value, data)

	final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
		transformEnumValueDefinition(definition, data)

	final override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) =
		transformFieldDefinition(definition, data)

	final override fun visitFieldSelection(selection: GFieldSelection, data: Data) =
		transformFieldSelection(selection, data)

	final override fun visitFloatValue(value: GValue.Float, data: Data) =
		transformFloatValue(value, data)

	final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) =
		transformFragmentDefinition(definition, data)

	final override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) =
		transformFragmentSelection(selection, data)

	final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
		transformInlineFragmentSelection(selection, data)

	final override fun visitInputObjectType(type: GInputObjectType, data: Data) =
		transformInputObjectType(type, data)

	final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
		transformInputObjectTypeExtension(extension, data)

	final override fun visitIntValue(value: GValue.Int, data: Data) =
		transformIntValue(value, data)

	final override fun visitInterfaceType(type: GInterfaceType, data: Data) =
		transformInterfaceType(type, data)

	final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
		transformInterfaceTypeExtension(extension, data)

	final override fun visitListValue(value: GValue.List, data: Data) =
		transformListValue(value, data)

	final override fun visitListTypeRef(ref: GListTypeRef, data: Data) =
		transformListTypeRef(ref, data)

	final override fun visitName(name: GName, data: Data) =
		transformName(name, data)

	final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) =
		transformNamedTypeRef(ref, data)

	final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
		transformNonNullTypeRef(ref, data)

	final override fun visitNullValue(value: GValue.Null, data: Data) =
		transformNullValue(value, data)

	final override fun visitObjectType(type: GObjectType, data: Data) =
		transformObjectType(type, data)

	final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
		transformObjectTypeExtension(extension, data)

	final override fun visitObjectValue(value: GValue.Object, data: Data) =
		transformObjectValue(value, data)

	final override fun visitObjectValueField(field: GObjectValueField, data: Data) =
		transformObjectValueField(field, data)

	final override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) =
		transformOperationDefinition(definition, data)

	final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
		transformOperationTypeDefinition(definition, data)

	final override fun visitScalarType(type: GScalarType, data: Data) =
		transformScalarType(type, data)

	final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
		transformScalarTypeExtension(extension, data)

	final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) =
		transformSchemaDefinition(definition, data)

	final override fun visitSchemaExtension(extension: GSchemaExtensionDefinition, data: Data) =
		transformSchemaExtension(extension, data)

	final override fun visitSelection(selection: GSelection, data: Data) =
		transformSelection(selection, data)

	final override fun visitSelectionSet(set: GSelectionSet, data: Data) =
		transformSelectionSet(set, data)

	final override fun visitStringValue(value: GValue.String, data: Data) =
		transformStringValue(value, data)

	final override fun visitType(type: GType, data: Data) =
		transformType(type, data)

	final override fun visitTypeExtension(extension: GTypeExtension, data: Data) =
		transformTypeExtension(extension, data)

	final override fun visitTypeRef(ref: GTypeRef, data: Data) =
		transformTypeRef(ref, data)

	final override fun visitTypeSystemDefinition(definition: GTypeSystemDefinition, data: Data) =
		transformTypeSystemDefinition(definition, data)

	final override fun visitTypeSystemExtensionDefinition(definition: GTypeSystemExtensionDefinition, data: Data) =
		transformTypeSystemExtensionDefinition(definition, data)

	final override fun visitUnionType(type: GUnionType, data: Data) =
		transformUnionType(type, data)

	final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
		transformUnionTypeExtension(extension, data)

	final override fun visitValue(value: GValue, data: Data) =
		transformValue(value, data)

	final override fun visitVariableDefinition(value: GVariableDefinition, data: Data) =
		transformVariableDefinition(value, data)

	final override fun visitVariableValue(value: GValue.Variable, data: Data) =
		transformVariableValue(value, data)
}
