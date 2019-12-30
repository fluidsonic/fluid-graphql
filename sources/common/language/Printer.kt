package io.fluidsonic.graphql


internal object Printer {

	fun print(ast: GAst, indent: String = "\t"): String {
		val writer = GWriter(indent = indent)
		ast.accept(PrintVisitor(writer))

		return writer.toString()
	}
}


private class PrintVisitor(
	private val writer: GWriter
) : Visitor.Typed.WithoutData<Unit>() {

	override fun onArgument(argument: GArgument, visit: Visit) = writer {
		writeAst(argument.nameNode)
		writeRaw(": ")
		writeAst(argument.value)
	}


	override fun onArgumentDefinition(definition: GArgumentDefinition, visit: Visit) = writer {
		writeAst(definition.descriptionNode)
		writeAst(definition.nameNode)
		writeRaw(": ")
		writeAst(definition.type)

		definition.defaultValue?.let { defaultValue ->
			writeRaw(" = ")
			writeAst(defaultValue)
		}

		writeDirectives(definition.directives)
	}


	override fun onBooleanValue(value: GBooleanValue, visit: Visit) = writer {
		writeRaw(if (value.value) "true" else "false")
	}


	override fun onDirective(directive: GDirective, visit: Visit) = writer {
		writeRaw("@")
		writeAst(directive.nameNode)
		writeArguments(directive.arguments)
	}


	override fun onDirectiveDefinition(definition: GDirectiveDefinition, visit: Visit) = writer {
		writeAst(definition.descriptionNode)
		writeRaw("directive @")
		writeAst(definition.nameNode)
		writeArgumentDefinitions(definition.argumentDefinitions)
		writeRaw(" on ")
		definition.locationNodes.forEachIndexed { index, location ->
			if (index > 0)
				writeRaw(" | ")

			writeAst(location)
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

			writeAst(definition)
		}
	}


	override fun onEnumType(type: GEnumType, visit: Visit) = writer {
		writeAst(type.descriptionNode)
		writeRaw("enum ")
		writeAst(type.nameNode)
		writeDirectives(type.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(type.values)
	}


	override fun onEnumTypeExtension(extension: GEnumTypeExtension, visit: Visit) = writer {
		writeRaw("extend enum ")
		writeAst(extension.nameNode)
		writeDirectives(extension.directives)
		writeRaw(" ")
		writeEnumValueDefinitions(extension.values)
	}


	override fun onEnumValue(value: GEnumValue, visit: Visit) = writer {
		writeRaw(value.name)
	}


	override fun onEnumValueDefinition(definition: GEnumValueDefinition, visit: Visit) = writer {
		writeAst(definition.descriptionNode)
		writeAst(definition.nameNode)
		writeDirectives(definition.directives)
	}


	override fun onFieldDefinition(definition: GFieldDefinition, visit: Visit) = writer {
		writeAst(definition.descriptionNode)
		writeAst(definition.nameNode)
		writeArgumentDefinitions(definition.argumentDefinitions)
		writeRaw(": ")
		writeAst(definition.type)
		writeDirectives(definition.directives)
	}


	override fun onFieldSelection(selection: GFieldSelection, visit: Visit) = writer {
		selection.aliasNode?.let { alias ->
			writeAst(alias)
			writeRaw(": ")
		}
		writeAst(selection.nameNode)
		writeArguments(selection.arguments)
		writeDirectives(selection.directives)
		selection.selectionSet?.let { set ->
			writeRaw(" ")
			writeAst(set)
		}
	}


	override fun onFloatValue(value: GFloatValue, visit: Visit) = writer {
		writeRaw(value.value.toString())
	}


	override fun onFragmentDefinition(definition: GFragmentDefinition, visit: Visit) = writer {
		writeRaw("fragment ")
		writeAst(definition.nameNode)
		writeVariableDefinitions(definition.variableDefinitions)
		writeRaw(" on ")
		writeAst(definition.typeCondition)
		writeDirectives(definition.directives)
		writeRaw(" ")
		writeAst(definition.selectionSet)
		writeLinebreak()
	}


	override fun onFragmentSelection(selection: GFragmentSelection, visit: Visit) = writer {
		writeRaw("...")
		writeAst(selection.nameNode)
		writeDirectives(selection.directives)
	}


	override fun onInputObjectType(type: GInputObjectType, visit: Visit) = writer {
		writeAst(type.descriptionNode)
		writeRaw("input ")
		writeAst(type.nameNode)
		writeDirectives(type.directives)
		writeInputObjectTypeArguments(type.argumentDefinitions)
	}


	override fun onInputObjectTypeExtension(extension: GInputObjectTypeExtension, visit: Visit) = writer {
		writeRaw("extend input ")
		writeAst(extension.nameNode)
		writeDirectives(extension.directives)
		writeInputObjectTypeArguments(extension.argumentDefinitions)
	}


	override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, visit: Visit) = writer {
		writeRaw("...")
		selection.typeCondition?.let { typeCondition ->
			writeRaw(" on")
			writeAst(typeCondition)
		}
		writeDirectives(selection.directives)
		writeRaw(" ")
		writeAst(selection.selectionSet)
	}


	override fun onIntValue(value: GIntValue, visit: Visit) = writer {
		writeRaw(value.value.toString())
	}


	override fun onInterfaceType(type: GInterfaceType, visit: Visit) = writer {
		writeAst(type.descriptionNode)
		writeRaw("interface ")
		writeAst(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeDirectives(type.directives)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	override fun onInterfaceTypeExtension(extension: GInterfaceTypeExtension, visit: Visit) = writer {
		writeRaw("extend interface ")
		writeAst(extension.nameNode)
		writeImplementedInterfaces(extension.interfaces)
		writeDirectives(extension.directives)
		writeFieldDefinitions(extension.fieldDefinitions)
	}


	override fun onListValue(value: GListValue, visit: Visit) = writer {
		writeRaw("[")
		value.elements.forEachIndexed { index, element ->
			if (index > 0)
				writeRaw(", ")

			writeAst(element)
		}
		writeRaw("]")
	}


	override fun onListTypeRef(ref: GListTypeRef, visit: Visit) = writer {
		writeRaw("[")
		writeAst(ref.elementType)
		writeRaw("]")
	}


	override fun onName(name: GName, visit: Visit) = writer {
		writeRaw(name.value)
	}


	override fun onNamedTypeRef(ref: GNamedTypeRef, visit: Visit) = writer {
		writeAst(ref.nameNode)
	}


	override fun onNonNullTypeRef(ref: GNonNullTypeRef, visit: Visit) = writer {
		writeAst(ref.nullableRef)
		writeRaw("!")
	}


	override fun onNullValue(value: GNullValue, visit: Visit) = writer {
		writeRaw("null")
	}


	override fun onObjectType(type: GObjectType, visit: Visit) = writer {
		writeAst(type.descriptionNode)
		writeRaw("type ")
		writeAst(type.nameNode)
		writeImplementedInterfaces(type.interfaces)
		writeFieldDefinitions(type.fieldDefinitions)
	}


	override fun onObjectTypeExtension(extension: GObjectTypeExtension, visit: Visit) = writer {
		writeRaw("extend type ")
		writeAst(extension.nameNode)
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

					writeAst(field)
				}

				writeLinebreak()
			}
		else
			writeRaw("{}")
	}


	override fun onObjectValueField(field: GObjectValueField, visit: Visit) = writer {
		writeAst(field.nameNode)
		writeRaw(": ")
		writeAst(field.value)
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
				writeAst(name)
			}

			if (definition.variableDefinitions.isNotEmpty()) {
				if (definition.nameNode === null)
					writeRaw(" ")

				writeVariableDefinitions(definition.variableDefinitions)
			}

			writeDirectives(definition.directives)
			writeRaw(" ")
		}

		writeAst(definition.selectionSet)
		writeLinebreak()
	}


	override fun onOperationTypeDefinition(definition: GOperationTypeDefinition, visit: Visit) = writer {
		writeRaw(definition.operationType.name)
		writeRaw(": ")
		writeAst(definition.type)
	}


	override fun onScalarType(type: GScalarType, visit: Visit) = writer {
		writeAst(type.descriptionNode)
		writeRaw("scalar ")
		writeAst(type.nameNode)
		writeDirectives(type.directives)
	}


	override fun onScalarTypeExtension(extension: GScalarTypeExtension, visit: Visit) = writer {
		writeRaw("extend scalar ")
		writeAst(extension.nameNode)
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
				writeAst(it)
			}

			mutationOperation?.let {
				writeLinebreak()
				writeAst(it)
			}

			subscriptionOperation?.let {
				writeLinebreak()
				writeAst(it)
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
				writeAst(selection)
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


	override fun onSyntheticNode(node: GAst, visit: Visit) {
		error("Cannot print AST of ${node::class}")
	}


	override fun onUnionType(type: GUnionType, visit: Visit) = writer {
		writeAst(type.descriptionNode)
		writeRaw("union ")
		writeAst(type.nameNode)
		writeDirectives(type.directives)
		writePossibleTypes(type.possibleTypes)
	}


	override fun onUnionTypeExtension(extension: GUnionTypeExtension, visit: Visit) = writer {
		writeRaw("union ")
		writeAst(extension.nameNode)
		writeDirectives(extension.directives)
		writePossibleTypes(extension.possibleTypes)
	}


	override fun onVariableDefinition(definition: GVariableDefinition, visit: Visit) = writer {
		writeRaw("$")
		writeAst(definition.nameNode)
		writeRaw(": ")
		writeAst(definition.type)
		definition.defaultValue?.let { defaultValue ->
			writeRaw(" = ")
			writeAst(defaultValue)
		}
		writeDirectives(definition.directives)
	}


	override fun onVariableRef(ref: GVariableRef, visit: Visit) = writer {
		writeRaw("$")
		writeAst(ref.nameNode)
	}


	private fun writeAst(ast: GAst?) {
		ast?.accept(this)
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

			writeAst(argument)
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

				writeAst(definition)
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
				writeAst(definition)
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
			writeAst(directive)
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

				writeAst(definition)
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

				writeAst(definition)
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

			writeAst(type)
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

				writeAst(definition)
				writeLinebreak()
			}
		}
	}


	private fun GWriter.writeOperationTypeDefinitions(definitions: Collection<GOperationTypeDefinition>) {
		if (definitions.isEmpty())
			return

		writeBlock {
			definitions.forEach { definition ->
				writeAst(definition)
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

			writeAst(type.nameNode)
		}
	}


	private fun GWriter.writeVariableDefinitions(definitions: Collection<GVariableDefinition>) {
		if (definitions.isEmpty())
			return

		writeRaw("(")
		definitions.forEachIndexed { index, definition ->
			if (index > 0)
				writeRaw(", ")

			writeAst(definition)
		}
		writeRaw(")")
	}
}
