package io.fluidsonic.graphql


internal class Token(
	val kind: Kind,
	val startPosition: Int,
	val endPosition: Int,
	val lineNumber: Int,
	val linePosition: Int,
	val previousToken: Token?,
	val value: String? = null,
	var nextToken: Token? = null
) {

	override fun toString() =
		if (value != null) "$kind \"$value\""
		else kind.toString()


	enum class Kind(private val label: String) {

		AMP("\"&\""),
		AT("\"@\""),
		BANG("\"!\""),
		BLOCK_STRING("BlockString"),
		BRACE_L("\"{\""),
		BRACE_R("\"}\""),
		BRACKET_L("\"[\""),
		BRACKET_R("\"]\""),
		COLON("\":\""),
		COMMENT("Comment"),
		DOLLAR("\"$\""),
		EOF("<end of input>"),
		EQUALS("\"=\""),
		FLOAT("Float"),
		INT("Int"),
		NAME("Name"),
		PAREN_L("\"(\""),
		PAREN_R("\")\""),
		PIPE("\"|\""),
		SOF("<start of input>"),
		SPREAD("\"...\""),
		STRING("String");


		override fun toString() =
			label
	}
}
