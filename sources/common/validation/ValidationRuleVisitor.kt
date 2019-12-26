package io.fluidsonic.graphql


internal class ValidationRuleVisitor(
	private val rule: ValidationRule
) : GAstVisitor<Unit, ValidationContext> {

	override fun visitArgument(argument: GArgument, data: ValidationContext) =
		rule.validateArgument(argument, data)

	override fun visitArgumentDefinition(definition: GArgumentDefinition, data: ValidationContext) =
		rule.validateArgumentDefinition(definition, data)

	override fun visitBooleanValue(value: GValue.Boolean, data: ValidationContext) =
		rule.validateBooleanValue(value, data)

	override fun visitDirective(directive: GDirective, data: ValidationContext) =
		rule.validateDirective(directive, data)

	override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: ValidationContext) =
		rule.validateDirectiveDefinition(definition, data)

	override fun visitDocument(document: GDocument, data: ValidationContext) =
		rule.validateDocument(document, data)

	override fun visitEnumType(type: GEnumType, data: ValidationContext) =
		rule.validateEnumType(type, data)

	override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: ValidationContext) =
		rule.validateEnumTypeExtension(extension, data)

	override fun visitEnumValue(value: GValue.Enum, data: ValidationContext) =
		rule.validateEnumValue(value, data)

	override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: ValidationContext) =
		rule.validateEnumValueDefinition(definition, data)

	override fun visitFieldDefinition(definition: GFieldDefinition, data: ValidationContext) =
		rule.validateFieldDefinition(definition, data)

	override fun visitFieldSelection(selection: GFieldSelection, data: ValidationContext) =
		rule.validateFieldSelection(selection, data)

	override fun visitFloatValue(value: GValue.Float, data: ValidationContext) =
		rule.validateFloatValue(value, data)

	override fun visitFragmentDefinition(definition: GFragmentDefinition, data: ValidationContext) =
		rule.validateFragmentDefinition(definition, data)

	override fun visitFragmentSelection(selection: GFragmentSelection, data: ValidationContext) =
		rule.validateFragmentSelection(selection, data)

	override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: ValidationContext) =
		rule.validateInlineFragmentSelection(selection, data)

	override fun visitInputObjectType(type: GInputObjectType, data: ValidationContext) =
		rule.validateInputObjectType(type, data)

	override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: ValidationContext) =
		rule.validateInputObjectTypeExtension(extension, data)

	override fun visitIntValue(value: GValue.Int, data: ValidationContext) =
		rule.validateIntValue(value, data)

	override fun visitInterfaceType(type: GInterfaceType, data: ValidationContext) =
		rule.validateInterfaceType(type, data)

	override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: ValidationContext) =
		rule.validateInterfaceTypeExtension(extension, data)

	override fun visitListTypeRef(ref: GListTypeRef, data: ValidationContext) =
		rule.validateListTypeRef(ref, data)

	override fun visitListValue(value: GValue.List, data: ValidationContext) =
		rule.validateListValue(value, data)

	override fun visitName(name: GName, data: ValidationContext) =
		rule.validateName(name, data)

	override fun visitNamedTypeRef(ref: GNamedTypeRef, data: ValidationContext) =
		rule.validateNamedTypeRef(ref, data)

	override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: ValidationContext) =
		rule.validateNonNullTypeRef(ref, data)

	override fun visitNullValue(value: GValue.Null, data: ValidationContext) =
		rule.validateNullValue(value, data)

	override fun visitObjectType(type: GObjectType, data: ValidationContext) =
		rule.validateObjectType(type, data)

	override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: ValidationContext) =
		rule.validateObjectTypeExtension(extension, data)

	override fun visitObjectValue(value: GValue.Object, data: ValidationContext) =
		rule.validateObjectValue(value, data)

	override fun visitObjectValueField(field: GObjectValueField, data: ValidationContext) =
		rule.validateObjectValueField(field, data)

	override fun visitOperationDefinition(definition: GOperationDefinition, data: ValidationContext) =
		rule.validateOperationDefinition(definition, data)

	override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: ValidationContext) =
		rule.validateOperationTypeDefinition(definition, data)

	override fun visitScalarType(type: GScalarType, data: ValidationContext) =
		rule.validateScalarType(type, data)

	override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: ValidationContext) =
		rule.validateScalarTypeExtension(extension, data)

	override fun visitSchemaDefinition(definition: GSchemaDefinition, data: ValidationContext) =
		rule.validateSchemaDefinition(definition, data)

	override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, data: ValidationContext) =
		rule.validateSchemaExtensionDefinition(definition, data)

	override fun visitSelectionSet(set: GSelectionSet, data: ValidationContext) =
		rule.validateSelectionSet(set, data)

	override fun visitStringValue(value: GValue.String, data: ValidationContext) =
		rule.validateStringValue(value, data)

	override fun visitSyntheticNode(node: GAst, data: ValidationContext) =
		rule.validateSyntheticNode(node, data)

	override fun visitUnionType(type: GUnionType, data: ValidationContext) =
		rule.validateUnionType(type, data)

	override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: ValidationContext) =
		rule.validateUnionTypeExtension(extension, data)

	override fun visitVariableDefinition(definition: GVariableDefinition, data: ValidationContext) =
		rule.validateVariableDefinition(definition, data)

	override fun visitVariableValue(value: GValue.Variable, data: ValidationContext) =
		rule.validateVariableValue(value, data)
}
