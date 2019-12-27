package tests


fun String.toKotlinRawString(indentation: String = "") =
	"$indentation\"\"\"\n" +
		replace("\$", "\${'$'}")
			.lines()
			.joinToString("\n") { "$indentation\t$it".ifBlank { "" } } +
		"\n$indentation\"\"\""
