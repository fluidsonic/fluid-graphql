package io.fluidsonic.graphql


internal object Printer {

	fun print(node: GNode, indent: String = "  ") =
		IndentingWriter(indent = indent).run {
			writeNode(node)
			toString()
		}


	private fun IndentingWriter.writeNode(node: GNode) {
		when (node) {
			is GArgument -> writeNode(node)
			is GArgumentDefinition -> writeNode(node)
			is GBooleanValue -> writeNode(node)
			is GDirective -> writeNode(node)
			is GDirectiveDefinition -> writeNode(node)
			is GDocument -> writeNode(node)
			is GEnumType -> writeNode(node)
			is GEnumTypeExtension -> writeNode(node)
			is GEnumValue -> writeNode(node)
			is GEnumValueDefinition -> writeNode(node)
			is GFieldDefinition -> writeNode(node)
			is GFieldSelection -> writeNode(node)
			is GFloatValue -> writeNode(node)
			is GFragmentDefinition -> writeNode(node)
			is GFragmentSelection -> writeNode(node)
			is GInlineFragmentSelection -> writeNode(node)
			is GInputObjectType -> writeNode(node)
			is GInputObjectTypeExtension -> writeNode(node)
			is GIntValue -> writeNode(node)
			is GInterfaceType -> writeNode(node)
			is GInterfaceTypeExtension -> writeNode(node)
			is GListType -> error("Cannot print AST of ${node::class}")
			is GListTypeRef -> writeNode(node)
			is GListValue -> writeNode(node)
			is GName -> writeNode(node)
			is GNamedTypeRef -> writeNode(node)
			is GNonNullType -> error("Cannot print AST of ${node::class}")
			is GNonNullTypeRef -> writeNode(node)
			is GNullValue -> writeNode(node)
			is GObjectType -> writeNode(node)
			is GObjectTypeExtension -> writeNode(node)
			is GObjectValue -> writeNode(node)
			is GOperationDefinition -> writeNode(node)
			is GOperationTypeDefinition -> writeNode(node)
			is GScalarType -> writeNode(node)
			is GScalarTypeExtension -> writeNode(node)
			is GSchemaDefinition -> writeNode(node)
			is GSchemaExtension -> writeNode(node)
			is GSelectionSet -> writeNode(node)
			is GStringValue -> writeNode(node)
			is GUnionType -> writeNode(node)
			is GUnionTypeExtension -> writeNode(node)
			is GVariableDefinition -> writeNode(node)
			is GVariableRef -> writeNode(node)
		}
	}


	private fun IndentingWriter.writeNode(argument: GArgument) {
		writeNode(argument.nameNode)
		writeRaw(": ")
		writeNode(argument.value)
	}


	private fun IndentingWriter.writeNode(definition: GArgumentDefinition) {
		definition.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeNode(definition.nameNode)
		writeRaw(": ")
		writeNode(definition.type)

		definition.defaultValue?.let { defaultValue ->
			writeRaw(" = ")
			writeNode(defaultValue)
		}

		writeDirectives(definition.directives)
	}


	private fun IndentingWriter.writeNode(value: GBooleanValue) {
		writeRaw(if (value.value) "true" else "false")
	}


	private fun IndentingWriter.writeNode(directive: GDirective) {
		writeRaw("@")
		writeNode(directive.nameNode)
		writeArguments(directive.arguments)
	}


