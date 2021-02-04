package io.fluidsonic.graphql


@InternalGraphqlApi
public abstract class Visitor<out Result, in Data> {

	public abstract fun onNode(node: GNode, data: Data, visit: Visit): Result


	public companion object {

		// FIXME rename
		public fun <Result> ofResult(result: Result): Visitor<Result, Any?> = object : Visitor<Result, Any?>() {

			override fun onNode(node: GNode, data: Any?, visit: Visit) =
				result
		}
	}


	public abstract class Hierarchical<out Result, in Data> : Typed<Result, Data>() {

		protected abstract fun onAny(node: GNode, data: Data, visit: Visit): Result

		protected open fun onAbstractType(type: GAbstractType, data: Data, visit: Visit): Result = onCompositeType(type, data, visit)
		protected open fun onCompositeType(type: GCompositeType, data: Data, visit: Visit): Result = onNamedType(type, data, visit)
		protected open fun onDefinition(definition: GDefinition, data: Data, visit: Visit): Result = onAny(definition, data, visit)
		protected open fun onExecutableDefinition(definition: GExecutableDefinition, data: Data, visit: Visit): Result = onDefinition(definition, data, visit)
		protected open fun onLeafType(type: GLeafType, data: Data, visit: Visit): Result = onNamedType(type, data, visit)
		protected open fun onNamedType(type: GNamedType, data: Data, visit: Visit): Result = onType(type, data, visit)
		protected open fun onSelection(selection: GSelection, data: Data, visit: Visit): Result = onAny(selection, data, visit)
		protected open fun onType(type: GType, data: Data, visit: Visit): Result = onTypeSystemDefinition(type, data, visit)
		protected open fun onTypeExtension(extension: GTypeExtension, data: Data, visit: Visit): Result = onTypeSystemExtension(extension, data, visit)
		protected open fun onTypeRef(ref: GTypeRef, data: Data, visit: Visit): Result = onAny(ref, data, visit)
		protected open fun onTypeSystemDefinition(definition: GTypeSystemDefinition, data: Data, visit: Visit): Result = onDefinition(definition, data, visit)
		protected open fun onTypeSystemExtension(extension: GTypeSystemExtension, data: Data, visit: Visit): Result = onDefinition(extension, data, visit)
		protected open fun onValue(value: GValue, data: Data, visit: Visit): Result = onAny(value, data, visit)

