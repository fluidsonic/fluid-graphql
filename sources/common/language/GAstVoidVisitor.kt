package io.fluidsonic.graphql

import io.fluidsonic.graphql.GAst.*


abstract class GAstVoidVisitor : GAstVisitor<Unit, Nothing?> {

	abstract fun visitNode(node: GAst)


	open fun visitArgument(argument: Argument) =
		visitNode(argument)

	open fun visitArgumentDefinition(definition: ArgumentDefinition) =
		visitNode(definition)

	open fun visitBooleanValue(value: Value.Boolean) =
		visitValue(value)

	open fun visitDefinition(definition: Definition) =
		visitNode(definition)

	open fun visitDirective(directive: Directive) =
		visitNode(directive)

	open fun visitDirectiveDefinition(definition: Definition.TypeSystem.Directive) =
		visitTypeSystemDefinition(definition)

	open fun visitDocument(document: Document) =
		visitNode(document)

	open fun visitEnumTypeDefinition(definition: Definition.TypeSystem.Type.Enum) =
		visitTypeDefinition(definition)

	open fun visitEnumTypeExtension(extension: Definition.TypeSystemExtension.Type.Enum) =
		visitTypeExtension(extension)

	open fun visitEnumValue(value: Value.Enum) =
		visitValue(value)

	open fun visitEnumValueDefinition(definition: EnumValueDefinition) =
		visitNode(definition)

	open fun visitFieldDefinition(definition: FieldDefinition) =
		visitNode(definition)

	open fun visitFieldSelection(selection: Selection.Field) =
		visitSelection(selection)

	open fun visitFloatValue(value: Value.Float) =
		visitValue(value)

	open fun visitFragmentDefinition(definition: Definition.Fragment) =
		visitDefinition(definition)

	open fun visitFragmentSelection(selection: Selection.Fragment) =
		visitSelection(selection)

	open fun visitInlineFragmentSelection(selection: Selection.InlineFragment) =
		visitSelection(selection)

	open fun visitInputObjectTypeDefinition(definition: Definition.TypeSystem.Type.InputObject) =
		visitTypeDefinition(definition)

