package io.fluidsonic.graphql


// FIXME add newline after descriptions
internal object Printer {

	fun print(node: GNode, indent: String = "\t") =
		GWriter(indent = indent).run {
			writeNode(node)
			toString()
		}


	private fun GWriter.writeNode(node: GNode?) {
		when (node) {
			null -> Unit
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
			is GObjectValueField -> writeNode(node)
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


	private fun GWriter.writeNode(argument: GArgument) {
		writeNode(argument.nameNode)
		writeRaw(": ")
		writeNode(argument.value)
	}


	private fun GWriter.writeNode(definition: GArgumentDefinition) {
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


	private fun GWriter.writeNode(value: GBooleanValue) {
		writeRaw(if (value.value) "true" else "false")
	}


	private fun GWriter.writeNode(directive: GDirective) {
		writeRaw("@")
		writeNode(directive.nameNode)
		writeArguments(directive.arguments)
	}


	private fun GWriter.writeNode(definition: GDirectiveDefinition) {
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


	private fun GWriter.writeNode(document: GDocument) {
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


	private fun GWriter.writeNode(type: GEnumType) {
		writeNode(type.descriptionNode)
		writeRaw("enum ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(type.values)
	}


	private fun GWriter.writeNode(extension: GEnumTypeExtension) {
		writeRaw("extend enum ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(extension.values)
	}


	private fun GWriter.writeNode(value: GEnumValue) {
		writeRaw(value.name)
	}


	private fun GWriter.writeNode(definition: GEnumValueDefinition) {
		writeNode(definition.descriptionNode)
		writeNode(definition.nameNode)
		writeDirectives(definition.directives)
	}


	private fun GWriter.writeNode(definition: GFieldDefinition) {
		writeNode(definition.descriptionNode)
		writeNode(definition.nameNode)
		writeArgumentDefinitions(definition.argumentDefinitions)
		writeRaw(": ")
		writeNode(definition.type)
		writeDirectives(definition.directives)
	}


	private fun GWriter.writeNode(selection: GFieldSelection) {
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


	private fun GWriter.writeNode(value: GFloatValue) {
		writeRaw(value.value.toString())
	}


	private fun GWriter.writeNode(definition: GFragmentDefinition) {
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


	private fun GWriter.writeNode(selection: GFragmentSelection) {
		writeRaw("...")
		writeNode(selection.nameNode)
		writeDirectives(selection.directives)
	}


	private fun GWriter.writeNode(type: GInputObjectType) {
		writeNode(type.descriptionNode)
		writeRaw("input ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writeInputObjectTypeArguments(type.argumentDefinitions)
	}


	private fun GWriter.writeNode(extension: GInputObjectTypeExtension) {
		writeRaw("extend input ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writeInputObjectTypeArguments(extension.argumentDefinitions)
	}


	private fun GWriter.writeNode(selection: GInlineFragmentSelection) {
		writeRaw("...")
		selection.typeCondition?.let { typeCondition ->
			writeRaw(" on ")
			writeNode(typeCondition)
		}
		writeDirectives(selection.directives)
		writeRaw(" ")
		writeNode(selection.selectionSet)
	}


	private fun GWriter.writeNode(value: GIntValue) {
		writeRaw(value.value.toString())
	}


	private fun GWriter.writeNode(type: GInterfaceType) {
		writeNode(type.descriptionNode)
		writeRaw("interface ")
		writeNode(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeDirectives(type.directives)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	private fun GWriter.writeNode(extension: GInterfaceTypeExtension) {
		writeRaw("extend interface ")
		writeNode(extension.nameNode)
		writeImplementedInterfaces(extension.interfaces)
		writeDirectives(extension.directives)
		writeFieldDefinitions(extension.fieldDefinitions)
	}


	private fun GWriter.writeNode(value: GListValue) {
		writeRaw("[")
		value.elements.forEachIndexed { index, element ->
			if (index > 0)
				writeRaw(", ")

			writeNode(element)
		}
		writeRaw("]")
	}


	private fun GWriter.writeNode(ref: GListTypeRef) {
		writeRaw("[")
		writeNode(ref.elementType)
		writeRaw("]")
	}


	private fun GWriter.writeNode(name: GName) {
		writeRaw(name.value)
	}


	private fun GWriter.writeNode(ref: GNamedTypeRef) {
		writeNode(ref.nameNode)
	}


	private fun GWriter.writeNode(ref: GNonNullTypeRef) {
		writeNode(ref.nullableRef)
		writeRaw("!")
	}


	private fun GWriter.writeNode(value: GNullValue) {
		writeRaw("null")
	}


	private fun GWriter.writeNode(type: GObjectType) {
		writeNode(type.descriptionNode)
		writeRaw("type ")
		writeNode(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	private fun GWriter.writeNode(extension: GObjectTypeExtension) {
		writeRaw("extend type ")
		writeNode(extension.nameNode)
		writeImplementedInterfaces(extension.interfaces)
		writeFieldDefinitions(extension.fieldDefinitions)
	}


	private fun GWriter.writeNode(value: GObjectValue) {
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


	private fun GWriter.writeNode(field: GObjectValueField) {
		writeNode(field.nameNode)
		writeRaw(": ")
		writeNode(field.value)
	}


	private fun GWriter.writeNode(definition: GOperationDefinition) {
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


	private fun GWriter.writeNode(definition: GOperationTypeDefinition) {
		writeRaw(definition.operationType.name)
		writeRaw(": ")
		writeNode(definition.type)
	}


	private fun GWriter.writeNode(type: GScalarType) {
		writeNode(type.descriptionNode)
		writeRaw("scalar ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
	}


	private fun GWriter.writeNode(extension: GScalarTypeExtension) {
		writeRaw("extend scalar ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
	}


	private fun GWriter.writeNode(definition: GSchemaDefinition) {
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


	private fun GWriter.writeNode(definition: GSchemaExtension) {
		writeRaw("extend schema")
		writeDirectives(definition.directives)
		writeOperationTypeDefinitions(definition.operationTypeDefinitions)
	}


	private fun GWriter.writeNode(set: GSelectionSet) {
		writeBlock {
			set.selections.forEach { selection ->
				writeNode(selection)
				writeLinebreak()
			}
		}
	}


	// FIXME proper escaping logic, line wrapping, indentation
	private fun GWriter.writeNode(value: GStringValue) {
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


	private fun GWriter.writeNode(type: GUnionType) {
		writeNode(type.descriptionNode)
		writeRaw("union ")
		writeNode(type.nameNode)
		writeDirectives(type.directives)
		writePossibleTypes(type.possibleTypes)
	}


	private fun GWriter.writeNode(extension: GUnionTypeExtension) {
		writeRaw("union ")
		writeNode(extension.nameNode)
		writeDirectives(extension.directives)
		writePossibleTypes(extension.possibleTypes)
	}


	private fun GWriter.writeNode(definition: GVariableDefinition) {
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


	private fun GWriter.writeNode(ref: GVariableRef) {
		writeRaw("$")
		writeNode(ref.nameNode)
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
