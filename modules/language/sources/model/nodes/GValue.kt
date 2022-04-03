package io.fluidsonic.graphql


public sealed class GValue(
	extensions: GNodeExtensionSet<GValue>,
	origin: GDocumentPosition?,
) : GNode(
	extensions = extensions,
	origin = origin
) {

	public abstract val kind: Kind

	public abstract fun unwrap(): Any? // FIXME not language module


	public companion object {

		public fun parse(source: GDocumentSource.Parsable): GResult<GValue> =
			Parser.parseValue(source)


		public fun parse(content: String, name: String = "<value>"): GResult<GValue> =
			parse(GDocumentSource.of(content = content, name = name))
	}


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
