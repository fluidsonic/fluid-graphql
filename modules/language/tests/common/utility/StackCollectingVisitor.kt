package testing

import io.fluidsonic.graphql.*


internal class StackCollectingVisitor(
	private val suffix: String = "",
	val target: Target = Target(),
	val skipsChildrenInNode: (node: GNode) -> Boolean = { false },
	val abortsInNode: (node: GNode) -> Boolean = { false }
) : Visitor.Typed<Unit, StackCollectingVisitor.Data>() {

	private fun on(node: GNode, name: String, data: Data, visit: Visit) {
		target.currentStack += "$name$suffix($data)"
		target.stacks += target.currentStack.toList()

		when {
			abortsInNode(node) -> visit.abort()
			skipsChildrenInNode(node) -> visit.skipChildren()
			else -> visit.visitChildren(Data(value = data.value + 1, suffix = suffix))
		}

		target.currentStack.removeAt(target.currentStack.size - 1)
	}


	override fun onArgument(argument: GArgument, data: Data, visit: Visit) = on(argument, "Argument", data, visit)
	override fun onArgumentDefinition(definition: GArgumentDefinition, data: Data, visit: Visit) = on(definition, "ArgumentDefinition", data, visit)
	override fun onBooleanValue(value: GBooleanValue, data: Data, visit: Visit) = on(value, "BooleanValue", data, visit)
	override fun onDirective(directive: GDirective, data: Data, visit: Visit) = on(directive, "Directive", data, visit)
	override fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Data, visit: Visit) = on(definition, "DirectiveDefinition", data, visit)
	override fun onDocument(document: GDocument, data: Data, visit: Visit) = on(document, "Document", data, visit)
	override fun onEnumType(type: GEnumType, data: Data, visit: Visit) = on(type, "EnumType", data, visit)
	override fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Data, visit: Visit) = on(extension, "EnumTypeExtension", data, visit)
	override fun onEnumValue(value: GEnumValue, data: Data, visit: Visit) = on(value, "EnumValue", data, visit)
	override fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Data, visit: Visit) = on(definition, "EnumValueDefinition", data, visit)
	override fun onFieldDefinition(definition: GFieldDefinition, data: Data, visit: Visit) = on(definition, "FieldDefinition", data, visit)
	override fun onFieldSelection(selection: GFieldSelection, data: Data, visit: Visit) = on(selection, "FieldSelection", data, visit)
	override fun onFloatValue(value: GFloatValue, data: Data, visit: Visit) = on(value, "FloatValue", data, visit)
	override fun onFragmentDefinition(definition: GFragmentDefinition, data: Data, visit: Visit) = on(definition, "FragmentDefinition", data, visit)
	override fun onFragmentSelection(selection: GFragmentSelection, data: Data, visit: Visit) = on(selection, "FragmentSelection", data, visit)
	override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data, visit: Visit) = on(selection, "InlineFragmentSelection", data, visit)
	override fun onInputObjectType(type: GInputObjectType, data: Data, visit: Visit) = on(type, "InputObjectType", data, visit)
	override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data, visit: Visit) = on(extension, "InputObjectTypeExtension", data, visit)
	override fun onIntValue(value: GIntValue, data: Data, visit: Visit) = on(value, "IntValue", data, visit)
	override fun onInterfaceType(type: GInterfaceType, data: Data, visit: Visit) = on(type, "InterfaceType", data, visit)
	override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data, visit: Visit) = on(extension, "InterfaceTypeExtension", data, visit)
	override fun onListTypeRef(ref: GListTypeRef, data: Data, visit: Visit) = on(ref, "ListTypeRef", data, visit)
	override fun onListValue(value: GListValue, data: Data, visit: Visit) = on(value, "ListValue", data, visit)
	override fun onName(name: GName, data: Data, visit: Visit) = on(name, "Name", data, visit)
	override fun onNamedTypeRef(ref: GNamedTypeRef, data: Data, visit: Visit) = on(ref, "NamedTypeRef", data, visit)
	override fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Data, visit: Visit) = on(ref, "NonNullTypeRef", data, visit)
	override fun onNullValue(value: GNullValue, data: Data, visit: Visit) = on(value, "NullValue", data, visit)
	override fun onObjectType(type: GObjectType, data: Data, visit: Visit) = on(type, "ObjectType", data, visit)
	override fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Data, visit: Visit) = on(extension, "ObjectTypeExtension", data, visit)
	override fun onObjectValue(value: GObjectValue, data: Data, visit: Visit) = on(value, "ObjectValue", data, visit)
	override fun onOperationDefinition(definition: GOperationDefinition, data: Data, visit: Visit) = on(definition, "OperationDefinition", data, visit)
	override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data, visit: Visit) = on(definition, "OperationTypeDefinition", data, visit)
	override fun onScalarType(type: GScalarType, data: Data, visit: Visit) = on(type, "ScalarType", data, visit)
	override fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Data, visit: Visit) = on(extension, "ScalarTypeExtension", data, visit)
	override fun onSchemaDefinition(definition: GSchemaDefinition, data: Data, visit: Visit) = on(definition, "SchemaDefinition", data, visit)
	override fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Data, visit: Visit) = on(definition, "SchemaExtensionDefinition", data, visit)
	override fun onSelectionSet(set: GSelectionSet, data: Data, visit: Visit) = on(set, "SelectionSet", data, visit)
	override fun onStringValue(value: GStringValue, data: Data, visit: Visit) = on(value, "StringValue", data, visit)
	override fun onSyntheticNode(node: GNode, data: Data, visit: Visit) = on(node, "SyntheticNode", data, visit)
	override fun onUnionType(type: GUnionType, data: Data, visit: Visit) = on(type, "UnionType", data, visit)
	override fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Data, visit: Visit) = on(extension, "UnionTypeExtension", data, visit)
	override fun onVariableDefinition(definition: GVariableDefinition, data: Data, visit: Visit) = on(definition, "VariableDefinition", data, visit)
	override fun onVariableRef(ref: GVariableRef, data: Data, visit: Visit) = on(ref, "VariableRef", data, visit)


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
