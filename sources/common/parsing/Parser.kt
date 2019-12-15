package io.fluidsonic.graphql

import io.fluidsonic.graphql.AstNode.*


internal class Parser private constructor(
	source: String
) {

	private val lexer = Lexer(source = source)


	private inline fun <T> any(
		openKind: Token.Kind,
		parse: () -> T,
		closeKind: Token.Kind
	): List<T> {
		expectToken(openKind)

		val elements = mutableListOf<T>()

		while (expectOptionalToken(closeKind) != null)
			elements += parse()

		return elements
	}


	private fun expectKeyword(keyword: String) {
		lexer.currentToken
			.takeIf { it.kind == Token.Kind.NAME && it.value == keyword }
			?.also { lexer.advance() }
			?: unexpectedTokenError(expected = "\"$keyword\"")
	}


	private fun expectOptionalKeyword(keyword: String) =
		null != lexer.currentToken
			.takeIf { it.kind == Token.Kind.NAME && it.value == keyword }
			?.also { lexer.advance() }


	private fun expectOptionalToken(kind: Token.Kind) =
		lexer.currentToken
			.takeIf { it.kind == kind }
			?.also { lexer.advance() }


	private fun expectToken(kind: Token.Kind) =
		lexer.currentToken
			.takeIf { it.kind == kind }
			?.also { lexer.advance() }
			?: unexpectedTokenError(expected = kind.toString())


	private fun makeSourceLocation(startToken: Token) =
		SourceLocation(
			startToken = startToken,
			endToken = lexer.previousToken
		)


	private inline fun <T> many(
		openKind: Token.Kind,
		parse: () -> T,
		closeKind: Token.Kind
	): List<T> {
		val elements = mutableListOf<T>()

		expectToken(openKind)

		do elements += parse()
		while (expectOptionalToken(closeKind) == null)

		return elements
	}


	private inline fun <T> optionalMany(
		openKind: Token.Kind,
		parse: () -> T,
		closeKind: Token.Kind
	) =
		if (expectOptionalToken(openKind) != null) {
			val elements = mutableListOf<T>()

			do elements += parse()
			while (expectOptionalToken(closeKind) == null)

			elements
		}
		else
			emptyList<T>()


	private fun parseArgument(isConstant: Boolean): Argument {
		val startToken = lexer.currentToken
		val name = parseName()

		expectToken(Token.Kind.COLON)

		return Argument(
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken),
			value = parseValue(isConstant = isConstant)
		)
	}


	private fun parseArguments(isConstant: Boolean) =
		optionalMany(
			Token.Kind.PAREN_L,
			{ parseArgument(isConstant) },
			Token.Kind.PAREN_R
		)


	private fun parseArgumentDefinition(): ArgumentDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		expectToken(Token.Kind.COLON)
		val type = parseTypeReference()
		val defaultValue = expectOptionalToken(Token.Kind.EQUALS)
			?.let { parseValue(isConstant = true) }
		val directives = parseDirectives(isConstant = true)

		return ArgumentDefinition(
			defaultValue = defaultValue,
			description = description,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken),
			type = type
		)
	}


	private fun parseArgumentDefinitions(isBlock: Boolean) =
		optionalMany(
			if (isBlock) Token.Kind.BRACE_L else Token.Kind.PAREN_L,
			this::parseArgumentDefinition,
			if (isBlock) Token.Kind.BRACE_R else Token.Kind.PAREN_R
		)


	private fun parseDefinition() =
		when {
			peek(Token.Kind.NAME) ->
				when (lexer.currentToken.value) {
					"directive", "input", "enum", "interface", "scalar", "schema", "type", "union" ->
						parseTypeSystemDefinition()

					"extend" ->
						parseTypeSystemExtension()

					"fragment" ->
						parseFragmentDefinition()

					"mutation", "query", "subscription" ->
						parseOperationDefinition()

					else ->
						unexpectedTokenError()
				}

			peek(Token.Kind.BRACE_L) ->
				parseOperationDefinition()

			peekDescription() ->
				parseTypeSystemDefinition()

			else ->
				unexpectedTokenError()
		}


	private fun parseDescription() =
		peekDescription().thenTake { parseStringValue() }


	private fun parseDirective(isConstant: Boolean): Directive {
		val startToken = expectToken(Token.Kind.AT)
		val name = parseName()
		val arguments = parseArguments(isConstant = isConstant)

		return Directive(
			arguments = arguments,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseDirectiveDefinition(): Definition.TypeSystem.Directive {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("directive")
		expectToken(Token.Kind.AT)
		val name = parseName()
		val arguments = parseArgumentDefinitions(isBlock = false)
		val isRepeatable = expectOptionalKeyword("repeatable")
		expectKeyword("on")
		val locations = parseDirectiveLocations()

		return Definition.TypeSystem.Directive(
			arguments = arguments,
			description = description,
			isRepeatable = isRepeatable,
			locations = locations,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseDirectiveLocation() =
		parseName()
			.takeIf { name -> GDirectiveLocation.values().any { it.name == name.value } }
			?: unexpectedTokenError()


	private fun parseDirectiveLocations(): List<Name> {
		val locations = mutableListOf<Name>()

		expectOptionalToken(Token.Kind.PIPE)

		do locations += parseDirectiveLocation()
		while (expectOptionalToken(Token.Kind.PIPE) != null)

		return locations
	}


	private fun parseDirectives(isConstant: Boolean): List<Directive> {
		val directives = mutableListOf<Directive>()

		while (peek(Token.Kind.AT))
			directives += parseDirective(isConstant = isConstant)

		return directives
	}


	private fun parseDocument(): Document {
		val startToken = lexer.currentToken
		val definitions = many(
			Token.Kind.SOF,
			this::parseDefinition,
			Token.Kind.EOF
		)

		return Document(
			definitions = definitions,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseEnumTypeDefinition(): Definition.TypeSystem.Type.Enum {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("enum")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val values = parseEnumValueDefinitions()

		return Definition.TypeSystem.Type.Enum(
			description = description,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken),
			values = values
		)
	}


	private fun parseEnumTypeExtension(): Definition.TypeSystemExtension.Type.Enum {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("enum")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val values = parseEnumValueDefinitions()

		if (directives.isEmpty() && values.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Type.Enum(
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken),
			values = values
		)
	}


	private fun parseEnumValueDefinition(): EnumValueDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		val directives = parseDirectives(isConstant = true)

		return EnumValueDefinition(
			description = description,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseEnumValueDefinitions() =
		optionalMany(
			Token.Kind.BRACE_L,
			this::parseEnumValueDefinition,
			Token.Kind.BRACE_R
		)


	private fun parseFieldSelection(): Selection.Field {
		val startToken = lexer.currentToken
		val nameOrAlias = parseName()

		val alias: Name?
		val name: Name

		if (expectOptionalToken(Token.Kind.COLON) != null) {
			alias = nameOrAlias
			name = parseName()
		}
		else {
			alias = null
			name = nameOrAlias
		}

		val arguments = parseArguments(isConstant = false)
		val directives = parseDirectives(isConstant = false)
		val selectionSet = peek(Token.Kind.BRACE_L).thenTake { parseSelectionSet() }

		return Selection.Field(
			arguments = arguments,
			alias = alias,
			directives = directives,
			name = name,
			selectionSet = selectionSet,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseFieldDefinition(): FieldDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		val arguments = parseArgumentDefinitions(isBlock = false)
		expectToken(Token.Kind.COLON)
		val type = parseTypeReference()
		val directives = parseDirectives(isConstant = true)

		return FieldDefinition(
			arguments = arguments,
			description = description,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken),
			type = type
		)
	}


	private fun parseFieldDefinitions() =
		optionalMany(
			Token.Kind.BRACE_L,
			this::parseFieldDefinition,
			Token.Kind.BRACE_R
		)


	private fun parseFragmentSelection(): Selection {
		val startToken = expectToken(Token.Kind.SPREAD)

		val hasTypeCondition = expectOptionalKeyword("on")
		if (!hasTypeCondition && peek(Token.Kind.NAME)) {
			val name = parseFragmentName()
			val directives = parseDirectives(isConstant = false)

			return Selection.FragmentSpread(
				directives = directives,
				name = name,
				sourceLocation = makeSourceLocation(startToken = startToken)
			)
		}

		val typeCondition = hasTypeCondition.thenTake { parseNamedType() }
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return Selection.InlineFragment(
			directives = directives,
			selectionSet = selectionSet,
			sourceLocation = makeSourceLocation(startToken = startToken),
			typeCondition = typeCondition
		)
	}


	private fun parseFragmentDefinition(): Definition.Fragment {
		val startToken = lexer.currentToken
		expectKeyword("fragment")
		val name = parseFragmentName()
		expectKeyword("on")
		val typeCondition = parseNamedType()
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return Definition.Fragment(
			directives = directives,
			name = name,
			selectionSet = selectionSet,
			sourceLocation = makeSourceLocation(startToken = startToken),
			typeCondition = typeCondition
		)
	}


	private fun parseFragmentName() =
		lexer.currentToken
			.takeIf { it.value != "on" }
			?.let { parseName() }
			?: unexpectedTokenError()


	private fun parseImplementsInterfaces(): List<TypeReference.Named> {
		if (!expectOptionalKeyword("implements"))
			return emptyList()

		val interfaces = mutableListOf<TypeReference.Named>()

		expectOptionalToken(Token.Kind.AMP)
		do interfaces += parseNamedType()
		while (expectOptionalToken(Token.Kind.AMP) != null)

		return interfaces
	}


	private fun parseInputObjectTypeDefinition(): Definition.TypeSystem.Type.InputObject {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("input")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val arguments = parseArgumentDefinitions(isBlock = true)

		return Definition.TypeSystem.Type.InputObject(
			arguments = arguments,
			description = description,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseInputObjectTypeExtension(): Definition.TypeSystemExtension.Type.InputObject {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("input")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val arguments = parseArgumentDefinitions(isBlock = true)

		if (directives.isEmpty() && arguments.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Type.InputObject(
			arguments = arguments,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseInterfaceTypeDefinition(): Definition.TypeSystem.Type.Interface {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("interface")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(isConstant = true)
		val fields = parseFieldDefinitions()

		return Definition.TypeSystem.Type.Interface(
			description = description,
			directives = directives,
			fields = fields,
			interfaces = interfaces,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseInterfaceTypeExtension(): Definition.TypeSystemExtension.Type.Interface {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("interface")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(isConstant = true)
		val fields = parseFieldDefinitions()

		if (interfaces.isEmpty() && directives.isEmpty() && fields.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Type.Interface(
			directives = directives,
			fields = fields,
			interfaces = interfaces,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseList(isConstant: Boolean): Value.List {
		val startToken = lexer.currentToken
		val elements = any(
			Token.Kind.BRACKET_L,
			{ parseValue(isConstant = isConstant) },
			Token.Kind.BRACKET_R
		)

		return Value.List(
			elements = elements,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseName(): Name {
		val startToken = expectToken(Token.Kind.NAME)

		return Name(
			value = startToken.value!!,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseNamedType(): TypeReference.Named {
		val startToken = lexer.currentToken
		val name = parseName()

		return TypeReference.Named(
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseObject(isConstant: Boolean): Value.Object {
		val startToken = lexer.currentToken
		val fields = any(
			Token.Kind.BRACE_L,
			{ parseObjectField(isConstant = isConstant) },
			Token.Kind.BRACE_R
		)

		return Value.Object(
			fields = fields,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseObjectField(isConstant: Boolean): Value.Object.Field {
		val startToken = lexer.currentToken
		val name = parseName()
		expectToken(Token.Kind.COLON)
		val value = parseValue(isConstant = isConstant)

		return Value.Object.Field(
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken),
			value = value
		)
	}


	private fun parseObjectTypeDefinition(): Definition.TypeSystem.Type.Object {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("type")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(isConstant = true)
		val fields = parseFieldDefinitions()

		return Definition.TypeSystem.Type.Object(
			description = description,
			directives = directives,
			fields = fields,
			interfaces = interfaces,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseObjectTypeExtension(): Definition.TypeSystemExtension.Type.Object {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("type")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(true)
		val fields = parseFieldDefinitions()

		if (interfaces.isEmpty() && directives.isEmpty() && fields.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Type.Object(
			directives = directives,
			fields = fields,
			interfaces = interfaces,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseOperationDefinition(): Definition.Operation {
		val startToken = lexer.currentToken

		if (peek(Token.Kind.BRACE_L)) {
			val selectionSet = parseSelectionSet()

			return Definition.Operation(
				directives = emptyList(),
				name = null,
				selectionSet = selectionSet,
				sourceLocation = makeSourceLocation(startToken = startToken),
				type = GOperationType.query,
				variableDefinitions = emptyList()
			)
		}

		val type = parseOperationType()
		val name = peek(Token.Kind.NAME).thenTake { parseName() }
		val variableDefinitions = parseVariableDefinitions()
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return Definition.Operation(
			directives = directives,
			name = name,
			selectionSet = selectionSet,
			sourceLocation = makeSourceLocation(startToken = startToken),
			type = type,
			variableDefinitions = variableDefinitions
		)
	}


	private fun parseOperationType() =
		when (lexer.currentToken.value) {
			"mutation" -> GOperationType.mutation
			"query" -> GOperationType.query
			"subscription" -> GOperationType.subscription
			else -> unexpectedTokenError()
		}


	private fun parseOperationTypeDefinition(): OperationTypeDefinition {
		val startToken = lexer.currentToken
		val operation = parseOperationType()
		expectToken(Token.Kind.COLON)
		val type = parseNamedType()

		return OperationTypeDefinition(
			operation = operation,
			sourceLocation = makeSourceLocation(startToken = startToken),
			type = type
		)
	}


	private fun parseScalarTypeExtension(): Definition.TypeSystemExtension.Type.Scalar {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("scalar")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)

		if (directives.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Type.Scalar(
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseSchemaExtension(): Definition.TypeSystemExtension.Schema {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("schema")
		val directives = parseDirectives(isConstant = true)
		val operationTypes = optionalMany(
			Token.Kind.BRACE_L,
			this::parseOperationTypeDefinition,
			Token.Kind.BRACE_R
		)

		if (directives.isEmpty() && operationTypes.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Schema(
			directives = directives,
			operationTypes = operationTypes,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseScalarTypeDefinition(): Definition.TypeSystem.Type.Scalar {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("scalar")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)

		return Definition.TypeSystem.Type.Scalar(
			description = description,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseSchemaDefinition(): Definition.TypeSystem.Schema {
		val startToken = lexer.currentToken
		expectKeyword("schema")
		val directives = parseDirectives(isConstant = true)
		val operationTypes = many(
			Token.Kind.BRACE_L,
			this::parseOperationTypeDefinition,
			Token.Kind.BRACE_R
		)

		return Definition.TypeSystem.Schema(
			directives = directives,
			operationTypes = operationTypes,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseSelection() =
		if (peek(Token.Kind.SPREAD)) parseFragmentSelection()
		else parseFieldSelection()


	private fun parseSelectionSet(): SelectionSet {
		val startToken = lexer.currentToken
		val selections = many(
			Token.Kind.BRACE_L,
			this::parseSelection,
			Token.Kind.BRACE_R
		)

		return SelectionSet(
			selections = selections,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseStringValue(): Value.String {
		val startToken = lexer.currentToken
		lexer.advance()

		return Value.String(
			isBlock = startToken.kind == Token.Kind.BLOCK_STRING,
			sourceLocation = makeSourceLocation(startToken = startToken),
			value = startToken.value!!
		)
	}


	private fun parseTypeReference(): TypeReference {
		val startToken = lexer.currentToken

		var type: TypeReference
		if (expectOptionalToken(Token.Kind.BRACKET_L) != null) {
			type = parseTypeReference()
			expectToken(Token.Kind.BRACKET_R)

			type = TypeReference.List(
				elementType = type,
				sourceLocation = makeSourceLocation(startToken = startToken)
			)
		}
		else
			type = parseNamedType()

		if (expectOptionalToken(Token.Kind.BANG) != null)
			type = TypeReference.NonNull(
				nullableType = type,
				sourceLocation = makeSourceLocation(startToken = startToken)
			)

		return type
	}


	private fun parseTypeSystemDefinition(): Definition.TypeSystem {
		val keywordToken =
			if (peekDescription()) lexer.lookahead()
			else lexer.currentToken

		if (keywordToken.kind != Token.Kind.NAME)
			unexpectedTokenError(keywordToken)

		return when (keywordToken.value) {
			"directive" -> parseDirectiveDefinition()
			"enum" -> parseEnumTypeDefinition()
			"input" -> parseInputObjectTypeDefinition()
			"interface" -> parseInterfaceTypeDefinition()
			"scalar" -> parseScalarTypeDefinition()
			"schema" -> parseSchemaDefinition()
			"type" -> parseObjectTypeDefinition()
			"union" -> parseUnionTypeDefinition()
			else -> unexpectedTokenError(keywordToken)
		}
	}


	private fun parseTypeSystemExtension(): Definition.TypeSystemExtension {
		val keywordToken = lexer.lookahead()
		if (keywordToken.kind != Token.Kind.NAME)
			unexpectedTokenError(keywordToken)

		return when (keywordToken.value) {
			"schema" -> parseSchemaExtension()
			"scalar" -> parseScalarTypeExtension()
			"type" -> parseObjectTypeExtension()
			"interface" -> parseInterfaceTypeExtension()
			"union" -> parseUnionTypeExtension()
			"enum" -> parseEnumTypeExtension()
			"input" -> parseInputObjectTypeExtension()
			else -> unexpectedTokenError(keywordToken)
		}
	}


	private fun parseUnionMemberTypes(): List<TypeReference.Named> {
		if (expectOptionalToken(Token.Kind.EQUALS) == null)
			return emptyList()

		val types = mutableListOf<TypeReference.Named>()

		expectOptionalToken(Token.Kind.PIPE)
		do types += parseNamedType()
		while (expectOptionalToken(Token.Kind.PIPE) != null)

		return types
	}


	private fun parseUnionTypeDefinition(): Definition.TypeSystem.Type.Union {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("union")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val types = parseUnionMemberTypes()

		return Definition.TypeSystem.Type.Union(
			description = description,
			directives = directives,
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken),
			types = types
		)
	}


	private fun parseUnionTypeExtension(): Definition.TypeSystemExtension.Type.Union {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("union")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val types = parseUnionMemberTypes()

		if (directives.isEmpty() && types.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Type.Union(
			directives = directives,
			name = name,
			types = types,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseValue(isConstant: Boolean): Value {
		val startToken = lexer.currentToken

		return when (startToken.kind) {
			Token.Kind.BRACKET_L ->
				parseList(isConstant)

			Token.Kind.BRACE_L ->
				parseObject(isConstant)

			Token.Kind.INT -> {
				lexer.advance()

				Value.Int(
					sourceLocation = makeSourceLocation(startToken = startToken),
					value = startToken.value!!
				)
			}

			Token.Kind.FLOAT -> {
				lexer.advance()

				Value.Float(
					sourceLocation = makeSourceLocation(startToken = startToken),
					value = startToken.value!!
				)
			}

			Token.Kind.STRING,
			Token.Kind.BLOCK_STRING ->
				parseStringValue()

			Token.Kind.NAME -> {
				lexer.advance()

				when (startToken.value) {
					"true", "false" ->
						Value.Boolean(
							sourceLocation = makeSourceLocation(startToken = startToken),
							value = startToken.value == "true"
						)

					"null" ->
						Value.Null(
							sourceLocation = makeSourceLocation(startToken = startToken)
						)

					else ->
						Value.Enum(
							name = startToken.value!!,
							sourceLocation = makeSourceLocation(startToken = startToken)
						)
				}
			}

			Token.Kind.DOLLAR ->
				if (isConstant)
					unexpectedTokenError()
				else
					parseVariable()

			else ->
				unexpectedTokenError()
		}
	}


	private fun parseVariable(): Value.Variable {
		val startToken = lexer.currentToken
		expectToken(Token.Kind.DOLLAR)
		val name = parseName()

		return Value.Variable(
			name = name,
			sourceLocation = makeSourceLocation(startToken = startToken)
		)
	}


	private fun parseVariableDefinition(): VariableDefinition {
		val startToken = lexer.currentToken
		val variable = parseVariable()
		expectToken(Token.Kind.COLON)
		val type = parseTypeReference()
		val defaultValue = expectOptionalToken(Token.Kind.EQUALS)?.let { parseValue(isConstant = true) }
		val directives = parseDirectives(isConstant = true)

		return VariableDefinition(
			defaultValue = defaultValue,
			directives = directives,
			sourceLocation = makeSourceLocation(startToken = startToken),
			type = type,
			variable = variable
		)
	}


	private fun parseVariableDefinitions() =
		optionalMany(
			Token.Kind.PAREN_L,
			this::parseVariableDefinition,
			Token.Kind.PAREN_R
		)


	private fun peek(kind: Token.Kind) =
		lexer.currentToken.kind == kind


	private fun peekDescription() =
		peek(Token.Kind.STRING) || peek(Token.Kind.BLOCK_STRING)


	private fun unexpectedTokenError(token: Token = lexer.currentToken, expected: String? = null): Nothing =
		throw GError.syntax(
			description = when (expected) {
				null -> "Unexpected $token"
				else -> "Unexpected $token, expected $expected"
			},
			position = token.startPosition
		)


	companion object {

		// FIXME allow infos about source
		fun parse(source: String) =
			Parser(source = source).parseDocument()
	}
}
