package io.fluidsonic.graphql

/**
 * Prints [schema] as a GraphQL SDL document.
 *
 * The output is the schema's *public* shape and not a faithful copy of the document it was built from:
 * type extensions appear already merged into what they extend, the built-in scalars, the introspection
 * types and the specified directive definitions are omitted, and every applied directive is dropped except
 * `@deprecated`, `@oneOf` and `@specifiedBy`. Definitions keep the order the schema declares them in — the
 * schema block first, then the directive definitions, then the types — and descriptions print as block
 * strings. The result has no trailing line break.
 *
 * A `schema { … }` block is printed only when the schema carries a description or when a root operation
 * type has a name other than `Query`, `Mutation` or `Subscription`.
 *
 * @param indent The string one nesting level is indented by.
 * @return The schema as SDL, matching what graphql-js's `printSchema()` produces for the same schema.
 */
public fun printSchema(schema: GSchema, indent: String = "  "): String = SchemaPrinter.printSchema(schema = schema, indent = indent)

/**
 * Prints [type] as a single GraphQL SDL type definition, applying the same filtering as [printSchema].
 *
 * Unlike [printSchema] this prints whichever type it is given, including a built-in scalar and an
 * introspection type. The result has no trailing line break.
 *
 * @param indent The string one nesting level is indented by.
 * @return The type definition as SDL, matching what graphql-js's `printType()` produces for the same type.
 */
public fun printType(type: GNamedType, indent: String = "  "): String = SchemaPrinter.printType(type = type, indent = indent)

/**
 * Prints a resolved [GSchema], rather than an AST, as SDL — see [printSchema] for the contract.
 *
 * This is deliberately separate from [Printer]: that one prints a [GNode] as written and must stay lossless,
 * while this one filters and reifies, and matches graphql-js's `printSchema()` down to its whitespace.
 */
internal object SchemaPrinter {

	/** The names the specification reserves for the built-in scalar types. */
	private val builtinScalarTypeNames = setOf("Boolean", "Float", "ID", "Int", "String")

	/** The default of `@deprecated`'s `reason` argument, which prints as a bare `@deprecated`. */
	private val defaultDeprecationReason =
		(GLanguage.defaultDeprecatedDirective.argumentDefinition("reason")?.defaultValue as? GStringValue)?.value

	/** The five directives every schema has, whose definitions are never printed. */
	private val specifiedDirectiveNames = setOf(
		GLanguage.defaultDeprecatedDirective.name,
		GLanguage.defaultIncludeDirective.name,
		GLanguage.defaultOneOfDirective.name,
		GLanguage.defaultSkipDirective.name,
		GLanguage.defaultSpecifiedByDirective.name,
	)

	fun printSchema(schema: GSchema, indent: String): String {
		val definitions = mutableListOf<String>()

		printSchemaDefinition(schema = schema, indent = indent)?.let { definitions += it }

		schema.directiveDefinitions
			.filterNot { it.name in specifiedDirectiveNames }
			.mapTo(definitions) { printDirectiveDefinition(definition = it, indent = indent) }

		// Filtering by name rather than by identity is mandatory: every schema owns its own built-in scalar
		// instances, so there is no singleton to compare against.
		schema.types
			.filterNot { it.name in builtinScalarTypeNames || GLanguage.isValidIntrospectionName(it.name) }
			.mapTo(definitions) { printType(type = it, indent = indent) }

		return definitions.joinToString("\n\n")
	}

	fun printType(type: GNamedType, indent: String): String = when (type) {
		is GEnumType -> SdlTextPrinter.printDescription(type.description) + "enum " + type.name +
			SdlTextPrinter.printBlock(
				type.values.mapIndexed { index, value ->
					SdlTextPrinter.printDescription(description = value.description, indentation = indent, isFirst = index == 0) +
						indent + value.name + printDeprecated(value)
				},
			)

		is GInputObjectType -> SdlTextPrinter.printDescription(type.description) + "input " + type.name +
			(if (type.directive(GLanguage.defaultOneOfDirective.name) !== null) " @oneOf" else "") +
			SdlTextPrinter.printBlock(
				type.argumentDefinitions.mapIndexed { index, field ->
					SdlTextPrinter.printDescription(description = field.description, indentation = indent, isFirst = index == 0) +
						indent + printInputValue(field)
				},
			)

		is GInterfaceType -> SdlTextPrinter.printDescription(type.description) + "interface " + type.name +
			printImplementedInterfaces(type.interfaces) + printFields(fieldDefinitions = type.fieldDefinitions, indent = indent)

		is GObjectType -> SdlTextPrinter.printDescription(type.description) + "type " + type.name +
			printImplementedInterfaces(type.interfaces) + printFields(fieldDefinitions = type.fieldDefinitions, indent = indent)

		is GScalarType -> SdlTextPrinter.printDescription(type.description) + "scalar " + type.name +
			(
				type.directive(GLanguage.defaultSpecifiedByDirective.name)?.argument("url")?.value
					?.let { " @specifiedBy(url: ${SdlTextPrinter.printValue(it)})" }
					.orEmpty()
				)

		is GUnionType -> SdlTextPrinter.printDescription(type.description) + "union " + type.name +
			if (type.possibleTypes.isNotEmpty()) type.possibleTypes.joinToString(prefix = " = ", separator = " | ") { it.name } else ""
	}

