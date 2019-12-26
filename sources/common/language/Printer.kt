package io.fluidsonic.graphql


internal object Printer {

	fun print(ast: GAst, indent: String = "\t"): String {
		val writer = GWriter(indent = indent)
		ast.accept(Visitor(writer))

		return writer.toString()
	}


	private class Visitor(
		private val writer: GWriter
	) : GAstVoidVisitor() {

		override fun visitArgument(argument: GArgument) = writer {
			writeAst(argument.nameNode)
			writeRaw(": ")
			writeAst(argument.value)
		}


		override fun visitArgumentDefinition(definition: GArgumentDefinition) = writer {
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


		override fun visitBooleanValue(value: GValue.Boolean) = writer {
			writeRaw(if (value.value) "true" else "false")
		}


		override fun visitDirective(directive: GDirective) = writer {
			writeRaw("@")
			writeAst(directive.nameNode)
			writeArguments(directive.arguments)
		}


		override fun visitDirectiveDefinition(definition: GDirectiveDefinition) = writer {
			writeAst(definition.descriptionNode)
			writeRaw("directive @")
			writeAst(definition.nameNode)
			writeArgumentDefinitions(definition.arguments)
			writeRaw(" on ")
			definition.locationNodes.forEachIndexed { index, location ->
				if (index > 0)
					writeRaw(" | ")

				writeAst(location)
			}
		}


		override fun visitDocument(document: GDocument) = writer {
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


		override fun visitEnumType(type: GEnumType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("enum ")
			writeAst(type.nameNode)
			writeDirectives(type.directives)
			writeRaw(" ")
			writeEnumValueDefinitions(type.values)
		}


		override fun visitEnumTypeExtension(extension: GEnumTypeExtension) = writer {
			writeRaw("extend enum ")
			writeAst(extension.nameNode)
			writeDirectives(extension.directives)
			writeRaw(" ")
			writeEnumValueDefinitions(extension.values)
		}


		override fun visitEnumValue(value: GValue.Enum) = writer {
			writeRaw(value.name)
		}


		override fun visitEnumValueDefinition(definition: GEnumValueDefinition) = writer {
			writeAst(definition.descriptionNode)
			writeAst(definition.nameNode)
			writeDirectives(definition.directives)
		}


		override fun visitFieldDefinition(definition: GFieldDefinition) = writer {
			writeAst(definition.descriptionNode)
			writeAst(definition.nameNode)
			writeArgumentDefinitions(definition.arguments)
			writeRaw(": ")
			writeAst(definition.type)
			writeDirectives(definition.directives)
		}


		override fun visitFieldSelection(selection: GFieldSelection) = writer {
			selection.aliasNode?.let { alias ->
				writeAst(alias)
				writeRaw(": ")
			}
			writeAst(selection.nameNode)
			writeArguments(selection.arguments)
			writeDirectives(selection.directives)
			selection.selectionSet?.let { selectionSet ->
				writeRaw(" ")
				writeAst(selection.selectionSet)
			}
		}


		override fun visitFloatValue(value: GValue.Float) = writer {
			writeRaw(value.value)
		}


		override fun visitFragmentDefinition(definition: GFragmentDefinition) = writer {
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


		override fun visitFragmentSelection(selection: GFragmentSelection) = writer {
			writeRaw("...")
			writeAst(selection.nameNode)
			writeDirectives(selection.directives)
		}


		override fun visitInputObjectType(type: GInputObjectType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("input ")
			writeAst(type.nameNode)
			writeDirectives(type.directives)
			writeInputObjectTypeArguments(type.arguments)
		}


		override fun visitInputObjectTypeExtension(extension: GInputObjectTypeExtension) = writer {
			writeRaw("extend input ")
			writeAst(extension.nameNode)
			writeDirectives(extension.directives)
			writeInputObjectTypeArguments(extension.arguments)
		}


		override fun visitInlineFragmentSelection(selection: GInlineFragmentSelection) = writer {
			writeRaw("...")
			selection.typeCondition?.let { typeCondition ->
				writeRaw(" on")
				writeAst(typeCondition)
			}
			writeDirectives(selection.directives)
			writeRaw(" ")
			writeAst(selection.selectionSet)
		}


		override fun visitIntValue(value: GValue.Int) = writer {
			writeRaw(value.value)
		}


		override fun visitInterfaceType(type: GInterfaceType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("interface ")
			writeAst(type.nameNode)
			writeImplementedInterfaces(type.interfaces)
			writeDirectives(type.directives)
			writeFieldDefinitions(type.fields)
		}


		override fun visitInterfaceTypeExtension(extension: GInterfaceTypeExtension) = writer {
			writeRaw("extend interface ")
			writeAst(extension.nameNode)
			writeImplementedInterfaces(extension.interfaces)
			writeDirectives(extension.directives)
			writeFieldDefinitions(extension.fields)
		}


		override fun visitListValue(value: GValue.List) = writer {
			writeRaw("[")
			value.elements.forEachIndexed { index, element ->
				if (index > 0)
					writeRaw(", ")

				writeAst(element)
			}
			writeRaw("]")
		}


		override fun visitListTypeRef(ref: GListTypeRef) = writer {
			writeRaw("[")
			writeAst(ref.elementType)
			writeRaw("]")
		}


		override fun visitName(name: GName) = writer {
			writeRaw(name.value)
		}


		override fun visitNamedTypeRef(ref: GNamedTypeRef) = writer {
			writeAst(ref.nameNode)
		}


		override fun visitNonNullTypeRef(ref: GNonNullTypeRef) = writer {
			writeAst(ref.nullableType)
			writeRaw("!")
		}


		override fun visitNullValue(value: GValue.Null) = writer {
			writeRaw("null")
		}


		override fun visitObjectType(type: GObjectType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("type ")
			writeAst(type.nameNode)
			writeImplementedInterfaces(type.interfaces)
			writeFieldDefinitions(type.fields)
		}


		override fun visitObjectTypeExtension(extension: GObjectTypeExtension) = writer {
			writeRaw("extend type ")
			writeAst(extension.nameNode)
			writeImplementedInterfaces(extension.interfaces)
			writeFieldDefinitions(extension.fields)
		}


		override fun visitObjectValue(value: GValue.Object) = writer {
			if (value.fields.isNotEmpty())
				writeBlock {
					value.fields.forEachIndexed { index, field ->
						if (index > 0)
							writeRaw(",")

						writeAst(field)
						writeLinebreak()
					}

				}
			else
				writeRaw("{}")
		}


		override fun visitObjectValueField(field: GObjectValueField) = writer {
			writeAst(field.nameNode)
			writeRaw(": ")
			writeAst(field.value)
		}


		override fun visitOperationDefinition(definition: GOperationDefinition) = writer {
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


		override fun visitOperationTypeDefinition(definition: GOperationTypeDefinition) = writer {
			writeRaw(definition.operation.name)
			writeRaw(": ")
			writeAst(definition.type)
		}


		override fun visitScalarType(type: GScalarType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("scalar ")
			writeAst(type.nameNode)
			writeDirectives(type.directives)
		}


		override fun visitScalarTypeExtension(extension: GScalarTypeExtension) = writer {
			writeRaw("extend scalar ")
			writeAst(extension.nameNode)
			writeDirectives(extension.directives)
		}


		override fun visitSchemaDefinition(definition: GSchemaDefinition) = writer {
			val queryOperation = definition.operationTypes.firstOrNull { it.operation == GOperationType.query }
				?.takeIf { it.type.name != GSpecification.defaultQueryTypeName }

			val mutationOperation = definition.operationTypes.firstOrNull { it.operation == GOperationType.mutation }
				?.takeIf { it.type.name != GSpecification.defaultMutationTypeName }

			val subscriptionOperation = definition.operationTypes.firstOrNull { it.operation == GOperationType.subscription }
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


		override fun visitSchemaExtensionDefinition(definition: GSchemaExtensionDefinition) = writer {
			writeRaw("extend schema")
			writeDirectives(definition.directives)
			writeOperationTypeDefinitions(definition.operationTypes)
		}


		override fun visitSelectionSet(set: GSelectionSet) = writer {
			writeBlock {
				set.selections.forEach { selection ->
					writeAst(selection)
					writeLinebreak()
				}
			}
		}


		// FIXME escaping, line wrapping, indentation
		override fun visitStringValue(value: GValue.String) = writer {
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


		override fun visitSyntheticNode(node: GAst) {
			error("Cannot print AST of ${node::class}")
		}


		override fun visitUnionType(type: GUnionType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("union ")
			writeAst(type.nameNode)
			writeDirectives(type.directives)
			writePossibleTypes(type.possibleTypes)
		}


		override fun visitUnionTypeExtension(extension: GUnionTypeExtension) = writer {
			writeRaw("union ")
			writeAst(extension.nameNode)
			writeDirectives(extension.directives)
			writePossibleTypes(extension.possibleTypes)
		}


		override fun visitVariableDefinition(definition: GVariableDefinition) = writer {
			writeAst(definition.variable)
			writeRaw(": ")
			writeAst(definition.type)
			definition.defaultValue?.let { defaultValue ->
				writeRaw(" = ")
				writeAst(defaultValue)
			}
			writeDirectives(definition.directives)
		}


		override fun visitVariableValue(value: GValue.Variable) = writer {
			writeRaw("$")
			writeAst(value.nameNode)
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
}
