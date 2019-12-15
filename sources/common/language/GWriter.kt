package io.fluidsonic.graphql


// FIXME get rid of this
class GWriter(
	val indent: String = "\t"
) {

	private val builder = StringBuilder()
	private var currentLineIsIndented = false

	@PublishedApi
	internal var indentationLevel = 0


	fun clear() {
		builder.clear()

		currentLineIsIndented = false
		indentationLevel = 0
	}


	inline fun <R> indented(block: () -> R) =
		try {
			indentationLevel += 1
			block()
		}
		finally {
			indentationLevel -= 1
		}


	fun isEmpty() =
		builder.isEmpty()


	fun isNotEmpty() =
		builder.isNotEmpty()


	val length
		get() = builder.length


	override fun toString() =
		builder.toString()


	private fun writeIndentationIfNeeded() {
		if (currentLineIsIndented) return
		repeat(indentationLevel) { builder.append(indent) }
		currentLineIsIndented = true
	}


	fun writeLinebreak() {
		builder.append('\n')
		currentLineIsIndented = false
	}


	fun writeRaw(char: Char) {
		if (char == '\n')
			writeLinebreak()
		else {
			writeIndentationIfNeeded()
			builder.append(char)
		}
	}


	fun writeRaw(string: String) {
		var startIndex = 0
		while (startIndex < string.length) {
			val newlineIndex = string.indexOf('\n', startIndex = startIndex)
			val endIndex = if (newlineIndex >= 0) newlineIndex else string.length
			if (endIndex > startIndex) {
				writeIndentationIfNeeded()
				builder.append(string, startIndex, endIndex)
			}
			if (newlineIndex >= 0) {
				writeLinebreak()
			}

			startIndex = endIndex + 1
		}
	}


	companion object {

		inline operator fun invoke(indent: String = "\t", block: GWriter.() -> Unit) =
			GWriter(indent = indent).apply(block).toString()
	}
}


fun GWriter.writeArgument(argument: GArgument) {
	writeName(argument.name)
	writeRaw(": ")
	writeValue(argument.value)
}


fun GWriter.writeArgumentDefinition(definition: GArgumentDefinition) {
	writeName(definition.name)
	writeRaw(": ")
	writeTypeName(definition.type)

	definition.defaultValue?.let { defaultValue ->
		writeRaw(" = ")
		writeValue(defaultValue)
	}
}


fun GWriter.writeArgumentDefinitions(definitions: Collection<GArgumentDefinition>) {
	if (definitions.isEmpty())
		return

	writeRaw("(")

	val useSingleLine = definitions.all { it.description.isNullOrEmpty() }
	if (useSingleLine) {
		definitions.forEachIndexed { index, definition ->
			if (index > 0)
				writeRaw(", ")

			writeArgumentDefinition(definition)
		}
	}
	else {
		definitions.forEach { definition ->
			writeLinebreak()
			writeArgumentDefinition(definition)
		}
	}
	writeRaw(")")
}


fun GWriter.writeArguments(arguments: Collection<GArgument>) {
	if (arguments.isEmpty())
		return

	writeRaw("(")
	arguments.forEachIndexed { index, argument ->
		if (index > 0)
			writeRaw(", ")

		writeArgument(argument)
	}
	writeRaw(")")
}


inline fun <R> GWriter.writeBlock(block: () -> R): R {
	writeRaw("{")
	writeLinebreak()
	val result = indented(block)
	writeRaw("}")

	return result
}


fun GWriter.writeDescription(description: String?) {
	if (description == null)
		return

	return writeStringValue(description, block = true)
}


fun GWriter.writeDirective(directive: GDirective) {
	writeRaw("@")
	writeName(directive.name)
	writeArguments(directive.arguments.values)
}


fun GWriter.writeDirectiveDefinition(definition: GDirectiveDefinition) {
	writeDescription(definition.description)
	writeRaw("directive @")
	writeName(definition.name)
	writeArgumentDefinitions(definition.arguments.values)
	writeRaw(" on ")
	definition.locations
		.sortedBy { it.name }
		.forEachIndexed { index, location ->
			if (index > 0)
				writeRaw(" | ")

			writeName(location.name)
		}
}


fun GWriter.writeDirectives(directives: Collection<GDirective>) {
	if (directives.isEmpty())
		return

	directives.forEach { directive ->
		writeRaw(" ")
		writeDirective(directive)
	}
}


fun GWriter.writeDirectiveDefinitions(definitions: Collection<GDirectiveDefinition>) {
	definitions.forEach { definition ->
		writeDirectiveDefinition(definition)
		writeLinebreak()
	}
}