	open fun visitInputObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.InputObject) =
		visitTypeExtension(extension)

	open fun visitIntValue(value: Value.Int) =
		visitValue(value)

	open fun visitInterfaceTypeDefinition(definition: Definition.TypeSystem.Type.Interface) =
		visitTypeDefinition(definition)

	open fun visitInterfaceTypeExtension(extension: Definition.TypeSystemExtension.Type.Interface) =
		visitTypeExtension(extension)

	open fun visitListValue(value: Value.List) =
		visitValue(value)

	open fun visitListTypeReference(reference: TypeReference.List) =
		visitTypeReference(reference)

	open fun visitName(name: Name) =
		visitNode(name)

	open fun visitNamedTypeReference(reference: TypeReference.Named) =
		visitTypeReference(reference)

	open fun visitNonNullTypeReference(reference: TypeReference.NonNull) =
		visitTypeReference(reference)

	open fun visitNullValue(value: Value.Null) =
		visitValue(value)

	open fun visitObjectTypeDefinition(definition: Definition.TypeSystem.Type.Object) =
		visitTypeDefinition(definition)

	open fun visitObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.Object) =
		visitTypeExtension(extension)

	open fun visitObjectValue(value: Value.Object) =
		visitValue(value)

	open fun visitObjectValueField(value: Value.Object.Field) =
		visitNode(value)

	open fun visitOperationDefinition(definition: Definition.Operation) =
		visitDefinition(definition)

	open fun visitOperationTypeDefinition(definition: OperationTypeDefinition) =
		visitNode(definition)

	open fun visitScalarTypeDefinition(definition: Definition.TypeSystem.Type.Scalar) =
		visitTypeDefinition(definition)

	open fun visitScalarTypeExtension(extension: Definition.TypeSystemExtension.Type.Scalar) =
		visitTypeExtension(extension)

	open fun visitSchemaDefinition(definition: Definition.TypeSystem.Schema) =
		visitTypeSystemDefinition(definition)

	open fun visitSchemaExtension(extension: Definition.TypeSystemExtension.Schema) =
		visitTypeSystemExtension(extension)

	open fun visitSelection(selection: Selection) =
		visitNode(selection)

	open fun visitSelectionSet(set: SelectionSet) =
		visitNode(set)

	open fun visitStringValue(value: Value.String) =
		visitValue(value)

	open fun visitTypeDefinition(definition: Definition.TypeSystem.Type) =
		visitTypeSystemDefinition(definition)

	open fun visitTypeExtension(extension: Definition.TypeSystemExtension.Type) =
		visitTypeSystemExtension(extension)

	open fun visitTypeReference(reference: TypeReference) =
		visitNode(reference)

	open fun visitTypeSystemDefinition(definition: Definition.TypeSystem) =
		visitDefinition(definition)

	open fun visitTypeSystemExtension(extension: Definition.TypeSystemExtension) =
		visitDefinition(extension)

	open fun visitUnionTypeDefinition(definition: Definition.TypeSystem.Type.Union) =
		visitTypeDefinition(definition)

	open fun visitUnionTypeExtension(extension: Definition.TypeSystemExtension.Type.Union) =
		visitTypeExtension(extension)

	open fun visitValue(value: Value) =
		visitNode(value)

	open fun visitVariableDefinition(value: VariableDefinition) =
		visitNode(value)

	open fun visitVariableValue(value: Value.Variable) =
		visitValue(value)


	final override fun visitNode(node: GAst, data: Nothing?) =
		visitNode(node)


	final override fun visitArgument(argument: Argument, data: Nothing?) =
		visitArgument(argument)

	final override fun visitArgumentDefinition(definition: ArgumentDefinition, data: Nothing?) =
		visitArgumentDefinition(definition)

	final override fun visitBooleanValue(value: Value.Boolean, data: Nothing?) =
		visitBooleanValue(value)

	final override fun visitDefinition(definition: Definition, data: Nothing?) =
		visitDefinition(definition)

	final override fun visitDirective(directive: Directive, data: Nothing?) =
		visitDirective(directive)

	final override fun visitDirectiveDefinition(definition: Definition.TypeSystem.Directive, data: Nothing?) =
		visitDirectiveDefinition(definition)

	final override fun visitDocument(document: Document, data: Nothing?) =
		visitDocument(document)

	final override fun visitEnumTypeDefinition(definition: Definition.TypeSystem.Type.Enum, data: Nothing?) =
		visitEnumTypeDefinition(definition)

	final override fun visitEnumTypeExtension(extension: Definition.TypeSystemExtension.Type.Enum, data: Nothing?) =
		visitEnumTypeExtension(extension)

	final override fun visitEnumValue(value: Value.Enum, data: Nothing?) =
		visitEnumValue(value)

	final override fun visitEnumValueDefinition(definition: EnumValueDefinition, data: Nothing?) =
		visitEnumValueDefinition(definition)

	final override fun visitFieldDefinition(definition: FieldDefinition, data: Nothing?) =
		visitFieldDefinition(definition)

	final override fun visitFieldSelection(selection: Selection.Field, data: Nothing?) =
		visitFieldSelection(selection)

	final override fun visitFloatValue(value: Value.Float, data: Nothing?) =
		visitFloatValue(value)

	final override fun visitFragmentDefinition(definition: Definition.Fragment, data: Nothing?) =
		visitFragmentDefinition(definition)

	final override fun visitFragmentSelection(selection: Selection.Fragment, data: Nothing?) =
		visitFragmentSelection(selection)

	final override fun visitInlineFragmentSelection(selection: Selection.InlineFragment, data: Nothing?) =
		visitInlineFragmentSelection(selection)

	final override fun visitInputObjectTypeDefinition(definition: Definition.TypeSystem.Type.InputObject, data: Nothing?) =
		visitInputObjectTypeDefinition(definition)

	final override fun visitInputObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.InputObject, data: Nothing?) =
		visitInputObjectTypeExtension(extension)

	final override fun visitIntValue(value: Value.Int, data: Nothing?) =
		visitIntValue(value)

	final override fun visitInterfaceTypeDefinition(definition: Definition.TypeSystem.Type.Interface, data: Nothing?) =
		visitInterfaceTypeDefinition(definition)

	final override fun visitInterfaceTypeExtension(extension: Definition.TypeSystemExtension.Type.Interface, data: Nothing?) =
		visitInterfaceTypeExtension(extension)

	final override fun visitListValue(value: Value.List, data: Nothing?) =
		visitListValue(value)

	final override fun visitListTypeReference(reference: TypeReference.List, data: Nothing?) =
		visitListTypeReference(reference)

	final override fun visitName(name: Name, data: Nothing?) =
		visitName(name)

	final override fun visitNamedTypeReference(reference: TypeReference.Named, data: Nothing?) =
		visitNamedTypeReference(reference)

	final override fun visitNonNullTypeReference(reference: TypeReference.NonNull, data: Nothing?) =
		visitNonNullTypeReference(reference)

	final override fun visitNullValue(value: Value.Null, data: Nothing?) =
		visitNullValue(value)

	final override fun visitObjectTypeDefinition(definition: Definition.TypeSystem.Type.Object, data: Nothing?) =
		visitObjectTypeDefinition(definition)

	final override fun visitObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.Object, data: Nothing?) =
		visitObjectTypeExtension(extension)

	final override fun visitObjectValue(value: Value.Object, data: Nothing?) =
		visitObjectValue(value)

	final override fun visitObjectValueField(value: Value.Object.Field, data: Nothing?) =
		visitObjectValueField(value)

	final override fun visitOperationDefinition(definition: Definition.Operation, data: Nothing?) =
		visitOperationDefinition(definition)

	final override fun visitOperationTypeDefinition(definition: OperationTypeDefinition, data: Nothing?) =
		visitOperationTypeDefinition(definition)

	final override fun visitScalarTypeDefinition(definition: Definition.TypeSystem.Type.Scalar, data: Nothing?) =
		visitScalarTypeDefinition(definition)

	final override fun visitScalarTypeExtension(extension: Definition.TypeSystemExtension.Type.Scalar, data: Nothing?) =
		visitScalarTypeExtension(extension)

	final override fun visitSchemaDefinition(definition: Definition.TypeSystem.Schema, data: Nothing?) =
		visitSchemaDefinition(definition)

	final override fun visitSchemaExtension(extension: Definition.TypeSystemExtension.Schema, data: Nothing?) =
		visitSchemaExtension(extension)

	final override fun visitSelection(selection: Selection, data: Nothing?) =
		visitSelection(selection)

	final override fun visitSelectionSet(set: SelectionSet, data: Nothing?) =
		visitSelectionSet(set)

	final override fun visitStringValue(value: Value.String, data: Nothing?) =
		visitStringValue(value)

	final override fun visitTypeDefinition(definition: Definition.TypeSystem.Type, data: Nothing?) =
		visitTypeDefinition(definition)

	final override fun visitTypeExtension(extension: Definition.TypeSystemExtension.Type, data: Nothing?) =
		visitTypeExtension(extension)

	final override fun visitTypeReference(reference: TypeReference, data: Nothing?) =
		visitTypeReference(reference)

	final override fun visitTypeSystemDefinition(definition: Definition.TypeSystem, data: Nothing?) =
		visitTypeSystemDefinition(definition)

	final override fun visitTypeSystemExtension(extension: Definition.TypeSystemExtension, data: Nothing?) =
		visitTypeSystemExtension(extension)

	final override fun visitUnionTypeDefinition(definition: Definition.TypeSystem.Type.Union, data: Nothing?) =
		visitUnionTypeDefinition(definition)

	final override fun visitUnionTypeExtension(extension: Definition.TypeSystemExtension.Type.Union, data: Nothing?) =
		visitUnionTypeExtension(extension)

	final override fun visitValue(value: Value, data: Nothing?) =
		visitValue(value)

	final override fun visitVariableDefinition(value: VariableDefinition, data: Nothing?) =
		visitVariableDefinition(value)

	final override fun visitVariableValue(value: Value.Variable, data: Nothing?) =
		visitVariableValue(value)
}
