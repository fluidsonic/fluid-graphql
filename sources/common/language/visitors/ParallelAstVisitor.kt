package io.fluidsonic.graphql


internal class ParallelAstVisitor<in Data>(
	visitors: Collection<GAstVisitor<*, Data>>
) : GAstVisitor<Unit, Data> {

	private val visitors = visitors.toList()


	private inline fun parallelize(visit: GAstVisitor<*, Data>.() -> Unit) =
		visitors.forEach(visit)


	override fun visitArgument(argument: GArgument, data: Data) =
		parallelize { visitArgument(argument, data) }

	override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data) =
		parallelize { visitArgumentDefinition(definition, data) }

	override fun visitBooleanValue(value: GValue.Boolean, data: Data) =
		parallelize { visitBooleanValue(value, data) }

	override fun visitDirective(directive: GDirective, data: Data) =
		parallelize { visitDirective(directive, data) }

	override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
		parallelize { visitDirectiveDefinition(definition, data) }

	override fun visitDocument(document: GDocument, data: Data) =
		parallelize { visitDocument(document, data) }

	override fun visitEnumType(type: GEnumType, data: Data) =
		parallelize { visitEnumType(type, data) }

	override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
		parallelize { visitEnumTypeExtension(extension, data) }

	override fun visitEnumValue(value: GValue.Enum, data: Data) =
		parallelize { visitEnumValue(value, data) }

	override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
		parallelize { visitEnumValueDefinition(definition, data) }

	override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) =
		parallelize { visitFieldDefinition(definition, data) }

	override fun visitFieldSelection(selection: GFieldSelection, data: Data) =
		parallelize { visitFieldSelection(selection, data) }

	override fun visitFloatValue(value: GValue.Float, data: Data) =
		parallelize { visitFloatValue(value, data) }

	override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) =
		parallelize { visitFragmentDefinition(definition, data) }

	override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) =
		parallelize { visitFragmentSelection(selection, data) }

	override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
		parallelize { visitInlineFragmentSelection(selection, data) }

	override fun visitInputObjectType(type: GInputObjectType, data: Data) =
		parallelize { visitInputObjectType(type, data) }

	override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
		parallelize { visitInputObjectTypeExtension(extension, data) }

	override fun visitIntValue(value: GValue.Int, data: Data) =
		parallelize { visitIntValue(value, data) }

	override fun visitInterfaceType(type: GInterfaceType, data: Data) =
		parallelize { visitInterfaceType(type, data) }

	override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
		parallelize { visitInterfaceTypeExtension(extension, data) }

	override fun visitListTypeRef(ref: GListTypeRef, data: Data) =
		parallelize { visitListTypeRef(ref, data) }

	override fun visitListValue(value: GValue.List, data: Data) =
		parallelize { visitListValue(value, data) }

	override fun visitName(name: GName, data: Data) =
		parallelize { visitName(name, data) }

	override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) =
		parallelize { visitNamedTypeRef(ref, data) }

	override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
		parallelize { visitNonNullTypeRef(ref, data) }

	override fun visitNullValue(value: GValue.Null, data: Data) =
		parallelize { visitNullValue(value, data) }

	override fun visitObjectType(type: GObjectType, data: Data) =
		parallelize { visitObjectType(type, data) }

	override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
		parallelize { visitObjectTypeExtension(extension, data) }

	override fun visitObjectValue(value: GValue.Object, data: Data) =
		parallelize { visitObjectValue(value, data) }

	override fun visitObjectValueField(field: GObjectValueField, data: Data) =
		parallelize { visitObjectValueField(field, data) }

	override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) =
		parallelize { visitOperationDefinition(definition, data) }

	override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
		parallelize { visitOperationTypeDefinition(definition, data) }

	override fun visitScalarType(type: GScalarType, data: Data) =
		parallelize { visitScalarType(type, data) }

	override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
		parallelize { visitScalarTypeExtension(extension, data) }

	override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) =
		parallelize { visitSchemaDefinition(definition, data) }

	override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data) =
		parallelize { visitSchemaExtensionDefinition(definition, data) }

	override fun visitSelectionSet(set: GSelectionSet, data: Data) =
		parallelize { visitSelectionSet(set, data) }

	override fun visitStringValue(value: GValue.String, data: Data) =
		parallelize { visitStringValue(value, data) }

	override fun visitSyntheticNode(node: GAst, data: Data) =
		parallelize { visitSyntheticNode(node, data) }

	override fun visitUnionType(type: GUnionType, data: Data) =
		parallelize { visitUnionType(type, data) }

	override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
		parallelize { visitUnionTypeExtension(extension, data) }

	override fun visitVariableDefinition(definition: GVariableDefinition, data: Data) =
		parallelize { visitVariableDefinition(definition, data) }

	override fun visitVariableValue(value: GValue.Variable, data: Data) =
		parallelize { visitVariableValue(value, data) }
}


fun <Data> Collection<GAstVisitor<*, Data>>.parallelize(): GAstVisitor<Unit, Data> =
	ParallelAstVisitor(visitors = this)
