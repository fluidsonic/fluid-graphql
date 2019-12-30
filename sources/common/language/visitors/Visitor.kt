package io.fluidsonic.graphql


abstract class Visitor<out Result, in Data> {

	abstract fun onNode(node: GAst, data: Data, visit: Visit): Result


	companion object {

		fun <Result> ofResult(result: Result) = object : Visitor<Result, Any?>() {

			override fun onNode(node: GAst, data: Any?, visit: Visit) =
				result
		}
	}


	abstract class Hierarchical<out Result, in Data> : Typed<Result, Data>() {

		protected abstract fun onAny(node: GAst, data: Data, visit: Visit): Result

		protected open fun onAbstractType(type: GAbstractType, data: Data, visit: Visit) = onCompositeType(type, data, visit)
		protected open fun onCompositeType(type: GCompositeType, data: Data, visit: Visit) = onNamedType(type, data, visit)
		protected open fun onDefinition(definition: GDefinition, data: Data, visit: Visit) = onAny(definition, data, visit)
		protected open fun onExecutableDefinition(definition: GExecutableDefinition, data: Data, visit: Visit) = onDefinition(definition, data, visit)
		protected open fun onLeafType(type: GLeafType, data: Data, visit: Visit) = onNamedType(type, data, visit)
		protected open fun onNamedType(type: GNamedType, data: Data, visit: Visit) = onType(type, data, visit)
		protected open fun onSelection(selection: GSelection, data: Data, visit: Visit) = onAny(selection, data, visit)
		protected open fun onType(type: GType, data: Data, visit: Visit) = onTypeSystemDefinition(type, data, visit)
		protected open fun onTypeExtension(extension: GTypeExtension, data: Data, visit: Visit) = onTypeSystemExtension(extension, data, visit)
		protected open fun onTypeRef(ref: GTypeRef, data: Data, visit: Visit) = onAny(ref, data, visit)
		protected open fun onTypeSystemDefinition(definition: GTypeSystemDefinition, data: Data, visit: Visit) = onDefinition(definition, data, visit)
		protected open fun onTypeSystemExtension(extension: GTypeSystemExtension, data: Data, visit: Visit) = onDefinition(extension, data, visit)
		protected open fun onValue(value: GValue, data: Data, visit: Visit) = onAny(value, data, visit)

