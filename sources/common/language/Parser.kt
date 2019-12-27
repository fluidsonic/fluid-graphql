package io.fluidsonic.graphql

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


	private fun parseArgument(isConstant: Boolean): GArgument {
		val startToken = lexer.currentToken
		val name = parseName()
		expectToken(TokenKind.COLON)
		val value = parseValue(isConstant = isConstant)

		return GArgument(
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


	private fun parseArgumentDefinition(definitionType: ArgumentDefinitionType): GArgumentDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		expectToken(TokenKind.COLON)
		val type = parseTypeReference()
		val defaultValue = expectOptionalToken(TokenKind.EQUALS)
			?.let { parseValue(isConstant = true) }
		val directives = parseDirectives(isConstant = true)

		return when (definitionType) {
			ArgumentDefinitionType.directiveDefinition ->
				GDirectiveArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					origin = makeOrigin(startToken = startToken),
					type = type
				)

			ArgumentDefinitionType.fieldDefinition ->
				GFieldArgumentDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					origin = makeOrigin(startToken = startToken),
					type = type
				)

			ArgumentDefinitionType.inputField ->
				GInputFieldDefinition(
					defaultValue = defaultValue,
					description = description,
					directives = directives,
					name = name,
					origin = makeOrigin(startToken = startToken),
					type = type
				)
		}
	}


	private fun parseArgumentDefinitions(isBlock: Boolean, definitionType: ArgumentDefinitionType) =
		optionalMany(
			if (isBlock) TokenKind.BRACE_L else TokenKind.PAREN_L,
			{ parseArgumentDefinition(definitionType) },
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


	private fun parseDirective(isConstant: Boolean): GDirective {
		val startToken = expectToken(TokenKind.AT)
		val name = parseName()
		val arguments = parseArguments(isConstant = isConstant)

		return GDirective(
			arguments = arguments,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	@Suppress("UNCHECKED_CAST")
	private fun parseDirectiveDefinition(): GDirectiveDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("directive")
		expectToken(TokenKind.AT)
		val name = parseName()
		val arguments = parseArgumentDefinitions(isBlock = false, definitionType = ArgumentDefinitionType.directiveDefinition)
			as List<GDirectiveArgumentDefinition>
		val isRepeatable = expectOptionalKeyword("repeatable")
		expectKeyword("on")
		val locations = parseDirectiveLocations()

		return GDirectiveDefinition(
			argumentDefinitions = arguments,
			description = description,
			isRepeatable = isRepeatable,
			locations = locations,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseDirectiveLocation(): GName {
		val token = lexer.currentToken
		val name = parseName()

		if (GDirectiveLocation.values().none { it.name == name.value })
			unexpectedTokenError(token = token)

		return name
	}


	private fun parseDirectiveLocations(): List<GName> {
		val locations = mutableListOf<GName>()

		expectOptionalToken(TokenKind.PIPE)

		do locations += parseDirectiveLocation()
		while (expectOptionalToken(TokenKind.PIPE) != null)

		return locations
	}


	private fun parseDirectives(isConstant: Boolean): List<GDirective> {
		val directives = mutableListOf<GDirective>()

		while (peek(TokenKind.AT))
			directives += parseDirective(isConstant = isConstant)

		return directives
	}


	private fun parseDocument(): GDocument {
		val startToken = lexer.currentToken
		val definitions = many(
			TokenKind.SOF,
			this::parseDefinition,
			TokenKind.EOF
		)

		return GDocument(
			definitions = definitions,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseEnumTypeDefinition(): GEnumType {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("enum")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val values = parseEnumValueDefinitions()

		return GEnumType(
			description = description,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			values = values
		)
	}


	private fun parseEnumTypeExtension(): GEnumTypeExtension {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("enum")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val values = parseEnumValueDefinitions()

		if (directives.isEmpty() && values.isEmpty())
			unexpectedTokenError()

		return GEnumTypeExtension(
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			values = values
		)
	}


	private fun parseEnumValueDefinition(): GEnumValueDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		val directives = parseDirectives(isConstant = true)

		return GEnumValueDefinition(
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


	private fun parseFieldSelection(): GFieldSelection {
		val startToken = lexer.currentToken
		val nameOrAlias = parseName()

		val alias: GName?
		val name: GName

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

		return GFieldSelection(
			arguments = arguments,
			alias = alias,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			selectionSet = selectionSet
		)
	}


	@Suppress("UNCHECKED_CAST")
	private fun parseFieldDefinition(): GFieldDefinition {
		val startToken = lexer.currentToken
		val description = parseDescription()
		val name = parseName()
		val arguments = parseArgumentDefinitions(isBlock = false, definitionType = ArgumentDefinitionType.fieldDefinition)
			as List<GFieldArgumentDefinition>
		expectToken(TokenKind.COLON)
		val type = parseTypeReference()
		val directives = parseDirectives(isConstant = true)

		return GFieldDefinition(
			argumentDefinitions = arguments,
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


	private fun parseFragmentSelection(): GSelection {
		val startToken = expectToken(TokenKind.SPREAD)

		val hasTypeCondition = expectOptionalKeyword("on")
		if (!hasTypeCondition && peek(TokenKind.NAME)) {
			val name = parseFragmentName()
			val directives = parseDirectives(isConstant = false)

			return GFragmentSelection(
				directives = directives,
				name = name,
				origin = makeOrigin(startToken = startToken)
			)
		}

		val typeCondition = hasTypeCondition.thenTake { parseNamedType() }
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return GInlineFragmentSelection(
			directives = directives,
			selectionSet = selectionSet,
			origin = makeOrigin(startToken = startToken),
			typeCondition = typeCondition
		)
	}


	private fun parseFragmentDefinition(): GFragmentDefinition {
		val startToken = lexer.currentToken
		expectKeyword("fragment")
		val name = parseFragmentName()
		val variableDefinitions = parseVariableDefinitions()
		expectKeyword("on")
		val typeCondition = parseNamedType()
		val directives = parseDirectives(isConstant = false)
		val selectionSet = parseSelectionSet()

		return GFragmentDefinition(
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


	private fun parseImplementsInterfaces(): List<GNamedTypeRef> {
		if (!expectOptionalKeyword("implements"))
			return emptyList()

		val interfaces = mutableListOf<GNamedTypeRef>()

		expectOptionalToken(TokenKind.AMP)
		do interfaces += parseNamedType()
		while (expectOptionalToken(TokenKind.AMP) != null)

		return interfaces
	}


	@Suppress("UNCHECKED_CAST")
	private fun parseInputObjectTypeDefinition(): GInputObjectType {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("input")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val arguments = parseArgumentDefinitions(isBlock = true, definitionType = ArgumentDefinitionType.inputField)
			as List<GInputFieldDefinition>

		return GInputObjectType(
			argumentDefinitions = arguments,
			description = description,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	@Suppress("UNCHECKED_CAST")
	private fun parseInputObjectTypeExtension(): GInputObjectTypeExtension {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("input")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val arguments = parseArgumentDefinitions(isBlock = true, definitionType = ArgumentDefinitionType.inputField)
			as List<GInputFieldDefinition>

		if (directives.isEmpty() && arguments.isEmpty())
			unexpectedTokenError()

		return GInputObjectTypeExtension(
			argumentDefinitions = arguments,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseInterfaceTypeDefinition(): GInterfaceType {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("interface")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(isConstant = true)
		val fields = parseFieldDefinitions()

		return GInterfaceType(
			description = description,
			directives = directives,
			fieldDefinitions = fields,
			interfaces = interfaces,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseInterfaceTypeExtension(): GInterfaceTypeExtension {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("interface")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(isConstant = true)
		val fields = parseFieldDefinitions()

		if (interfaces.isEmpty() && directives.isEmpty() && fields.isEmpty())
			unexpectedTokenError()

		return GInterfaceTypeExtension(
			directives = directives,
			fieldDefinitions = fields,
			interfaces = interfaces,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseList(isConstant: Boolean): GValue.List {
		val startToken = lexer.currentToken
		val elements = any(
			TokenKind.BRACKET_L,
			{ parseValue(isConstant = isConstant) },
			TokenKind.BRACKET_R
		)

		return GValue.List(
			elements = elements,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseName(): GName {
		val startToken = expectToken(TokenKind.NAME)

		return GName(
			origin = makeOrigin(startToken = startToken),
			value = startToken.value!!
		)
	}


	private fun parseNamedType(): GNamedTypeRef {
		val startToken = lexer.currentToken
		val name = parseName()

		return GNamedTypeRef(
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseObject(isConstant: Boolean): GValue.Object {
		val startToken = lexer.currentToken
		val fields = any(
			TokenKind.BRACE_L,
			{ parseObjectField(isConstant = isConstant) },
			TokenKind.BRACE_R
		)

		return GValue.Object(
			fields = fields,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseObjectField(isConstant: Boolean): GObjectValueField {
		val startToken = lexer.currentToken
		val name = parseName()
		expectToken(TokenKind.COLON)
		val value = parseValue(isConstant = isConstant)

		return GObjectValueField(
			name = name,
			origin = makeOrigin(startToken = startToken),
			value = value
		)
	}


	private fun parseObjectTypeDefinition(): GObjectType {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("type")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(isConstant = true)
		val fields = parseFieldDefinitions()

		return GObjectType(
			description = description,
			directives = directives,
			fieldDefinitions = fields,
			interfaces = interfaces,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseObjectTypeExtension(): GObjectTypeExtension {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("type")
		val name = parseName()
		val interfaces = parseImplementsInterfaces()
		val directives = parseDirectives(true)
		val fields = parseFieldDefinitions()

		if (interfaces.isEmpty() && directives.isEmpty() && fields.isEmpty())
			unexpectedTokenError()

		return GObjectTypeExtension(
			directives = directives,
			fieldDefinitions = fields,
			interfaces = interfaces,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseOperationDefinition(): GOperationDefinition {
		val startToken = lexer.currentToken

		if (peek(TokenKind.BRACE_L)) {
			val selectionSet = parseSelectionSet()

			return GOperationDefinition(
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

		return GOperationDefinition(
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


	private fun parseOperationTypeDefinition(): GOperationTypeDefinition {
		val startToken = lexer.currentToken
		val operation = parseOperationType()
		expectToken(TokenKind.COLON)
		val type = parseNamedType()

		return GOperationTypeDefinition(
			operationType = operation,
			origin = makeOrigin(startToken = startToken),
			type = type
		)
	}


	private fun parseScalarTypeExtension(): GScalarTypeExtension {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("scalar")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)

		if (directives.isEmpty())
			unexpectedTokenError()

		return GScalarTypeExtension(
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseSchemaExtension(): GSchemaExtensionDefinition {
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

		return GSchemaExtensionDefinition(
			directives = directives,
			operationTypeDefinitions = operationTypes,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseScalarTypeDefinition(): GCustomScalarType {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("scalar")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)

		return GCustomScalarType(
			description = description,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseSchemaDefinition(): GSchemaDefinition {
		val startToken = lexer.currentToken
		expectKeyword("schema")
		val directives = parseDirectives(isConstant = true)
		val operationTypes = many(
			TokenKind.BRACE_L,
			this::parseOperationTypeDefinition,
			TokenKind.BRACE_R
		)

		return GSchemaDefinition(
			directives = directives,
			operationTypeDefinitions = operationTypes,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseSelection() =
		if (peek(TokenKind.SPREAD)) parseFragmentSelection()
		else parseFieldSelection()


	private fun parseSelectionSet(): GSelectionSet {
		val startToken = lexer.currentToken
		val selections = many(
			TokenKind.BRACE_L,
			this::parseSelection,
			TokenKind.BRACE_R
		)

		return GSelectionSet(
			origin = makeOrigin(startToken = startToken),
			selections = selections
		)
	}


	private fun parseStringValue(): GValue.String {
		val startToken = lexer.currentToken
		lexer.advance()

		return GValue.String(
			isBlock = startToken.kind == TokenKind.BLOCK_STRING,
			origin = makeOrigin(startToken = startToken),
			value = startToken.value!!
		)
	}


	private fun parseTypeReference(): GTypeRef {
		val startToken = lexer.currentToken

		var type: GTypeRef
		if (expectOptionalToken(TokenKind.BRACKET_L) != null) {
			type = parseTypeReference()
			expectToken(TokenKind.BRACKET_R)

			type = GListTypeRef(
				elementType = type,
				origin = makeOrigin(startToken = startToken)
			)
		}
		else
			type = parseNamedType()

		if (expectOptionalToken(TokenKind.BANG) != null)
			type = GNonNullTypeRef(
				nullableType = type,
				origin = makeOrigin(startToken = startToken)
			)

		return type
	}


	private fun parseTypeSystemDefinition(): GTypeSystemDefinition {
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


	private fun parseTypeSystemExtension(): GTypeSystemExtensionDefinition {
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


	private fun parseUnionMemberTypes(): List<GNamedTypeRef> {
		if (expectOptionalToken(TokenKind.EQUALS) == null)
			return emptyList()

		val types = mutableListOf<GNamedTypeRef>()

		expectOptionalToken(TokenKind.PIPE)
		do types += parseNamedType()
		while (expectOptionalToken(TokenKind.PIPE) != null)

		return types
	}


	private fun parseUnionTypeDefinition(): GUnionType {
		val startToken = lexer.currentToken
		val description = parseDescription()
		expectKeyword("union")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val types = parseUnionMemberTypes()

		return GUnionType(
			description = description,
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			possibleTypes = types
		)
	}


	private fun parseUnionTypeExtension(): GUnionTypeExtension {
		val startToken = lexer.currentToken
		expectKeyword("extend")
		expectKeyword("union")
		val name = parseName()
		val directives = parseDirectives(isConstant = true)
		val types = parseUnionMemberTypes()

		if (directives.isEmpty() && types.isEmpty())
			unexpectedTokenError()

		return GUnionTypeExtension(
			directives = directives,
			name = name,
			origin = makeOrigin(startToken = startToken),
			possibleTypes = types
		)
	}


	private fun parseValue(isConstant: Boolean): GValue {
		val startToken = lexer.currentToken

		return when (startToken.kind) {
			TokenKind.BRACKET_L ->
				parseList(isConstant)

			TokenKind.BRACE_L ->
				parseObject(isConstant)

			TokenKind.FLOAT -> {
				val stringValue = startToken.value!!
				val floatValue = try {
					stringValue.toFloat()
				}
				catch (e: NumberFormatException) {
					throw GError.syntax(
						description = "Invalid Float value '$stringValue'",
						origin = makeOrigin(startToken = startToken, endToken = startToken)
					)
				}

				lexer.advance()

				GValue.Float(
					origin = makeOrigin(startToken = startToken),
					value = floatValue
				)
			}

			TokenKind.INT -> {
				val stringValue = startToken.value!!
				val intValue = try {
					stringValue.toInt()
				}
				catch (e: NumberFormatException) {
					throw GError.syntax(
						description = "Invalid Int value '$stringValue'",
						origin = makeOrigin(startToken = startToken, endToken = startToken)
					)
				}

				lexer.advance()

				GValue.Int(
					origin = makeOrigin(startToken = startToken),
					value = intValue
				)
			}

			TokenKind.STRING,
			TokenKind.BLOCK_STRING ->
				parseStringValue()

			TokenKind.NAME -> {
				lexer.advance()

				when (startToken.value) {
					"true", "false" ->
						GValue.Boolean(
							origin = makeOrigin(startToken = startToken),
							value = startToken.value == "true"
						)

					"null" ->
						GValue.Null(
							origin = makeOrigin(startToken = startToken)
						)

					else ->
						GValue.Enum(
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


	private fun parseVariable(): GValue.Variable {
		val startToken = lexer.currentToken
		expectToken(TokenKind.DOLLAR)
		val name = parseName()

		return GValue.Variable(
			name = name,
			origin = makeOrigin(startToken = startToken)
		)
	}


	private fun parseVariableDefinition(): GVariableDefinition {
		val startToken = lexer.currentToken
		val variable = parseVariable()
		expectToken(TokenKind.COLON)
		val type = parseTypeReference()
		val defaultValue = expectOptionalToken(TokenKind.EQUALS)?.let { parseValue(isConstant = true) }
		val directives = parseDirectives(isConstant = true)

		return GVariableDefinition(
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


		fun parseTypeReference(source: GSource.Parsable): GTypeRef {
			val parser = Parser(source = source)
			parser.expectToken(TokenKind.SOF)
			val reference = parser.parseTypeReference()
			parser.expectToken(TokenKind.EOF)

			return reference
		}


		fun parseValue(source: GSource.Parsable): GValue {
			val parser = Parser(source = source)
			parser.expectToken(TokenKind.SOF)
			val value = parser.parseValue(isConstant = true)
			parser.expectToken(TokenKind.EOF)

			return value
		}
	}


	private enum class ArgumentDefinitionType {

		directiveDefinition,
		fieldDefinition,
		inputField
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
