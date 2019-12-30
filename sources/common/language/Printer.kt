package io.fluidsonic.graphql


internal object Printer {

	fun print(node: GNode, indent: String = "\t"): String {
		val writer = GWriter(indent = indent)
		node.accept(PrintVisitor(writer))

		return writer.toString()
	}
}


private class PrintVisitor(
	private val writer: GWriter
) : Visitor.Typed.WithoutData<Unit>() {

	override fun onArgument(argument: GArgument, visit: Visit) = writer {
		writeNode(argument.nameNode)
		writeRaw(": ")
		writeNode(argument.value)
	}


	override fun onArgumentDefinition(definition: GArgumentDefinition, visit: Visit) = writer {
		writeNode(definition.descriptionNode)
		writeNode(definition.nameNode)
		writeRaw(": ")
		writeNode(definition.type)

		definition.defaultValue?.let { defaultValue ->
			writeRaw(" = ")
			writeNode(defaultValue)
		}

		writeDirectives(definition.directives)
	}


	override fun onBooleanValue(value: GBooleanValue, visit: Visit) = writer {
		writeRaw(if (value.value) "true" else "false")
	}


	override fun onDirective(directive: GDirective, visit: Visit) = writer {
		writeRaw("@")
		writeNode(directive.nameNode)
		writeArguments(directive.arguments)
	}


	override fun onDirectiveDefinition(definition: GDirectiveDefinition, visit: Visit) = writer {
		writeNode(definition.descriptionNode)
		writeRaw("directive @")
		writeNode(definition.nameNode)
		writeArgumentDefinitions(definition.argumentDefinitions)
		writeRaw(" on ")
		definition.locationNodes.forEachIndexed { index, location ->
			if (index > 0)
				writeRaw(" | ")

			writeNode(location)
		}
	}


	override fun onDocument(document: GDocument, visit: Visit) = writer {
		val startLength = length

		document.definitions.forEach { definition ->
			// Cannot use index as some definitions like GSchemaDefinition may have no content and thus not produce any output.
			if (length > startLength) {
				writeLinebreak()
				writeLinebreak()
			}

			writeNode(definition)
		}
	}


	override fun onEnumType(type: GEnumType, visit: Visit) = writer {
		writeNode(type.descriptionNode)
		writeRaw("enum ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(type.values)
	}


	override fun onEnumTypeExtension(extension: GEnumTypeExtension, visit: Visit) = writer {
		writeRaw("extend enum ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(extension.values)
	}


	override fun onEnumValue(value: GEnumValue, visit: Visit) = writer {
		writeRaw(value.name)
	}


	override fun onEnumValueDefinition(definition: GEnumValueDefinition, visit: Visit) = writer {
		writeNode(definition.descriptionNode)
		writeNode(definition.nameNode)
		writeDirectives(definition.directives)
	}


	override fun onFieldDefinition(definition: GFieldDefinition, visit: Visit) = writer {
		writeNode(definition.descriptionNode)
		writeNode(definition.nameNode)
		writeArgumentDefinitions(definition.argumentDefinitions)
		writeRaw(": ")
		writeNode(definition.type)
		writeDirectives(definition.directives)
	}


	override fun onFieldSelection(selection: GFieldSelection, visit: Visit) = writer {
		selection.aliasNode?.let { alias ->
			writeNode(alias)
			writeRaw(": ")
		}
		writeNode(selection.nameNode)
		writeArguments(selection.arguments)
		writeDirectives(selection.directives)
		selection.selectionSet?.let { set ->
			writeRaw(" ")
			writeNode(set)
		}
	}


	override fun onFloatValue(value: GFloatValue, visit: Visit) = writer {
		writeRaw(value.value.toString())
	}


	override fun onFragmentDefinition(definition: GFragmentDefinition, visit: Visit) = writer {
		writeRaw("fragment ")
		writeNode(definition.nameNode)
		writeVariableDefinitions(definition.variableDefinitions)
		writeRaw(" on ")
		writeNode(definition.typeCondition)
		writeDirectives(definition.directives)
		writeRaw(" ")
		writeNode(definition.selectionSet)
		writeLinebreak()
	}


	override fun onFragmentSelection(selection: GFragmentSelection, visit: Visit) = writer {
		writeRaw("...")
		writeNode(selection.nameNode)
		writeDirectives(selection.directives)
	}


	override fun onInputObjectType(type: GInputObjectType, visit: Visit) = writer {
		writeNode(type.descriptionNode)
		writeRaw("input ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writeInputObjectTypeArguments(type.argumentDefinitions)
	}


	override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, visit: Visit) = writer {
		writeRaw("extend input ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writeInputObjectTypeArguments(extension.argumentDefinitions)
	}


	override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, visit: Visit) = writer {
		writeRaw("...")
		selection.typeCondition?.let { typeCondition ->
			writeRaw(" on")
			writeNode(typeCondition)
		}
		writeDirectives(selection.directives)
		writeRaw(" ")
		writeNode(selection.selectionSet)
	}


	override fun onIntValue(value: GIntValue, visit: Visit) = writer {
		writeRaw(value.value.toString())
	}


	override fun onInterfaceType(type: GInterfaceType, visit: Visit) = writer {
		writeNode(type.descriptionNode)
		writeRaw("interface ")
		writeNode(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeDirectives(type.directives)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, visit: Visit) = writer {
		writeRaw("extend interface ")
		writeNode(extension.nameNode)
		writeImplementedInterfaces(extension.interfaces)
		writeDirectives(extension.directives)
		writeFieldDefinitions(extension.fieldDefinitions)
	}


	override fun onListValue(value: GListValue, visit: Visit) = writer {
		writeRaw("[")
		value.elements.forEachIndexed { index, element ->
			if (index > 0)
				writeRaw(", ")

			writeNode(element)
		}
		writeRaw("]")
	}


	override fun onListTypeRef(ref: GListTypeRef, visit: Visit) = writer {
		writeRaw("[")
		writeNode(ref.elementType)
		writeRaw("]")
	}


	override fun onName(name: GName, visit: Visit) = writer {
		writeRaw(name.value)
	}


	override fun onNamedTypeRef(ref: GNamedTypeRef, visit: Visit) = writer {
		writeNode(ref.nameNode)
	}


	override fun onNonNullTypeRef(ref: GNonNullTypeRef, visit: Visit) = writer {
		writeNode(ref.nullableRef)
		writeRaw("!")
	}


	override fun onNullValue(value: GNullValue, visit: Visit) = writer {
		writeRaw("null")
	}


	override fun onObjectType(type: GObjectType, visit: Visit) = writer {
		writeNode(type.descriptionNode)
		writeRaw("type ")
		writeNode(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	override fun onObjectTypeExtension(extension: GObjectTypeExtension, visit: Visit) = writer {
		writeRaw("extend type ")
		writeNode(extension.nameNode)
		writeImplementedInterfaces(extension.interfaces)
		writeFieldDefinitions(extension.fieldDefinitions)
	}


	override fun onObjectValue(value: GObjectValue, visit: Visit) = writer {
		if (value.fields.isNotEmpty())
			writeBlock {
				value.fields.forEachIndexed { index, field ->
					if (index > 0) {
						writeRaw(",")
						writeLinebreak()
					}

					writeNode(field)
				}

				writeLinebreak()
			}
		else
			writeRaw("{}")
	}


	override fun onObjectValueField(field: GObjectValueField, visit: Visit) = writer {
		writeNode(field.nameNode)
		writeRaw(": ")
		writeNode(field.value)
	}


	override fun onOperationDefinition(definition: GOperationDefinition, visit: Visit) = writer {
		val canUseShortSyntax = definition.nameNode === null &&
			definition.directives.isEmpty() &&
			definition.variableDefinitions.isEmpty() &&
			definition.type === GOperationType.query

		if (!canUseShortSyntax) {
			writeRaw(definition.type.name)

			definition.nameNode?.let { name ->
				writeRaw(" ")
				writeNode(name)
			}

			if (definition.variableDefinitions.isNotEmpty()) {
				if (definition.nameNode === null)
					writeRaw(" ")

				writeVariableDefinitions(definition.variableDefinitions)
			}

			writeDirectives(definition.directives)
			writeRaw(" ")
		}

		writeNode(definition.selectionSet)
		writeLinebreak()
	}


	override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, visit: Visit) = writer {
		writeRaw(definition.operationType.name)
		writeRaw(": ")
		writeNode(definition.type)
	}


	override fun onScalarType(type: GScalarType, visit: Visit) = writer {
		writeNode(type.descriptionNode)
		writeRaw("scalar ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
	}


	override fun onScalarTypeExtension(extension: GScalarTypeExtension, visit: Visit) = writer {
		writeRaw("extend scalar ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
	}


	override fun onSchemaDefinition(definition: GSchemaDefinition, visit: Visit) = writer {
		val queryOperation = definition.operationTypeDefinitions.firstOrNull { it.operationType == GOperationType.query }
			?.takeIf { it.type.name != GSpecification.defaultQueryTypeName }

		val mutationOperation = definition.operationTypeDefinitions.firstOrNull { it.operationType == GOperationType.mutation }
			?.takeIf { it.type.name != GSpecification.defaultMutationTypeName }

		val subscriptionOperation = definition.operationTypeDefinitions.firstOrNull { it.operationType == GOperationType.subscription }
			?.takeIf { it.type.name != GSpecification.defaultSubscriptionTypeName }

		if (queryOperation == null && mutationOperation == null && subscriptionOperation == null)
			return

		writeRaw("schema ")
		writeBlock {
			queryOperation?.let {
				writeLinebreak()
				writeNode(it)
			}

			mutationOperation?.let {
				writeLinebreak()
				writeNode(it)
			}

			subscriptionOperation?.let {
				writeLinebreak()
				writeNode(it)
			}
		}
	}


	override fun onSchemaExtensionDefinition(definition: GSchemaExtension, visit: Visit) = writer {
		writeRaw("extend schema")
		writeDirectives(definition.directives)
		writeOperationTypeDefinitions(definition.operationTypeDefinitions)
	}


	override fun onSelectionSet(set: GSelectionSet, visit: Visit) = writer {
		writeBlock {
			set.selections.forEach { selection ->
				writeNode(selection)
				writeLinebreak()
			}
		}
	}


	// FIXME escaping, line wrapping, indentation
	override fun onStringValue(value: GStringValue, visit: Visit) = writer {
		val string = value.value

		if (value.isBlock) {
			val isMultiline = string.length > 70 ||
				string.indexOfFirst { it == '\n' || it == '\r' } >= 0

			writeRaw("\"\"\"")
			if (isMultiline)
				writeLinebreak()
			writeRaw(string)
			if (isMultiline)
				writeLinebreak()
			writeRaw("\"\"\"")
			writeLinebreak()
		}
		else {
			writeRaw("\"")
			writeRaw(string)
			writeRaw("\"")
		}
	}


	override fun onSyntheticNode(node: GNode, visit: Visit) {
		error("Cannot print AST of ${node::class}")
	}


	override fun onUnionType(type: GUnionType, visit: Visit) = writer {
		writeNode(type.descriptionNode)
		writeRaw("union ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writePossibleTypes(type.possibleTypes)
	}


	override fun onUnionTypeExtension(extension: GUnionTypeExtension, visit: Visit) = writer {
		writeRaw("union ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writePossibleTypes(extension.possibleTypes)
	}


	override fun onVariableDefinition(definition: GVariableDefinition, visit: Visit) = writer {
		writeRaw("$")
		writeNode(definition.nameNode)
		writeRaw(": ")
		writeNode(definition.type)
		definition.defaultValue?.let { defaultValue ->
			writeRaw(" = ")
			writeNode(defaultValue)
		}
		writeDirectives(definition.directives)
	}


	override fun onVariableRef(ref: GVariableRef, visit: Visit) = writer {
		writeRaw("$")
		writeNode(ref.nameNode)
	}


	private fun writeNode(node: GNode?) {
		node?.accept(this)
	}


	private inline operator fun GWriter.invoke(block: GWriter.() -> Unit) =
		block()


	private fun GWriter.writeArguments(arguments: Collection<GArgument>) {
		if (arguments.isEmpty())
			return

		writeRaw("(")
		arguments.forEachIndexed { index, argument ->
			if (index > 0)
				writeRaw(", ")

			writeNode(argument)
		}
		writeRaw(")")
	}


	private fun GWriter.writeArgumentDefinitions(definitions: List<GArgumentDefinition>) {
		if (definitions.isEmpty())
			return

		writeRaw("(")

		val useSingleLine = definitions.all { it.description == null }
		if (useSingleLine) {
			definitions.forEachIndexed { index, definition ->
				if (index > 0)
					writeRaw(", ")

				writeNode(definition)
			}
		}
		else {
			definitions.forEachIndexed { index, definition ->
				if (index > 0) {
					if (
						definition.description != null ||
						definitions[index - 1].description != null ||
						definitions.getOrNull(index + 1)?.description != null
					)
						writeLinebreak()
				}

				writeLinebreak()
				writeNode(definition)
			}
			writeLinebreak()
		}

		writeRaw(")")
	}


	private inline fun <R> GWriter.writeBlock(block: () -> R): R {
		writeRaw("{")
		writeLinebreak()
		val result = indented(block)
		writeRaw("}")

		return result
	}


	private fun GWriter.writeDirectives(directives: List<GDirective>) {
		if (directives.isEmpty())
			return

		directives.forEach { directive ->
			writeRaw(" ")
			writeNode(directive)
		}
	}


	private fun GWriter.writeEnumValueDefinitions(definitions: List<GEnumValueDefinition>) {
		if (definitions.isEmpty())
			return

		writeBlock {
			definitions.forEachIndexed { index, definition ->
				if (index > 0) {
					if (
						definition.description != null ||
						definitions[index - 1].description != null ||
						definitions.getOrNull(index + 1)?.description != null
					)
						writeLinebreak()
				}

				writeNode(definition)
				writeLinebreak()
			}
		}
	}


	private fun GWriter.writeFieldDefinitions(definitions: List<GFieldDefinition>) {
		if (definitions.isEmpty())
			return

		writeRaw(" ")

		writeBlock {
			definitions.forEachIndexed { index, definition ->
				if (index > 0) {
					if (
						definition.descriptionNode != null ||
						definitions[index - 1].descriptionNode != null ||
						definitions.getOrNull(index + 1)?.descriptionNode != null
					)
						writeLinebreak()
				}

				writeNode(definition)
				writeLinebreak()
			}
		}
	}


	private fun GWriter.writeImplementedInterfaces(interfaces: List<GNamedTypeRef>) {
		if (interfaces.isEmpty())
			return

		writeRaw(" implements ")
		interfaces.forEachIndexed { index, type ->
			if (index > 0)
				writeRaw(" & ")

			writeNode(type)
		}
	}


	private fun GWriter.writeInputObjectTypeArguments(definitions: List<GArgumentDefinition>) {
		if (definitions.isEmpty())
			return

		writeRaw(" ")

		writeBlock {
			definitions.forEachIndexed { index, definition ->
				if (index > 0) {
					if (
						definition.description != null ||
						definitions[index - 1].description != null ||
						definitions.getOrNull(index + 1)?.description != null
					)
						writeLinebreak()
				}

				writeNode(definition)
				writeLinebreak()
			}
		}
	}


	private fun GWriter.writeOperationTypeDefinitions(definitions: Collection<GOperationTypeDefinition>) {
		if (definitions.isEmpty())
			return

		writeBlock {
			definitions.forEach { definition ->
				writeNode(definition)
				writeLinebreak()
			}
		}
	}


	private fun GWriter.writePossibleTypes(types: Collection<GNamedTypeRef>) {
		if (types.isEmpty())
			return

		writeRaw(" = ")

		types.forEachIndexed { index, type ->
			if (index > 0)
				writeRaw(" | ")

			writeNode(type.nameNode)
		}
	}


	private fun GWriter.writeVariableDefinitions(definitions: Collection<GVariableDefinition>) {
		if (definitions.isEmpty())
			return

		writeRaw("(")
		definitions.forEachIndexed { index, definition ->
			if (index > 0)
				writeRaw(", ")

			writeNode(definition)
		}
		writeRaw(")")
	}
}
