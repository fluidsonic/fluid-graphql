package io.fluidsonic.graphql


// TODO It may be okay to widen the transformation result type for some node types
abstract class GAstTransformer<in Data> : GAstVisitor<GAst, Data> {

	abstract fun transformArgument(argument: GArgument, data: Data): GArgument
	abstract fun transformArgumentDefinition(definition: GArgumentDefinition, data: Data): GArgumentDefinition
	abstract fun transformBooleanValue(value: GValue.Boolean, data: Data): GValue.Boolean
	abstract fun transformDirective(directive: GDirective, data: Data): GDirective
	abstract fun transformDirectiveDefinition(definition: GDirectiveDefinition, data: Data): GDirectiveDefinition
	abstract fun transformDocument(document: GDocument, data: Data): GDocument
	abstract fun transformEnumType(type: GEnumType, data: Data): GEnumType
	abstract fun transformEnumTypeExtension(extension: GEnumTypeExtension, data: Data): GEnumTypeExtension
	abstract fun transformEnumValue(value: GValue.Enum, data: Data): GValue.Enum
	abstract fun transformEnumValueDefinition(definition: GEnumValueDefinition, data: Data): GEnumValueDefinition
	abstract fun transformFieldDefinition(definition: GFieldDefinition, data: Data): GFieldDefinition
	abstract fun transformFieldSelection(selection: GFieldSelection, data: Data): GFieldSelection
	abstract fun transformFloatValue(value: GValue.Float, data: Data): GValue.Float
	abstract fun transformFragmentDefinition(definition: GFragmentDefinition, data: Data): GFragmentDefinition
	abstract fun transformFragmentSelection(selection: GFragmentSelection, data: Data): GFragmentSelection
	abstract fun transformInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data): GInlineFragmentSelection
	abstract fun transformInputObjectType(type: GInputObjectType, data: Data): GInputObjectType
	abstract fun transformInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data): GInputObjectTypeExtension
	abstract fun transformIntValue(value: GValue.Int, data: Data): GValue.Int
	abstract fun transformInterfaceType(type: GInterfaceType, data: Data): GInterfaceType
	abstract fun transformInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data): GInterfaceTypeExtension
	abstract fun transformListTypeRef(ref: GListTypeRef, data: Data): GListTypeRef
	abstract fun transformListValue(value: GValue.List, data: Data): GValue.List
	abstract fun transformName(name: GName, data: Data): GName
	abstract fun transformNamedTypeRef(ref: GNamedTypeRef, data: Data): GNamedTypeRef
	abstract fun transformNonNullTypeRef(ref: GNonNullTypeRef, data: Data): GNonNullTypeRef
	abstract fun transformNullValue(value: GValue.Null, data: Data): GValue.Null
	abstract fun transformObjectType(type: GObjectType, data: Data): GObjectType
	abstract fun transformObjectTypeExtension(extension: GObjectTypeExtension, data: Data): GObjectTypeExtension
	abstract fun transformObjectValue(value: GValue.Object, data: Data): GValue.Object
	abstract fun transformObjectValueField(field: GObjectValueField, data: Data): GObjectValueField
	abstract fun transformOperationDefinition(definition: GOperationDefinition, data: Data): GOperationDefinition
	abstract fun transformOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data): GOperationTypeDefinition
	abstract fun transformScalarType(type: GScalarType, data: Data): GScalarType
	abstract fun transformScalarTypeExtension(extension: GScalarTypeExtension, data: Data): GScalarTypeExtension
	abstract fun transformSchemaDefinition(definition: GSchemaDefinition, data: Data): GSchemaDefinition
	abstract fun transformSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data): GSchemaExtensionDefinition
	abstract fun transformSelectionSet(set: GSelectionSet, data: Data): GSelectionSet
	abstract fun transformStringValue(value: GValue.String, data: Data): GValue.String
	abstract fun transformSyntheticNode(node: GAst, data: Data): GAst
	abstract fun transformUnionType(type: GUnionType, data: Data): GUnionType
	abstract fun transformUnionTypeExtension(extension: GUnionTypeExtension, data: Data): GUnionTypeExtension
	abstract fun transformVariableDefinition(definition: GVariableDefinition, data: Data): GVariableDefinition
	abstract fun transformVariableValue(value: GValue.Variable, data: Data): GValue.Variable


	final override fun visitArgument(argument: GArgument, data: Data) =
		transformArgument(argument, data)

	final override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data) =
		transformArgumentDefinition(definition, data)

	final override fun visitBooleanValue(value: GValue.Boolean, data: Data) =
		transformBooleanValue(value, data)

	final override fun visitDirective(directive: GDirective, data: Data) =
		transformDirective(directive, data)

	final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
		transformDirectiveDefinition(definition, data)

	final override fun visitDocument(document: GDocument, data: Data) =
		transformDocument(document, data)

	final override fun visitEnumType(type: GEnumType, data: Data) =
		transformEnumType(type, data)

	final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
		transformEnumTypeExtension(extension, data)

	final override fun visitEnumValue(value: GValue.Enum, data: Data) =
		transformEnumValue(value, data)

	final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
		transformEnumValueDefinition(definition, data)

	final override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) =
		transformFieldDefinition(definition, data)

	final override fun visitFieldSelection(selection: GFieldSelection, data: Data) =
		transformFieldSelection(selection, data)

	final override fun visitFloatValue(value: GValue.Float, data: Data) =
		transformFloatValue(value, data)

	final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) =
		transformFragmentDefinition(definition, data)

	final override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) =
		transformFragmentSelection(selection, data)

	final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
		transformInlineFragmentSelection(selection, data)

	final override fun visitInputObjectType(type: GInputObjectType, data: Data) =
		transformInputObjectType(type, data)

	final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
		transformInputObjectTypeExtension(extension, data)

	final override fun visitIntValue(value: GValue.Int, data: Data) =
		transformIntValue(value, data)

	final override fun visitInterfaceType(type: GInterfaceType, data: Data) =
		transformInterfaceType(type, data)

	final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
		transformInterfaceTypeExtension(extension, data)

	final override fun visitListTypeRef(ref: GListTypeRef, data: Data) =
		transformListTypeRef(ref, data)

	final override fun visitListValue(value: GValue.List, data: Data) =
		transformListValue(value, data)

	final override fun visitName(name: GName, data: Data) =
		transformName(name, data)

	final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) =
		transformNamedTypeRef(ref, data)

	final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
		transformNonNullTypeRef(ref, data)

	final override fun visitNullValue(value: GValue.Null, data: Data) =
		transformNullValue(value, data)

	final override fun visitObjectType(type: GObjectType, data: Data) =
		transformObjectType(type, data)

	final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
		transformObjectTypeExtension(extension, data)

	final override fun visitObjectValue(value: GValue.Object, data: Data) =
		transformObjectValue(value, data)

	final override fun visitObjectValueField(field: GObjectValueField, data: Data) =
		transformObjectValueField(field, data)

	final override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) =
		transformOperationDefinition(definition, data)

	final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
		transformOperationTypeDefinition(definition, data)

	final override fun visitScalarType(type: GScalarType, data: Data) =
		transformScalarType(type, data)

	final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
		transformScalarTypeExtension(extension, data)

	final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) =
		transformSchemaDefinition(definition, data)

	final override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data) =
		transformSchemaExtensionDefinition(definition, data)

	final override fun visitSelectionSet(set: GSelectionSet, data: Data) =
		transformSelectionSet(set, data)

	final override fun visitStringValue(value: GValue.String, data: Data) =
		transformStringValue(value, data)

	final override fun visitSyntheticNode(node: GAst, data: Data) =
		transformSyntheticNode(node, data)

	final override fun visitUnionType(type: GUnionType, data: Data) =
		transformUnionType(type, data)

	final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
		transformUnionTypeExtension(extension, data)

	final override fun visitVariableDefinition(definition: GVariableDefinition, data: Data) =
		transformVariableDefinition(definition, data)

	final override fun visitVariableValue(value: GValue.Variable, data: Data) =
		transformVariableValue(value, data)


	abstract class WithDefaults<in Data> : GAstTransformer<Data>() {

		open fun <Type : GAbstractType> transformAbstractType(type: Type, data: Data) =
			transformNamedType(type, data)

		override fun transformArgument(argument: GArgument, data: Data) =
			transformNode(argument, data)

		override fun transformArgumentDefinition(definition: GArgumentDefinition, data: Data) =
			transformNode(definition, data)

		override fun transformBooleanValue(value: GValue.Boolean, data: Data) =
			transformValue(value, data)

		open fun transformCustomScalarType(type: GCustomScalarType, data: Data) =
			transformScalarType(type, data)

		open fun <Definition : GDefinition> transformDefinition(definition: Definition, data: Data) =
			transformNode(definition, data)

		override fun transformDirective(directive: GDirective, data: Data) =
			transformNode(directive, data)

		override fun transformDirectiveDefinition(definition: GDirectiveDefinition, data: Data) =
			transformTypeSystemDefinition(definition, data)

		override fun transformDocument(document: GDocument, data: Data) =
			transformNode(document, data)

		override fun transformEnumType(type: GEnumType, data: Data) =
			transformNamedType(type, data)

		override fun transformEnumTypeExtension(extension: GEnumTypeExtension, data: Data) =
			transformTypeExtension(extension, data)

		override fun transformEnumValue(value: GValue.Enum, data: Data) =
			transformValue(value, data)

		override fun transformEnumValueDefinition(definition: GEnumValueDefinition, data: Data) =
			transformNode(definition, data)

		open fun <Definition : GExecutableDefinition> transformExecutableDefinition(definition: Definition, data: Data) =
			transformDefinition(definition, data)

		override fun transformFieldDefinition(definition: GFieldDefinition, data: Data) =
			transformNode(definition, data)

		override fun transformFieldSelection(selection: GFieldSelection, data: Data) =
			transformSelection(selection, data)

		override fun transformFloatValue(value: GValue.Float, data: Data) =
			transformValue(value, data)

		override fun transformFragmentDefinition(definition: GFragmentDefinition, data: Data) =
			transformExecutableDefinition(definition, data)

		override fun transformFragmentSelection(selection: GFragmentSelection, data: Data) =
			transformSelection(selection, data)

		override fun transformInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) =
			transformSelection(selection, data)

		override fun transformInputObjectType(type: GInputObjectType, data: Data) =
			transformNamedType(type, data)

		override fun transformInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) =
			transformTypeExtension(extension, data)

		override fun transformIntValue(value: GValue.Int, data: Data) =
			transformValue(value, data)

		override fun transformInterfaceType(type: GInterfaceType, data: Data) =
			transformAbstractType(type, data)

		override fun transformInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) =
			transformTypeExtension(extension, data)

		override fun transformListTypeRef(ref: GListTypeRef, data: Data) =
			transformTypeRef(ref, data)

		override fun transformListValue(value: GValue.List, data: Data) =
			transformValue(value, data)

		override fun transformName(name: GName, data: Data) =
			transformNode(name, data)

		open fun <Type : GNamedType> transformNamedType(type: Type, data: Data) =
			transformTypeSystemDefinition(type, data)

		override fun transformNamedTypeRef(ref: GNamedTypeRef, data: Data) =
			transformTypeRef(ref, data)

		open fun <Node : GAst> transformNode(node: Node, data: Data) =
			node

		override fun transformNonNullTypeRef(ref: GNonNullTypeRef, data: Data) =
			transformTypeRef(ref, data)

		override fun transformNullValue(value: GValue.Null, data: Data) =
			transformValue(value, data)

		override fun transformObjectType(type: GObjectType, data: Data) =
			transformNamedType(type, data)

		override fun transformObjectTypeExtension(extension: GObjectTypeExtension, data: Data) =
			transformTypeExtension(extension, data)

		override fun transformObjectValue(value: GValue.Object, data: Data) =
			transformValue(value, data)

		override fun transformObjectValueField(field: GObjectValueField, data: Data) =
			transformNode(field, data)

		override fun transformOperationDefinition(definition: GOperationDefinition, data: Data) =
			transformExecutableDefinition(definition, data)

		override fun transformOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) =
			transformNode(definition, data)

		override fun transformScalarType(type: GScalarType, data: Data) =
			transformNamedType(type, data)

		override fun transformScalarTypeExtension(extension: GScalarTypeExtension, data: Data) =
			transformTypeExtension(extension, data)

		override fun transformSchemaDefinition(definition: GSchemaDefinition, data: Data) =
			transformTypeSystemDefinition(definition, data)

		override fun transformSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: Data) =
			transformTypeSystemExtensionDefinition(definition, data)

		open fun <Selection : GSelection> transformSelection(selection: Selection, data: Data) =
			transformNode(selection, data)

		override fun transformSelectionSet(set: GSelectionSet, data: Data) =
			transformNode(set, data)

		override fun transformStringValue(value: GValue.String, data: Data) =
			transformValue(value, data)

		override fun transformSyntheticNode(node: GAst, data: Data) =
			transformNode(node, data)

		open fun <Extension : GTypeExtension> transformTypeExtension(extension: Extension, data: Data) =
			transformTypeSystemExtensionDefinition(extension, data)

		open fun <Ref : GTypeRef> transformTypeRef(ref: Ref, data: Data) =
			transformNode(ref, data)

		open fun <Definition : GTypeSystemDefinition> transformTypeSystemDefinition(definition: Definition, data: Data) =
			transformDefinition(definition, data)

		open fun <Definition : GTypeSystemExtensionDefinition> transformTypeSystemExtensionDefinition(definition: Definition, data: Data) =
			transformDefinition(definition, data)

		override fun transformUnionType(type: GUnionType, data: Data) =
			transformAbstractType(type, data)

		override fun transformUnionTypeExtension(extension: GUnionTypeExtension, data: Data) =
			transformTypeExtension(extension, data)

		open fun <Value : GValue> transformValue(value: Value, data: Data) =
			transformNode(value, data)

		override fun transformVariableDefinition(definition: GVariableDefinition, data: Data) =
			transformNode(definition, data)

		override fun transformVariableValue(value: GValue.Variable, data: Data) =
			transformValue(value, data)
	}
}
