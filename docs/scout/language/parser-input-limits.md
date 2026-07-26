# Parser limits: numeric range is a syntax error, token count is opt-in

Two ways the parser rejects input that the GraphQL grammar itself accepts; matters when parsing untrusted documents and when comparing parse behaviour against graphql-js.

**Numeric range is enforced at parse time, and that is a structural divergence from upstream.** `GFloatValue` (`modules/language/sources/model/nodes/GNode.kt`) stores a Kotlin `Double` and `check`s `isFinite()` in its `init`; `GIntValue` stores an `Int`. So `Parser.parseValue` must reject a grammatically valid *FloatValue* such as `1e400` — it rounds to infinity — and any *IntValue* outside 32 bits, both as `GError.syntax` ("Invalid Float value '…'."). graphql-js keeps `FloatValue.value` as a **string** and converts only during coercion, so it has no equivalent failure mode; do not expect its parser to agree here.

**`maxTokens` is a per-call, opt-in cap.** `GDocument.parse(source, maxTokens)` defaults to `null`, meaning unlimited; the executor's `documentSource` overloads never pass it, so requests through `GExecutor` are uncapped. Enforcement lives in `Lexer.readCountedToken`, which charges every token except comments and end-of-input — whitespace and commas never become tokens, and a whole block string counts as one — and aborts with a syntax error once the cap is exceeded. Because a document of exactly `maxTokens` tokens still parses, off-by-one expectations in tests matter (`modules/language/tests/parsing/LexerTests.kt`).

The cap does not bound recursion: parsing is recursive descent, so deeply nested input can still exhaust the stack.

Related: ../spec/graphql-js-parity.md, printer-lossiness.md.