		override fun onArgument(argument: GArgument, data: Data, visit: Visit) = onAny(argument, data, visit)
		override fun onArgumentDefinition(definition: GArgumentDefinition, data: Data, visit: Visit) = onAny(definition, data, visit)
		override fun onBooleanValue(value: GBooleanValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onDirective(directive: GDirective, data: Data, visit: Visit) = onAny(directive, data, visit)
		override fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Data, visit: Visit) = onTypeSystemDefinition(definition, data, visit)
		override fun onDocument(document: GDocument, data: Data, visit: Visit) = onAny(document, data, visit)
		override fun onEnumType(type: GEnumType, data: Data, visit: Visit) = onLeafType(type, data, visit)
		override fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Data, visit: Visit) = onTypeExtension(extension, data, visit)
		override fun onEnumValue(value: GEnumValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Data, visit: Visit) = onAny(definition, data, visit)
		override fun onFieldDefinition(definition: GFieldDefinition, data: Data, visit: Visit) = onAny(definition, data, visit)
		override fun onFieldSelection(selection: GFieldSelection, data: Data, visit: Visit) = onSelection(selection, data, visit)
		override fun onFloatValue(value: GFloatValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onFragmentDefinition(definition: GFragmentDefinition, data: Data, visit: Visit) = onExecutableDefinition(definition, data, visit)
		override fun onFragmentSelection(selection: GFragmentSelection, data: Data, visit: Visit) = onSelection(selection, data, visit)
		override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data, visit: Visit) = onSelection(selection, data, visit)
		override fun onInputObjectType(type: GInputObjectType, data: Data, visit: Visit) = onCompositeType(type, data, visit)
		override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data, visit: Visit) = onTypeExtension(extension, data, visit)
		override fun onIntValue(value: GIntValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onInterfaceType(type: GInterfaceType, data: Data, visit: Visit) = onAbstractType(type, data, visit)
		override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data, visit: Visit) = onTypeExtension(extension, data, visit)
		override fun onListTypeRef(ref: GListTypeRef, data: Data, visit: Visit) = onTypeRef(ref, data, visit)
		override fun onListValue(value: GListValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onName(name: GName, data: Data, visit: Visit) = onAny(name, data, visit)
		override fun onNamedTypeRef(ref: GNamedTypeRef, data: Data, visit: Visit) = onTypeRef(ref, data, visit)
		override fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Data, visit: Visit) = onTypeRef(ref, data, visit)
		override fun onNullValue(value: GNullValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onObjectType(type: GObjectType, data: Data, visit: Visit) = onCompositeType(type, data, visit)
		override fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Data, visit: Visit) = onTypeExtension(extension, data, visit)
		override fun onObjectValue(value: GObjectValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onObjectValueField(field: GObjectValueField, data: Data, visit: Visit) = onAny(field, data, visit)
		override fun onOperationDefinition(definition: GOperationDefinition, data: Data, visit: Visit) = onExecutableDefinition(definition, data, visit)
		override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data, visit: Visit) = onAny(definition, data, visit)
		override fun onScalarType(type: GScalarType, data: Data, visit: Visit) = onNamedType(type, data, visit)
		override fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Data, visit: Visit) = onTypeExtension(extension, data, visit)
		override fun onSchemaDefinition(definition: GSchemaDefinition, data: Data, visit: Visit) = onTypeSystemDefinition(definition, data, visit)
		override fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Data, visit: Visit) = onTypeSystemExtension(definition, data, visit)
		override fun onSelectionSet(set: GSelectionSet, data: Data, visit: Visit) = onAny(set, data, visit)
		override fun onStringValue(value: GStringValue, data: Data, visit: Visit) = onValue(value, data, visit)
		override fun onSyntheticNode(node: GAst, data: Data, visit: Visit) = onAny(node, data, visit)
		override fun onUnionType(type: GUnionType, data: Data, visit: Visit) = onAbstractType(type, data, visit)
		override fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Data, visit: Visit) = onTypeExtension(extension, data, visit)
		override fun onVariableDefinition(definition: GVariableDefinition, data: Data, visit: Visit) = onAny(definition, data, visit)
		override fun onVariableRef(ref: GVariableRef, data: Data, visit: Visit) = onValue(ref, data, visit)


		abstract class WithoutData<out Result> : Hierarchical<Result, Nothing?>() {

			protected abstract fun onAny(node: GAst, visit: Visit): Result

			protected open fun onAbstractType(type: GAbstractType, visit: Visit) = onCompositeType(type, visit)
			protected open fun onArgument(argument: GArgument, visit: Visit) = onAny(argument, visit)
			protected open fun onArgumentDefinition(definition: GArgumentDefinition, visit: Visit) = onAny(definition, visit)
			protected open fun onBooleanValue(value: GBooleanValue, visit: Visit) = onValue(value, visit)
			protected open fun onCompositeType(type: GCompositeType, visit: Visit) = onNamedType(type, visit)
			protected open fun onDefinition(definition: GDefinition, visit: Visit) = onAny(definition, visit)
			protected open fun onDirective(directive: GDirective, visit: Visit) = onAny(directive, visit)
			protected open fun onDirectiveDefinition(definition: GDirectiveDefinition, visit: Visit) = onTypeSystemDefinition(definition, visit)
			protected open fun onDocument(document: GDocument, visit: Visit) = onAny(document, visit)
			protected open fun onEnumType(type: GEnumType, visit: Visit) = onLeafType(type, visit)
			protected open fun onEnumTypeExtension(extension: GEnumTypeExtension, visit: Visit) = onTypeExtension(extension, visit)
			protected open fun onEnumValue(value: GEnumValue, visit: Visit) = onValue(value, visit)
			protected open fun onEnumValueDefinition(definition: GEnumValueDefinition, visit: Visit) = onAny(definition, visit)
			protected open fun onExecutableDefinition(definition: GExecutableDefinition, visit: Visit) = onDefinition(definition, visit)
			protected open fun onFieldDefinition(definition: GFieldDefinition, visit: Visit) = onAny(definition, visit)
			protected open fun onFieldSelection(selection: GFieldSelection, visit: Visit) = onSelection(selection, visit)
			protected open fun onFloatValue(value: GFloatValue, visit: Visit) = onValue(value, visit)
			protected open fun onFragmentDefinition(definition: GFragmentDefinition, visit: Visit) = onExecutableDefinition(definition, visit)
			protected open fun onFragmentSelection(selection: GFragmentSelection, visit: Visit) = onSelection(selection, visit)
			protected open fun onInlineFragmentSelection(selection: GInlineFragmentSelection, visit: Visit) = onSelection(selection, visit)
			protected open fun onInputObjectType(type: GInputObjectType, visit: Visit) = onCompositeType(type, visit)
			protected open fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, visit: Visit) = onTypeExtension(extension, visit)
			protected open fun onIntValue(value: GIntValue, visit: Visit) = onValue(value, visit)
			protected open fun onInterfaceType(type: GInterfaceType, visit: Visit) = onAbstractType(type, visit)
			protected open fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, visit: Visit) = onTypeExtension(extension, visit)
			protected open fun onLeafType(type: GLeafType, visit: Visit) = onNamedType(type, visit)
			protected open fun onListTypeRef(ref: GListTypeRef, visit: Visit) = onTypeRef(ref, visit)
			protected open fun onListValue(value: GListValue, visit: Visit) = onValue(value, visit)
			protected open fun onName(name: GName, visit: Visit) = onAny(name, visit)
			protected open fun onNamedType(type: GNamedType, visit: Visit) = onType(type, visit)
			protected open fun onNamedTypeRef(ref: GNamedTypeRef, visit: Visit) = onTypeRef(ref, visit)
			protected open fun onNonNullTypeRef(ref: GNonNullTypeRef, visit: Visit) = onTypeRef(ref, visit)
			protected open fun onNullValue(value: GNullValue, visit: Visit) = onValue(value, visit)
			protected open fun onObjectType(type: GObjectType, visit: Visit) = onCompositeType(type, visit)
			protected open fun onObjectTypeExtension(extension: GObjectTypeExtension, visit: Visit) = onTypeExtension(extension, visit)
			protected open fun onObjectValue(value: GObjectValue, visit: Visit) = onValue(value, visit)
			protected open fun onObjectValueField(field: GObjectValueField, visit: Visit) = onAny(field, visit)
			protected open fun onOperationDefinition(definition: GOperationDefinition, visit: Visit) = onExecutableDefinition(definition, visit)
			protected open fun onOperationTypeDefinition(definition: GOperationTypeDefinition, visit: Visit) = onAny(definition, visit)
			protected open fun onScalarType(type: GScalarType, visit: Visit) = onNamedType(type, visit)
			protected open fun onScalarTypeExtension(extension: GScalarTypeExtension, visit: Visit) = onTypeExtension(extension, visit)
			protected open fun onSchemaDefinition(definition: GSchemaDefinition, visit: Visit) = onTypeSystemDefinition(definition, visit)
			protected open fun onSchemaExtensionDefinition(definition: GSchemaExtension, visit: Visit) = onTypeSystemExtension(definition, visit)
			protected open fun onSelection(selection: GSelection, visit: Visit) = onAny(selection, visit)
			protected open fun onSelectionSet(set: GSelectionSet, visit: Visit) = onAny(set, visit)
			protected open fun onStringValue(value: GStringValue, visit: Visit) = onValue(value, visit)
			protected open fun onSyntheticNode(node: GAst, visit: Visit) = onAny(node, visit)
			protected open fun onType(type: GType, visit: Visit) = onTypeSystemDefinition(type, visit)
			protected open fun onTypeExtension(extension: GTypeExtension, visit: Visit) = onTypeSystemExtension(extension, visit)
			protected open fun onTypeRef(ref: GTypeRef, visit: Visit) = onAny(ref, visit)
			protected open fun onTypeSystemDefinition(definition: GTypeSystemDefinition, visit: Visit) = onDefinition(definition, visit)
			protected open fun onTypeSystemExtension(extension: GTypeSystemExtension, visit: Visit) = onDefinition(extension, visit)
			protected open fun onUnionType(type: GUnionType, visit: Visit) = onAbstractType(type, visit)
			protected open fun onUnionTypeExtension(extension: GUnionTypeExtension, visit: Visit) = onTypeExtension(extension, visit)
			protected open fun onValue(value: GValue, visit: Visit) = onAny(value, visit)
			protected open fun onVariableDefinition(definition: GVariableDefinition, visit: Visit) = onAny(definition, visit)
			protected open fun onVariableRef(ref: GVariableRef, visit: Visit) = onValue(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onAbstractType(type: GAbstractType, data: Nothing?, visit: Visit) = onCompositeType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onAny(node: GAst, data: Nothing?, visit: Visit) = onAny(node, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgument(argument: GArgument, data: Nothing?, visit: Visit) = onArgument(argument, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgumentDefinition(definition: GArgumentDefinition, data: Nothing?, visit: Visit) = onAny(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onBooleanValue(value: GBooleanValue, data: Nothing?, visit: Visit) = onBooleanValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onCompositeType(type: GCompositeType, data: Nothing?, visit: Visit) = onNamedType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDefinition(definition: GDefinition, data: Nothing?, visit: Visit) = onAny(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirective(directive: GDirective, data: Nothing?, visit: Visit) = onDirective(directive, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?, visit: Visit) = onDirectiveDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDocument(document: GDocument, data: Nothing?, visit: Visit) = onDocument(document, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumType(type: GEnumType, data: Nothing?, visit: Visit) = onEnumType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?, visit: Visit) = onEnumTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValue(value: GEnumValue, data: Nothing?, visit: Visit) = onEnumValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?, visit: Visit) = onEnumValueDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onExecutableDefinition(definition: GExecutableDefinition, data: Nothing?, visit: Visit) = onDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldDefinition(definition: GFieldDefinition, data: Nothing?, visit: Visit) = onFieldDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldSelection(selection: GFieldSelection, data: Nothing?, visit: Visit) = onFieldSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFloatValue(value: GFloatValue, data: Nothing?, visit: Visit) = onFloatValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentDefinition(definition: GFragmentDefinition, data: Nothing?, visit: Visit) = onFragmentDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentSelection(selection: GFragmentSelection, data: Nothing?, visit: Visit) = onFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?, visit: Visit) = onInlineFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectType(type: GInputObjectType, data: Nothing?, visit: Visit) = onInputObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?, visit: Visit) = onInputObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onIntValue(value: GIntValue, data: Nothing?, visit: Visit) = onIntValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceType(type: GInterfaceType, data: Nothing?, visit: Visit) = onInterfaceType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?, visit: Visit) = onInterfaceTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onLeafType(type: GLeafType, data: Nothing?, visit: Visit) = onNamedType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListTypeRef(ref: GListTypeRef, data: Nothing?, visit: Visit) = onListTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListValue(value: GListValue, data: Nothing?, visit: Visit) = onListValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onName(name: GName, data: Nothing?, visit: Visit) = onName(name, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNamedType(type: GNamedType, data: Nothing?, visit: Visit) = onType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNamedTypeRef(ref: GNamedTypeRef, data: Nothing?, visit: Visit) = onNamedTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?, visit: Visit) = onNonNullTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNullValue(value: GNullValue, data: Nothing?, visit: Visit) = onNullValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectType(type: GObjectType, data: Nothing?, visit: Visit) = onObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?, visit: Visit) = onObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectValue(value: GObjectValue, data: Nothing?, visit: Visit) = onObjectValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectValueField(field: GObjectValueField, data: Nothing?, visit: Visit) = onObjectValueField(field, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationDefinition(definition: GOperationDefinition, data: Nothing?, visit: Visit) = onOperationDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?, visit: Visit) = onOperationTypeDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarType(type: GScalarType, data: Nothing?, visit: Visit) = onNamedType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?, visit: Visit) = onScalarTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaDefinition(definition: GSchemaDefinition, data: Nothing?, visit: Visit) = onSchemaDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Nothing?, visit: Visit) = onSchemaExtensionDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSelection(selection: GSelection, data: Nothing?, visit: Visit) = onAny(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSelectionSet(set: GSelectionSet, data: Nothing?, visit: Visit) = onSelectionSet(set, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onStringValue(value: GStringValue, data: Nothing?, visit: Visit) = onStringValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSyntheticNode(node: GAst, data: Nothing?, visit: Visit) = onSyntheticNode(node, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onType(type: GType, data: Nothing?, visit: Visit) = onTypeSystemDefinition(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeExtension(extension: GTypeExtension, data: Nothing?, visit: Visit) = onTypeSystemExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeRef(ref: GTypeRef, data: Nothing?, visit: Visit) = onAny(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeSystemDefinition(definition: GTypeSystemDefinition, data: Nothing?, visit: Visit) = onDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeSystemExtension(extension: GTypeSystemExtension, data: Nothing?, visit: Visit) = onDefinition(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionType(type: GUnionType, data: Nothing?, visit: Visit) = onUnionType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?, visit: Visit) = onUnionTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onValue(value: GValue, data: Nothing?, visit: Visit) = onAny(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableDefinition(definition: GVariableDefinition, data: Nothing?, visit: Visit) = onVariableDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableRef(ref: GVariableRef, data: Nothing?, visit: Visit) = onVariableRef(ref, visit)
		}
	}


	abstract class Typed<out Result, in Data> : Visitor<Result, Data>() {

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun onNode(node: GAst, data: Data, visit: Visit) =
			when (node) {
				is GArgument -> onArgument(node, data, visit)
				is GArgumentDefinition -> onArgumentDefinition(node, data, visit)
				is GBooleanValue -> onBooleanValue(node, data, visit)
				is GDirective -> onDirective(node, data, visit)
				is GDirectiveDefinition -> onDirectiveDefinition(node, data, visit)
				is GDocument -> onDocument(node, data, visit)
				is GEnumType -> onEnumType(node, data, visit)
				is GEnumTypeExtension -> onEnumTypeExtension(node, data, visit)
				is GEnumValue -> onEnumValue(node, data, visit)
				is GEnumValueDefinition -> onEnumValueDefinition(node, data, visit)
				is GFieldDefinition -> onFieldDefinition(node, data, visit)
				is GFieldSelection -> onFieldSelection(node, data, visit)
				is GFloatValue -> onFloatValue(node, data, visit)
				is GFragmentDefinition -> onFragmentDefinition(node, data, visit)
				is GFragmentSelection -> onFragmentSelection(node, data, visit)
				is GInlineFragmentSelection -> onInlineFragmentSelection(node, data, visit)
				is GInputObjectType -> onInputObjectType(node, data, visit)
				is GInputObjectTypeExtension -> onInputObjectTypeExtension(node, data, visit)
				is GIntValue -> onIntValue(node, data, visit)
				is GInterfaceType -> onInterfaceType(node, data, visit)
				is GInterfaceTypeExtension -> onInterfaceTypeExtension(node, data, visit)
				is GListType -> onSyntheticNode(node, data, visit)
				is GListTypeRef -> onListTypeRef(node, data, visit)
				is GListValue -> onListValue(node, data, visit)
				is GName -> onName(node, data, visit)
				is GNamedTypeRef -> onNamedTypeRef(node, data, visit)
				is GNonNullType -> onSyntheticNode(node, data, visit)
				is GNonNullTypeRef -> onNonNullTypeRef(node, data, visit)
				is GNullValue -> onNullValue(node, data, visit)
				is GObjectType -> onObjectType(node, data, visit)
				is GObjectTypeExtension -> onObjectTypeExtension(node, data, visit)
				is GObjectValue -> onObjectValue(node, data, visit)
				is GObjectValueField -> onObjectValueField(node, data, visit)
				is GOperationDefinition -> onOperationDefinition(node, data, visit)
				is GOperationTypeDefinition -> onOperationTypeDefinition(node, data, visit)
				is GScalarType -> onScalarType(node, data, visit)
				is GScalarTypeExtension -> onScalarTypeExtension(node, data, visit)
				is GSchemaDefinition -> onSchemaDefinition(node, data, visit)
				is GSchemaExtension -> onSchemaExtensionDefinition(node, data, visit)
				is GSelectionSet -> onSelectionSet(node, data, visit)
				is GStringValue -> onStringValue(node, data, visit)
				is GUnionType -> onUnionType(node, data, visit)
				is GUnionTypeExtension -> onUnionTypeExtension(node, data, visit)
				is GVariableDefinition -> onVariableDefinition(node, data, visit)
				is GVariableRef -> onVariableRef(node, data, visit)
			}


		protected abstract fun onArgument(argument: GArgument, data: Data, visit: Visit): Result
		protected abstract fun onArgumentDefinition(definition: GArgumentDefinition, data: Data, visit: Visit): Result
		protected abstract fun onBooleanValue(value: GBooleanValue, data: Data, visit: Visit): Result
		protected abstract fun onDirective(directive: GDirective, data: Data, visit: Visit): Result
		protected abstract fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Data, visit: Visit): Result
		protected abstract fun onDocument(document: GDocument, data: Data, visit: Visit): Result
		protected abstract fun onEnumType(type: GEnumType, data: Data, visit: Visit): Result
		protected abstract fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onEnumValue(value: GEnumValue, data: Data, visit: Visit): Result
		protected abstract fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Data, visit: Visit): Result
		protected abstract fun onFieldDefinition(definition: GFieldDefinition, data: Data, visit: Visit): Result
		protected abstract fun onFieldSelection(selection: GFieldSelection, data: Data, visit: Visit): Result
		protected abstract fun onFloatValue(value: GFloatValue, data: Data, visit: Visit): Result
		protected abstract fun onFragmentDefinition(definition: GFragmentDefinition, data: Data, visit: Visit): Result
		protected abstract fun onFragmentSelection(selection: GFragmentSelection, data: Data, visit: Visit): Result
		protected abstract fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data, visit: Visit): Result
		protected abstract fun onInputObjectType(type: GInputObjectType, data: Data, visit: Visit): Result
		protected abstract fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onIntValue(value: GIntValue, data: Data, visit: Visit): Result
		protected abstract fun onInterfaceType(type: GInterfaceType, data: Data, visit: Visit): Result
		protected abstract fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onListTypeRef(ref: GListTypeRef, data: Data, visit: Visit): Result
		protected abstract fun onListValue(value: GListValue, data: Data, visit: Visit): Result
		protected abstract fun onName(name: GName, data: Data, visit: Visit): Result
		protected abstract fun onNamedTypeRef(ref: GNamedTypeRef, data: Data, visit: Visit): Result
		protected abstract fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Data, visit: Visit): Result
		protected abstract fun onNullValue(value: GNullValue, data: Data, visit: Visit): Result
		protected abstract fun onObjectType(type: GObjectType, data: Data, visit: Visit): Result
		protected abstract fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onObjectValue(value: GObjectValue, data: Data, visit: Visit): Result
		protected abstract fun onObjectValueField(field: GObjectValueField, data: Data, visit: Visit): Result
		protected abstract fun onOperationDefinition(definition: GOperationDefinition, data: Data, visit: Visit): Result
		protected abstract fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data, visit: Visit): Result
		protected abstract fun onScalarType(type: GScalarType, data: Data, visit: Visit): Result
		protected abstract fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onSchemaDefinition(definition: GSchemaDefinition, data: Data, visit: Visit): Result
		protected abstract fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Data, visit: Visit): Result
		protected abstract fun onSelectionSet(set: GSelectionSet, data: Data, visit: Visit): Result
		protected abstract fun onStringValue(value: GStringValue, data: Data, visit: Visit): Result
		protected abstract fun onSyntheticNode(node: GAst, data: Data, visit: Visit): Result
		protected abstract fun onUnionType(type: GUnionType, data: Data, visit: Visit): Result
		protected abstract fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onVariableDefinition(definition: GVariableDefinition, data: Data, visit: Visit): Result
		protected abstract fun onVariableRef(ref: GVariableRef, data: Data, visit: Visit): Result


		abstract class WithoutData<out Result> : Typed<Result, Nothing?>() {

			protected abstract fun onArgument(argument: GArgument, visit: Visit): Result
			protected abstract fun onArgumentDefinition(definition: GArgumentDefinition, visit: Visit): Result
			protected abstract fun onBooleanValue(value: GBooleanValue, visit: Visit): Result
			protected abstract fun onDirective(directive: GDirective, visit: Visit): Result
			protected abstract fun onDirectiveDefinition(definition: GDirectiveDefinition, visit: Visit): Result
			protected abstract fun onDocument(document: GDocument, visit: Visit): Result
			protected abstract fun onEnumType(type: GEnumType, visit: Visit): Result
			protected abstract fun onEnumTypeExtension(extension: GEnumTypeExtension, visit: Visit): Result
			protected abstract fun onEnumValue(value: GEnumValue, visit: Visit): Result
			protected abstract fun onEnumValueDefinition(definition: GEnumValueDefinition, visit: Visit): Result
			protected abstract fun onFieldDefinition(definition: GFieldDefinition, visit: Visit): Result
			protected abstract fun onFieldSelection(selection: GFieldSelection, visit: Visit): Result
			protected abstract fun onFloatValue(value: GFloatValue, visit: Visit): Result
			protected abstract fun onFragmentDefinition(definition: GFragmentDefinition, visit: Visit): Result
			protected abstract fun onFragmentSelection(selection: GFragmentSelection, visit: Visit): Result
			protected abstract fun onInlineFragmentSelection(selection: GInlineFragmentSelection, visit: Visit): Result
			protected abstract fun onInputObjectType(type: GInputObjectType, visit: Visit): Result
			protected abstract fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, visit: Visit): Result
			protected abstract fun onIntValue(value: GIntValue, visit: Visit): Result
			protected abstract fun onInterfaceType(type: GInterfaceType, visit: Visit): Result
			protected abstract fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, visit: Visit): Result
			protected abstract fun onListTypeRef(ref: GListTypeRef, visit: Visit): Result
			protected abstract fun onListValue(value: GListValue, visit: Visit): Result
			protected abstract fun onName(name: GName, visit: Visit): Result
			protected abstract fun onNamedTypeRef(ref: GNamedTypeRef, visit: Visit): Result
			protected abstract fun onNonNullTypeRef(ref: GNonNullTypeRef, visit: Visit): Result
			protected abstract fun onNullValue(value: GNullValue, visit: Visit): Result
			protected abstract fun onObjectType(type: GObjectType, visit: Visit): Result
			protected abstract fun onObjectTypeExtension(extension: GObjectTypeExtension, visit: Visit): Result
			protected abstract fun onObjectValue(value: GObjectValue, visit: Visit): Result
			protected abstract fun onObjectValueField(field: GObjectValueField, visit: Visit): Result
			protected abstract fun onOperationDefinition(definition: GOperationDefinition, visit: Visit): Result
			protected abstract fun onOperationTypeDefinition(definition: GOperationTypeDefinition, visit: Visit): Result
			protected abstract fun onScalarType(type: GScalarType, visit: Visit): Result
			protected abstract fun onScalarTypeExtension(extension: GScalarTypeExtension, visit: Visit): Result
			protected abstract fun onSchemaDefinition(definition: GSchemaDefinition, visit: Visit): Result
			protected abstract fun onSchemaExtensionDefinition(definition: GSchemaExtension, visit: Visit): Result
			protected abstract fun onSelectionSet(set: GSelectionSet, visit: Visit): Result
			protected abstract fun onStringValue(value: GStringValue, visit: Visit): Result
			protected abstract fun onSyntheticNode(node: GAst, visit: Visit): Result
			protected abstract fun onUnionType(type: GUnionType, visit: Visit): Result
			protected abstract fun onUnionTypeExtension(extension: GUnionTypeExtension, visit: Visit): Result
			protected abstract fun onVariableDefinition(definition: GVariableDefinition, visit: Visit): Result
			protected abstract fun onVariableRef(ref: GVariableRef, visit: Visit): Result

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgument(argument: GArgument, data: Nothing?, visit: Visit) = onArgument(argument, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgumentDefinition(definition: GArgumentDefinition, data: Nothing?, visit: Visit) = onArgumentDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onBooleanValue(value: GBooleanValue, data: Nothing?, visit: Visit) = onBooleanValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirective(directive: GDirective, data: Nothing?, visit: Visit) = onDirective(directive, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?, visit: Visit) = onDirectiveDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDocument(document: GDocument, data: Nothing?, visit: Visit) = onDocument(document, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumType(type: GEnumType, data: Nothing?, visit: Visit) = onEnumType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?, visit: Visit) = onEnumTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValue(value: GEnumValue, data: Nothing?, visit: Visit) = onEnumValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?, visit: Visit) = onEnumValueDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldDefinition(definition: GFieldDefinition, data: Nothing?, visit: Visit) = onFieldDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldSelection(selection: GFieldSelection, data: Nothing?, visit: Visit) = onFieldSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFloatValue(value: GFloatValue, data: Nothing?, visit: Visit) = onFloatValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentDefinition(definition: GFragmentDefinition, data: Nothing?, visit: Visit) = onFragmentDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentSelection(selection: GFragmentSelection, data: Nothing?, visit: Visit) = onFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?, visit: Visit) = onInlineFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectType(type: GInputObjectType, data: Nothing?, visit: Visit) = onInputObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?, visit: Visit) = onInputObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onIntValue(value: GIntValue, data: Nothing?, visit: Visit) = onIntValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceType(type: GInterfaceType, data: Nothing?, visit: Visit) = onInterfaceType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?, visit: Visit) = onInterfaceTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListTypeRef(ref: GListTypeRef, data: Nothing?, visit: Visit) = onListTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListValue(value: GListValue, data: Nothing?, visit: Visit) = onListValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onName(name: GName, data: Nothing?, visit: Visit) = onName(name, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNamedTypeRef(ref: GNamedTypeRef, data: Nothing?, visit: Visit) = onNamedTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?, visit: Visit) = onNonNullTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNullValue(value: GNullValue, data: Nothing?, visit: Visit) = onNullValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectType(type: GObjectType, data: Nothing?, visit: Visit) = onObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?, visit: Visit) = onObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectValue(value: GObjectValue, data: Nothing?, visit: Visit) = onObjectValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectValueField(field: GObjectValueField, data: Nothing?, visit: Visit) = onObjectValueField(field, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationDefinition(definition: GOperationDefinition, data: Nothing?, visit: Visit) = onOperationDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?, visit: Visit) = onOperationTypeDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarType(type: GScalarType, data: Nothing?, visit: Visit) = onScalarType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?, visit: Visit) = onScalarTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaDefinition(definition: GSchemaDefinition, data: Nothing?, visit: Visit) = onSchemaDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Nothing?, visit: Visit) = onSchemaExtensionDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSelectionSet(set: GSelectionSet, data: Nothing?, visit: Visit) = onSelectionSet(set, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onStringValue(value: GStringValue, data: Nothing?, visit: Visit) = onStringValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSyntheticNode(node: GAst, data: Nothing?, visit: Visit) = onSyntheticNode(node, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionType(type: GUnionType, data: Nothing?, visit: Visit) = onUnionType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?, visit: Visit) = onUnionTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableDefinition(definition: GVariableDefinition, data: Nothing?, visit: Visit) = onVariableDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableRef(ref: GVariableRef, data: Nothing?, visit: Visit) = onVariableRef(ref, visit)
		}
	}


	abstract class WithoutData<out Result> : Visitor<Result, Nothing?>() {

		abstract fun onNode(node: GAst, visit: Visit): Result


		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun onNode(node: GAst, data: Nothing?, visit: Visit) =
			onNode(node, visit)
	}


	fun <Data> Visit.visitChildren(data: Data) =
		__unsafeVisitChildren(data)
}


internal fun <Result> GAst.accept(visitor: Visitor<Result, Nothing?>) =
	accept(visitor = visitor, data = null)


internal fun <Result> GAst.accept(visitCoordinator: VisitCoordinator<Result, Nothing?>) =
	accept(visitCoordinator = visitCoordinator, data = null)


internal fun <Result, Data> GAst.accept(
	visitor: Visitor<Result, Data>,
	data: Data
) =
	accept(visitCoordinator = VisitCoordinator.default(visitor), data = data)


internal fun <Result, Data> GAst.accept(visitCoordinator: VisitCoordinator<Result, Data>, data: Data) =
	visitCoordinator.visit(node = this, data = data)