		override fun onArgument(argument: GArgument, data: Data, visit: Visit): Result = onAny(argument, data, visit)
		override fun onArgumentDefinition(definition: GArgumentDefinition, data: Data, visit: Visit): Result = onAny(definition, data, visit)
		override fun onBooleanValue(value: GBooleanValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onDirective(directive: GDirective, data: Data, visit: Visit): Result = onAny(directive, data, visit)
		override fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Data, visit: Visit): Result = onTypeSystemDefinition(definition, data, visit)
		override fun onDocument(document: GDocument, data: Data, visit: Visit): Result = onAny(document, data, visit)
		override fun onEnumType(type: GEnumType, data: Data, visit: Visit): Result = onLeafType(type, data, visit)
		override fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Data, visit: Visit): Result = onTypeExtension(extension, data, visit)
		override fun onEnumValue(value: GEnumValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Data, visit: Visit): Result = onAny(definition, data, visit)
		override fun onFieldDefinition(definition: GFieldDefinition, data: Data, visit: Visit): Result = onAny(definition, data, visit)
		override fun onFieldSelection(selection: GFieldSelection, data: Data, visit: Visit): Result = onSelection(selection, data, visit)
		override fun onFloatValue(value: GFloatValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onFragmentDefinition(definition: GFragmentDefinition, data: Data, visit: Visit): Result = onExecutableDefinition(definition, data, visit)
		override fun onFragmentSelection(selection: GFragmentSelection, data: Data, visit: Visit): Result = onSelection(selection, data, visit)
		override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data, visit: Visit): Result = onSelection(selection, data, visit)
		override fun onInputObjectType(type: GInputObjectType, data: Data, visit: Visit): Result = onCompositeType(type, data, visit)
		override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data, visit: Visit): Result = onTypeExtension(extension, data, visit)
		override fun onIntValue(value: GIntValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onInterfaceType(type: GInterfaceType, data: Data, visit: Visit): Result = onAbstractType(type, data, visit)
		override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data, visit: Visit): Result = onTypeExtension(extension, data, visit)
		override fun onListTypeRef(ref: GListTypeRef, data: Data, visit: Visit): Result = onTypeRef(ref, data, visit)
		override fun onListValue(value: GListValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onName(name: GName, data: Data, visit: Visit): Result = onAny(name, data, visit)
		override fun onNamedTypeRef(ref: GNamedTypeRef, data: Data, visit: Visit): Result = onTypeRef(ref, data, visit)
		override fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Data, visit: Visit): Result = onTypeRef(ref, data, visit)
		override fun onNullValue(value: GNullValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onObjectType(type: GObjectType, data: Data, visit: Visit): Result = onCompositeType(type, data, visit)
		override fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Data, visit: Visit): Result = onTypeExtension(extension, data, visit)
		override fun onObjectValue(value: GObjectValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onOperationDefinition(definition: GOperationDefinition, data: Data, visit: Visit): Result = onExecutableDefinition(definition, data, visit)
		override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data, visit: Visit): Result = onAny(definition, data, visit)
		override fun onScalarType(type: GScalarType, data: Data, visit: Visit): Result = onNamedType(type, data, visit)
		override fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Data, visit: Visit): Result = onTypeExtension(extension, data, visit)
		override fun onSchemaDefinition(definition: GSchemaDefinition, data: Data, visit: Visit): Result = onTypeSystemDefinition(definition, data, visit)
		override fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Data, visit: Visit): Result = onTypeSystemExtension(definition, data, visit)
		override fun onSelectionSet(set: GSelectionSet, data: Data, visit: Visit): Result = onAny(set, data, visit)
		override fun onStringValue(value: GStringValue, data: Data, visit: Visit): Result = onValue(value, data, visit)
		override fun onSyntheticNode(node: GNode, data: Data, visit: Visit): Result = onAny(node, data, visit)
		override fun onUnionType(type: GUnionType, data: Data, visit: Visit): Result = onAbstractType(type, data, visit)
		override fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Data, visit: Visit): Result = onTypeExtension(extension, data, visit)
		override fun onVariableDefinition(definition: GVariableDefinition, data: Data, visit: Visit): Result = onAny(definition, data, visit)
		override fun onVariableRef(ref: GVariableRef, data: Data, visit: Visit): Result = onValue(ref, data, visit)


		public abstract class WithoutData<out Result> : Hierarchical<Result, Nothing?>() {

			protected abstract fun onAny(node: GNode, visit: Visit): Result

			protected open fun onAbstractType(type: GAbstractType, visit: Visit): Result = onCompositeType(type, visit)
			protected open fun onArgument(argument: GArgument, visit: Visit): Result = onAny(argument, visit)
			protected open fun onArgumentDefinition(definition: GArgumentDefinition, visit: Visit): Result = onAny(definition, visit)
			protected open fun onBooleanValue(value: GBooleanValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onCompositeType(type: GCompositeType, visit: Visit): Result = onNamedType(type, visit)
			protected open fun onDefinition(definition: GDefinition, visit: Visit): Result = onAny(definition, visit)
			protected open fun onDirective(directive: GDirective, visit: Visit): Result = onAny(directive, visit)
			protected open fun onDirectiveDefinition(definition: GDirectiveDefinition, visit: Visit): Result = onTypeSystemDefinition(definition, visit)
			protected open fun onDocument(document: GDocument, visit: Visit): Result = onAny(document, visit)
			protected open fun onEnumType(type: GEnumType, visit: Visit): Result = onLeafType(type, visit)
			protected open fun onEnumTypeExtension(extension: GEnumTypeExtension, visit: Visit): Result = onTypeExtension(extension, visit)
			protected open fun onEnumValue(value: GEnumValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onEnumValueDefinition(definition: GEnumValueDefinition, visit: Visit): Result = onAny(definition, visit)
			protected open fun onExecutableDefinition(definition: GExecutableDefinition, visit: Visit): Result = onDefinition(definition, visit)
			protected open fun onFieldDefinition(definition: GFieldDefinition, visit: Visit): Result = onAny(definition, visit)
			protected open fun onFieldSelection(selection: GFieldSelection, visit: Visit): Result = onSelection(selection, visit)
			protected open fun onFloatValue(value: GFloatValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onFragmentDefinition(definition: GFragmentDefinition, visit: Visit): Result = onExecutableDefinition(definition, visit)
			protected open fun onFragmentSelection(selection: GFragmentSelection, visit: Visit): Result = onSelection(selection, visit)
			protected open fun onInlineFragmentSelection(selection: GInlineFragmentSelection, visit: Visit): Result = onSelection(selection, visit)
			protected open fun onInputObjectType(type: GInputObjectType, visit: Visit): Result = onCompositeType(type, visit)
			protected open fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, visit: Visit): Result = onTypeExtension(extension, visit)
			protected open fun onIntValue(value: GIntValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onInterfaceType(type: GInterfaceType, visit: Visit): Result = onAbstractType(type, visit)
			protected open fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, visit: Visit): Result = onTypeExtension(extension, visit)
			protected open fun onLeafType(type: GLeafType, visit: Visit): Result = onNamedType(type, visit)
			protected open fun onListTypeRef(ref: GListTypeRef, visit: Visit): Result = onTypeRef(ref, visit)
			protected open fun onListValue(value: GListValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onName(name: GName, visit: Visit): Result = onAny(name, visit)
			protected open fun onNamedType(type: GNamedType, visit: Visit): Result = onType(type, visit)
			protected open fun onNamedTypeRef(ref: GNamedTypeRef, visit: Visit): Result = onTypeRef(ref, visit)
			protected open fun onNonNullTypeRef(ref: GNonNullTypeRef, visit: Visit): Result = onTypeRef(ref, visit)
			protected open fun onNullValue(value: GNullValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onObjectType(type: GObjectType, visit: Visit): Result = onCompositeType(type, visit)
			protected open fun onObjectTypeExtension(extension: GObjectTypeExtension, visit: Visit): Result = onTypeExtension(extension, visit)
			protected open fun onObjectValue(value: GObjectValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onOperationDefinition(definition: GOperationDefinition, visit: Visit): Result = onExecutableDefinition(definition, visit)
			protected open fun onOperationTypeDefinition(definition: GOperationTypeDefinition, visit: Visit): Result = onAny(definition, visit)
			protected open fun onScalarType(type: GScalarType, visit: Visit): Result = onNamedType(type, visit)
			protected open fun onScalarTypeExtension(extension: GScalarTypeExtension, visit: Visit): Result = onTypeExtension(extension, visit)
			protected open fun onSchemaDefinition(definition: GSchemaDefinition, visit: Visit): Result = onTypeSystemDefinition(definition, visit)
			protected open fun onSchemaExtensionDefinition(definition: GSchemaExtension, visit: Visit): Result = onTypeSystemExtension(definition, visit)
			protected open fun onSelection(selection: GSelection, visit: Visit): Result = onAny(selection, visit)
			protected open fun onSelectionSet(set: GSelectionSet, visit: Visit): Result = onAny(set, visit)
			protected open fun onStringValue(value: GStringValue, visit: Visit): Result = onValue(value, visit)
			protected open fun onSyntheticNode(node: GNode, visit: Visit): Result = onAny(node, visit)
			protected open fun onType(type: GType, visit: Visit): Result = onTypeSystemDefinition(type, visit)
			protected open fun onTypeExtension(extension: GTypeExtension, visit: Visit): Result = onTypeSystemExtension(extension, visit)
			protected open fun onTypeRef(ref: GTypeRef, visit: Visit): Result = onAny(ref, visit)
			protected open fun onTypeSystemDefinition(definition: GTypeSystemDefinition, visit: Visit): Result = onDefinition(definition, visit)
			protected open fun onTypeSystemExtension(extension: GTypeSystemExtension, visit: Visit): Result = onDefinition(extension, visit)
			protected open fun onUnionType(type: GUnionType, visit: Visit): Result = onAbstractType(type, visit)
			protected open fun onUnionTypeExtension(extension: GUnionTypeExtension, visit: Visit): Result = onTypeExtension(extension, visit)
			protected open fun onValue(value: GValue, visit: Visit): Result = onAny(value, visit)
			protected open fun onVariableDefinition(definition: GVariableDefinition, visit: Visit): Result = onAny(definition, visit)
			protected open fun onVariableRef(ref: GVariableRef, visit: Visit): Result = onValue(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onAbstractType(type: GAbstractType, data: Nothing?, visit: Visit): Result = onCompositeType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onAny(node: GNode, data: Nothing?, visit: Visit): Result = onAny(node, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgument(argument: GArgument, data: Nothing?, visit: Visit): Result = onArgument(argument, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgumentDefinition(definition: GArgumentDefinition, data: Nothing?, visit: Visit): Result = onAny(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onBooleanValue(value: GBooleanValue, data: Nothing?, visit: Visit): Result = onBooleanValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onCompositeType(type: GCompositeType, data: Nothing?, visit: Visit): Result = onNamedType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDefinition(definition: GDefinition, data: Nothing?, visit: Visit): Result = onAny(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirective(directive: GDirective, data: Nothing?, visit: Visit): Result = onDirective(directive, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?, visit: Visit): Result = onDirectiveDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDocument(document: GDocument, data: Nothing?, visit: Visit): Result = onDocument(document, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumType(type: GEnumType, data: Nothing?, visit: Visit): Result = onEnumType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?, visit: Visit): Result = onEnumTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValue(value: GEnumValue, data: Nothing?, visit: Visit): Result = onEnumValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?, visit: Visit): Result = onEnumValueDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onExecutableDefinition(definition: GExecutableDefinition, data: Nothing?, visit: Visit): Result = onDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldDefinition(definition: GFieldDefinition, data: Nothing?, visit: Visit): Result = onFieldDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldSelection(selection: GFieldSelection, data: Nothing?, visit: Visit): Result = onFieldSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFloatValue(value: GFloatValue, data: Nothing?, visit: Visit): Result = onFloatValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentDefinition(definition: GFragmentDefinition, data: Nothing?, visit: Visit): Result = onFragmentDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentSelection(selection: GFragmentSelection, data: Nothing?, visit: Visit): Result = onFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?, visit: Visit): Result = onInlineFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectType(type: GInputObjectType, data: Nothing?, visit: Visit): Result = onInputObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?, visit: Visit): Result = onInputObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onIntValue(value: GIntValue, data: Nothing?, visit: Visit): Result = onIntValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceType(type: GInterfaceType, data: Nothing?, visit: Visit): Result = onInterfaceType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?, visit: Visit): Result = onInterfaceTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onLeafType(type: GLeafType, data: Nothing?, visit: Visit): Result = onNamedType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListTypeRef(ref: GListTypeRef, data: Nothing?, visit: Visit): Result = onListTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListValue(value: GListValue, data: Nothing?, visit: Visit): Result = onListValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onName(name: GName, data: Nothing?, visit: Visit): Result = onName(name, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNamedType(type: GNamedType, data: Nothing?, visit: Visit): Result = onType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNamedTypeRef(ref: GNamedTypeRef, data: Nothing?, visit: Visit): Result = onNamedTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?, visit: Visit): Result = onNonNullTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNullValue(value: GNullValue, data: Nothing?, visit: Visit): Result = onNullValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectType(type: GObjectType, data: Nothing?, visit: Visit): Result = onObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?, visit: Visit): Result = onObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectValue(value: GObjectValue, data: Nothing?, visit: Visit): Result = onObjectValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationDefinition(definition: GOperationDefinition, data: Nothing?, visit: Visit): Result = onOperationDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?, visit: Visit): Result = onOperationTypeDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarType(type: GScalarType, data: Nothing?, visit: Visit): Result = onNamedType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?, visit: Visit): Result = onScalarTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaDefinition(definition: GSchemaDefinition, data: Nothing?, visit: Visit): Result = onSchemaDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Nothing?, visit: Visit): Result = onSchemaExtensionDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSelection(selection: GSelection, data: Nothing?, visit: Visit): Result = onAny(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSelectionSet(set: GSelectionSet, data: Nothing?, visit: Visit): Result = onSelectionSet(set, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onStringValue(value: GStringValue, data: Nothing?, visit: Visit): Result = onStringValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSyntheticNode(node: GNode, data: Nothing?, visit: Visit): Result = onSyntheticNode(node, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onType(type: GType, data: Nothing?, visit: Visit): Result = onTypeSystemDefinition(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeExtension(extension: GTypeExtension, data: Nothing?, visit: Visit): Result = onTypeSystemExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeRef(ref: GTypeRef, data: Nothing?, visit: Visit): Result = onAny(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeSystemDefinition(definition: GTypeSystemDefinition, data: Nothing?, visit: Visit): Result = onDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onTypeSystemExtension(extension: GTypeSystemExtension, data: Nothing?, visit: Visit): Result = onDefinition(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionType(type: GUnionType, data: Nothing?, visit: Visit): Result = onUnionType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?, visit: Visit): Result = onUnionTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onValue(value: GValue, data: Nothing?, visit: Visit): Result = onAny(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableDefinition(definition: GVariableDefinition, data: Nothing?, visit: Visit): Result = onVariableDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableRef(ref: GVariableRef, data: Nothing?, visit: Visit): Result = onVariableRef(ref, visit)
		}
	}


	public abstract class Typed<out Result, in Data> : Visitor<Result, Data>() {

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun onNode(node: GNode, data: Data, visit: Visit): Result =
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
		protected abstract fun onOperationDefinition(definition: GOperationDefinition, data: Data, visit: Visit): Result
		protected abstract fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data, visit: Visit): Result
		protected abstract fun onScalarType(type: GScalarType, data: Data, visit: Visit): Result
		protected abstract fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onSchemaDefinition(definition: GSchemaDefinition, data: Data, visit: Visit): Result
		protected abstract fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Data, visit: Visit): Result
		protected abstract fun onSelectionSet(set: GSelectionSet, data: Data, visit: Visit): Result
		protected abstract fun onStringValue(value: GStringValue, data: Data, visit: Visit): Result
		protected abstract fun onSyntheticNode(node: GNode, data: Data, visit: Visit): Result
		protected abstract fun onUnionType(type: GUnionType, data: Data, visit: Visit): Result
		protected abstract fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Data, visit: Visit): Result
		protected abstract fun onVariableDefinition(definition: GVariableDefinition, data: Data, visit: Visit): Result
		protected abstract fun onVariableRef(ref: GVariableRef, data: Data, visit: Visit): Result


		public abstract class WithoutData<out Result> : Typed<Result, Nothing?>() {

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
			protected abstract fun onOperationDefinition(definition: GOperationDefinition, visit: Visit): Result
			protected abstract fun onOperationTypeDefinition(definition: GOperationTypeDefinition, visit: Visit): Result
			protected abstract fun onScalarType(type: GScalarType, visit: Visit): Result
			protected abstract fun onScalarTypeExtension(extension: GScalarTypeExtension, visit: Visit): Result
			protected abstract fun onSchemaDefinition(definition: GSchemaDefinition, visit: Visit): Result
			protected abstract fun onSchemaExtensionDefinition(definition: GSchemaExtension, visit: Visit): Result
			protected abstract fun onSelectionSet(set: GSelectionSet, visit: Visit): Result
			protected abstract fun onStringValue(value: GStringValue, visit: Visit): Result
			protected abstract fun onSyntheticNode(node: GNode, visit: Visit): Result
			protected abstract fun onUnionType(type: GUnionType, visit: Visit): Result
			protected abstract fun onUnionTypeExtension(extension: GUnionTypeExtension, visit: Visit): Result
			protected abstract fun onVariableDefinition(definition: GVariableDefinition, visit: Visit): Result
			protected abstract fun onVariableRef(ref: GVariableRef, visit: Visit): Result

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgument(argument: GArgument, data: Nothing?, visit: Visit): Result = onArgument(argument, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onArgumentDefinition(definition: GArgumentDefinition, data: Nothing?, visit: Visit): Result = onArgumentDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onBooleanValue(value: GBooleanValue, data: Nothing?, visit: Visit): Result = onBooleanValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirective(directive: GDirective, data: Nothing?, visit: Visit): Result = onDirective(directive, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?, visit: Visit): Result = onDirectiveDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onDocument(document: GDocument, data: Nothing?, visit: Visit): Result = onDocument(document, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumType(type: GEnumType, data: Nothing?, visit: Visit): Result = onEnumType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?, visit: Visit): Result = onEnumTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValue(value: GEnumValue, data: Nothing?, visit: Visit): Result = onEnumValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?, visit: Visit): Result = onEnumValueDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldDefinition(definition: GFieldDefinition, data: Nothing?, visit: Visit): Result = onFieldDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFieldSelection(selection: GFieldSelection, data: Nothing?, visit: Visit): Result = onFieldSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFloatValue(value: GFloatValue, data: Nothing?, visit: Visit): Result = onFloatValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentDefinition(definition: GFragmentDefinition, data: Nothing?, visit: Visit): Result = onFragmentDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onFragmentSelection(selection: GFragmentSelection, data: Nothing?, visit: Visit): Result = onFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?, visit: Visit): Result = onInlineFragmentSelection(selection, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectType(type: GInputObjectType, data: Nothing?, visit: Visit): Result = onInputObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?, visit: Visit): Result = onInputObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onIntValue(value: GIntValue, data: Nothing?, visit: Visit): Result = onIntValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceType(type: GInterfaceType, data: Nothing?, visit: Visit): Result = onInterfaceType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?, visit: Visit): Result = onInterfaceTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListTypeRef(ref: GListTypeRef, data: Nothing?, visit: Visit): Result = onListTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onListValue(value: GListValue, data: Nothing?, visit: Visit): Result = onListValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onName(name: GName, data: Nothing?, visit: Visit): Result = onName(name, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNamedTypeRef(ref: GNamedTypeRef, data: Nothing?, visit: Visit): Result = onNamedTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?, visit: Visit): Result = onNonNullTypeRef(ref, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onNullValue(value: GNullValue, data: Nothing?, visit: Visit): Result = onNullValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectType(type: GObjectType, data: Nothing?, visit: Visit): Result = onObjectType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?, visit: Visit): Result = onObjectTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onObjectValue(value: GObjectValue, data: Nothing?, visit: Visit): Result = onObjectValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationDefinition(definition: GOperationDefinition, data: Nothing?, visit: Visit): Result = onOperationDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?, visit: Visit): Result = onOperationTypeDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarType(type: GScalarType, data: Nothing?, visit: Visit): Result = onScalarType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?, visit: Visit): Result = onScalarTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaDefinition(definition: GSchemaDefinition, data: Nothing?, visit: Visit): Result = onSchemaDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSchemaExtensionDefinition(definition: GSchemaExtension, data: Nothing?, visit: Visit): Result = onSchemaExtensionDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSelectionSet(set: GSelectionSet, data: Nothing?, visit: Visit): Result = onSelectionSet(set, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onStringValue(value: GStringValue, data: Nothing?, visit: Visit): Result = onStringValue(value, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onSyntheticNode(node: GNode, data: Nothing?, visit: Visit): Result = onSyntheticNode(node, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionType(type: GUnionType, data: Nothing?, visit: Visit): Result = onUnionType(type, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?, visit: Visit): Result = onUnionTypeExtension(extension, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableDefinition(definition: GVariableDefinition, data: Nothing?, visit: Visit): Result = onVariableDefinition(definition, visit)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun onVariableRef(ref: GVariableRef, data: Nothing?, visit: Visit): Result = onVariableRef(ref, visit)
		}
	}


	public abstract class WithoutData<out Result> : Visitor<Result, Nothing?>() {

		public abstract fun onNode(node: GNode, visit: Visit): Result


		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun onNode(node: GNode, data: Nothing?, visit: Visit): Result =
			onNode(node, visit)
	}


	public fun <Data> Visit.visitChildren(data: Data): Unit =
		__unsafeVisitChildren(data)
}


@InternalGraphqlApi
public fun <Result> GNode.accept(visitor: Visitor<Result, Nothing?>): Result =
	accept(visitor = visitor, data = null)


@InternalGraphqlApi
public fun <Result> GNode.accept(visitCoordinator: VisitCoordinator<Result, Nothing?>): Result =
	accept(visitCoordinator = visitCoordinator, data = null)


@InternalGraphqlApi
public fun <Result, Data> GNode.accept(
	visitor: Visitor<Result, Data>,
	data: Data
): Result =
	accept(visitCoordinator = VisitCoordinator.default(visitor), data = data)


@InternalGraphqlApi
public fun <Result, Data> GNode.accept(visitCoordinator: VisitCoordinator<Result, Data>, data: Data): Result =
	visitCoordinator.visit(node = this, data = data)
