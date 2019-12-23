package io.fluidsonic.graphql

import io.fluidsonic.graphql.GAst.*
import io.fluidsonic.graphql.Token.Kind as TokenKind


internal class Parser private constructor(
	source: GSource.Parsable
) {

	private val lexer = Lexer(source = source)


	private inline fun <T> any(
		openKind: TokenKind,
		parse: () -> T,
		closeKind: TokenKind
	): List<T> {
		expectToken(openKind)

		val elements = mutableListOf<T>()

		while (expectOptionalToken(closeKind) == null)
			elements += parse()

		return elements
	}


	private fun expectKeyword(keyword: String) {
		lexer.currentToken
			.takeIf { it.kind == TokenKind.NAME && it.value == keyword }
			?.also { lexer.advance() }
			?: unexpectedTokenError(expected = "\"$keyword\"")
	}


	private fun expectOptionalKeyword(keyword: String) =
		null != lexer.currentToken
			.takeIf { it.kind == TokenKind.NAME && it.value == keyword }
			?.also { lexer.advance() }


	private fun expectOptionalToken(kind: TokenKind) =
		lexer.currentToken
			.takeIf { it.kind == kind }
			?.also { lexer.advance() }


	private fun expectToken(kind: TokenKind) =
		lexer.currentToken
			.takeIf { it.kind == kind }
			?.also { lexer.advance() }
			?: unexpectedTokenError(expected = kind.toString())


	private fun makeOrigin(startToken: Token, endToken: Token = lexer.previousToken) =
		lexer.source.makeOrigin(
			startPosition = startToken.startPosition,
			endPosition = endToken.endPosition,
			column = startToken.startPosition - startToken.linePosition + 1,
			line = startToken.lineNumber
		)
			?: Origin(
				source = lexer.source,
				startToken = startToken,
				endToken = endToken
			)


	private inline fun <T> many(
		openKind: TokenKind,
		parse: () -> T,
		closeKind: TokenKind
	): List<T> {
		val elements = mutableListOf<T>()

		expectToken(openKind)

		do elements += parse()
		while (expectOptionalToken(closeKind) == null)

		return elements
	}


	private inline fun <T> optionalMany(
		openKind: TokenKind,
		parse: () -> T,
		closeKind: TokenKind
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
		expectToken(TokenKind.COLON)
		val value = parseValue(isConstant = isConstant)

		return Argument(
			name = name,
			origin = makeOrigin(startToken = startToken),
			value = value
		)
	}


	private fun parseArguments(isConstant: Boolean) =
		optionalMany(
			TokenKind.PAREN_L,
			{ parseArgument(isConstant) },
			TokenKind.PAREN_R
		)


	private fun parseArgumentDefinition(): ArgumentDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		expectToken(TokenKind.COLON)
		val type = parseTypeReference()
		val defaultValue = expectOptionalToken(TokenKind.EQUALS)
			?.let { parseValue(isConstant = true) }
		val directives = parseDirectives(isConstant = true)

		return ArgumentDefinition(
			defaultValue = defaultValue,
			description = description,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			type = type
		)
	}


	private fun parseArgumentDefinitions(isBlock: Boolean) =
		optionalMany(
			if (isBlock) TokenKind.BRACE_L else TokenKind.PAREN_L,
			this::parseArgumentDefinition,
			if (isBlock) TokenKind.BRACE_R else TokenKind.PAREN_R
		)


	private fun parseDefinition() =
		when {
			peek(TokenKind.NAME) ->
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

			peek(TokenKind.BRACE_L) ->
				parseOperationDefinition()

			peekDescription() ->
				parseTypeSystemDefinition()

			else ->
				unexpectedTokenError()
		}


	private fun parseDescription() =
		peekDescription().thenTake { parseStringValue() }


	private fun parseDirective(isConstant: Boolean): Directive {
		val startToken = expectToken(TokenKind.AT)
		val name = parseName()
		val arguments = parseArguments(isConstant = isConstant)

		return Directive(
			arguments = arguments,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseDirectiveDefinition(): Definition.TypeSystem.Directive {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("directive")
		expectToken(TokenKind.AT)
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
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseDirectiveLocation(): Name {
		val token = lexer.currentToken
		val name = parseName()

		if (GDirectiveLocation.values().none { it.name == name.value })
			unexpectedTokenError(token = token)

		return name
	}


	private fun parseDirectiveLocations(): List<Name> {
		val locations = mutableListOf<Name>()

		expectOptionalToken(TokenKind.PIPE)

		do locations += parseDirectiveLocation()
		while (expectOptionalToken(TokenKind.PIPE) != null)

		return locations
	}


	private fun parseDirectives(isConstant: Boolean): List<Directive> {
		val directives = mutableListOf<Directive>()

		while (peek(TokenKind.AT))
			directives += parseDirective(isConstant = isConstant)

		return directives
	}


	private fun parseDocument(): Document {
		val startToken = lexer.currentToken
		val definitions = many(
			TokenKind.SOF,
			this::parseDefinition,
			TokenKind.EOF
		)

		return Document(
			definitions = definitions,
			origin = makeOrigin(startToken = startToken)
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
			origin = makeOrigin(startToken = startToken),
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
			origin = makeOrigin(startToken = startToken),
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
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseEnumValueDefinitions() =
		optionalMany(
			TokenKind.BRACE_L,
			this::parseEnumValueDefinition,
			TokenKind.BRACE_R
		)


	private fun parseFieldSelection(): Selection.Field {
		val startToken = lexer.currentToken
		val nameOrAlias = parseName()

		val alias: Name?
		val name: Name

		if (expectOptionalToken(TokenKind.COLON) != null) {
			alias = nameOrAlias
			name = parseName()
		}
		else {
			alias = null
			name = nameOrAlias
		}

		val arguments = parseArguments(isConstant = false)
		val directives = parseDirectives(isConstant = false)
		val selectionSet = peek(TokenKind.BRACE_L).thenTake { parseSelectionSet() }

		return Selection.Field(
			arguments = arguments,
			alias = alias,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			selectionSet = selectionSet
		)
	}


	private fun parseFieldDefinition(): FieldDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		val arguments = parseArgumentDefinitions(isBlock = false)
		expectToken(TokenKind.COLON)
		val type = parseTypeReference()
		val directives = parseDirectives(isConstant = true)

		return FieldDefinition(
			arguments = arguments,
			description = description,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			type = type
		)
	}


	private fun parseFieldDefinitions() =
		optionalMany(
			TokenKind.BRACE_L,
			this::parseFieldDefinition,
			TokenKind.BRACE_R
		)


	private fun parseFragmentSelection(): Selection {
		val startToken = expectToken(TokenKind.SPREAD)

		val hasTypeCondition = expectOptionalKeyword("on")
		if (!hasTypeCondition && peek(TokenKind.NAME)) {
			val name = parseFragmentName()
			val directives = parseDirectives(isConstant = false)

			return Selection.Fragment(
				directives = directives,
				name = name,
				origin = makeOrigin(startToken = startToken)
			)
		}

		val typeCondition = hasTypeCondition.thenTake { parseNamedType() }
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return Selection.InlineFragment(
			directives = directives,
			selectionSet = selectionSet,
			origin = makeOrigin(startToken = startToken),
			typeCondition = typeCondition
		)
	}


	private fun parseFragmentDefinition(): Definition.Fragment {
		val startToken = lexer.currentToken
		expectKeyword("fragment")
		val name = parseFragmentName()
		expectKeyword("on")
		val variableDefinitions = parseVariableDefinitions()
		val typeCondition = parseNamedType()
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return Definition.Fragment(
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			selectionSet = selectionSet,
			typeCondition = typeCondition,
			variableDefinitions = variableDefinitions
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

		expectOptionalToken(TokenKind.AMP)
		do interfaces += parseNamedType()
		while (expectOptionalToken(TokenKind.AMP) != null)

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
			origin = makeOrigin(startToken = startToken)
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
			origin = makeOrigin(startToken = startToken)
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
			origin = makeOrigin(startToken = startToken)
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
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseList(isConstant: Boolean): Value.List {
		val startToken = lexer.currentToken
		val elements = any(
			TokenKind.BRACKET_L,
			{ parseValue(isConstant = isConstant) },
			TokenKind.BRACKET_R
		)

		return Value.List(
			elements = elements,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseName(): Name {
		val startToken = expectToken(TokenKind.NAME)

		return Name(
			origin = makeOrigin(startToken = startToken),
			value = startToken.value!!
		)
	}


	private fun parseNamedType(): TypeReference.Named {
		val startToken = lexer.currentToken
		val name = parseName()

		return TypeReference.Named(
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseObject(isConstant: Boolean): Value.Object {
		val startToken = lexer.currentToken
		val fields = any(
			TokenKind.BRACE_L,
			{ parseObjectField(isConstant = isConstant) },
			TokenKind.BRACE_R
		)

		return Value.Object(
			fields = fields,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseObjectField(isConstant: Boolean): Value.Object.Field {
		val startToken = lexer.currentToken
		val name = parseName()
		expectToken(TokenKind.COLON)
		val value = parseValue(isConstant = isConstant)

		return Value.Object.Field(
			name = name,
			origin = makeOrigin(startToken = startToken),
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
			origin = makeOrigin(startToken = startToken)
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
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseOperationDefinition(): Definition.Operation {
		val startToken = lexer.currentToken

		if (peek(TokenKind.BRACE_L)) {
			val selectionSet = parseSelectionSet()

			return Definition.Operation(
				directives = emptyList(),
				name = null,
				origin = makeOrigin(startToken = startToken),
				selectionSet = selectionSet,
				type = GOperationType.query,
				variableDefinitions = emptyList()
			)
		}

		val type = parseOperationType()
		val name = peek(TokenKind.NAME).thenTake { parseName() }
		val variableDefinitions = parseVariableDefinitions()
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return Definition.Operation(
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			selectionSet = selectionSet,
			type = type,
			variableDefinitions = variableDefinitions
		)
	}


	private fun parseOperationType(): GOperationType {
		val token = expectToken(TokenKind.NAME)

		return when (token.value) {
			"mutation" -> GOperationType.mutation
			"query" -> GOperationType.query
			"subscription" -> GOperationType.subscription
			else -> unexpectedTokenError(token)
		}
	}


	private fun parseOperationTypeDefinition(): OperationTypeDefinition {
		val startToken = lexer.currentToken
		val operation = parseOperationType()
		expectToken(TokenKind.COLON)
		val type = parseNamedType()

		return OperationTypeDefinition(
			operation = operation,
			origin = makeOrigin(startToken = startToken),
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
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseSchemaExtension(): Definition.TypeSystemExtension.Schema {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("schema")
		val directives = parseDirectives(isConstant = true)
		val operationTypes = optionalMany(
			TokenKind.BRACE_L,
			this::parseOperationTypeDefinition,
			TokenKind.BRACE_R
		)

		if (directives.isEmpty() && operationTypes.isEmpty())
			unexpectedTokenError()

		return Definition.TypeSystemExtension.Schema(
			directives = directives,
			operationTypes = operationTypes,
			origin = makeOrigin(startToken = startToken)
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
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseSchemaDefinition(): Definition.TypeSystem.Schema {
		val startToken = lexer.currentToken
		expectKeyword("schema")
		val directives = parseDirectives(isConstant = true)
		val operationTypes = many(
			TokenKind.BRACE_L,
			this::parseOperationTypeDefinition,
			TokenKind.BRACE_R
		)

		return Definition.TypeSystem.Schema(
			directives = directives,
			operationTypes = operationTypes,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseSelection() =
		if (peek(TokenKind.SPREAD)) parseFragmentSelection()
		else parseFieldSelection()


	private fun parseSelectionSet(): SelectionSet {
		val startToken = lexer.currentToken
		val selections = many(
			TokenKind.BRACE_L,
			this::parseSelection,
			TokenKind.BRACE_R
		)

		return SelectionSet(
			origin = makeOrigin(startToken = startToken),
			selections = selections
		)
	}


	private fun parseStringValue(): Value.String {
		val startToken = lexer.currentToken
		lexer.advance()

		return Value.String(
			isBlock = startToken.kind == TokenKind.BLOCK_STRING,
			origin = makeOrigin(startToken = startToken),
			value = startToken.value!!
		)
	}


	private fun parseTypeReference(): TypeReference {
		val startToken = lexer.currentToken

		var type: TypeReference
		if (expectOptionalToken(TokenKind.BRACKET_L) != null) {
			type = parseTypeReference()
			expectToken(TokenKind.BRACKET_R)

			type = TypeReference.List(
				elementType = type,
				origin = makeOrigin(startToken = startToken)
			)
		}
		else
			type = parseNamedType()

		if (expectOptionalToken(TokenKind.BANG) != null)
			type = TypeReference.NonNull(
				nullableType = type,
				origin = makeOrigin(startToken = startToken)
			)

		return type
	}


	private fun parseTypeSystemDefinition(): Definition.TypeSystem {
		val keywordToken =
			if (peekDescription()) lexer.lookahead()
			else lexer.currentToken

		if (keywordToken.kind != TokenKind.NAME)
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
		if (keywordToken.kind != TokenKind.NAME)
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
		if (expectOptionalToken(TokenKind.EQUALS) == null)
			return emptyList()

		val types = mutableListOf<TypeReference.Named>()

		expectOptionalToken(TokenKind.PIPE)
		do types += parseNamedType()
		while (expectOptionalToken(TokenKind.PIPE) != null)

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
			origin = makeOrigin(startToken = startToken),
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
			origin = makeOrigin(startToken = startToken),
			types = types
		)
	}


	private fun parseValue(isConstant: Boolean): Value {
		val startToken = lexer.currentToken

		return when (startToken.kind) {
			TokenKind.BRACKET_L ->
				parseList(isConstant)

			TokenKind.BRACE_L ->
				parseObject(isConstant)

			TokenKind.INT -> {
				lexer.advance()

				Value.Int(
					origin = makeOrigin(startToken = startToken),
					value = startToken.value!!
				)
			}

			TokenKind.FLOAT -> {
				lexer.advance()

				Value.Float(
					origin = makeOrigin(startToken = startToken),
					value = startToken.value!!
				)
			}

			TokenKind.STRING,
			TokenKind.BLOCK_STRING ->
				parseStringValue()

			TokenKind.NAME -> {
				lexer.advance()

				when (startToken.value) {
					"true", "false" ->
						Value.Boolean(
							origin = makeOrigin(startToken = startToken),
							value = startToken.value == "true"
						)

					"null" ->
						Value.Null(
							origin = makeOrigin(startToken = startToken)
						)

					else ->
						Value.Enum(
							name = startToken.value!!,
							origin = makeOrigin(startToken = startToken)
						)
				}
			}

			TokenKind.DOLLAR ->
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
		expectToken(TokenKind.DOLLAR)
		val name = parseName()

		return Value.Variable(
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseVariableDefinition(): VariableDefinition {
		val startToken = lexer.currentToken
		val variable = parseVariable()
		expectToken(TokenKind.COLON)
		val type = parseTypeReference()
		val defaultValue = expectOptionalToken(TokenKind.EQUALS)?.let { parseValue(isConstant = true) }
		val directives = parseDirectives(isConstant = true)

		return VariableDefinition(
			defaultValue = defaultValue,
			directives = directives,
			origin = makeOrigin(startToken = startToken),
			type = type,
			variable = variable
		)
	}


	private fun parseVariableDefinitions() =
		optionalMany(
			TokenKind.PAREN_L,
			this::parseVariableDefinition,
			TokenKind.PAREN_R
		)


	private fun peek(kind: TokenKind) =
		lexer.currentToken.kind == kind


	private fun peekDescription() =
		peek(TokenKind.STRING) || peek(TokenKind.BLOCK_STRING)


	private fun unexpectedTokenError(token: Token = lexer.currentToken, expected: String? = null): Nothing =
		throw GError.syntax(
			description = when (expected) {
				null -> "Unexpected $token."
				else -> "Expected $expected, found $token."
			},
			origin = makeOrigin(startToken = token, endToken = token)
		)


	companion object {

		fun parseDocument(source: GSource.Parsable) =
			Parser(source = source).parseDocument()


		fun parseTypeReference(source: GSource.Parsable): TypeReference {
			val parser = Parser(source = source)
			parser.expectToken(TokenKind.SOF)
			val reference = parser.parseTypeReference()
			parser.expectToken(TokenKind.EOF)

			return reference
		}


		fun parseValue(source: GSource.Parsable): Value {
			val parser = Parser(source = source)
			parser.expectToken(TokenKind.SOF)
			val value = parser.parseValue(isConstant = true)
			parser.expectToken(TokenKind.EOF)

			return value
		}
	}


	private data class Origin(
		override val source: GSource,
		val startToken: Token,
		val endToken: Token
	) : GOrigin {

		override val column: Int
			get() = startToken.startPosition - startToken.linePosition + 1


		override val endPosition
			get() = endToken.endPosition


		override val line
			get() = startToken.lineNumber


		override val startPosition
			get() = startToken.startPosition


		override fun toString() =
			"$startPosition .. ${endPosition - 1}"
	}
}
