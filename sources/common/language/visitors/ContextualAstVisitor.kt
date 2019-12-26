package io.fluidsonic.graphql


internal class ContextualAstVisitor<out Result, in Data>(
	private val context: GAstVisitorContext,
	private val visitor: GAstVisitor<Result, Data>
) : GTraversingAstVisitor<Result, Data>() {

	private inline fun contextualize(node: GAst, visit: GAstVisitor<Result, Data>.() -> Result) =
		context.with(node) {
			visitor.visit()
				.also { descend() }
		}


	override fun traverseArgument(argument: GArgument, data: Data) =
		contextualize(argument) { visitArgument(argument, data) }

	override fun traverseArgumentDefinition(definition: GArgumentDefinition, data: Data) =
		contextualize(definition) { visitArgumentDefinition(definition, data) }

	override fun traverseBooleanValue(value: GValue.Boolean, data: Data) =
		contextualize(value) { visitBooleanValue(value, data) }

	override fun traverseDirective(directive: GDirective, data: Data) =
		contextualize(directive) { visitDirective(directive, data) }

	override fun traverseDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
		contextualize(definition) { visitDirectiveDefinition(definition, data) }

	override fun traverseDocument(document: GDocument, data: Data) =
		contextualize(document) { visitDocument(document, data) }

	override fun traverseEnumType(type: GEnumType, data: Data) =
		contextualize(type) { visitEnumType(type, data) }

	override fun traverseEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
		contextualize(extension) { visitEnumTypeExtension(extension, data) }

	override fun traverseEnumValue(value: GValue.Enum, data: Data) =
		contextualize(value) { visitEnumValue(value, data) }

	override fun traverseEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
		contextualize(definition) { visitEnumValueDefinition(definition, data) }

	override fun traverseFieldDefinition(definition: GFieldDefinition, data: Data) =
		contextualize(definition) { visitFieldDefinition(definition, data) }

	override fun traverseFieldSelection(selection: GFieldSelection, data: Data) =
		contextualize(selection) { visitFieldSelection(selection, data) }

	override fun traverseFloatValue(value: GValue.Float, data: Data) =
		contextualize(value) { visitFloatValue(value, data) }

	override fun traverseFragmentDefinition(definition: GFragmentDefinition, data: Data) =
		contextualize(definition) { visitFragmentDefinition(definition, data) }

	override fun traverseFragmentSelection(selection: GFragmentSelection, data: Data) =
		contextualize(selection) { visitFragmentSelection(selection, data) }

	override fun traverseInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
		contextualize(selection) { visitInlineFragmentSelection(selection, data) }

	override fun traverseInputObjectType(type: GInputObjectType, data: Data) =
		contextualize(type) { visitInputObjectType(type, data) }

	override fun traverseInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
		contextualize(extension) { visitInputObjectTypeExtension(extension, data) }

	override fun traverseIntValue(value: GValue.Int, data: Data) =
		contextualize(value) { visitIntValue(value, data) }

	override fun traverseInterfaceType(type: GInterfaceType, data: Data) =
		contextualize(type) { visitInterfaceType(type, data) }

	override fun traverseInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
		contextualize(extension) { visitInterfaceTypeExtension(extension, data) }

	override fun traverseListTypeRef(ref: GListTypeRef, data: Data) =
		contextualize(ref) { visitListTypeRef(ref, data) }

	override fun traverseListValue(value: GValue.List, data: Data) =
		contextualize(value) { visitListValue(value, data) }

	override fun traverseName(name: GName, data: Data) =
		contextualize(name) { visitName(name, data) }

	override fun traverseNamedTypeRef(ref: GNamedTypeRef, data: Data) =
		contextualize(ref) { visitNamedTypeRef(ref, data) }

	override fun traverseNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
		contextualize(ref) { visitNonNullTypeRef(ref, data) }

	override fun traverseNullValue(value: GValue.Null, data: Data) =
		contextualize(value) { visitNullValue(value, data) }

	override fun traverseObjectType(type: GObjectType, data: Data) =
		contextualize(type) { visitObjectType(type, data) }

	override fun traverseObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
		contextualize(extension) { visitObjectTypeExtension(extension, data) }

	override fun traverseObjectValue(value: GValue.Object, data: Data) =
		contextualize(value) { visitObjectValue(value, data) }

	override fun traverseObjectValueField(field: GObjectValueField, data: Data) =
		contextualize(field) { visitObjectValueField(field, data) }

	override fun traverseOperationDefinition(definition: GOperationDefinition, data: Data) =
		contextualize(definition) { visitOperationDefinition(definition, data) }

	override fun traverseOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
		contextualize(definition) { visitOperationTypeDefinition(definition, data) }

	override fun traverseScalarType(type: GScalarType, data: Data) =
		contextualize(type) { visitScalarType(type, data) }

	override fun traverseScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
		contextualize(extension) { visitScalarTypeExtension(extension, data) }

	override fun traverseSchemaDefinition(definition: GSchemaDefinition, data: Data) =
		contextualize(definition) { visitSchemaDefinition(definition, data) }

	override fun traverseSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data) =
		contextualize(definition) { visitSchemaExtensionDefinition(definition, data) }

	override fun traverseSelectionSet(set: GSelectionSet, data: Data) =
		contextualize(set) { visitSelectionSet(set, data) }

	override fun traverseStringValue(value: GValue.String, data: Data) =
		contextualize(value) { visitStringValue(value, data) }

	override fun traverseSyntheticNode(node: GAst, data: Data) =
		contextualize(node) { visitSyntheticNode(node, data) }

	override fun traverseUnionType(type: GUnionType, data: Data) =
		contextualize(type) { visitUnionType(type, data) }

	override fun traverseUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
		contextualize(extension) { visitUnionTypeExtension(extension, data) }

	override fun traverseVariableDefinition(definition: GVariableDefinition, data: Data) =
		contextualize(definition) { visitVariableDefinition(definition, data) }

	override fun traverseVariableValue(value: GValue.Variable, data: Data) =
		contextualize(value) { visitVariableValue(value, data) }
}


fun <Result, Data> GAstVisitor<Result, Data>.contextualize(context: GAstVisitorContext): GAstVisitor<Result, Data> =
	ContextualAstVisitor(context = context, visitor = this)
