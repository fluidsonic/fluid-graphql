package io.fluidsonic.graphql


class GSchemaPrinter {

	private val writer = IndentableWriter()


	private inline fun <R> indented(block: () -> R) =
		writer.indented(block)


	private fun print(string: String) =
		writer.write(string)


	fun print(schema: GSchema): String {
		printHeader(schema)

		if (schema.directives.isNotEmpty()) {
			if (writer.isNotEmpty())
				print("\n")

			printDirectiveDefinitions(schema.directives.sortedBy { it.name })
		}

		if (schema.types.isNotEmpty()) {
			if (writer.isNotEmpty())
				print("\n")

			printTypes(schema.types.sortedBy { it.name })
		}

		return writer.toString()
			.also { writer.clear() }
	}


	private fun printArgument(argument: GArgument) {
		print(argument.name)
		print(": ")
		printValue(argument.value)
	}


	private fun printArguments(arguments: Collection<GArgument>) {
		if (arguments.isEmpty())
			return

		print("(")
		arguments.forEachIndexed { index, argument ->
			if (index > 0)
				print(", ")

			printArgument(argument)
		}
		print(")")
	}


	private inline fun <R> printBlock(block: () -> R): R {
		print("{\n")
		val result = indented(block)
		print("}")

		return result
	}


	// FIXME escaping, line wrapping, indentation
	private fun printBlockString(string: String) {
		print("\"\"\"")
		print(string)
		print("\"\"\"\n")
	}


	private fun printDescription(description: String?) {
		if (description == null)
			return

		return printBlockString(description)
	}


	private fun printDirective(directive: GDirective) {
		print("@")
		print(directive.name)
		printArguments(directive.arguments)
	}


	private fun printDirectiveDefinition(definition: GDirectiveDefinition) {
		printDescription(definition.description)
		print("directive @")
		print(definition.name)
		printParameters(definition.arguments)
		print(" on ")
		definition.locations
			.sortedBy { it.name }
			.forEachIndexed { index, location ->
				if (index > 0)
					print(" | ")

				print(location.name)
			}
		print("\n")
	}


	private fun printDirectiveDefinitions(definitions: Collection<GDirectiveDefinition>) {
		definitions.forEach(this::printDirectiveDefinition)
	}


	private fun printDirectives(directives: Collection<GDirective>) {
		if (directives.isEmpty())
			return

		print(" ")
		directives.forEachIndexed { index, directive ->
			if (index > 0)
				print(" ")

			printDirective(directive)
		}
	}


	private fun printEnum(type: GEnumType) {
		printDescription(type.description)
		print("enum ")
		print(type.name)
		printBlock {
			type.enumValues(includeDeprecated = true).forEach { value ->
				printDescription(value.description)
				print(value.name)
				printDirectives(value.directives)
				print("\n")
			}
		}
		print("\n")
	}


	private fun printField(field: GFieldDefinition) {
		printDescription(field.description)
		print(field.name)
		printParameters(field.args)
		print(": ")
		printTypeName(field.type)
		printDirectives(field.directives)
		print("\n")
	}


	private fun printFields(fields: List<GFieldDefinition>) {
		fields.forEach(this::printField)
	}


	private fun printHeader(schema: GSchema) {
		if (schema.usesDefaultOperationNames)
			return

		print("schema ")
		printBlock {
			schema.queryType
				.takeIf { it.name != GSpecification.defaultQueryTypeName }
				?.let {
					print("\nquery: ")
					print(schema.queryType.name)
				}

			schema.mutationType
				?.takeIf { it.name != GSpecification.defaultMutationTypeName }
				?.let {
					print("\nmutation: ")
					print(it.name)
				}

			schema.subscriptionType
				?.takeIf { it.name != GSpecification.defaultSubscriptionTypeName }
				?.let {
					print("\nsubscription: ")
					print(it.name)
				}
		}
		print("\n")
	}


	private fun printImplementedInterfaces(interfaces: Collection<GInterfaceType>) {
		if (interfaces.isEmpty())
			return

		print(" implements ")
		interfaces.forEachIndexed { index, it ->
			if (index > 0)
				print(" & ")

			print(it.name)
		}
	}


	private fun printInputObject(type: GInputObjectType) {
		printDescription(type.description)
		print("input ")
		print(type.name)
		printBlock {
			type.inputFields.forEach { field ->
				printParameter(field)
				print("\n")
			}
		}
		print("\n")
	}


	private fun printInterface(type: GInterfaceType) {
		printDescription(type.description)
		print("interface ")
		print(type.name)
		print(" ")
		printBlock {
			printFields(type.fields(includeDeprecated = true))
		}
		print("\n")
	}


	private fun printObject(type: GObjectType) {
		printDescription(type.description)
		print("type ")
		print(type.name)
		printImplementedInterfaces(type.interfaces.sortedBy { it.name })
		print(" ")
		printBlock {
			printFields(type.fields(includeDeprecated = true))
		}
		print("\n")
	}


	private fun printParameter(parameter: GParameter) {
		print(parameter.name)
		print(": ")
		printTypeName(parameter.type)

		parameter.defaultValue?.let { defaultValue ->
			print(" = ")
			printValue(defaultValue)
		}
	}


	private fun printParameters(parameters: Collection<GParameter>) {
		if (parameters.isEmpty())
			return

		print("(")
		if (parameters.all { it.description.isNullOrEmpty() }) {
			parameters.forEachIndexed { index, parameter ->
				if (index > 0)
					print(", ")

				printParameter(parameter)
			}
		}
		else {
			parameters.forEach { parameter ->
				print("\n")
				printParameter(parameter)
			}
		}
		print(")")
	}


	private fun printScalar(type: GScalarType) {
		printDescription(type.description)
		print("scalar ")
		print(type.name)
		print("\n")
	}


	// FIXME escaping
	private fun printString(string: String) {
		print("\"")
		print(string)
		print("\"")
	}


	private fun printType(type: GNamedType) {
		when (type) {
			is GEnumType -> printEnum(type)
			is GInputObjectType -> printInputObject(type)
			is GInterfaceType -> printInterface(type)
			is GObjectType -> printObject(type)
			is GScalarType -> printScalar(type)
			is GUnionType -> printUnion(type)
		}
	}


	private fun printTypeName(type: GType) {
		print(type.toString()) // FIXME move logic here
	}


	private fun printTypes(types: Collection<GNamedType>) {
		val lastIndex = types.size - 1

		types.forEachIndexed { index, type ->
			printType(type)

			if (index < lastIndex)
				print("\n")
		}
	}


	private fun printUnion(type: GUnionType) {
		printDescription(type.description)
		print("union ")
		print(type.name)
		print(" = ")
		type.possibleTypes.forEachIndexed { index, possibleType ->
			if (index > 0)
				print(" | ")

			print(possibleType.name)
		}
		print("\n")
	}


	private fun printValue(value: GValue) {
		when (value) {
			is GValue.Boolean -> print(value.value.toString())
			is GValue.EnumValue -> print(value.value)
			is GValue.Float -> print(value.value.toString())
			is GValue.Int -> print(value.value.toString())
			is GValue.List -> {
				print("[")
				value.value.forEachIndexed { index, element ->
					if (index > 0)
						print(", ")

					printValue(element)
				}
				print("]")
			}
			is GValue.Null -> print("null")
			is GValue.Object -> {
				printBlock {
					value.value.forEach { (fieldName, fieldValue) ->
						print(fieldName)
						print(": ")
						printValue(fieldValue)
						print("\n")
					}
				}
			}
			is GValue.String -> printString(value.value)
		}
	}
}