	private fun IndentingWriter.writeNode(definition: GDirectiveDefinition) {
		definition.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
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


	private fun IndentingWriter.writeNode(document: GDocument) {
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


	private fun IndentingWriter.writeNode(type: GEnumType) {
		type.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeRaw("enum ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(type.values)
	}


	private fun IndentingWriter.writeNode(extension: GEnumTypeExtension) {
		writeRaw("extend enum ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(extension.values)
	}


	private fun IndentingWriter.writeNode(value: GEnumValue) {
		writeRaw(value.name)
	}


	private fun IndentingWriter.writeNode(definition: GEnumValueDefinition) {
		definition.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeNode(definition.nameNode)
		writeDirectives(definition.directives)
	}


	private fun IndentingWriter.writeNode(definition: GFieldDefinition) {
		definition.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeNode(definition.nameNode)
		writeArgumentDefinitions(definition.argumentDefinitions)
		writeRaw(": ")
		writeNode(definition.type)
		writeDirectives(definition.directives)
	}


	private fun IndentingWriter.writeNode(selection: GFieldSelection) {
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


	private fun IndentingWriter.writeNode(value: GFloatValue) {
		// FIXME Write own implementation for consistent results?
		writeRaw(value.value.toConsistentString())
	}


	private fun IndentingWriter.writeNode(definition: GFragmentDefinition) {
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


	private fun IndentingWriter.writeNode(selection: GFragmentSelection) {
		writeRaw("...")
		writeNode(selection.nameNode)
		writeDirectives(selection.directives)
	}


	private fun IndentingWriter.writeNode(type: GInputObjectType) {
		type.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeRaw("input ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writeInputObjectTypeArguments(type.argumentDefinitions)
	}


	private fun IndentingWriter.writeNode(extension: GInputObjectTypeExtension) {
		writeRaw("extend input ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writeInputObjectTypeArguments(extension.argumentDefinitions)
	}


	private fun IndentingWriter.writeNode(selection: GInlineFragmentSelection) {
		writeRaw("...")
		selection.typeCondition?.let { typeCondition ->
			writeRaw(" on ")
			writeNode(typeCondition)
		}
		writeDirectives(selection.directives)
		writeRaw(" ")
		writeNode(selection.selectionSet)
	}


	private fun IndentingWriter.writeNode(value: GIntValue) {
		writeRaw(value.value.toString())
	}


	private fun IndentingWriter.writeNode(type: GInterfaceType) {
		type.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeRaw("interface ")
		writeNode(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeDirectives(type.directives)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	private fun IndentingWriter.writeNode(extension: GInterfaceTypeExtension) {
		writeRaw("extend interface ")
		writeNode(extension.nameNode)
		writeImplementedInterfaces(extension.interfaces)
		writeDirectives(extension.directives)
		writeFieldDefinitions(extension.fieldDefinitions)
	}


	private fun IndentingWriter.writeNode(value: GListValue) {
		writeRaw("[")
		value.elements.forEachIndexed { index, element ->
			if (index > 0)
				writeRaw(", ")

			writeNode(element)
		}
		writeRaw("]")
	}


	private fun IndentingWriter.writeNode(ref: GListTypeRef) {
		writeRaw("[")
		writeNode(ref.elementType)
		writeRaw("]")
	}


	private fun IndentingWriter.writeNode(name: GName) {
		writeRaw(name.value)
	}


	private fun IndentingWriter.writeNode(ref: GNamedTypeRef) {
		writeNode(ref.nameNode)
	}


	private fun IndentingWriter.writeNode(ref: GNonNullTypeRef) {
		writeNode(ref.nullableRef)
		writeRaw("!")
	}


	private fun IndentingWriter.writeNode(@Suppress("UNUSED_PARAMETER") value: GNullValue) {
		writeRaw("null")
	}


	private fun IndentingWriter.writeNode(type: GObjectType) {
		type.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeRaw("type ")
		writeNode(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	private fun IndentingWriter.writeNode(extension: GObjectTypeExtension) {
		writeRaw("extend type ")
		writeNode(extension.nameNode)
		writeImplementedInterfaces(extension.interfaces)
		writeFieldDefinitions(extension.fieldDefinitions)
	}


	private fun IndentingWriter.writeNode(value: GObjectValue) {
		if (value.arguments.isNotEmpty())
			writeBlock {
				value.arguments.forEachIndexed { index, field ->
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


	private fun IndentingWriter.writeNode(definition: GOperationDefinition) {
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


	private fun IndentingWriter.writeNode(definition: GOperationTypeDefinition) {
		writeRaw(definition.operationType.name)
		writeRaw(": ")
		writeNode(definition.type)
	}


	private fun IndentingWriter.writeNode(type: GScalarType) {
		type.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeRaw("scalar ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
	}


	private fun IndentingWriter.writeNode(extension: GScalarTypeExtension) {
		writeRaw("extend scalar ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
	}


	private fun IndentingWriter.writeNode(definition: GSchemaDefinition) {
		val queryOperation = definition.operationTypeDefinition(GOperationType.query)
			?.takeIf { it.type.name != GLanguage.defaultQueryTypeName }

		val mutationOperation = definition.operationTypeDefinition(GOperationType.mutation)
			?.takeIf { it.type.name != GLanguage.defaultMutationTypeName }

		val subscriptionOperation = definition.operationTypeDefinition(GOperationType.subscription)
			?.takeIf { it.type.name != GLanguage.defaultSubscriptionTypeName }

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


	private fun IndentingWriter.writeNode(definition: GSchemaExtension) {
		writeRaw("extend schema")
		writeDirectives(definition.directives)
		writeOperationTypeDefinitions(definition.operationTypeDefinitions)
	}


	private fun IndentingWriter.writeNode(set: GSelectionSet) {
		writeBlock {
			set.selections.forEach { selection ->
				writeNode(selection)
				writeLinebreak()
			}
		}
	}


	// FIXME proper escaping logic, line wrapping, indentation
	private fun IndentingWriter.writeNode(value: GStringValue) {
		val string = value.value

		if (value.isBlock) {
			val isMultiline = string.length > 70 ||
				string.indexOfFirst { it == '\n' || it == '\r' } >= 0

			writeRaw("\"\"\"")
			if (isMultiline)
				writeLinebreak()
			writeRaw(string.replace("\"\"\"", "\\\"\"\""))
			if (isMultiline)
				writeLinebreak()
			writeRaw("\"\"\"")
			writeLinebreak()
		}
		else {
			writeRaw("\"")
			writeRaw(string
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
			)
			writeRaw("\"")
		}
	}


	private fun IndentingWriter.writeNode(type: GUnionType) {
		type.descriptionNode?.let { descriptionNode ->
			writeNode(descriptionNode)
			writeRaw("\n")
		}
		writeRaw("union ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writePossibleTypes(type.possibleTypes)
	}


	private fun IndentingWriter.writeNode(extension: GUnionTypeExtension) {
		writeRaw("union ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writePossibleTypes(extension.possibleTypes)
	}


	private fun IndentingWriter.writeNode(definition: GVariableDefinition) {
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


	private fun IndentingWriter.writeNode(ref: GVariableRef) {
		writeRaw("$")
		writeNode(ref.nameNode)
	}


	private inline operator fun IndentingWriter.invoke(block: IndentingWriter.() -> Unit) =
		block()


	private fun IndentingWriter.writeArguments(arguments: Collection<GArgument>) {
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


	private fun IndentingWriter.writeArgumentDefinitions(definitions: List<GArgumentDefinition>) {
		if (definitions.isEmpty())
			return

		writeRaw("(")

		val hasDescriptions = definitions.any { it.description != null }
		val multiline = hasDescriptions || definitions.size > 3
		if (multiline) {
			indented {
				definitions.forEachIndexed { index, definition ->
					if (index > 0 && hasDescriptions)
						writeLinebreak()

					writeLinebreak()
					writeNode(definition)
				}
				writeLinebreak()
			}
		}
		else {
			definitions.forEachIndexed { index, definition ->
				if (index > 0)
					writeRaw(", ")

				writeNode(definition)
			}
		}

		writeRaw(")")
	}


	private inline fun <Result> IndentingWriter.writeBlock(block: () -> Result): Result {
		writeRaw("{")
		writeLinebreak()
		val result = indented(block)
		writeRaw("}")

		return result
	}


	private fun IndentingWriter.writeDirectives(directives: List<GDirective>) {
		if (directives.isEmpty())
			return

		directives.forEach { directive ->
			writeRaw(" ")
			writeNode(directive)
		}
	}


	private fun IndentingWriter.writeEnumValueDefinitions(definitions: List<GEnumValueDefinition>) {
		if (definitions.isEmpty())
			return

		val includeExtraLinebreak = definitions.any { it.description != null }

		writeBlock {
			definitions.forEachIndexed { index, definition ->
				if (index > 0 && includeExtraLinebreak)
					writeLinebreak()

				writeNode(definition)
				writeLinebreak()
			}
		}
	}


	private fun IndentingWriter.writeFieldDefinitions(definitions: List<GFieldDefinition>) {
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


	private fun IndentingWriter.writeImplementedInterfaces(interfaces: List<GNamedTypeRef>) {
		if (interfaces.isEmpty())
			return

		writeRaw(" implements ")
		interfaces.forEachIndexed { index, type ->
			if (index > 0)
				writeRaw(" & ")

			writeNode(type)
		}
	}


	private fun IndentingWriter.writeInputObjectTypeArguments(definitions: List<GArgumentDefinition>) {
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


	private fun IndentingWriter.writeOperationTypeDefinitions(definitions: Collection<GOperationTypeDefinition>) {
		if (definitions.isEmpty())
			return

		writeBlock {
			definitions.forEach { definition ->
				writeNode(definition)
				writeLinebreak()
			}
		}
	}


	private fun IndentingWriter.writePossibleTypes(types: Collection<GNamedTypeRef>) {
		if (types.isEmpty())
			return

		writeRaw(" = ")

		types.forEachIndexed { index, type ->
			if (index > 0)
				writeRaw(" | ")

			writeNode(type.nameNode)
		}
	}


	private fun IndentingWriter.writeVariableDefinitions(definitions: Collection<GVariableDefinition>) {
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
