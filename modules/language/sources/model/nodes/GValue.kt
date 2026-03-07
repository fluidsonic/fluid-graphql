package io.fluidsonic.graphql


/**
 * The sealed base class for all GraphQL value AST nodes.
 *
 * Concrete subtypes: [GBooleanValue], [GEnumValue], [GFloatValue], [GIntValue], [GListValue],
 * [GNullValue], [GObjectValue], [GStringValue], [GVariableRef].
 *
 * Use [parse] to parse a standalone value literal, [kind] to identify the value type at runtime,
 * and [unwrap] to convert to a plain Kotlin value (not supported for [GVariableRef]).
 */
public sealed class GValue(
	extensions: GNodeExtensionSet<GValue>,
	origin: GDocumentPosition?,
) : GNode(
	extensions = extensions,
	origin = origin
) {

	/** Identifies the concrete kind of this value. */
	public abstract val kind: Kind

	/**
	 * Converts this value to a plain Kotlin value: `Boolean`, `Double`, `Int`, `String`,
	 * `List<Any?>`, `Map<String, Any?>`, or `null` for [GNullValue].
	 *
	 * Throws for [GVariableRef] since variables cannot be resolved at the AST level.
	 */
	public abstract fun unwrap(): Any? // FIXME not language module


	public companion object {

		/**
		 * Parses a standalone GraphQL value literal from [source].
		 *
		 * Returns a [GResult.Success] with the parsed value, or a [GResult.Failure] with parse errors.
		 */
		public fun parse(source: GDocumentSource.Parsable): GResult<GValue> =
			Parser.parseValue(source)


		public fun parse(content: String, name: String = "<value>"): GResult<GValue> =
			parse(GDocumentSource.of(content = content, name = name))
	}


	/** Identifies the kind of a [GValue] at runtime. */
	public enum class Kind {

		BOOLEAN,
		ENUM,
		FLOAT,
		INT,
		LIST,
		NULL,
		OBJECT,
		STRING,
		VARIABLE;


		override fun toString(): String = when (this) {
			BOOLEAN -> "boolean"
			ENUM -> "enum"
			FLOAT -> "float"
			INT -> "int"
			LIST -> "list"
			NULL -> "null"
			OBJECT -> "input object"
			STRING -> "string"
			VARIABLE -> "variable"
		}


		public companion object
	}
}