fun GWriter.writeEnumTypeDefinition(definition: GEnumType) {
	writeDescription(definition.description)
	writeRaw("enum ")
	writeName(definition.name)
	writeRaw(" ")
	writeEnumValueDefinitions(definition.values.values)
	writeLinebreak()
}


fun GWriter.writeEnumValueDefinition(definition: GEnumValueDefinition) {
	writeDescription(definition.description)
	writeName(definition.name)
	writeDirectives(definition.directives)
}


fun GWriter.writeEnumValueDefinitions(definitions: Collection<GEnumValueDefinition>) {
	writeBlock {
		definitions.forEach { definition ->
			writeEnumValueDefinition(definition)
			writeLinebreak()
		}
	}
}


fun GWriter.writeFieldDefinition(definition: GFieldDefinition) {
	writeDescription(definition.description)
	writeName(definition.name)
	writeArgumentDefinitions(definition.arguments.values)
	writeRaw(": ")
	writeTypeName(definition.type)
	writeDirectives(definition.directives)
}


fun GWriter.writeFieldDefinitions(definitions: Collection<GFieldDefinition>) {
	writeBlock {
		definitions.forEach { definition ->
			writeFieldDefinition(definition)
			writeLinebreak()
		}
	}
}


fun GWriter.writeImplementedInterfaces(interfaces: Collection<GInterfaceType>) {
	if (interfaces.isEmpty())
		return

	writeRaw(" implements ")
	interfaces
		.sortedBy { it.name }
		.forEachIndexed { index, iface ->
			if (index > 0)
				writeRaw(" & ")

			writeName(iface.name)
		}
}


fun GWriter.writeInputFieldDefinitions(definitions: Collection<GArgumentDefinition>) {
	writeBlock {
		definitions.forEach { definition ->
			writeArgumentDefinition(definition)
			writeLinebreak()
		}
	}
}


fun GWriter.writeInputObjectTypeDefinition(definition: GInputObjectType) {
	writeDescription(definition.description)
	writeRaw("input ")
	writeName(definition.name)
	writeInputFieldDefinitions(definition.arguments.values)
	writeLinebreak()
}


fun GWriter.writeInterfaceTypeDefinition(definition: GInterfaceType) {
	writeDescription(definition.description)
	writeRaw("interface ")
	writeName(definition.name)
	writeRaw(" ")
	writeFieldDefinitions(definition.fields.values)
	writeLinebreak()
}


fun GWriter.writeName(name: String) {
	writeRaw(name)
}


fun GWriter.writeObjectTypeDefinition(definition: GObjectType) {
	writeDescription(definition.description)
	writeRaw("type ")
	writeName(definition.name)
	writeImplementedInterfaces(definition.interfaces)
	writeRaw(" ")
	writeFieldDefinitions(definition.fields.values)
	writeLinebreak()
}


fun GWriter.writeScalarTypeDefinition(type: GScalarType) {
	writeDescription(type.description)
	writeRaw("scalar ")
	writeName(type.name)
	writeLinebreak()
}


fun GWriter.writeSchema(schema: GSchema) {
	writeSchemaHeader(schema)

	if (schema.directives.isNotEmpty()) {
		if (isNotEmpty())
			writeLinebreak()

		writeDirectiveDefinitions(schema.directives.sortedBy { it.name })
	}

	if (schema.types.isNotEmpty()) {
		if (isNotEmpty())
			writeLinebreak()

		writeTypeDefinitions(schema.types.values.sortedBy { it.name })
	}
}


fun GWriter.writeSchemaHeader(schema: GSchema) {
	if (schema.rootTypeNamesFollowCommonConvention)
		return

	writeRaw("schema ")
	writeBlock {
		schema.queryType
			?.takeIf { it.name != GSpecification.defaultQueryTypeName }
			?.let {
				writeLinebreak()
				writeRaw("query: ")
				writeName(schema.queryType.name)
			}

		schema.mutationType
			?.takeIf { it.name != GSpecification.defaultMutationTypeName }
			?.let {
				writeLinebreak()
				writeRaw("mutation: ")
				writeName(it.name)
			}

		schema.subscriptionType
			?.takeIf { it.name != GSpecification.defaultSubscriptionTypeName }
			?.let {
				writeLinebreak()
				writeStringValue("subscription: ")
				writeName(it.name)
			}
	}
	writeLinebreak()
}


