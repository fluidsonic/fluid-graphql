package io.fluidsonic.graphql

import kotlin.jvm.*


internal abstract class Visitor<out Result, Data>(
	automaticallyVisitsChildren: Boolean = true
) {

	private var coordination: VisitCoordination<Data>? = null


	protected fun abort() {
		coordination?.abort()
			?: error(".abort() can only be called from within a visited function.")
	}


	private var _automaticallyVisitsChildren = automaticallyVisitsChildren
	protected var automaticallyVisitsChildren
		get() = coordination?.automaticallyVisitsChildren ?: _automaticallyVisitsChildren
		set(value) {
			_automaticallyVisitsChildren = value
			coordination?.automaticallyVisitsChildren = value
		}


	internal open fun dispatchVisit(node: GAst, data: Data, coordination: VisitCoordination<Data>): Result {
		val isInitialDispatch: Boolean

		when {
			this.coordination === null -> {
				isInitialDispatch = true

				coordination.automaticallyVisitsChildren = _automaticallyVisitsChildren
				this.coordination = coordination
			}

			coordination !== this.coordination ->
				error(
					"A visitor cannot be used multiple times concurrently.\n\n" +
						"`dispatcher.dispatchVisit()` was called while visiting with a different coordination:\n\n" +
						"Current: ${this.coordination}\n\n" +
						"New: $coordination"
				)

			else ->
				isInitialDispatch = false
		}

		return try {
			when (node) {
				is GArgument -> visitArgument(node, data)
				is GBooleanType -> visitBooleanType(node, data)
				is GBooleanValue -> visitBooleanValue(node, data)
				is GCustomScalarType -> visitCustomScalarType(node, data)
				is GDirective -> visitDirective(node, data)
				is GDirectiveArgumentDefinition -> visitDirectiveArgumentDefinition(node, data)
				is GDirectiveDefinition -> visitDirectiveDefinition(node, data)
				is GDocument -> visitDocument(node, data)
				is GEnumType -> visitEnumType(node, data)
				is GEnumTypeExtension -> visitEnumTypeExtension(node, data)
				is GEnumValue -> visitEnumValue(node, data)
				is GEnumValueDefinition -> visitEnumValueDefinition(node, data)
				is GFieldArgumentDefinition -> visitFieldArgumentDefinition(node, data)
				is GFieldDefinition -> visitFieldDefinition(node, data)
				is GFieldSelection -> visitFieldSelection(node, data)
				is GFloatType -> visitFloatType(node, data)
				is GFloatValue -> visitFloatValue(node, data)
				is GFragmentDefinition -> visitFragmentDefinition(node, data)
				is GFragmentSelection -> visitFragmentSelection(node, data)
				is GIdType -> visitIdType(node, data)
				is GInlineFragmentSelection -> visitInlineFragmentSelection(node, data)
				is GInputObjectArgumentDefinition -> visitInputObjectArgumentDefinition(node, data)
				is GInputObjectType -> visitInputObjectType(node, data)
				is GInputObjectTypeExtension -> visitInputObjectTypeExtension(node, data)
				is GIntType -> visitIntType(node, data)
				is GIntValue -> visitIntValue(node, data)
				is GInterfaceType -> visitInterfaceType(node, data)
				is GInterfaceTypeExtension -> visitInterfaceTypeExtension(node, data)
				is GListType -> visitSyntheticNode(node, data)
				is GListTypeRef -> visitListTypeRef(node, data)
				is GListValue -> visitListValue(node, data)
				is GName -> visitName(node, data)
				is GNamedTypeRef -> visitNamedTypeRef(node, data)
				is GNonNullType -> visitSyntheticNode(node, data)
				is GNonNullTypeRef -> visitNonNullTypeRef(node, data)
				is GNullValue -> visitNullValue(node, data)
				is GObjectType -> visitObjectType(node, data)
				is GObjectTypeExtension -> visitObjectTypeExtension(node, data)
				is GObjectValue -> visitObjectValue(node, data)
				is GObjectValueField -> visitObjectValueField(node, data)
				is GOperationDefinition -> visitOperationDefinition(node, data)
				is GOperationTypeDefinition -> visitOperationTypeDefinition(node, data)
				is GScalarTypeExtension -> visitScalarTypeExtension(node, data)
				is GSchemaDefinition -> visitSchemaDefinition(node, data)
				is GSchemaExtension -> visitSchemaExtensionDefinition(node, data)
				is GSelectionSet -> visitSelectionSet(node, data)
				is GStringType -> visitStringType(node, data)
				is GStringValue -> visitStringValue(node, data)
				is GUnionType -> visitUnionType(node, data)
				is GUnionTypeExtension -> visitUnionTypeExtension(node, data)
				is GVariableDefinition -> visitVariableDefinition(node, data)
				is GVariableRef -> visitVariableRef(node, data)
			}
		}
		finally {
			if (isInitialDispatch)
				this.coordination = null
		}
	}


	protected val isAborting
		get() = coordination?.isAborting ?: error(".isAborting can only be called from within a visited function.")


	protected val isSkippingChildren
		get() = coordination?.isSkippingChildren ?: error(".isSkippingChildren can only be called from within a visited function.")


	protected fun skipChildren() {
		coordination?.skipChildren()
			?: error(".skipChildren() can only be called from within a visited function.")
	}


	protected fun visitChildren() {
		coordination?.visitChildren()
			?: error(".visitChildren() can only be called from within a visited function.")
	}


	protected fun visitChildren(data: Data) {
		coordination?.visitChildren(data = data)
			?: error(".visitChildren() can only be called from within a visited function.")
	}


	protected abstract fun visitArgument(argument: GArgument, data: Data): Result
	protected abstract fun visitBooleanType(type: GBooleanType, data: Data): Result
	protected abstract fun visitBooleanValue(value: GBooleanValue, data: Data): Result
	protected abstract fun visitCustomScalarType(type: GCustomScalarType, data: Data): Result
	protected abstract fun visitDirective(directive: GDirective, data: Data): Result
	protected abstract fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition, data: Data): Result
	protected abstract fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data): Result
	protected abstract fun visitDocument(document: GDocument, data: Data): Result
	protected abstract fun visitEnumType(type: GEnumType, data: Data): Result
	protected abstract fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data): Result
	protected abstract fun visitEnumValue(value: GEnumValue, data: Data): Result
	protected abstract fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data): Result
	protected abstract fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition, data: Data): Result
	protected abstract fun visitFieldDefinition(definition: GFieldDefinition, data: Data): Result
	protected abstract fun visitFieldSelection(selection: GFieldSelection, data: Data): Result
	protected abstract fun visitFloatType(type: GFloatType, data: Data): Result
	protected abstract fun visitFloatValue(value: GFloatValue, data: Data): Result
	protected abstract fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data): Result
	protected abstract fun visitFragmentSelection(selection: GFragmentSelection, data: Data): Result
	protected abstract fun visitIdType(type: GIdType, data: Data): Result
	protected abstract fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data): Result
	protected abstract fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition, data: Data): Result
	protected abstract fun visitInputObjectType(type: GInputObjectType, data: Data): Result
	protected abstract fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data): Result
	protected abstract fun visitIntType(type: GIntType, data: Data): Result
	protected abstract fun visitIntValue(value: GIntValue, data: Data): Result
	protected abstract fun visitInterfaceType(type: GInterfaceType, data: Data): Result
	protected abstract fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data): Result
	protected abstract fun visitListTypeRef(ref: GListTypeRef, data: Data): Result
	protected abstract fun visitListValue(value: GListValue, data: Data): Result
	protected abstract fun visitName(name: GName, data: Data): Result
	protected abstract fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data): Result
	protected abstract fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data): Result
	protected abstract fun visitNullValue(value: GNullValue, data: Data): Result
	protected abstract fun visitObjectType(type: GObjectType, data: Data): Result
	protected abstract fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data): Result
	protected abstract fun visitObjectValue(value: GObjectValue, data: Data): Result
	protected abstract fun visitObjectValueField(field: GObjectValueField, data: Data): Result
	protected abstract fun visitOperationDefinition(definition: GOperationDefinition, data: Data): Result
	protected abstract fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data): Result
	protected abstract fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data): Result
	protected abstract fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data): Result
	protected abstract fun visitSchemaExtensionDefinition(definition: GSchemaExtension, data: Data): Result
	protected abstract fun visitSelectionSet(set: GSelectionSet, data: Data): Result
	protected abstract fun visitStringType(type: GStringType, data: Data): Result
	protected abstract fun visitStringValue(value: GStringValue, data: Data): Result
	protected abstract fun visitSyntheticNode(node: GAst, data: Data): Result
	protected abstract fun visitUnionType(type: GUnionType, data: Data): Result
	protected abstract fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data): Result
	protected abstract fun visitVariableDefinition(definition: GVariableDefinition, data: Data): Result
	protected abstract fun visitVariableRef(ref: GVariableRef, data: Data): Result


	companion object {

		@Suppress("UNCHECKED_CAST")
		fun <Data> identity(): Visitor<Data, Data> =
			Identity()
	}


	abstract class Hierarchical<out Result, Data> internal constructor(
		descendsAutomatically: Boolean = true
	) : Visitor<Result, Data>(
		automaticallyVisitsChildren = descendsAutomatically
	) {

		protected abstract fun visitNode(node: GAst, data: Data): Result

		protected open fun visitAbstractType(type: GAbstractType, data: Data) = visitCompositeType(type, data)
		protected open fun visitArgumentDefinition(definition: GArgumentDefinition, data: Data) = visitNode(definition, data)
		protected open fun visitCompositeType(type: GCompositeType, data: Data) = visitNamedType(type, data)
		protected open fun visitDefinition(definition: GDefinition, data: Data) = visitNode(definition, data)
		protected open fun visitExecutableDefinition(definition: GExecutableDefinition, data: Data) = visitDefinition(definition, data)
		protected open fun visitLeafType(type: GLeafType, data: Data) = visitNamedType(type, data)
		protected open fun visitNamedType(type: GNamedType, data: Data) = visitType(type, data)
		protected open fun visitScalarType(type: GScalarType, data: Data) = visitNamedType(type, data)
		protected open fun visitSelection(selection: GSelection, data: Data) = visitNode(selection, data)
		protected open fun visitType(type: GType, data: Data) = visitTypeSystemDefinition(type, data)
		protected open fun visitTypeExtension(extension: GTypeExtension, data: Data) = visitTypeSystemExtension(extension, data)
		protected open fun visitTypeRef(ref: GTypeRef, data: Data) = visitNode(ref, data)
		protected open fun visitTypeSystemDefinition(definition: GTypeSystemDefinition, data: Data) = visitDefinition(definition, data)
		protected open fun visitTypeSystemExtension(extension: GTypeSystemExtension, data: Data) = visitDefinition(extension, data)
		protected open fun visitValue(value: GValue, data: Data) = visitNode(value, data)

		override fun visitArgument(argument: GArgument, data: Data) = visitNode(argument, data)
		override fun visitBooleanType(type: GBooleanType, data: Data) = visitScalarType(type, data)
		override fun visitBooleanValue(value: GBooleanValue, data: Data) = visitValue(value, data)
		override fun visitCustomScalarType(type: GCustomScalarType, data: Data) = visitScalarType(type, data)
		override fun visitDirective(directive: GDirective, data: Data) = visitNode(directive, data)
		override fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition, data: Data) = visitArgumentDefinition(definition, data)
		override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Data) = visitTypeSystemDefinition(definition, data)
		override fun visitDocument(document: GDocument, data: Data) = visitNode(document, data)
		override fun visitEnumType(type: GEnumType, data: Data) = visitLeafType(type, data)
		override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Data) = visitTypeExtension(extension, data)
		override fun visitEnumValue(value: GEnumValue, data: Data) = visitValue(value, data)
		override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Data) = visitNode(definition, data)
		override fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition, data: Data) = visitArgumentDefinition(definition, data)
		override fun visitFieldDefinition(definition: GFieldDefinition, data: Data) = visitNode(definition, data)
		override fun visitFieldSelection(selection: GFieldSelection, data: Data) = visitSelection(selection, data)
		override fun visitFloatType(type: GFloatType, data: Data) = visitScalarType(type, data)
		override fun visitFloatValue(value: GFloatValue, data: Data) = visitValue(value, data)
		override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Data) = visitExecutableDefinition(definition, data)
		override fun visitFragmentSelection(selection: GFragmentSelection, data: Data) = visitSelection(selection, data)
		override fun visitIdType(type: GIdType, data: Data) = visitScalarType(type, data)
		override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Data) = visitSelection(selection, data)
		override fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition, data: Data) = visitArgumentDefinition(definition, data)
		override fun visitInputObjectType(type: GInputObjectType, data: Data) = visitCompositeType(type, data)
		override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Data) = visitTypeExtension(extension, data)
		override fun visitIntType(type: GIntType, data: Data) = visitScalarType(type, data)
		override fun visitIntValue(value: GIntValue, data: Data) = visitValue(value, data)
		override fun visitInterfaceType(type: GInterfaceType, data: Data) = visitAbstractType(type, data)
		override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Data) = visitTypeExtension(extension, data)
		override fun visitListTypeRef(ref: GListTypeRef, data: Data) = visitTypeRef(ref, data)
		override fun visitListValue(value: GListValue, data: Data) = visitValue(value, data)
		override fun visitName(name: GName, data: Data) = visitNode(name, data)
		override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Data) = visitTypeRef(ref, data)
		override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Data) = visitTypeRef(ref, data)
		override fun visitNullValue(value: GNullValue, data: Data) = visitValue(value, data)
		override fun visitObjectType(type: GObjectType, data: Data) = visitCompositeType(type, data)
		override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Data) = visitTypeExtension(extension, data)
		override fun visitObjectValue(value: GObjectValue, data: Data) = visitValue(value, data)
		override fun visitObjectValueField(field: GObjectValueField, data: Data) = visitNode(field, data)
		override fun visitOperationDefinition(definition: GOperationDefinition, data: Data) = visitExecutableDefinition(definition, data)
		override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Data) = visitNode(definition, data)
		override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Data) = visitTypeExtension(extension, data)
		override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Data) = visitTypeSystemDefinition(definition, data)
		override fun visitSchemaExtensionDefinition(definition: GSchemaExtension, data: Data) = visitTypeSystemExtension(definition, data)
		override fun visitSelectionSet(set: GSelectionSet, data: Data) = visitNode(set, data)
		override fun visitStringType(type: GStringType, data: Data) = visitScalarType(type, data)
		override fun visitStringValue(value: GStringValue, data: Data) = visitValue(value, data)
		override fun visitSyntheticNode(node: GAst, data: Data) = visitNode(node, data)
		override fun visitUnionType(type: GUnionType, data: Data) = visitAbstractType(type, data)
		override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Data) = visitTypeExtension(extension, data)
		override fun visitVariableDefinition(definition: GVariableDefinition, data: Data) = visitNode(definition, data)
		override fun visitVariableRef(ref: GVariableRef, data: Data) = visitValue(ref, data)


		abstract class WithoutData<out Result>(
			descendsAutomatically: Boolean = true
		) : Hierarchical<Result, Nothing?>(
			descendsAutomatically = descendsAutomatically
		) {

			protected abstract fun visitNode(node: GAst): Result

			protected open fun visitAbstractType(type: GAbstractType) = visitCompositeType(type)
			protected open fun visitArgument(argument: GArgument) = visitNode(argument)
			protected open fun visitArgumentDefinition(definition: GArgumentDefinition) = visitNode(definition)
			protected open fun visitBooleanType(type: GBooleanType) = visitScalarType(type)
			protected open fun visitBooleanValue(value: GBooleanValue) = visitValue(value)
			protected open fun visitCompositeType(type: GCompositeType) = visitNamedType(type)
			protected open fun visitCustomScalarType(type: GCustomScalarType) = visitScalarType(type)
			protected open fun visitDefinition(definition: GDefinition) = visitNode(definition)
			protected open fun visitDirective(directive: GDirective) = visitNode(directive)
			protected open fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition) = visitArgumentDefinition(definition)
			protected open fun visitDirectiveDefinition(definition: GDirectiveDefinition) = visitTypeSystemDefinition(definition)
			protected open fun visitDocument(document: GDocument) = visitNode(document)
			protected open fun visitEnumType(type: GEnumType) = visitLeafType(type)
			protected open fun visitEnumTypeExtension(extension: GEnumTypeExtension) = visitTypeExtension(extension)
			protected open fun visitEnumValue(value: GEnumValue) = visitValue(value)
			protected open fun visitEnumValueDefinition(definition: GEnumValueDefinition) = visitNode(definition)
			protected open fun visitExecutableDefinition(definition: GExecutableDefinition) = visitDefinition(definition)
			protected open fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition) = visitArgumentDefinition(definition)
			protected open fun visitFieldDefinition(definition: GFieldDefinition) = visitNode(definition)
			protected open fun visitFieldSelection(selection: GFieldSelection) = visitSelection(selection)
			protected open fun visitFloatType(type: GFloatType) = visitScalarType(type)
			protected open fun visitFloatValue(value: GFloatValue) = visitValue(value)
			protected open fun visitFragmentDefinition(definition: GFragmentDefinition) = visitExecutableDefinition(definition)
			protected open fun visitFragmentSelection(selection: GFragmentSelection) = visitSelection(selection)
			protected open fun visitIdType(type: GIdType) = visitScalarType(type)
			protected open fun visitInlineFragmentSelection(selection: GInlineFragmentSelection) = visitSelection(selection)
			protected open fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition) = visitArgumentDefinition(definition)
			protected open fun visitInputObjectType(type: GInputObjectType) = visitCompositeType(type)
			protected open fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension) = visitTypeExtension(extension)
			protected open fun visitIntType(type: GIntType) = visitScalarType(type)
			protected open fun visitIntValue(value: GIntValue) = visitValue(value)
			protected open fun visitInterfaceType(type: GInterfaceType) = visitAbstractType(type)
			protected open fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension) = visitTypeExtension(extension)
			protected open fun visitLeafType(type: GLeafType) = visitNamedType(type)
			protected open fun visitListTypeRef(ref: GListTypeRef) = visitTypeRef(ref)
			protected open fun visitListValue(value: GListValue) = visitValue(value)
			protected open fun visitName(name: GName) = visitNode(name)
			protected open fun visitNamedType(type: GNamedType) = visitType(type)
			protected open fun visitNamedTypeRef(ref: GNamedTypeRef) = visitTypeRef(ref)
			protected open fun visitNonNullTypeRef(ref: GNonNullTypeRef) = visitTypeRef(ref)
			protected open fun visitNullValue(value: GNullValue) = visitValue(value)
			protected open fun visitObjectType(type: GObjectType) = visitCompositeType(type)
			protected open fun visitObjectTypeExtension(extension: GObjectTypeExtension) = visitTypeExtension(extension)
			protected open fun visitObjectValue(value: GObjectValue) = visitValue(value)
			protected open fun visitObjectValueField(field: GObjectValueField) = visitNode(field)
			protected open fun visitOperationDefinition(definition: GOperationDefinition) = visitExecutableDefinition(definition)
			protected open fun visitOperationTypeDefinition(definition: GOperationTypeDefinition) = visitNode(definition)
			protected open fun visitScalarType(type: GScalarType) = visitNamedType(type)
			protected open fun visitScalarTypeExtension(extension: GScalarTypeExtension) = visitTypeExtension(extension)
			protected open fun visitSchemaDefinition(definition: GSchemaDefinition) = visitTypeSystemDefinition(definition)
			protected open fun visitSchemaExtensionDefinition(definition: GSchemaExtension) = visitTypeSystemExtension(definition)
			protected open fun visitSelection(selection: GSelection) = visitNode(selection)
			protected open fun visitSelectionSet(set: GSelectionSet) = visitNode(set)
			protected open fun visitStringType(type: GStringType) = visitScalarType(type)
			protected open fun visitStringValue(value: GStringValue) = visitValue(value)
			protected open fun visitSyntheticNode(node: GAst) = visitNode(node)
			protected open fun visitType(type: GType) = visitTypeSystemDefinition(type)
			protected open fun visitTypeExtension(extension: GTypeExtension) = visitTypeSystemExtension(extension)
			protected open fun visitTypeRef(ref: GTypeRef) = visitNode(ref)
			protected open fun visitTypeSystemDefinition(definition: GTypeSystemDefinition) = visitDefinition(definition)
			protected open fun visitTypeSystemExtension(extension: GTypeSystemExtension) = visitDefinition(extension)
			protected open fun visitUnionType(type: GUnionType) = visitAbstractType(type)
			protected open fun visitUnionTypeExtension(extension: GUnionTypeExtension) = visitTypeExtension(extension)
			protected open fun visitValue(value: GValue) = visitNode(value)
			protected open fun visitVariableDefinition(definition: GVariableDefinition) = visitNode(definition)
			protected open fun visitVariableRef(ref: GVariableRef) = visitValue(ref)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitAbstractType(type: GAbstractType, data: Nothing?) = visitCompositeType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitArgument(argument: GArgument, data: Nothing?) = visitArgument(argument)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitArgumentDefinition(definition: GArgumentDefinition, data: Nothing?) = visitNode(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitBooleanType(type: GBooleanType, data: Nothing?) = visitBooleanType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitBooleanValue(value: GBooleanValue, data: Nothing?) = visitBooleanValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitCompositeType(type: GCompositeType, data: Nothing?) = visitNamedType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitCustomScalarType(type: GCustomScalarType, data: Nothing?) = visitCustomScalarType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitDefinition(definition: GDefinition, data: Nothing?) = visitNode(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitDirective(directive: GDirective, data: Nothing?) = visitDirective(directive)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition, data: Nothing?) = visitDirectiveArgumentDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?) = visitDirectiveDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitDocument(document: GDocument, data: Nothing?) = visitDocument(document)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitEnumType(type: GEnumType, data: Nothing?) = visitEnumType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?) = visitEnumTypeExtension(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitEnumValue(value: GEnumValue, data: Nothing?) = visitEnumValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?) = visitEnumValueDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitExecutableDefinition(definition: GExecutableDefinition, data: Nothing?) = visitDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition, data: Nothing?) = visitFieldArgumentDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitFieldDefinition(definition: GFieldDefinition, data: Nothing?) = visitFieldDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitFieldSelection(selection: GFieldSelection, data: Nothing?) = visitFieldSelection(selection)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitFloatType(type: GFloatType, data: Nothing?) = visitFloatType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitFloatValue(value: GFloatValue, data: Nothing?) = visitFloatValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Nothing?) = visitFragmentDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitFragmentSelection(selection: GFragmentSelection, data: Nothing?) = visitFragmentSelection(selection)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitIdType(type: GIdType, data: Nothing?) = visitIdType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?) = visitInlineFragmentSelection(selection)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition, data: Nothing?) = visitInputObjectArgumentDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitInputObjectType(type: GInputObjectType, data: Nothing?) = visitInputObjectType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?) = visitInputObjectTypeExtension(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitIntType(type: GIntType, data: Nothing?) = visitIntType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitIntValue(value: GIntValue, data: Nothing?) = visitIntValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitInterfaceType(type: GInterfaceType, data: Nothing?) = visitInterfaceType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?) = visitInterfaceTypeExtension(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitLeafType(type: GLeafType, data: Nothing?) = visitNamedType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitListTypeRef(ref: GListTypeRef, data: Nothing?) = visitListTypeRef(ref)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitListValue(value: GListValue, data: Nothing?) = visitListValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitName(name: GName, data: Nothing?) = visitName(name)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitNamedType(type: GNamedType, data: Nothing?) = visitType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Nothing?) = visitNamedTypeRef(ref)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitNode(node: GAst, data: Nothing?) = visitNode(node)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?) = visitNonNullTypeRef(ref)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitNullValue(value: GNullValue, data: Nothing?) = visitNullValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitObjectType(type: GObjectType, data: Nothing?) = visitObjectType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?) = visitObjectTypeExtension(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitObjectValue(value: GObjectValue, data: Nothing?) = visitObjectValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitObjectValueField(field: GObjectValueField, data: Nothing?) = visitObjectValueField(field)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitOperationDefinition(definition: GOperationDefinition, data: Nothing?) = visitOperationDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?) = visitOperationTypeDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitScalarType(type: GScalarType, data: Nothing?) = visitNamedType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?) = visitScalarTypeExtension(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Nothing?) = visitSchemaDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitSchemaExtensionDefinition(definition: GSchemaExtension, data: Nothing?) = visitSchemaExtensionDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitSelection(selection: GSelection, data: Nothing?) = visitNode(selection)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitSelectionSet(set: GSelectionSet, data: Nothing?) = visitSelectionSet(set)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitStringType(type: GStringType, data: Nothing?) = visitStringType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitStringValue(value: GStringValue, data: Nothing?) = visitStringValue(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitSyntheticNode(node: GAst, data: Nothing?) = visitSyntheticNode(node)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitType(type: GType, data: Nothing?) = visitTypeSystemDefinition(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitTypeExtension(extension: GTypeExtension, data: Nothing?) = visitTypeSystemExtension(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitTypeRef(ref: GTypeRef, data: Nothing?) = visitNode(ref)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitTypeSystemDefinition(definition: GTypeSystemDefinition, data: Nothing?) = visitDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitTypeSystemExtension(extension: GTypeSystemExtension, data: Nothing?) = visitDefinition(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitUnionType(type: GUnionType, data: Nothing?) = visitUnionType(type)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?) = visitUnionTypeExtension(extension)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitValue(value: GValue, data: Nothing?) = visitNode(value)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitVariableDefinition(definition: GVariableDefinition, data: Nothing?) = visitVariableDefinition(definition)

			@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
			final override fun visitVariableRef(ref: GVariableRef, data: Nothing?) = visitVariableRef(ref)
		}


		abstract class WithoutResult<Data>(
			descendsAutomatically: Boolean = true
		) : Hierarchical<Unit, Data>(
			descendsAutomatically = descendsAutomatically
		)


		abstract class WithoutResultAndData(
			descendsAutomatically: Boolean = true
		) : WithoutData<Unit>(
			descendsAutomatically = descendsAutomatically
		)
	}


	internal class Identity<Data> : Visitor.Hierarchical<Data, Data>(descendsAutomatically = false) {

		override fun visitNode(node: GAst, data: Data) =
			data
	}


	abstract class WithoutData<out Result>(
		descendsAutomatically: Boolean = true
	) : Visitor<Result, Nothing?>(
		automaticallyVisitsChildren = descendsAutomatically
	) {

		protected abstract fun visitArgument(argument: GArgument): Result
		protected abstract fun visitBooleanType(type: GBooleanType): Result
		protected abstract fun visitBooleanValue(value: GBooleanValue): Result
		protected abstract fun visitCustomScalarType(type: GCustomScalarType): Result
		protected abstract fun visitDirective(directive: GDirective): Result
		protected abstract fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition): Result
		protected abstract fun visitDirectiveDefinition(definition: GDirectiveDefinition): Result
		protected abstract fun visitDocument(document: GDocument): Result
		protected abstract fun visitEnumType(type: GEnumType): Result
		protected abstract fun visitEnumTypeExtension(extension: GEnumTypeExtension): Result
		protected abstract fun visitEnumValue(value: GEnumValue): Result
		protected abstract fun visitEnumValueDefinition(definition: GEnumValueDefinition): Result
		protected abstract fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition): Result
		protected abstract fun visitFieldDefinition(definition: GFieldDefinition): Result
		protected abstract fun visitFieldSelection(selection: GFieldSelection): Result
		protected abstract fun visitFloatType(type: GFloatType): Result
		protected abstract fun visitFloatValue(value: GFloatValue): Result
		protected abstract fun visitFragmentDefinition(definition: GFragmentDefinition): Result
		protected abstract fun visitFragmentSelection(selection: GFragmentSelection): Result
		protected abstract fun visitIdType(type: GIdType): Result
		protected abstract fun visitInlineFragmentSelection(selection: GInlineFragmentSelection): Result
		protected abstract fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition): Result
		protected abstract fun visitInputObjectType(type: GInputObjectType): Result
		protected abstract fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension): Result
		protected abstract fun visitIntType(type: GIntType): Result
		protected abstract fun visitIntValue(value: GIntValue): Result
		protected abstract fun visitInterfaceType(type: GInterfaceType): Result
		protected abstract fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension): Result
		protected abstract fun visitListTypeRef(ref: GListTypeRef): Result
		protected abstract fun visitListValue(value: GListValue): Result
		protected abstract fun visitName(name: GName): Result
		protected abstract fun visitNamedTypeRef(ref: GNamedTypeRef): Result
		protected abstract fun visitNonNullTypeRef(ref: GNonNullTypeRef): Result
		protected abstract fun visitNullValue(value: GNullValue): Result
		protected abstract fun visitObjectType(type: GObjectType): Result
		protected abstract fun visitObjectTypeExtension(extension: GObjectTypeExtension): Result
		protected abstract fun visitObjectValue(value: GObjectValue): Result
		protected abstract fun visitObjectValueField(field: GObjectValueField): Result
		protected abstract fun visitOperationDefinition(definition: GOperationDefinition): Result
		protected abstract fun visitOperationTypeDefinition(definition: GOperationTypeDefinition): Result
		protected abstract fun visitScalarTypeExtension(extension: GScalarTypeExtension): Result
		protected abstract fun visitSchemaDefinition(definition: GSchemaDefinition): Result
		protected abstract fun visitSchemaExtensionDefinition(definition: GSchemaExtension): Result
		protected abstract fun visitSelectionSet(set: GSelectionSet): Result
		protected abstract fun visitStringType(type: GStringType): Result
		protected abstract fun visitStringValue(value: GStringValue): Result
		protected abstract fun visitSyntheticNode(node: GAst): Result
		protected abstract fun visitUnionType(type: GUnionType): Result
		protected abstract fun visitUnionTypeExtension(extension: GUnionTypeExtension): Result
		protected abstract fun visitVariableDefinition(definition: GVariableDefinition): Result
		protected abstract fun visitVariableRef(ref: GVariableRef): Result

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitArgument(argument: GArgument, data: Nothing?) = visitArgument(argument)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitBooleanType(type: GBooleanType, data: Nothing?) = visitBooleanType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitBooleanValue(value: GBooleanValue, data: Nothing?) = visitBooleanValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitCustomScalarType(type: GCustomScalarType, data: Nothing?) = visitCustomScalarType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitDirective(directive: GDirective, data: Nothing?) = visitDirective(directive)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitDirectiveArgumentDefinition(definition: GDirectiveArgumentDefinition, data: Nothing?) = visitDirectiveArgumentDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitDirectiveDefinition(definition: GDirectiveDefinition, data: Nothing?) = visitDirectiveDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitDocument(document: GDocument, data: Nothing?) = visitDocument(document)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitEnumType(type: GEnumType, data: Nothing?) = visitEnumType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitEnumTypeExtension(extension: GEnumTypeExtension, data: Nothing?) = visitEnumTypeExtension(extension)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitEnumValue(value: GEnumValue, data: Nothing?) = visitEnumValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitEnumValueDefinition(definition: GEnumValueDefinition, data: Nothing?) = visitEnumValueDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitFieldArgumentDefinition(definition: GFieldArgumentDefinition, data: Nothing?) = visitFieldArgumentDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitFieldDefinition(definition: GFieldDefinition, data: Nothing?) = visitFieldDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitFieldSelection(selection: GFieldSelection, data: Nothing?) = visitFieldSelection(selection)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitFloatType(type: GFloatType, data: Nothing?) = visitFloatType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitFloatValue(value: GFloatValue, data: Nothing?) = visitFloatValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitFragmentDefinition(definition: GFragmentDefinition, data: Nothing?) = visitFragmentDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitFragmentSelection(selection: GFragmentSelection, data: Nothing?) = visitFragmentSelection(selection)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitIdType(type: GIdType, data: Nothing?) = visitIdType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection, data: Nothing?) = visitInlineFragmentSelection(selection)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitInputObjectArgumentDefinition(definition: GInputObjectArgumentDefinition, data: Nothing?) = visitInputObjectArgumentDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitInputObjectType(type: GInputObjectType, data: Nothing?) = visitInputObjectType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension, data: Nothing?) = visitInputObjectTypeExtension(extension)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitIntType(type: GIntType, data: Nothing?) = visitIntType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitIntValue(value: GIntValue, data: Nothing?) = visitIntValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitInterfaceType(type: GInterfaceType, data: Nothing?) = visitInterfaceType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension, data: Nothing?) = visitInterfaceTypeExtension(extension)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitListTypeRef(ref: GListTypeRef, data: Nothing?) = visitListTypeRef(ref)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitListValue(value: GListValue, data: Nothing?) = visitListValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitName(name: GName, data: Nothing?) = visitName(name)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitNamedTypeRef(ref: GNamedTypeRef, data: Nothing?) = visitNamedTypeRef(ref)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitNonNullTypeRef(ref: GNonNullTypeRef, data: Nothing?) = visitNonNullTypeRef(ref)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitNullValue(value: GNullValue, data: Nothing?) = visitNullValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitObjectType(type: GObjectType, data: Nothing?) = visitObjectType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitObjectTypeExtension(extension: GObjectTypeExtension, data: Nothing?) = visitObjectTypeExtension(extension)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitObjectValue(value: GObjectValue, data: Nothing?) = visitObjectValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitObjectValueField(field: GObjectValueField, data: Nothing?) = visitObjectValueField(field)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitOperationDefinition(definition: GOperationDefinition, data: Nothing?) = visitOperationDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition, data: Nothing?) = visitOperationTypeDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitScalarTypeExtension(extension: GScalarTypeExtension, data: Nothing?) = visitScalarTypeExtension(extension)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitSchemaDefinition(definition: GSchemaDefinition, data: Nothing?) = visitSchemaDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitSchemaExtensionDefinition(definition: GSchemaExtension, data: Nothing?) = visitSchemaExtensionDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitSelectionSet(set: GSelectionSet, data: Nothing?) = visitSelectionSet(set)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitStringType(type: GStringType, data: Nothing?) = visitStringType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitStringValue(value: GStringValue, data: Nothing?) = visitStringValue(value)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitSyntheticNode(node: GAst, data: Nothing?) = visitSyntheticNode(node)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitUnionType(type: GUnionType, data: Nothing?) = visitUnionType(type)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitUnionTypeExtension(extension: GUnionTypeExtension, data: Nothing?) = visitUnionTypeExtension(extension)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitVariableDefinition(definition: GVariableDefinition, data: Nothing?) = visitVariableDefinition(definition)

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		final override fun visitVariableRef(ref: GVariableRef, data: Nothing?) = visitVariableRef(ref)
	}


	abstract class WithoutResult<Data>(
		descendsAutomatically: Boolean = true
	) : Visitor<Unit, Data>(
		automaticallyVisitsChildren = descendsAutomatically
	)


	abstract class WithoutResultAndData(
		descendsAutomatically: Boolean = true
	) : WithoutData<Unit>(
		descendsAutomatically = descendsAutomatically
	)
}


