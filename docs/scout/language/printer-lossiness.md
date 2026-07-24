# The printer is normalizing and lossy — parse/print round-trips lose data

What `modules/language/sources/printing/Printer.kt` silently drops or rewrites; matters for any code relying on `toString()`/print round-trips.

- **Directives on object types vanish.** `writeNode(GObjectType)` and `writeNode(GObjectTypeExtension)` never call `writeDirectives`; interface, enum, scalar, union, and input types do print theirs. A directive attached via the DSL is stored on the AST but absent from printed SDL (acknowledged in a comment in `modules/dsl/tests/SchemaBuilderTests.kt`, `objectTypeWithDirective`).
- **Union extensions print without `extend`.** `writeNode(GUnionTypeExtension)` writes `union `, so the output re-parses as a duplicate union definition.
- **Synthetic wrapper types are unprintable.** Printing `GListType`/`GNonNullType` hits `error("Cannot print AST")` — only the `*TypeRef` variants print.
- **Schema definitions with default root names print to nothing.** `writeNode(GSchemaDefinition)` emits nothing when roots use standard names with no description/directives. Because a definition can print empty, the `GDocument` writer decides blank-line separators by comparing writer length before/after (a comment explains index-based separation cannot work) — a non-local constraint when editing.
- **Comments never survive.** The Lexer produces COMMENT tokens into its doubly linked token chain, but `Lexer.lookahead()` (`modules/language/sources/parsing/Lexer.kt`) skips them in a loop, so they can never reach the parser, AST, or printer.
- `Printer.print` defaults to two-space indent, unlike the repo's tab convention and unlike `IndentingWriter`'s own tab default.

Related: ../testing/printer-golden-conventions.md.
