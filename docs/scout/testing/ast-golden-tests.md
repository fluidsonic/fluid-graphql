# AST and visitor golden tests: exclusive-end ranges, origin wildcards, graphql-js lineage

Hidden wiring behind the language module's golden tests; matters when writing position assertions or regenerating expectations.

**Origin ranges use an EXCLUSIVE end.** `assertAt(0 .. 41)` and all `AstBuilder` origins treat `range.last` as the exclusive endPosition, inverting normal Kotlin `IntRange` semantics. The only documentation is a comment in `modules/language/tests/utility/AstAssertions.kt` ("exclusive end ... for sake of readability"). Inclusive-range intuition produces off-by-one failures.

**Origin-including AST comparison needs the makeSource path.** Two-arg `assertAst` compares with `equalsNode(includingOrigin = true)`. It works only because the test source is a custom `GDocumentSource.Parsable` whose `makeOrigin()` override makes the parser produce `AstBuilder.DocumentPosition` instances (the parser consults the source's `makeOrigin` first, token positions are just fallback), and because `DocumentPosition.equals` treats negative line/column on either side as a wildcard. New origin-including tests must go through the same wiring or never match.

**graphql-js is the reference.** Parsing test files carry header comments naming the graphql-js test files they port; asserted error messages follow graphql-js wording. Kitchen-sink fixtures are Kotlin `const val` raw strings escaping `$` as `${'$'}` and `"""` per KT-2425 — reproduce that noise when editing.

**Visitor golden stacks are paste-able.** `makePrettyStacks` renders stacks as literal `listOf(...)` lines so a failure prints the replacement expected value verbatim. BOM handling is deliberately unpinned (`SourceTextTests.testUnicodeBOM` accepts both parse and error).

Related: test-packages-and-helpers.md, ../language/ast-equality.md.
