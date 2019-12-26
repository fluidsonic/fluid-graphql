package io.fluidsonic.graphql


abstract class GTraversingAstVisitor<out Result, in Data>() : GAstVisitor<Result, Data> {

	private var currentData: Data? = null
	private var currentNode: GAst? = null


	@Suppress("UNCHECKED_CAST")
	protected fun descend() {
		val node = checkNotNull(currentNode) { "descend() can only be used from within a traverse…() function." }

		node.acceptChildren(visitor = this, data = currentData as Data)
	}


	protected fun descend(data: Data) {
		val node = checkNotNull(currentNode) { "descend() can only be used from within a traverse…() function." }

		node.acceptChildren(visitor = this, data = data)
	}


	private inline fun traverse(node: GAst, data: Data, visit: () -> Result): Result {
		val parentData = currentData
		val parentNode = currentNode

		currentData = data
		currentNode = node

		try {
			return visit()
		}
		finally {
			currentData = parentData
			currentNode = parentNode
		}
	}


	abstract fun traverseArgument(argument: GArgument, data: Data): Result
	abstract fun traverseArgumentDefinition(definition: GArgumentDefinition, data: Data): Result
	abstract fun traverseBooleanValue(value: GValue.Boolean, data: Data): Result
	abstract fun traverseDirective(directive: GDirective, data: Data): Result
	abstract fun traverseDirectiveDefinition(definition: GDirectiveDefinition, data: Data): Result
	abstract fun traverseDocument(document: GDocument, data: Data): Result
	abstract fun traverseEnumType(type: GEnumType, data: Data): Result
	abstract fun traverseEnumTypeExtension(extension: GEnumTypeExtension, data: Data): Result
	abstract fun traverseEnumValue(value: GValue.Enum, data: Data): Result
	abstract fun traverseEnumValueDefinition(definition: GEnumValueDefinition, data: Data): Result
	abstract fun traverseFieldDefinition(definition: GFieldDefinition, data: Data): Result
	abstract fun traverseFieldSelection(selection: GFieldSelection, data: Data): Result
	abstract fun traverseFloatValue(value: GValue.Float, data: Data): Result
	abstract fun traverseFragmentDefinition(definition: GFragmentDefinition, data: Data): Result
	abstract fun traverseFragmentSelection(selection: GFragmentSelection, data: Data): Result
	abstract fun traverseInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data): Result
	abstract fun traverseInputObjectType(type: GInputObjectType, data: Data): Result
	abstract fun traverseInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data): Result
	abstract fun traverseIntValue(value: GValue.Int, data: Data): Result
	abstract fun traverseInterfaceType(type: GInterfaceType, data: Data): Result
	abstract fun traverseInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data): Result
	abstract fun traverseListTypeRef(ref: GListTypeRef, data: Data): Result
	abstract fun traverseListValue(value: GValue.List, data: Data): Result
	abstract fun traverseName(name: GName, data: Data): Result
	abstract fun traverseNamedTypeRef(ref: GNamedTypeRef, data: Data): Result
	abstract fun traverseNonNullTypeRef(ref: GNonNullTypeRef, data: Data): Result
	abstract fun traverseNullValue(value: GValue.Null, data: Data): Result
	abstract fun traverseObjectType(type: GObjectType, data: Data): Result
	abstract fun traverseObjectTypeExtension(extension: GObjectTypeExtension, data: Data): Result
	abstract fun traverseObjectValue(value: GValue.Object, data: Data): Result
	abstract fun traverseObjectValueField(field: GObjectValueField, data: Data): Result
	abstract fun traverseOperationDefinition(definition: GOperationDefinition, data: Data): Result
	abstract fun traverseOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data): Result
	abstract fun traverseScalarType(type: GScalarType, data: Data): Result
	abstract fun traverseScalarTypeExtension(extension: GScalarTypeExtension, data: Data): Result
	abstract fun traverseSchemaDefinition(definition: GSchemaDefinition, data: Data): Result
	abstract fun traverseSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data): Result
	abstract fun traverseSelectionSet(set: GSelectionSet, data: Data): Result
	abstract fun traverseStringValue(value: GValue.String, data: Data): Result
	abstract fun traverseSyntheticNode(node: GAst, data: Data): Result
	abstract fun traverseUnionType(type: GUnionType, data: Data): Result
	abstract fun traverseUnionTypeExtension(extension: GUnionTypeExtension, data: Data): Result
	abstract fun traverseVariableDefinition(definition: GVariableDefinition, data: Data): Result
	abstract fun traverseVariableValue(value: GValue.Variable, data: Data): Result


	final override fun visitArgument(argument: GArgument, data: Data) =
		traverse(argument, data) { traverseArgument(argument, data) }

	final override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data) =
		traverse(definition, data) { traverseArgumentDefinition(definition, data) }

	final override fun visitBooleanValue(value: GValue.Boolean, data: Data) =
		traverse(value, data) { traverseBooleanValue(value, data) }

	final override fun visitDirective(directive: GDirective, data: Data) =
		traverse(directive, data) { traverseDirective(directive, data) }

	final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
		traverse(definition, data) { traverseDirectiveDefinition(definition, data) }

	final override fun visitDocument(document: GDocument, data: Data) =
		traverse(document, data) { traverseDocument(document, data) }

	final override fun visitEnumType(type: GEnumType, data: Data) =
		traverse(type, data) { traverseEnumType(type, data) }

	final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
		traverse(extension, data) { traverseEnumTypeExtension(extension, data) }

	final override fun visitEnumValue(value: GValue.Enum, data: Data) =
		traverse(value, data) { traverseEnumValue(value, data) }

	final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
		traverse(definition, data) { traverseEnumValueDefinition(definition, data) }

	final override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) =
		traverse(definition, data) { traverseFieldDefinition(definition, data) }

	final override fun visitFieldSelection(selection: GFieldSelection, data: Data) =
		traverse(selection, data) { traverseFieldSelection(selection, data) }

	final override fun visitFloatValue(value: GValue.Float, data: Data) =
		traverse(value, data) { traverseFloatValue(value, data) }

	final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) =
		traverse(definition, data) { traverseFragmentDefinition(definition, data) }

	final override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) =
		traverse(selection, data) { traverseFragmentSelection(selection, data) }

	final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
		traverse(selection, data) { traverseInlineFragmentSelection(selection, data) }

	final override fun visitInputObjectType(type: GInputObjectType, data: Data) =
		traverse(type, data) { traverseInputObjectType(type, data) }

	final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
		traverse(extension, data) { traverseInputObjectTypeExtension(extension, data) }

	final override fun visitIntValue(value: GValue.Int, data: Data) =
		traverse(value, data) { traverseIntValue(value, data) }

	final override fun visitInterfaceType(type: GInterfaceType, data: Data) =
		traverse(type, data) { traverseInterfaceType(type, data) }

	final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
		traverse(extension, data) { traverseInterfaceTypeExtension(extension, data) }

	final override fun visitListTypeRef(ref: GListTypeRef, data: Data) =
		traverse(ref, data) { traverseListTypeRef(ref, data) }

	final override fun visitListValue(value: GValue.List, data: Data) =
		traverse(value, data) { traverseListValue(value, data) }

	final override fun visitName(name: GName, data: Data) =
		traverse(name, data) { traverseName(name, data) }

	final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) =
		traverse(ref, data) { traverseNamedTypeRef(ref, data) }

	final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
		traverse(ref, data) { traverseNonNullTypeRef(ref, data) }

	final override fun visitNullValue(value: GValue.Null, data: Data) =
		traverse(value, data) { traverseNullValue(value, data) }

	final override fun visitObjectType(type: GObjectType, data: Data) =
		traverse(type, data) { traverseObjectType(type, data) }

	final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
		traverse(extension, data) { traverseObjectTypeExtension(extension, data) }

	final override fun visitObjectValue(value: GValue.Object, data: Data) =
		traverse(value, data) { traverseObjectValue(value, data) }

	final override fun visitObjectValueField(field: GObjectValueField, data: Data) =
		traverse(field, data) { traverseObjectValueField(field, data) }

	final override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) =
		traverse(definition, data) { traverseOperationDefinition(definition, data) }

	final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
		traverse(definition, data) { traverseOperationTypeDefinition(definition, data) }

	final override fun visitScalarType(type: GScalarType, data: Data) =
		traverse(type, data) { traverseScalarType(type, data) }

	final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
		traverse(extension, data) { traverseScalarTypeExtension(extension, data) }

	final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) =
		traverse(definition, data) { traverseSchemaDefinition(definition, data) }

	final override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data) =
		traverse(definition, data) { traverseSchemaExtensionDefinition(definition, data) }

	final override fun visitSelectionSet(set: GSelectionSet, data: Data) =
		traverse(set, data) { traverseSelectionSet(set, data) }

	final override fun visitStringValue(value: GValue.String, data: Data) =
		traverse(value, data) { traverseStringValue(value, data) }

	final override fun visitSyntheticNode(node: GAst, data: Data) =
		traverse(node, data) { traverseSyntheticNode(node, data) }

	final override fun visitUnionType(type: GUnionType, data: Data) =
		traverse(type, data) { traverseUnionType(type, data) }

	final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
		traverse(extension, data) { traverseUnionTypeExtension(extension, data) }

	final override fun visitVariableDefinition(definition: GVariableDefinition, data: Data) =
		traverse(definition, data) { traverseVariableDefinition(definition, data) }

	final override fun visitVariableValue(value: GValue.Variable, data: Data) =
		traverse(value, data) { traverseVariableValue(value, data) }
}
