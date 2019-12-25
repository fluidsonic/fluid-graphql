package io.fluidsonic.graphql


interface GAstVisitor<out Result, in Data> {

	fun visitArgument(argument: GArgument, data: Data): Result
	fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data): Result
	fun visitBooleanValue(value: GValue.Boolean, data: Data): Result
	fun visitDirective(directive: GDirective, data: Data): Result
	fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data): Result
	fun visitDocument(document: GDocument, data: Data): Result
	fun visitEnumType(type: GEnumType, data: Data): Result
	fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data): Result
	fun visitEnumValue(value: GValue.Enum, data: Data): Result
	fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data): Result
	fun visitFieldDefinition(definition: GFieldDefinition, data: Data): Result
	fun visitFieldSelection(selection: GFieldSelection, data: Data): Result
	fun visitFloatValue(value: GValue.Float, data: Data): Result
	fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data): Result
	fun visitFragmentSelection(selection: GFragmentSelection, data: Data): Result
	fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data): Result
	fun visitInputObjectType(type: GInputObjectType, data: Data): Result
	fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data): Result
	fun visitIntValue(value: GValue.Int, data: Data): Result
	fun visitInterfaceType(type: GInterfaceType, data: Data): Result
	fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data): Result
	fun visitListTypeRef(ref: GListTypeRef, data: Data): Result
	fun visitListValue(value: GValue.List, data: Data): Result
	fun visitName(name: GName, data: Data): Result
	fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data): Result
	fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data): Result
	fun visitNullValue(value: GValue.Null, data: Data): Result
	fun visitObjectType(type: GObjectType, data: Data): Result
	fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data): Result
	fun visitObjectValue(value: GValue.Object, data: Data): Result
	fun visitObjectValueField(field: GObjectValueField, data: Data): Result
	fun visitOperationDefinition(definition: GOperationDefinition, data: Data): Result
	fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data): Result
	fun visitScalarType(type: GScalarType, data: Data): Result
	fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data): Result
	fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data): Result
	fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data): Result
	fun visitSelectionSet(set: GSelectionSet, data: Data): Result
	fun visitStringValue(value: GValue.String, data: Data): Result
	fun visitSyntheticNode(node: GAst, data: Data): Result
	fun visitUnionType(type: GUnionType, data: Data): Result
	fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data): Result
	fun visitVariableDefinition(definition: GVariableDefinition, data: Data): Result
	fun visitVariableValue(value: GValue.Variable, data: Data): Result


	interface WithDefaults<out Result, in Data> : GAstVisitor<Result, Data> {

		fun visitAbstractType(type: GAbstractType, data: Data) =
			visitNamedType(type, data)

		override fun visitArgument(argument: GArgument, data: Data) =
			visitNode(argument, data)

		override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data) =
			visitNode(definition, data)

		override fun visitBooleanValue(value: GValue.Boolean, data: Data) =
			visitValue(value, data)

		fun visitCustomScalarType(type: GCustomScalarType, data: Data) =
			visitScalarType(type, data)

		fun visitDefinition(definition: GDefinition, data: Data) =
			visitNode(definition, data)

		override fun visitDirective(directive: GDirective, data: Data) =
			visitNode(directive, data)

		override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
			visitTypeSystemDefinition(definition, data)

		override fun visitDocument(document: GDocument, data: Data) =
			visitNode(document, data)

		override fun visitEnumType(type: GEnumType, data: Data) =
			visitNamedType(type, data)

		override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
			visitTypeExtension(extension, data)

		override fun visitEnumValue(value: GValue.Enum, data: Data) =
			visitValue(value, data)

		override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
			visitNode(definition, data)

		override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) =
			visitNode(definition, data)

		override fun visitFieldSelection(selection: GFieldSelection, data: Data) =
			visitSelection(selection, data)

		override fun visitFloatValue(value: GValue.Float, data: Data) =
			visitValue(value, data)

		override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) =
			visitDefinition(definition, data)

		override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) =
			visitSelection(selection, data)

		override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
			visitSelection(selection, data)

		override fun visitInputObjectType(type: GInputObjectType, data: Data) =
			visitNamedType(type, data)

		override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
			visitTypeExtension(extension, data)

		override fun visitIntValue(value: GValue.Int, data: Data) =
			visitValue(value, data)

		override fun visitInterfaceType(type: GInterfaceType, data: Data) =
			visitAbstractType(type, data)

		override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
			visitTypeExtension(extension, data)

		override fun visitListTypeRef(ref: GListTypeRef, data: Data) =
			visitTypeRef(ref, data)

		override fun visitListValue(value: GValue.List, data: Data) =
			visitValue(value, data)

		override fun visitName(name: GName, data: Data) =
			visitNode(name, data)

		fun visitNamedType(type: GNamedType, data: Data) =
			visitTypeSystemDefinition(type, data)

		override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) =
			visitTypeRef(ref, data)

		fun visitNode(node: GAst, data: Data): Result

		override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
			visitTypeRef(ref, data)

		override fun visitNullValue(value: GValue.Null, data: Data) =
			visitValue(value, data)

		override fun visitObjectType(type: GObjectType, data: Data) =
			visitNamedType(type, data)

		override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
			visitTypeExtension(extension, data)

		override fun visitObjectValue(value: GValue.Object, data: Data) =
			visitValue(value, data)

		override fun visitObjectValueField(field: GObjectValueField, data: Data) =
			visitNode(field, data)

		override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) =
			visitDefinition(definition, data)

		override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
			visitNode(definition, data)

		override fun visitScalarType(type: GScalarType, data: Data) =
			visitNamedType(type, data)

		override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
			visitTypeExtension(extension, data)

		override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) =
			visitTypeSystemDefinition(definition, data)

		override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data) =
			visitTypeSystemExtensionDefinition(definition, data)

		fun visitSelection(selection: GSelection, data: Data) =
			visitNode(selection, data)

		override fun visitSelectionSet(set: GSelectionSet, data: Data) =
			visitNode(set, data)

		override fun visitStringValue(value: GValue.String, data: Data) =
			visitValue(value, data)

		override fun visitSyntheticNode(node: GAst, data: Data) =
			visitNode(node, data)

		fun visitTypeExtension(extension: GTypeExtension, data: Data) =
			visitTypeSystemExtensionDefinition(extension, data)

		fun visitTypeRef(ref: GTypeRef, data: Data) =
			visitNode(ref, data)

		fun visitTypeSystemDefinition(definition: GTypeSystemDefinition, data: Data) =
			visitDefinition(definition, data)

		fun visitTypeSystemExtensionDefinition(definition: GTypeSystemExtensionDefinition, data: Data) =
			visitDefinition(definition, data)

		override fun visitUnionType(type: GUnionType, data: Data) =
			visitAbstractType(type, data)

		override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
			visitTypeExtension(extension, data)

		fun visitValue(value: GValue, data: Data) =
			visitNode(value, data)

		override fun visitVariableDefinition(definition: GVariableDefinition, data: Data) =
			visitNode(definition, data)

		override fun visitVariableValue(value: GValue.Variable, data: Data) =
			visitValue(value, data)
	}
}
