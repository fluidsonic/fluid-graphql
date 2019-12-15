package io.fluidsonic.graphql

import io.fluidsonic.graphql.GAst.*


interface GAstVisitor<out Result, in Data> {

	fun visitNode(node: GAst, data: Data): Result


	fun visitArgument(argument: Argument, data: Data) =
		visitNode(argument, data)

	fun visitArgumentDefinition(definition: ArgumentDefinition, data: Data) =
		visitNode(definition, data)

	fun visitBooleanValue(value: Value.Boolean, data: Data) =
		visitValue(value, data)

	fun visitDefinition(definition: Definition, data: Data) =
		visitNode(definition, data)

	fun visitDirective(directive: Directive, data: Data) =
		visitNode(directive, data)

	fun visitDirectiveDefinition(definition: Definition.TypeSystem.Directive, data: Data) =
		visitTypeSystemDefinition(definition, data)

	fun visitDocument(document: Document, data: Data) =
		visitNode(document, data)

	fun visitEnumTypeDefinition(definition: Definition.TypeSystem.Type.Enum, data: Data) =
		visitTypeDefinition(definition, data)

	fun visitEnumTypeExtension(extension: Definition.TypeSystemExtension.Type.Enum, data: Data) =
		visitTypeExtension(extension, data)

	fun visitEnumValue(value: Value.Enum, data: Data) =
		visitValue(value, data)

	fun visitEnumValueDefinition(definition: EnumValueDefinition, data: Data) =
		visitNode(definition, data)

	fun visitFieldDefinition(definition: FieldDefinition, data: Data) =
		visitNode(definition, data)

	fun visitFieldSelection(selection: Selection.Field, data: Data) =
		visitSelection(selection, data)

	fun visitFloatValue(value: Value.Float, data: Data) =
		visitValue(value, data)

	fun visitFragmentDefinition(definition: Definition.Fragment, data: Data) =
		visitDefinition(definition, data)

	fun visitFragmentSelection(selection: Selection.Fragment, data: Data) =
		visitSelection(selection, data)

	fun visitInlineFragmentSelection(selection: Selection.InlineFragment, data: Data) =
		visitSelection(selection, data)

	fun visitInputObjectTypeDefinition(definition: Definition.TypeSystem.Type.InputObject, data: Data) =
		visitTypeDefinition(definition, data)

	fun visitInputObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.InputObject, data: Data) =
		visitTypeExtension(extension, data)

	fun visitIntValue(value: Value.Int, data: Data) =
		visitValue(value, data)

	fun visitInterfaceTypeDefinition(definition: Definition.TypeSystem.Type.Interface, data: Data) =
		visitTypeDefinition(definition, data)

	fun visitInterfaceTypeExtension(extension: Definition.TypeSystemExtension.Type.Interface, data: Data) =
		visitTypeExtension(extension, data)

	fun visitListValue(value: Value.List, data: Data) =
		visitValue(value, data)

	fun visitListTypeReference(reference: TypeReference.List, data: Data) =
		visitTypeReference(reference, data)

	fun visitName(name: Name, data: Data) =
		visitNode(name, data)

	fun visitNamedTypeReference(reference: TypeReference.Named, data: Data) =
		visitTypeReference(reference, data)

	fun visitNonNullTypeReference(reference: TypeReference.NonNull, data: Data) =
		visitTypeReference(reference, data)

	fun visitNullValue(value: Value.Null, data: Data) =
		visitValue(value, data)

	fun visitObjectTypeDefinition(definition: Definition.TypeSystem.Type.Object, data: Data) =
		visitTypeDefinition(definition, data)

	fun visitObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.Object, data: Data) =
		visitTypeExtension(extension, data)

	fun visitObjectValue(value: Value.Object, data: Data) =
		visitValue(value, data)

	fun visitObjectValueField(value: Value.Object.Field, data: Data) =
		visitNode(value, data)

	fun visitOperationDefinition(definition: Definition.Operation, data: Data) =
		visitDefinition(definition, data)

	fun visitOperationTypeDefinition(definition: OperationTypeDefinition, data: Data) =
		visitNode(definition, data)

	fun visitScalarTypeDefinition(definition: Definition.TypeSystem.Type.Scalar, data: Data) =
		visitTypeDefinition(definition, data)

	fun visitScalarTypeExtension(extension: Definition.TypeSystemExtension.Type.Scalar, data: Data) =
		visitTypeExtension(extension, data)

	fun visitSchemaDefinition(definition: Definition.TypeSystem.Schema, data: Data) =
		visitTypeSystemDefinition(definition, data)

	fun visitSchemaExtension(extension: Definition.TypeSystemExtension.Schema, data: Data) =
		visitTypeSystemExtension(extension, data)

	fun visitSelection(selection: Selection, data: Data) =
		visitNode(selection, data)

	fun visitSelectionSet(set: SelectionSet, data: Data) =
		visitNode(set, data)

	fun visitStringValue(value: Value.String, data: Data) =
		visitValue(value, data)

	fun visitTypeDefinition(definition: Definition.TypeSystem.Type, data: Data) =
		visitTypeSystemDefinition(definition, data)

	fun visitTypeExtension(extension: Definition.TypeSystemExtension.Type, data: Data) =
		visitTypeSystemExtension(extension, data)

	fun visitTypeReference(reference: TypeReference, data: Data) =
		visitNode(reference, data)

	fun visitTypeSystemDefinition(definition: Definition.TypeSystem, data: Data) =
		visitDefinition(definition, data)

	fun visitTypeSystemExtension(extension: Definition.TypeSystemExtension, data: Data) =
		visitDefinition(extension, data)

	fun visitUnionTypeDefinition(definition: Definition.TypeSystem.Type.Union, data: Data) =
		visitTypeDefinition(definition, data)

	fun visitUnionTypeExtension(extension: Definition.TypeSystemExtension.Type.Union, data: Data) =
		visitTypeExtension(extension, data)

	fun visitValue(value: Value, data: Data) =
		visitNode(value, data)

	fun visitVariableDefinition(value: VariableDefinition, data: Data) =
		visitNode(value, data)

	fun visitVariableValue(value: Value.Variable, data: Data) =
		visitValue(value, data)
}
