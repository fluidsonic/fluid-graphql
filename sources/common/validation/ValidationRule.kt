package io.fluidsonic.graphql


internal interface ValidationRule {

	fun afterTraversal(context: ValidationContext) =
		Unit

	fun beforeTraversal(context: ValidationContext) =
		Unit

	fun validateAbstractType(type: GAbstractType, context: ValidationContext) =
		validateNamedType(type, context)

	fun validateArgument(argument: GArgument, context: ValidationContext) =
		validateNode(argument, context)

	fun validateArgumentDefinition(definition: GArgumentDefinition, context: ValidationContext) =
		validateNode(definition, context)

	fun validateBooleanValue(value: GValue.Boolean, context: ValidationContext) =
		validateValue(value, context)

	fun validateCustomScalarType(type: GCustomScalarType, context: ValidationContext) =
		validateScalarType(type, context)

	fun validateDefinition(definition: GDefinition, context: ValidationContext) =
		validateNode(definition, context)

	fun validateDirective(directive: GDirective, context: ValidationContext) =
		validateNode(directive, context)

	fun validateDirectiveDefinition(definition: GDirectiveDefinition, context: ValidationContext) =
		validateTypeSystemDefinition(definition, context)

	fun validateDocument(document: GDocument, context: ValidationContext) =
		validateNode(document, context)

	fun validateEnumType(type: GEnumType, context: ValidationContext) =
		validateNamedType(type, context)

	fun validateEnumTypeExtension(extension: GEnumTypeExtension, context: ValidationContext) =
		validateTypeExtension(extension, context)

	fun validateEnumValue(value: GValue.Enum, context: ValidationContext) =
		validateValue(value, context)

	fun validateEnumValueDefinition(definition: GEnumValueDefinition, context: ValidationContext) =
		validateNode(definition, context)

	fun validateExecutableDefinition(definition: GDefinition, context: ValidationContext) =
		validateDefinition(definition, context)

	fun validateFieldDefinition(definition: GFieldDefinition, context: ValidationContext) =
		validateNode(definition, context)

	fun validateFieldSelection(selection: GFieldSelection, context: ValidationContext) =
		validateSelection(selection, context)

	fun validateFloatValue(value: GValue.Float, context: ValidationContext) =
		validateValue(value, context)

	fun validateFragmentDefinition(definition: GFragmentDefinition, context: ValidationContext) =
		validateExecutableDefinition(definition, context)

	fun validateFragmentSelection(selection: GFragmentSelection, context: ValidationContext) =
		validateSelection(selection, context)

	fun validateInlineFragmentSelection(selection: GInlineFragmentSelection, context: ValidationContext) =
		validateSelection(selection, context)

	fun validateInputObjectType(type: GInputObjectType, context: ValidationContext) =
		validateNamedType(type, context)

	fun validateInputObjectTypeExtension(extension: GInputObjectTypeExtension, context: ValidationContext) =
		validateTypeExtension(extension, context)

	fun validateIntValue(value: GValue.Int, context: ValidationContext) =
		validateValue(value, context)

	fun validateInterfaceType(type: GInterfaceType, context: ValidationContext) =
		validateAbstractType(type, context)

	fun validateInterfaceTypeExtension(extension: GInterfaceTypeExtension, context: ValidationContext) =
		validateTypeExtension(extension, context)

	fun validateListTypeRef(ref: GListTypeRef, context: ValidationContext) =
		validateTypeRef(ref, context)

	fun validateListValue(value: GValue.List, context: ValidationContext) =
		validateValue(value, context)

	fun validateName(name: GName, context: ValidationContext) =
		validateNode(name, context)

	fun validateNamedType(type: GNamedType, context: ValidationContext) =
		validateTypeSystemDefinition(type, context)

	fun validateNamedTypeRef(ref: GNamedTypeRef, context: ValidationContext) =
		validateTypeRef(ref, context)

	fun validateNode(node: GAst, context: ValidationContext) =
		Unit

	fun validateNonNullTypeRef(ref: GNonNullTypeRef, context: ValidationContext) =
		validateTypeRef(ref, context)

	fun validateNullValue(value: GValue.Null, context: ValidationContext) =
		validateValue(value, context)

	fun validateObjectType(type: GObjectType, context: ValidationContext) =
		validateNamedType(type, context)

	fun validateObjectTypeExtension(extension: GObjectTypeExtension, context: ValidationContext) =
		validateTypeExtension(extension, context)

	fun validateObjectValue(value: GValue.Object, context: ValidationContext) =
		validateValue(value, context)

	fun validateObjectValueField(field: GObjectValueField, context: ValidationContext) =
		validateNode(field, context)

	fun validateOperationDefinition(definition: GOperationDefinition, context: ValidationContext) =
		validateExecutableDefinition(definition, context)

	fun validateOperationTypeDefinition(definition: GOperationTypeDefinition, context: ValidationContext) =
		validateNode(definition, context)

	fun validateScalarType(type: GScalarType, context: ValidationContext) =
		validateNamedType(type, context)

	fun validateScalarTypeExtension(extension: GScalarTypeExtension, context: ValidationContext) =
		validateTypeExtension(extension, context)

	fun validateSchemaDefinition(definition: GSchemaDefinition, context: ValidationContext) =
		validateTypeSystemDefinition(definition, context)

	fun validateSchemaExtensionDefinition(definition: GSchemaExtensionDefinition, context: ValidationContext) =
		validateTypeSystemExtensionDefinition(definition, context)

	fun validateSelection(selection: GSelection, context: ValidationContext) =
		validateNode(selection, context)

	fun validateSelectionSet(set: GSelectionSet, context: ValidationContext) =
		validateNode(set, context)

	fun validateStringValue(value: GValue.String, context: ValidationContext) =
		validateValue(value, context)

	fun validateSyntheticNode(node: GAst, context: ValidationContext) =
		validateNode(node, context)

	fun validateTypeExtension(extension: GTypeExtension, context: ValidationContext) =
		validateTypeSystemExtensionDefinition(extension, context)

	fun validateTypeRef(ref: GTypeRef, context: ValidationContext) =
		validateNode(ref, context)

	fun validateTypeSystemDefinition(definition: GTypeSystemDefinition, context: ValidationContext) =
		validateDefinition(definition, context)

	fun validateTypeSystemExtensionDefinition(definition: GTypeSystemExtensionDefinition, context: ValidationContext) =
		validateDefinition(definition, context)

	fun validateUnionType(type: GUnionType, context: ValidationContext) =
		validateAbstractType(type, context)

	fun validateUnionTypeExtension(extension: GUnionTypeExtension, context: ValidationContext) =
		validateTypeExtension(extension, context)

	fun validateValue(value: GValue, context: ValidationContext) =
		validateNode(value, context)

	fun validateVariableDefinition(definition: GVariableDefinition, context: ValidationContext) =
		validateNode(definition, context)

	fun validateVariableValue(value: GValue.Variable, context: ValidationContext) =
		validateValue(value, context)
}
