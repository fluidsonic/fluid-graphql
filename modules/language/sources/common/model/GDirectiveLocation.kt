package io.fluidsonic.graphql


public enum class GDirectiveLocation {

	ARGUMENT_DEFINITION,
	ENUM,
	ENUM_VALUE,
	FIELD,
	FIELD_DEFINITION,
	FRAGMENT_DEFINITION,
	FRAGMENT_SPREAD,
	INLINE_FRAGMENT,
	INPUT_FIELD_DEFINITION,
	INPUT_OBJECT,
	INTERFACE,
	MUTATION,
	OBJECT,
	QUERY,
	SCALAR,
	SCHEMA,
	SUBSCRIPTION,
	UNION,
	VARIABLE_DEFINITION;


	public companion object {

		public fun forNode(node: GNode): GDirectiveLocation? = when (node) {
			is GInputObjectArgumentDefinition -> INPUT_FIELD_DEFINITION // Must be checked before superclass 'GArgumentDefinition'.

			is GArgumentDefinition -> ARGUMENT_DEFINITION
			is GEnumType -> ENUM
			is GEnumTypeExtension -> ENUM
			is GEnumValueDefinition -> ENUM_VALUE
			is GFieldSelection -> FIELD
			is GFieldDefinition -> FIELD_DEFINITION
			is GFragmentDefinition -> FRAGMENT_DEFINITION
			is GFragmentSelection -> FRAGMENT_SPREAD
			is GInlineFragmentSelection -> INLINE_FRAGMENT
			is GInputObjectType -> INPUT_OBJECT
			is GInputObjectTypeExtension -> INPUT_OBJECT
			is GInterfaceType -> INTERFACE
			is GInterfaceTypeExtension -> INTERFACE
			is GObjectType -> OBJECT
			is GObjectTypeExtension -> OBJECT
			is GOperationDefinition -> when (node.type) {
				GOperationType.mutation -> MUTATION
				GOperationType.query -> QUERY
				GOperationType.subscription -> SUBSCRIPTION
			}
			is GScalarType -> SCALAR
			is GScalarTypeExtension -> SCALAR
			is GSchemaDefinition -> SCHEMA
			is GSchemaExtension -> SCHEMA
			is GUnionType -> UNION
			is GUnionTypeExtension -> UNION
			is GVariableDefinition -> VARIABLE_DEFINITION

			is GArgument,
			is GDirective,
			is GDirectiveDefinition,
			is GDocument,
			is GName,
			is GOperationTypeDefinition,
			is GSelectionSet,
			is GTypeRef,
			is GValue,
			is GWrappingType ->
				null
		}
	}
}