fun GWriter.writeSelection(selection: GSelection) {
	@Suppress("UNUSED_VARIABLE")
	val exhaustive = when (selection) {
		is GFieldSelection -> {
			selection.alias?.let { alias ->
				writeName(alias)
				writeRaw(": ")
			}
			writeName(selection.name)
			writeArguments(selection.arguments)
			writeDirectives(selection.directives)
			selection.selectionSet?.let { selectionSet ->
				writeSelectionSet(selectionSet)
			}
			writeLinebreak()
		}

		is GFragmentSelection -> {
			writeRaw("...")
			writeName(selection.name)
			writeDirectives(selection.directives)
		}

		is GInlineFragmentSelection -> {
			writeRaw("...")
			selection.typeCondition?.let { typeCondition ->
				writeTypeCondition(typeCondition)
			}
			writeDirectives(selection.directives)
			writeSelectionSet(selection.selectionSet)
		}
	}
}


fun GWriter.writeSelectionSet(selectionSet: GSelectionSet) {
	writeBlock {
		selectionSet.selections.forEach { selection ->
			writeSelection(selection)
			writeLinebreak()
		}
	}
	writeLinebreak()
}


// FIXME escaping, line wrapping, indentation
fun GWriter.writeStringValue(string: String, block: Boolean = false) {
	if (block) {
		writeRaw("\"\"\"")
		writeRaw(string)
		writeRaw("\"\"\"")
		writeLinebreak()
	}
	else {
		writeRaw("\"")
		writeRaw(string)
		writeRaw("\"")
	}
}


fun GWriter.writeTypeCondition(condition: GNamedTypeRef) {
	writeRaw(" on ")
	writeTypeRef(condition)
}


fun GWriter.writeTypeDefinition(type: GNamedType) {
	@Suppress("UNUSED_VARIABLE")
	val exhaustive = when (type) {
		is GEnumType -> writeEnumTypeDefinition(type)
		is GInputObjectType -> writeInputObjectTypeDefinition(type)
		is GInterfaceType -> writeInterfaceTypeDefinition(type)
		is GObjectType -> writeObjectTypeDefinition(type)
		is GScalarType -> writeScalarTypeDefinition(type)
		is GUnionType -> writeUnionTypeDefinition(type)
	}
}


fun GWriter.writeTypeDefinitions(definitions: Collection<GNamedType>) {
	val lastIndex = definitions.size - 1

	definitions.forEachIndexed { index, definition ->
		writeTypeDefinition(definition)

		if (index < lastIndex)
			writeLinebreak()
	}
}


fun GWriter.writeTypeName(type: GType) {
	@Suppress("UNUSED_VARIABLE")
	val exhaustive = when (type) {
		is GNamedType ->
			writeName(type.name)

		is GListType -> {
			writeRaw("[")
			writeTypeName(type.ofType)
			writeRaw("]")
		}

		is GNonNullType -> {
			writeTypeName(type.ofType)
			writeRaw("!")
		}
	}
}


fun GWriter.writeTypeRef(ref: GTypeRef) {
	@Suppress("UNUSED_VARIABLE")
	val exhaustive = when (ref) {
		is GNamedTypeRef ->
			writeName(ref.name)

		is GListTypeRef -> {
			writeRaw("[")
			writeTypeRef(ref.elementType)
			writeRaw("]")
		}

		is GNonNullTypeRef -> {
			writeTypeRef(ref.nullableType)
			writeRaw("!")
		}
	}
}


fun GWriter.writeUnionTypeDefinition(type: GUnionType) {
	writeDescription(type.description)
	writeRaw("union ")
	writeName(type.name)
	writeRaw(" = ")
	type.types.forEachIndexed { index, possibleType ->
		if (index > 0)
			writeRaw(" | ")

		writeName(possibleType.name)
	}
	writeLinebreak()
}


fun GWriter.writeValue(value: Any?) {
	@Suppress("UNUSED_VARIABLE")
	val exhaustive = when (value) {
		null, GNullValue -> writeRaw("null")
		is GVariableReference -> {
			writeRaw("$")
			writeName(value.name)
		}
		is Boolean -> writeRaw(if (value) "true" else "false")
		// is GEnumValue -> writeName(value.value) // FIXME
		is Double -> writeRaw(value.toString())
		is Float -> writeRaw(value.toString())
		is Int -> writeRaw(value.toString())
		is Collection<*> -> {
			writeRaw("[")
			value.forEachIndexed { index, element ->
				if (index > 0)
					writeRaw(", ")

				writeValue(element)
			}
			writeRaw("]")
		}
		is String -> writeStringValue(value)
		else ->
			writeRaw("<OBJ>")
		// FIXME
//		{
//			if (value.value.isNotEmpty())
//				writeBlock {
//					value.value.forEach { (fieldName, fieldValue) ->
//						writeName(fieldName)
//						writeRaw(": ")
//						writeValue(fieldValue)
//						writeRaw("\n")
//					}
//				}
//			else
//				writeRaw("{}")
//		}
	}
}
