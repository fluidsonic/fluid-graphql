package io.fluidsonic.graphql


abstract class GAstVoidVisitor : GAstVisitor<Unit, Nothing?> {

	abstract fun visitArgument(argument: GArgument)
	abstract fun visitArgumentDefinition(definition: GArgumentDefinition)
	abstract fun visitBooleanValue(value: GValue.Boolean)
	abstract fun visitDirective(directive: GDirective)
	abstract fun visitDirectiveDefinition(definition: GDirectiveDefinition)
	abstract fun visitDocument(document: GDocument)
	abstract fun visitEnumType(type: GEnumType)
	abstract fun visitEnumTypeExtension(extension: GEnumTypeExtension)
	abstract fun visitEnumValue(value: GValue.Enum)
	abstract fun visitEnumValueDefinition(definition: GEnumValueDefinition)
	abstract fun visitFieldDefinition(definition: GFieldDefinition)
	abstract fun visitFieldSelection(selection: GFieldSelection)
	abstract fun visitFloatValue(value: GValue.Float)
	abstract fun visitFragmentDefinition(definition: GFragmentDefinition)
	abstract fun visitFragmentSelection(selection: GFragmentSelection)
	abstract fun visitInlineFragmentSelection(selection: GInlineFragmentSelection)
	abstract fun visitInputObjectType(type: GInputObjectType)
	abstract fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension)
	abstract fun visitIntValue(value: GValue.Int)
	abstract fun visitInterfaceType(type: GInterfaceType)
	abstract fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension)
	abstract fun visitListTypeRef(ref: GListTypeRef)
	abstract fun visitListValue(value: GValue.List)
	abstract fun visitName(name: GName)
	abstract fun visitNamedTypeRef(ref: GNamedTypeRef)
	abstract fun visitNonNullTypeRef(ref: GNonNullTypeRef)
	abstract fun visitNullValue(value: GValue.Null)
	abstract fun visitObjectType(type: GObjectType)
	abstract fun visitObjectTypeExtension(extension: GObjectTypeExtension)
	abstract fun visitObjectValue(value: GValue.Object)
	abstract fun visitObjectValueField(field: GObjectValueField)
	abstract fun visitOperationDefinition(definition: GOperationDefinition)
	abstract fun visitOperationTypeDefinition(definition: GOperationTypeDefinition)
	abstract fun visitScalarType(type: GScalarType)
	abstract fun visitScalarTypeExtension(extension: GScalarTypeExtension)
	abstract fun visitSchemaDefinition(definition: GSchemaDefinition)
	abstract fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition)
	abstract fun visitSelectionSet(set: GSelectionSet)
	abstract fun visitStringValue(value: GValue.String)
	abstract fun visitSyntheticNode(node: GAst)
	abstract fun visitUnionType(type: GUnionType)
	abstract fun visitUnionTypeExtension(extension: GUnionTypeExtension)
	abstract fun visitVariableDefinition(definition: GVariableDefinition)
	abstract fun visitVariableValue(value: GValue.Variable)


	final override fun visitArgument(argument: GArgument, data: Nothing?) =
		visitArgument(argument)

	final override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Nothing?) =
		visitArgumentDefinition(definition)

	final override fun visitBooleanValue(value: GValue.Boolean, data: Nothing?) =
		visitBooleanValue(value)

	final override fun visitDirective(directive: GDirective, data: Nothing?) =
		visitDirective(directive)

	final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?) =
		visitDirectiveDefinition(definition)

	final override fun visitDocument(document: GDocument, data: Nothing?) =
		visitDocument(document)

	final override fun visitEnumType(type: GEnumType, data: Nothing?) =
		visitEnumType(type)

	final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?) =
		visitEnumTypeExtension(extension)

	final override fun visitEnumValue(value: GValue.Enum, data: Nothing?) =
		visitEnumValue(value)

	final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?) =
		visitEnumValueDefinition(definition)

	final override fun visitFieldDefinition(definition: GFieldDefinition, data: Nothing?) =
		visitFieldDefinition(definition)

	final override fun visitFieldSelection(selection: GFieldSelection, data: Nothing?) =
		visitFieldSelection(selection)

	final override fun visitFloatValue(value: GValue.Float, data: Nothing?) =
		visitFloatValue(value)

	final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Nothing?) =
		visitFragmentDefinition(definition)

	final override fun visitFragmentSelection(selection: GFragmentSelection, data: Nothing?) =
		visitFragmentSelection(selection)

	final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?) =
		visitInlineFragmentSelection(selection)

	final override fun visitInputObjectType(type: GInputObjectType, data: Nothing?) =
		visitInputObjectType(type)

	final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?) =
		visitInputObjectTypeExtension(extension)

	final override fun visitIntValue(value: GValue.Int, data: Nothing?) =
		visitIntValue(value)

	final override fun visitInterfaceType(type: GInterfaceType, data: Nothing?) =
		visitInterfaceType(type)

	final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?) =
		visitInterfaceTypeExtension(extension)

	final override fun visitListTypeRef(ref: GListTypeRef, data: Nothing?) =
		visitListTypeRef(ref)

	final override fun visitListValue(value: GValue.List, data: Nothing?) =
		visitListValue(value)

	final override fun visitName(name: GName, data: Nothing?) =
		visitName(name)

	final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Nothing?) =
		visitNamedTypeRef(ref)

	final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?) =
		visitNonNullTypeRef(ref)

	final override fun visitNullValue(value: GValue.Null, data: Nothing?) =
		visitNullValue(value)

	final override fun visitObjectType(type: GObjectType, data: Nothing?) =
		visitObjectType(type)

	final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?) =
		visitObjectTypeExtension(extension)

	final override fun visitObjectValue(value: GValue.Object, data: Nothing?) =
		visitObjectValue(value)

	final override fun visitObjectValueField(field: GObjectValueField, data: Nothing?) =
		visitObjectValueField(field)

	final override fun visitOperationDefinition(definition: GOperationDefinition, data: Nothing?) =
		visitOperationDefinition(definition)

	final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?) =
		visitOperationTypeDefinition(definition)

	final override fun visitScalarType(type: GScalarType, data: Nothing?) =
		visitScalarType(type)

	final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?) =
		visitScalarTypeExtension(extension)

	final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Nothing?) =
		visitSchemaDefinition(definition)

	final override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Nothing?) =
		visitSchemaExtensionDefinition(definition)

	final override fun visitSelectionSet(set: GSelectionSet, data: Nothing?) =
		visitSelectionSet(set)

	final override fun visitStringValue(value: GValue.String, data: Nothing?) =
		visitStringValue(value)

	final override fun visitSyntheticNode(node: GAst, data: Nothing?) =
		visitSyntheticNode(node)

	final override fun visitUnionType(type: GUnionType, data: Nothing?) =
		visitUnionType(type)

	final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?) =
		visitUnionTypeExtension(extension)

	final override fun visitVariableDefinition(value: GVariableDefinition, data: Nothing?) =
		visitVariableDefinition(value)

	final override fun visitVariableValue(value: GValue.Variable, data: Nothing?) =
		visitVariableValue(value)


	abstract class WithDefaults : GAstVoidVisitor() {

		open fun visitAbstractType(type: GAbstractType) =
			visitNamedType(type)

		override fun visitArgument(argument: GArgument) =
			visitNode(argument)

		override fun visitArgumentDefinition(definition: GArgumentDefinition) =
			visitNode(definition)

		override fun visitBooleanValue(value: GValue.Boolean) =
			visitValue(value)

		open fun visitCustomScalarType(type: GCustomScalarType) =
			visitScalarType(type)

		open fun visitDefinition(definition: GDefinition) =
			visitNode(definition)

		override fun visitDirective(directive: GDirective) =
			visitNode(directive)

		override fun visitDirectiveDefinition(definition: GDirectiveDefinition) =
			visitTypeSystemDefinition(definition)

		override fun visitDocument(document: GDocument) =
			visitNode(document)

		override fun visitEnumType(type: GEnumType) =
			visitNamedType(type)

		override fun visitEnumTypeExtension(extension: GEnumTypeExtension) =
			visitTypeExtension(extension)

		override fun visitEnumValue(value: GValue.Enum) =
			visitValue(value)

		override fun visitEnumValueDefinition(definition: GEnumValueDefinition) =
			visitNode(definition)

		open fun visitExecutableDefinition(definition: GDefinition) =
			visitDefinition(definition)

		override fun visitFieldDefinition(definition: GFieldDefinition) =
			visitNode(definition)

		override fun visitFieldSelection(selection: GFieldSelection) =
			visitSelection(selection)

		override fun visitFloatValue(value: GValue.Float) =
			visitValue(value)

		override fun visitFragmentDefinition(definition: GFragmentDefinition) =
			visitExecutableDefinition(definition)

		override fun visitFragmentSelection(selection: GFragmentSelection) =
			visitSelection(selection)

		override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection) =
			visitSelection(selection)

		override fun visitInputObjectType(type: GInputObjectType) =
			visitNamedType(type)

		override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension) =
			visitTypeExtension(extension)

		override fun visitIntValue(value: GValue.Int) =
			visitValue(value)

		override fun visitInterfaceType(type: GInterfaceType) =
			visitAbstractType(type)

		override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension) =
			visitTypeExtension(extension)

		override fun visitListTypeRef(ref: GListTypeRef) =
			visitTypeRef(ref)

		override fun visitListValue(value: GValue.List) =
			visitValue(value)

		override fun visitName(name: GName) =
			visitNode(name)

		open fun visitNamedType(type: GNamedType) =
			visitTypeSystemDefinition(type)

		override fun visitNamedTypeRef(ref: GNamedTypeRef) =
			visitTypeRef(ref)

		open fun visitNode(node: GAst) =
			Unit

		override fun visitNonNullTypeRef(ref: GNonNullTypeRef) =
			visitTypeRef(ref)

		override fun visitNullValue(value: GValue.Null) =
			visitValue(value)

		override fun visitObjectType(type: GObjectType) =
			visitNamedType(type)

		override fun visitObjectTypeExtension(extension: GObjectTypeExtension) =
			visitTypeExtension(extension)

		override fun visitObjectValue(value: GValue.Object) =
			visitValue(value)

		override fun visitObjectValueField(field: GObjectValueField) =
			visitNode(field)

		override fun visitOperationDefinition(definition: GOperationDefinition) =
			visitExecutableDefinition(definition)

		override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition) =
			visitNode(definition)

		override fun visitScalarType(type: GScalarType) =
			visitNamedType(type)

		override fun visitScalarTypeExtension(extension: GScalarTypeExtension) =
			visitTypeExtension(extension)

		override fun visitSchemaDefinition(definition: GSchemaDefinition) =
			visitTypeSystemDefinition(definition)

		override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition) =
			visitTypeSystemExtensionDefinition(definition)

		open fun visitSelection(selection: GSelection) =
			visitNode(selection)

		override fun visitSelectionSet(set: GSelectionSet) =
			visitNode(set)

		override fun visitStringValue(value: GValue.String) =
			visitValue(value)

		override fun visitSyntheticNode(node: GAst) =
			visitNode(node)

		open fun visitTypeExtension(extension: GTypeExtension) =
			visitTypeSystemExtensionDefinition(extension)

		open fun visitTypeRef(ref: GTypeRef) =
			visitNode(ref)

		open fun visitTypeSystemDefinition(definition: GTypeSystemDefinition) =
			visitDefinition(definition)

		open fun visitTypeSystemExtensionDefinition(definition: GTypeSystemExtensionDefinition) =
			visitDefinition(definition)

		override fun visitUnionType(type: GUnionType) =
			visitAbstractType(type)

		override fun visitUnionTypeExtension(extension: GUnionTypeExtension) =
			visitTypeExtension(extension)

		open fun visitValue(value: GValue) =
			visitNode(value)

		override fun visitVariableDefinition(definition: GVariableDefinition) =
			visitNode(definition)

		override fun visitVariableValue(value: GValue.Variable) =
			visitValue(value)
	}
}
