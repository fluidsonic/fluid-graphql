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
			document.definitions.forEachIndexed { index, definition ->
				if (index > 0)
					writeLinebreak()

				writeAst(definition)
			}
		}


		override fun visitEnumType(type: GEnumType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("enum ")
			writeAst(type.nameNode)
			writeRaw(" ")
			writeEnumValueDefinitions(type.values)
			writeLinebreak()
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
			writeAst(selection.selectionSet)
			writeLinebreak()
		}


		override fun visitFloatValue(value: GValue.Float) = writer {
			writeRaw(value.value)
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
			writeRaw(" ")
			writeInputObjectTypeArguments(type.arguments)
			writeLinebreak()
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
			writeRaw(" ")
			writeFieldDefinitions(type.fields)
			writeLinebreak()
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
			writeRaw(" ")
			writeFieldDefinitions(type.fields)
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
			writeLinebreak()
		}


		// FIXME directives - everywhere
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
			writeLinebreak()
		}


		override fun visitVariableValue(value: GValue.Variable) = writer {
			writeRaw("$")
			writeAst(value.nameNode)
		}


		override fun visitSelectionSet(set: GSelectionSet) = writer {
			writeBlock {
				set.selections.forEach { selection ->
					writeAst(selection)
					writeLinebreak()
				}
			}
			writeLinebreak()
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


		override fun visitUnionType(type: GUnionType) = writer {
			writeAst(type.descriptionNode)
			writeRaw("union ")
			writeAst(type.nameNode)
			writeRaw(" = ")
			type.possibleTypes.forEachIndexed { index, type ->
				if (index > 0)
					writeRaw(" | ")

				writeAst(type.nameNode)
			}
			writeLinebreak()
		}


		override fun visitNode(node: GAst) =
			error("Cannot print node: ${node::class}")


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
	}
}
