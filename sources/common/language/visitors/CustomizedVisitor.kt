package io.fluidsonic.graphql


internal abstract class CustomizedVisitor<out Result, Data> :
	Visitor<Result, Data>(),
	VisitDispatcher<Result, Data> {

	abstract override fun dispatchVisit(node: GAst, data: Data, coordination: VisitCoordination<Data>): Result


	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitArgument(argument: GArgument, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitBooleanType(type: GBooleanType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitBooleanValue(value: GBooleanValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitCustomScalarType(type: GCustomScalarType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitDirective(directive: GDirective, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitDocument(document: GDocument, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitEnumType(type: GEnumType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitEnumValue(value: GEnumValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitFieldSelection(selection: GFieldSelection, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitFloatType(type: GFloatType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitFloatValue(value: GFloatValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitIdType(type: GIdType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitInputObjectType(type: GInputObjectType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitIntType(type: GIntType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitIntValue(value: GIntValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitInterfaceType(type: GInterfaceType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitListTypeRef(ref: GListTypeRef, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitListValue(value: GListValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitName(name: GName, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitNullValue(value: GNullValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitObjectType(type: GObjectType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitObjectValue(value: GObjectValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitObjectValueField(field: GObjectValueField, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitSchemaExtensionDefinition(definition: GSchemaExtension, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitSelectionSet(set: GSelectionSet, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitStringType(type: GStringType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitStringValue(value: GStringValue, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitSyntheticNode(node: GAst, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitUnionType(type: GUnionType, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitVariableDefinition(definition: GVariableDefinition, data: Data) = error("not called")

	@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
	final override fun visitVariableRef(ref: GVariableRef, data: Data) = error("not called")
}