//fun <Data> Iterable<GVisitor<Data, Data>>.serialize(): GVisitor<Data, Data> {
//	var firstVisitor: GVisitor<Data, Data>? = null
//	var previousVisitor: GVisitor<Data, Data>? = null
//
//	var count = 0
//	for (visitor in this) {
//		count += 1
//
//		if (previousVisitor !== null) {
//			previousVisitor = previousVisitor.then(visitor)
//
//			if (firstVisitor === null)
//				firstVisitor = previousVisitor
//		}
//
//		previousVisitor = visitor
//	}
//
//	firstVisitor ?: return GVisitor.identity()
//
//	return firstVisitor
//}


internal fun <Result, Data> GAst.accept(
	visitor: Visitor<Result, Data>,
	data: Data,
	coordinator: VisitCoordinator<Result, Data> = VisitCoordinator.default()
) =
	coordinator.coordinate(node = this, data = data, dispatcher = VisitDispatcher(visitor)).run()


internal fun <Result> GAst.accept(
	visitor: Visitor<Result, Nothing?>,
	coordinator: VisitCoordinator<Result, Nothing?> = VisitCoordinator.default()
) =
	accept(visitor, data = null, coordinator = coordinator)


@JvmName("acceptSimple")
internal fun GAst.accept(
	visitor: Visitor<Unit, Nothing?>,
	coordinator: VisitCoordinator<Unit, Nothing?> = VisitCoordinator.default()
) =
	accept(visitor, data = null, coordinator = coordinator)
