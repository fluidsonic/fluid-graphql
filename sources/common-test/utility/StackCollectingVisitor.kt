package tests

import io.fluidsonic.graphql.*


internal class StackCollectingVisitor(
	private val suffix: String = "",
	val target: Target = Target(),
	val skipsChildrenInNode: (node: GAst) -> Boolean = { false },
	val abortsInNode: (node: GAst) -> Boolean = { false }

) : Visitor.WithoutResult<StackCollectingVisitor.Data>() {

	private fun visit(node: GAst, name: String, data: Data) {
		target.currentStack += "$name$suffix($data)"
		target.stacks += target.currentStack.toList()

		when {
			abortsInNode(node) -> abort()
			skipsChildrenInNode(node) -> skipChildren()
			else -> visitChildren(Data(value = data.value + 1, suffix = suffix))
		}

		target.currentStack.removeAt(target.currentStack.size - 1)
	}

	override fun visitArgument(argument: GArgument, data: Data) = visit(argument, "Argument", data)
	override fun visitBooleanType(type: GBooleanType, data: Data) = visit(type, "BooleanType", data)
	override fun visitBooleanValue(value: GBooleanValue, data: Data) = visit(value, "BooleanValue", data)
	override fun visitCustomScalarType(type: GCustomScalarType, data: Data) = visit(type, "CustomScalarType", data)
	override fun visitDirective(directive: GDirective, data: Data) = visit(directive, "Directive", data)
	override fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition, data: Data) = visit(definition, "DirectiveArgumentDefinition", data)
	override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) = visit(definition, "DirectiveDefinition", data)
	override fun visitDocument(document: GDocument, data: Data) = visit(document, "Document", data)
	override fun visitEnumType(type: GEnumType, data: Data) = visit(type, "EnumType", data)
	override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) = visit(extension, "EnumTypeExtension", data)
	override fun visitEnumValue(value: GEnumValue, data: Data) = visit(value, "EnumValue", data)
	override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) = visit(definition, "EnumValueDefinition", data)
	override fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition, data: Data) = visit(definition, "FieldArgumentDefinition", data)
	override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) = visit(definition, "FieldDefinition", data)
	override fun visitFieldSelection(selection: GFieldSelection, data: Data) = visit(selection, "FieldSelection", data)
	override fun visitFloatType(type: GFloatType, data: Data) = visit(type, "FloatType", data)
	override fun visitFloatValue(value: GFloatValue, data: Data) = visit(value, "FloatValue", data)
	override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) = visit(definition, "FragmentDefinition", data)
	override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) = visit(selection, "FragmentSelection", data)
	override fun visitIdType(type: GIdType, data: Data) = visit(type, "IdType", data)
	override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) = visit(selection, "InlineFragmentSelection", data)
	override fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition, data: Data) = visit(definition, "InputObjectArgumentDefinition", data)
	override fun visitInputObjectType(type: GInputObjectType, data: Data) = visit(type, "InputObjectType", data)
	override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) = visit(extension, "InputObjectTypeExtension", data)
	override fun visitIntType(type: GIntType, data: Data) = visit(type, "IntType", data)
	override fun visitIntValue(value: GIntValue, data: Data) = visit(value, "IntValue", data)
	override fun visitInterfaceType(type: GInterfaceType, data: Data) = visit(type, "InterfaceType", data)
	override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) = visit(extension, "InterfaceTypeExtension", data)
	override fun visitListTypeRef(ref: GListTypeRef, data: Data) = visit(ref, "ListTypeRef", data)
	override fun visitListValue(value: GListValue, data: Data) = visit(value, "ListValue", data)
	override fun visitName(name: GName, data: Data) = visit(name, "Name", data)
	override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) = visit(ref, "NamedTypeRef", data)
	override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) = visit(ref, "NonNullTypeRef", data)
	override fun visitNullValue(value: GNullValue, data: Data) = visit(value, "NullValue", data)
	override fun visitObjectType(type: GObjectType, data: Data) = visit(type, "ObjectType", data)
	override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) = visit(extension, "ObjectTypeExtension", data)
	override fun visitObjectValue(value: GObjectValue, data: Data) = visit(value, "ObjectValue", data)
	override fun visitObjectValueField(field: GObjectValueField, data: Data) = visit(field, "ObjectValueField", data)
	override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) = visit(definition, "OperationDefinition", data)
	override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) = visit(definition, "OperationTypeDefinition", data)
	override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) = visit(extension, "ScalarTypeExtension", data)
	override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) = visit(definition, "SchemaDefinition", data)
	override fun visitSchemaExtensionDefinition(definition: GSchemaExtension, data: Data) = visit(definition, "SchemaExtensionDefinition", data)
	override fun visitSelectionSet(set: GSelectionSet, data: Data) = visit(set, "SelectionSet", data)
	override fun visitStringType(type: GStringType, data: Data) = visit(type, "StringType", data)
	override fun visitStringValue(value: GStringValue, data: Data) = visit(value, "StringValue", data)
	override fun visitSyntheticNode(node: GAst, data: Data) = visit(node, "SyntheticNode", data)
	override fun visitUnionType(type: GUnionType, data: Data) = visit(type, "UnionType", data)
	override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) = visit(extension, "UnionTypeExtension", data)
	override fun visitVariableDefinition(definition: GVariableDefinition, data: Data) = visit(definition, "VariableDefinition", data)
	override fun visitVariableRef(ref: GVariableRef, data: Data) = visit(ref, "VariableRef", data)


	class Data(
		val value: Int = 0,
		val suffix: String = ""
	) {

		override fun toString() = "$value$suffix"
	}


	class Target {

		val currentStack = mutableListOf<String>()
		val stacks = mutableListOf<List<String>>()
	}
}
