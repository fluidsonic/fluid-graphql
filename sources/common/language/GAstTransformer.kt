package io.fluidsonic.graphql

import io.fluidsonic.graphql.GAst.*


// TODO It may be okay to widen the transformation result type for some node types
// FIXME pretty useless unless we glue all changes together
abstract class GAstTransformer<in Data> : GAstVisitor<GAst, Data> {

	abstract fun <Node : GAst> transformNode(node: Node, data: Data): Node


	open fun transformArgument(argument: Argument, data: Data) =
		transformNode(argument, data)

	open fun transformArgumentDefinition(definition: ArgumentDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformBooleanValue(value: Value.Boolean, data: Data) =
		transformValue(value, data)

	open fun <Node : Definition> transformDefinition(definition: Node, data: Data) =
		transformNode(definition, data)

	open fun transformDirective(directive: Directive, data: Data) =
		transformNode(directive, data)

	open fun transformDirectiveDefinition(definition: Definition.TypeSystem.Directive, data: Data) =
		transformTypeSystemDefinition(definition, data)

	open fun transformDocument(document: Document, data: Data) =
		transformNode(document, data)

	open fun transformEnumTypeDefinition(definition: Definition.TypeSystem.Type.Enum, data: Data) =
		transformTypeDefinition(definition, data)

	open fun transformEnumTypeExtension(extension: Definition.TypeSystemExtension.Type.Enum, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformEnumValue(value: Value.Enum, data: Data) =
		transformValue(value, data)

	open fun transformEnumValueDefinition(definition: EnumValueDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformFieldDefinition(definition: FieldDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformFieldSelection(selection: Selection.Field, data: Data) =
		transformSelection(selection, data)

	open fun transformFloatValue(value: Value.Float, data: Data) =
		transformValue(value, data)

	open fun transformFragmentDefinition(definition: Definition.Fragment, data: Data) =
		transformDefinition(definition, data)

	open fun transformFragmentSelection(selection: Selection.Fragment, data: Data) =
		transformSelection(selection, data)

	open fun transformInlineFragmentSelection(selection: Selection.InlineFragment, data: Data) =
		transformSelection(selection, data)

	open fun transformInputObjectTypeDefinition(definition: Definition.TypeSystem.Type.InputObject, data: Data) =
		transformTypeDefinition(definition, data)

	open fun transformInputObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.InputObject, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformIntValue(value: Value.Int, data: Data) =
		transformValue(value, data)

	open fun transformInterfaceTypeDefinition(definition: Definition.TypeSystem.Type.Interface, data: Data) =
		transformTypeDefinition(definition, data)

	open fun transformInterfaceTypeExtension(extension: Definition.TypeSystemExtension.Type.Interface, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformListValue(value: Value.List, data: Data) =
		transformValue(value, data)

	open fun transformListTypeReference(reference: TypeReference.List, data: Data) =
		transformTypeReference(reference, data)

	open fun transformName(name: Name, data: Data) =
		transformNode(name, data)

	open fun transformNamedTypeReference(reference: TypeReference.Named, data: Data) =
		transformTypeReference(reference, data)

	open fun transformNonNullTypeReference(reference: TypeReference.NonNull, data: Data) =
		transformTypeReference(reference, data)

	open fun transformNullValue(value: Value.Null, data: Data) =
		transformValue(value, data)

	open fun transformObjectTypeDefinition(definition: Definition.TypeSystem.Type.Object, data: Data) =
		transformTypeDefinition(definition, data)

	open fun transformObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.Object, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformObjectValue(value: Value.Object, data: Data) =
		transformValue(value, data)

	open fun transformObjectValueField(value: Value.Object.Field, data: Data) =
		transformNode(value, data)

	open fun transformOperationDefinition(definition: Definition.Operation, data: Data) =
		transformDefinition(definition, data)

	open fun transformOperationTypeDefinition(definition: OperationTypeDefinition, data: Data) =
		transformNode(definition, data)

	open fun transformScalarTypeDefinition(definition: Definition.TypeSystem.Type.Scalar, data: Data) =
		transformTypeDefinition(definition, data)

	open fun transformScalarTypeExtension(extension: Definition.TypeSystemExtension.Type.Scalar, data: Data) =
		transformTypeExtension(extension, data)

	open fun transformSchemaDefinition(definition: Definition.TypeSystem.Schema, data: Data) =
		transformTypeSystemDefinition(definition, data)

	open fun transformSchemaExtension(extension: Definition.TypeSystemExtension.Schema, data: Data) =
		transformTypeSystemExtension(extension, data)

	open fun <Node : Selection> transformSelection(selection: Node, data: Data) =
		transformNode(selection, data)

	open fun transformSelectionSet(set: SelectionSet, data: Data) =
		transformNode(set, data)

	open fun transformStringValue(value: Value.String, data: Data) =
		transformValue(value, data)

	open fun <Node : Definition.TypeSystem.Type> transformTypeDefinition(definition: Node, data: Data) =
		transformTypeSystemDefinition(definition, data)

	open fun transformTypeExtension(extension: Definition.TypeSystemExtension.Type, data: Data) =
		transformTypeSystemExtension(extension, data)

	open fun <Node : TypeReference> transformTypeReference(reference: Node, data: Data) =
		transformNode(reference, data)

	open fun <Node : Definition.TypeSystem> transformTypeSystemDefinition(definition: Node, data: Data) =
		transformDefinition(definition, data)

	open fun <Node : Definition.TypeSystemExtension> transformTypeSystemExtension(extension: Node, data: Data) =
		transformDefinition(extension, data)

	open fun transformUnionTypeDefinition(definition: Definition.TypeSystem.Type.Union, data: Data) =
		transformTypeDefinition(definition, data)

	open fun transformUnionTypeExtension(extension: Definition.TypeSystemExtension.Type.Union, data: Data) =
		transformTypeExtension(extension, data)

	open fun <Node : Value> transformValue(value: Node, data: Data) =
		transformNode(value, data)

	open fun transformVariableDefinition(value: VariableDefinition, data: Data) =
		transformNode(value, data)

	open fun transformVariableValue(value: Value.Variable, data: Data) =
		transformValue(value, data)


	final override fun visitNode(node: GAst, data: Data) =
		transformNode(node, data)


	final override fun visitArgument(argument: Argument, data: Data) =
		transformArgument(argument, data)

	final override fun visitArgumentDefinition(definition: ArgumentDefinition, data: Data) =
		transformArgumentDefinition(definition, data)

	final override fun visitBooleanValue(value: Value.Boolean, data: Data) =
		transformBooleanValue(value, data)

	final override fun visitDefinition(definition: Definition, data: Data) =
		transformDefinition(definition, data)

	final override fun visitDirective(directive: Directive, data: Data) =
		transformDirective(directive, data)

	final override fun visitDirectiveDefinition(definition: Definition.TypeSystem.Directive, data: Data) =
		transformDirectiveDefinition(definition, data)

	final override fun visitDocument(document: Document, data: Data) =
		transformDocument(document, data)

	final override fun visitEnumTypeDefinition(definition: Definition.TypeSystem.Type.Enum, data: Data) =
		transformEnumTypeDefinition(definition, data)

	final override fun visitEnumTypeExtension(extension: Definition.TypeSystemExtension.Type.Enum, data: Data) =
		transformEnumTypeExtension(extension, data)

	final override fun visitEnumValue(value: Value.Enum, data: Data) =
		transformEnumValue(value, data)

	final override fun visitEnumValueDefinition(definition: EnumValueDefinition, data: Data) =
		transformEnumValueDefinition(definition, data)

	final override fun visitFieldDefinition(definition: FieldDefinition, data: Data) =
		transformFieldDefinition(definition, data)

	final override fun visitFieldSelection(selection: Selection.Field, data: Data) =
		transformFieldSelection(selection, data)

	final override fun visitFloatValue(value: Value.Float, data: Data) =
		transformFloatValue(value, data)

	final override fun visitFragmentDefinition(definition: Definition.Fragment, data: Data) =
		transformFragmentDefinition(definition, data)

	final override fun visitFragmentSelection(selection: Selection.Fragment, data: Data) =
		transformFragmentSelection(selection, data)

	final override fun visitInlineFragmentSelection(selection: Selection.InlineFragment, data: Data) =
		transformInlineFragmentSelection(selection, data)

	final override fun visitInputObjectTypeDefinition(definition: Definition.TypeSystem.Type.InputObject, data: Data) =
		transformInputObjectTypeDefinition(definition, data)

	final override fun visitInputObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.InputObject, data: Data) =
		transformInputObjectTypeExtension(extension, data)

	final override fun visitIntValue(value: Value.Int, data: Data) =
		transformIntValue(value, data)

	final override fun visitInterfaceTypeDefinition(definition: Definition.TypeSystem.Type.Interface, data: Data) =
		transformInterfaceTypeDefinition(definition, data)

	final override fun visitInterfaceTypeExtension(extension: Definition.TypeSystemExtension.Type.Interface, data: Data) =
		transformInterfaceTypeExtension(extension, data)

	final override fun visitListValue(value: Value.List, data: Data) =
		transformListValue(value, data)

	final override fun visitListTypeReference(reference: TypeReference.List, data: Data) =
		transformListTypeReference(reference, data)

	final override fun visitName(name: Name, data: Data) =
		transformName(name, data)

	final override fun visitNamedTypeReference(reference: TypeReference.Named, data: Data) =
		transformNamedTypeReference(reference, data)

	final override fun visitNonNullTypeReference(reference: TypeReference.NonNull, data: Data) =
		transformNonNullTypeReference(reference, data)

	final override fun visitNullValue(value: Value.Null, data: Data) =
		transformNullValue(value, data)

	final override fun visitObjectTypeDefinition(definition: Definition.TypeSystem.Type.Object, data: Data) =
		transformObjectTypeDefinition(definition, data)

	final override fun visitObjectTypeExtension(extension: Definition.TypeSystemExtension.Type.Object, data: Data) =
		transformObjectTypeExtension(extension, data)

	final override fun visitObjectValue(value: Value.Object, data: Data) =
		transformObjectValue(value, data)

	final override fun visitObjectValueField(value: Value.Object.Field, data: Data) =
		transformObjectValueField(value, data)

	final override fun visitOperationDefinition(definition: Definition.Operation, data: Data) =
		transformOperationDefinition(definition, data)

	final override fun visitOperationTypeDefinition(definition: OperationTypeDefinition, data: Data) =
		transformOperationTypeDefinition(definition, data)

	final override fun visitScalarTypeDefinition(definition: Definition.TypeSystem.Type.Scalar, data: Data) =
		transformScalarTypeDefinition(definition, data)

	final override fun visitScalarTypeExtension(extension: Definition.TypeSystemExtension.Type.Scalar, data: Data) =
		transformScalarTypeExtension(extension, data)

	final override fun visitSchemaDefinition(definition: Definition.TypeSystem.Schema, data: Data) =
		transformSchemaDefinition(definition, data)

	final override fun visitSchemaExtension(extension: Definition.TypeSystemExtension.Schema, data: Data) =
		transformSchemaExtension(extension, data)

	final override fun visitSelection(selection: Selection, data: Data) =
		transformSelection(selection, data)

	final override fun visitSelectionSet(set: SelectionSet, data: Data) =
		transformSelectionSet(set, data)

	final override fun visitStringValue(value: Value.String, data: Data) =
		transformStringValue(value, data)

	final override fun visitTypeDefinition(definition: Definition.TypeSystem.Type, data: Data) =
		transformTypeDefinition(definition, data)

	final override fun visitTypeExtension(extension: Definition.TypeSystemExtension.Type, data: Data) =
		transformTypeExtension(extension, data)

	final override fun visitTypeReference(reference: TypeReference, data: Data) =
		transformTypeReference(reference, data)

	final override fun visitTypeSystemDefinition(definition: Definition.TypeSystem, data: Data) =
		transformTypeSystemDefinition(definition, data)

	final override fun visitTypeSystemExtension(extension: Definition.TypeSystemExtension, data: Data) =
		transformTypeSystemExtension(extension, data)

	final override fun visitUnionTypeDefinition(definition: Definition.TypeSystem.Type.Union, data: Data) =
		transformUnionTypeDefinition(definition, data)

	final override fun visitUnionTypeExtension(extension: Definition.TypeSystemExtension.Type.Union, data: Data) =
		transformUnionTypeExtension(extension, data)

	final override fun visitValue(value: Value, data: Data) =
		transformValue(value, data)

	final override fun visitVariableDefinition(value: VariableDefinition, data: Data) =
		transformVariableDefinition(value, data)

	final override fun visitVariableValue(value: Value.Variable, data: Data) =
		transformVariableValue(value, data)
}
