package io.fluidsonic.graphql

import io.fluidsonic.graphql.GAst.*


internal object Printer {

	fun print(ast: GAst, indent: String = "\t"): String {
		val writer = GWriter(indent = indent)
		ast.accept(Visitor(writer))

		return writer.toString()
	}


	private class Visitor(
		private val writer: GWriter
	) : GAstVoidVisitor() {

		override fun visitArgument(argument: Argument) = writer {
			writeAst(argument.name)
			writeRaw(": ")
			writeAst(argument.value)
		}


		override fun visitArgumentDefinition(definition: ArgumentDefinition) = writer {
			writeAst(definition.description)
			writeAst(definition.name)
			writeRaw(": ")
			writeAst(definition.type)

			definition.defaultValue?.let { defaultValue ->
				writeRaw(" = ")
				writeAst(defaultValue)
			}

			writeDirectives(definition.directives)
		}


		override fun visitBooleanValue(value: Value.Boolean) = writer {
			writeRaw(if (value.value) "true" else "false")
		}


		override fun visitDirective(directive: Directive) = writer {
			writeRaw("@")
			writeAst(directive.name)
			writeArguments(directive.arguments)
		}


		override fun visitDirectiveDefinition(definition: Definition.TypeSystem.Directive) = writer {
			writeAst(definition.description)
			writeRaw("directive @")
			writeAst(definition.name)
			writeArgumentDefinitions(definition.arguments)
			writeRaw(" on ")
			definition.locations.forEachIndexed { index, location ->
				if (index > 0)
					writeRaw(" | ")

				writeAst(location)
			}
		}


		override fun visitDocument(document: Document) = writer {
			document.definitions.forEachIndexed { index, definition ->
				if (index > 0)
					writeLinebreak()

				writeAst(definition)
			}
		}


		override fun visitEnumTypeDefinition(definition: Definition.TypeSystem.Type.Enum) = writer {
			writeAst(definition.description)
			writeRaw("enum ")
			writeAst(definition.name)
			writeRaw(" ")
			writeEnumValueDefinitions(definition.values)
			writeLinebreak()
		}


		override fun visitEnumValue(value: Value.Enum) = writer {
			writeRaw(value.name)
		}


		override fun visitEnumValueDefinition(definition: EnumValueDefinition) = writer {
			writeAst(definition.description)
			writeAst(definition.name)
			writeDirectives(definition.directives)
		}


		override fun visitFieldDefinition(definition: FieldDefinition) = writer {
			writeAst(definition.description)
			writeAst(definition.name)
			writeArgumentDefinitions(definition.arguments)
			writeRaw(": ")
			writeAst(definition.type)
			writeDirectives(definition.directives)
		}


		override fun visitFieldSelection(selection: Selection.Field) = writer {
			selection.alias?.let { alias ->
				writeAst(alias)
				writeRaw(": ")
			}
			writeAst(selection.name)
			writeArguments(selection.arguments)
			writeDirectives(selection.directives)
			writeAst(selection.selectionSet)
			writeLinebreak()
		}


		override fun visitFloatValue(value: Value.Float) = writer {
			writeRaw(value.value)
		}


		override fun visitFragmentSelection(selection: Selection.Fragment) = writer {
			writeRaw("...")
			writeAst(selection.name)
			writeDirectives(selection.directives)
		}


		override fun visitInputObjectTypeDefinition(definition: Definition.TypeSystem.Type.InputObject) = writer {
			writeAst(definition.description)
			writeRaw("input ")
			writeAst(definition.name)
			writeRaw(" ")
			writeInputObjectTypeArguments(definition.arguments)
			writeLinebreak()
		}


		override fun visitInlineFragmentSelection(selection: Selection.InlineFragment) = writer {
			writeRaw("...")
			selection.typeCondition?.let { typeCondition ->
				writeRaw(" on")
				writeAst(typeCondition)
			}
			writeDirectives(selection.directives)
			writeRaw(" ")
			writeAst(selection.selectionSet)
		}


		override fun visitIntValue(value: Value.Int) = writer {
			writeRaw(value.value)
		}


		override fun visitInterfaceTypeDefinition(definition: Definition.TypeSystem.Type.Interface) = writer {
			writeAst(definition.description)
			writeRaw("interface ")
			writeAst(definition.name)
			writeRaw(" ")
			writeFieldDefinitions(definition.fields)
			writeLinebreak()
		}


		override fun visitListValue(value: Value.List) = writer {
			writeRaw("[")
			value.elements.forEachIndexed { index, element ->
				if (index > 0)
					writeRaw(", ")

				writeAst(element)
			}
			writeRaw("]")
		}


		override fun visitListTypeReference(reference: TypeReference.List) = writer {
			writeRaw("[")
			writeAst(reference.elementType)
			writeRaw("]")
		}


		override fun visitName(name: Name) = writer {
			writeRaw(name.value)
		}


		override fun visitNamedTypeReference(reference: TypeReference.Named) = writer {
			writeAst(reference.name)
		}


		override fun visitNonNullTypeReference(reference: TypeReference.NonNull) = writer {
			writeAst(reference.nullableType)
			writeRaw("!")
		}


		override fun visitNullValue(value: Value.Null) = writer {
			writeRaw("null")
		}


		override fun visitObjectTypeDefinition(definition: Definition.TypeSystem.Type.Object) = writer {
			writeAst(definition.description)
			writeRaw("type ")
			writeAst(definition.name)
			writeImplementedInterfaces(definition.interfaces)
			writeRaw(" ")
			writeFieldDefinitions(definition.fields)
			writeLinebreak()
		}


		override fun visitOperationTypeDefinition(definition: OperationTypeDefinition) = writer {
			writeRaw(definition.operation.name)
			writeRaw(": ")
			writeAst(definition.type)
		}


		override fun visitScalarTypeDefinition(definition: Definition.TypeSystem.Type.Scalar) = writer {
			writeAst(definition.description)
			writeRaw("scalar ")
			writeAst(definition.name)
			writeLinebreak()
		}


		// FIXME directives - everywhere
		override fun visitSchemaDefinition(definition: Definition.TypeSystem.Schema) = writer {
			val queryOperation = definition.operationTypes.firstOrNull { it.operation == GOperationType.query }
				?.takeIf { it.type.name.value != GSpecification.defaultQueryTypeName }

			val mutationOperation = definition.operationTypes.firstOrNull { it.operation == GOperationType.mutation }
				?.takeIf { it.type.name.value != GSpecification.defaultMutationTypeName }

			val subscriptionOperation = definition.operationTypes.firstOrNull { it.operation == GOperationType.subscription }
				?.takeIf { it.type.name.value != GSpecification.defaultSubscriptionTypeName }

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


		override fun visitVariableValue(value: Value.Variable) = writer {
			writeRaw("$")
			writeAst(value.name)
		}


		override fun visitSelectionSet(set: SelectionSet) = writer {
			writeBlock {
				set.selections.forEach { selection ->
					writeAst(selection)
					writeLinebreak()
				}
			}
			writeLinebreak()
		}


		// FIXME escaping, line wrapping, indentation
		override fun visitStringValue(value: Value.String) = writer {
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


		override fun visitUnionTypeDefinition(definition: Definition.TypeSystem.Type.Union) = writer {
			writeAst(definition.description)
			writeRaw("union ")
			writeAst(definition.name)
			writeRaw(" = ")
			definition.types.forEachIndexed { index, type ->
				if (index > 0)
					writeRaw(" | ")

				writeAst(type.name)
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


		private fun GWriter.writeArguments(arguments: Collection<Argument>) {
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


		private fun GWriter.writeArgumentDefinitions(definitions: List<ArgumentDefinition>) {
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


		private fun GWriter.writeDirectives(directives: List<Directive>) {
			if (directives.isEmpty())
				return

			directives.forEach { directive ->
				writeRaw(" ")
				writeAst(directive)
			}
		}


		private fun GWriter.writeEnumValueDefinitions(definitions: List<EnumValueDefinition>) {
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


		private fun GWriter.writeFieldDefinitions(definitions: List<FieldDefinition>) {
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


		private fun GWriter.writeImplementedInterfaces(interfaces: List<TypeReference.Named>) {
			if (interfaces.isEmpty())
				return

			writeRaw(" implements ")
			interfaces.forEachIndexed { index, type ->
				if (index > 0)
					writeRaw(" & ")

				writeAst(type)
			}
		}


		private fun GWriter.writeInputObjectTypeArguments(definitions: List<ArgumentDefinition>) {
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