	private fun printSchemaDefinition(schema: GSchema, indent: String): String? {
		val schemaDefinition = mergeSchemaExtensions(
			schemaDefinition = schema.document.definitions.filterIsInstance<GSchemaDefinition>().firstOrNull(),
			schemaExtensions = schema.document.definitions.filterIsInstance<GSchemaExtension>(),
		)
		val description = schemaDefinition?.description

		// The declared name wins over the resolved type's, so that a root operation type the schema does not
		// define is still printed as declared rather than dropped.
		fun rootTypeName(operationType: GOperationType, rootType: GObjectType?) =
			schemaDefinition?.operationTypeDefinition(operationType)?.type?.name ?: rootType?.name

		val queryTypeName = rootTypeName(GOperationType.query, schema.queryType)
		val mutationTypeName = rootTypeName(GOperationType.mutation, schema.mutationType)
		val subscriptionTypeName = rootTypeName(GOperationType.subscription, schema.subscriptionType)

		val usesConventionalRootTypeNames = (queryTypeName === null || queryTypeName == GLanguage.defaultQueryTypeName) &&
			(mutationTypeName === null || mutationTypeName == GLanguage.defaultMutationTypeName) &&
			(subscriptionTypeName === null || subscriptionTypeName == GLanguage.defaultSubscriptionTypeName)

		if (description === null && usesConventionalRootTypeNames) {
			return null
		}

		val operations = mutableListOf<String>()
		queryTypeName?.let { operations += "${indent}query: $it" }
		mutationTypeName?.let { operations += "${indent}mutation: $it" }
		subscriptionTypeName?.let { operations += "${indent}subscription: $it" }

		return SdlTextPrinter.printDescription(description) + "schema" + SdlTextPrinter.printBlock(operations)
	}

	private fun printDirectiveDefinition(definition: GDirectiveDefinition, indent: String): String =
		SdlTextPrinter.printDescription(definition.description) + "directive @" + definition.name +
			printArgumentDefinitions(definitions = definition.argumentDefinitions, indent = indent, indentation = "") +
			(if (definition.isRepeatable) " repeatable" else "") +
			definition.locationNodes.joinToString(prefix = " on ", separator = " | ") { it.value }

	private fun printFields(fieldDefinitions: List<GFieldDefinition>, indent: String): String = SdlTextPrinter.printBlock(
		fieldDefinitions.mapIndexed { index, field ->
			SdlTextPrinter.printDescription(description = field.description, indentation = indent, isFirst = index == 0) +
				indent + field.name +
				printArgumentDefinitions(definitions = field.argumentDefinitions, indent = indent, indentation = indent) +
				": " + Printer.print(field.type) + printDeprecated(field)
		},
	)

	/**
	 * Prints [definitions] as an argument list, wrapping onto several lines exactly when one of them carries
	 * a description — as graphql-js does, regardless of how many arguments there are.
	 */
	private fun printArgumentDefinitions(definitions: List<GArgumentDefinition>, indent: String, indentation: String): String = when {
		definitions.isEmpty() ->
			""

		definitions.none { it.description !== null } ->
			definitions.joinToString(prefix = "(", separator = ", ", postfix = ")") { printInputValue(it) }

		else -> {
			val nestedIndentation = indent + indentation

			definitions
				.mapIndexed { index, definition ->
					SdlTextPrinter.printDescription(description = definition.description, indentation = nestedIndentation, isFirst = index == 0) +
						nestedIndentation + printInputValue(definition)
				}
				.joinToString(prefix = "(\n", separator = "\n", postfix = "\n$indentation)")
		}
	}

	private fun printInputValue(definition: GArgumentDefinition): String = definition.name + ": " + Printer.print(definition.type) +
		definition.defaultValue?.let { " = " + SdlTextPrinter.printValue(it) }.orEmpty() +
		printDeprecated(definition)

	private fun printImplementedInterfaces(interfaces: List<GNamedTypeRef>): String =
		if (interfaces.isEmpty()) "" else interfaces.joinToString(prefix = " implements ", separator = " & ") { it.name }

	private fun printDeprecated(node: GNode.WithOptionalDeprecation): String {
		if (node.deprecation === null) {
			return ""
		}

		val reason = node.deprecationReason

		return if (reason === null || reason == defaultDeprecationReason) {
			" @deprecated"
		} else {
			" @deprecated(reason: ${Printer.print(GStringValue(reason))})"
		}
	}
}
