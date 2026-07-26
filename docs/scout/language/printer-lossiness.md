# What the printer still drops or refuses to print

Separates real, unrecoverable printing loss in `modules/language/sources/printing/Printer.kt` from content that never reaches the printer and from mere formatting choices; matters for any code relying on `toString()`/print round-trips.

## Unrecoverable: content the printer refuses or omits

- **Synthetic wrapper types are unprintable.** Printing a `GListType`/`GNonNullType` node hits `error("Cannot print AST of …")` — only the `*TypeRef` variants print. These are the *resolved* wrappers that `GSchema.resolveType(ref: GTypeRef)` builds, so printing a type obtained from the schema rather than from the AST fails.
- **Schema definitions with default root names print to nothing.** `writeNode(GSchemaDefinition)` returns early when the roots use the standard names and there is no description or directive. Because a definition can print empty, the `GDocument` writer decides blank-line separators by comparing writer length before and after (a comment explains index-based separation cannot work) — a non-local constraint when editing.

## Not the printer: comments are lost before parsing

The lexer produces COMMENT tokens into its doubly linked token chain, but `Lexer.lookahead()` (`modules/language/sources/parsing/Lexer.kt`) skips them in a loop, so they never reach the parser, the AST or the printer. No printer change can bring them back.

## Formatting only: nothing is lost

`Printer.print` defaults to a two-space indent, unlike the repository's tab convention and unlike `IndentingWriter`'s own tab default. Pass a different `indent` to change it.

## Already fixed

Directives on object types and object type extensions, the `extend` keyword on union extensions, `repeatable` on directive definitions, and the spacing around schema-definition directives were all printing defects and are fixed; `modules/language/tests/spec/PrinterTests.kt` now pins each. Stale comments claiming otherwise remain in `modules/dsl/tests/SchemaBuilderTests.kt` (`objectTypeWithDirective`).

Related: ../testing/printer-golden-conventions.md.
