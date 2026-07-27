# Printer formatting rules locked by golden-string tests

Undocumented formatting contracts any new golden-string test must reproduce exactly; recovered only by diffing inputs against expected strings.

**First establish which printer the test exercises.** `schema.toString()` and `modules/language/tests/spec/SchemaPrinterTests.kt` go through `SchemaPrinter`; `GDocument.toString()`, `Printer.print(...)` and `PrinterTests.kt` go through `Printer`. They format differently on purpose (../language/schema-printing.md), so a rule learnt from one does not transfer. The DSL suites `modules/dsl/tests/SchemaTests.kt` and `SchemaBuilderTests.kt` assert `schema.toString()` and therefore pin `SchemaPrinter` — which is why their expectations spell descriptions as block strings via a local `tripleQuote` constant, and why applied directives are absent from them.

Contracts that hold for `Printer`:

- `GDocument.toString()` output ends with a trailing newline; schema `toString()` output does not — an invisible difference that produces baffling failures.
- Definitions in a printed document are separated by TWO blank lines; type definitions in a printed schema by one.
- An enum where any value has a description prints blank lines between ALL its values; directives alone do NOT trigger this (`writeEnumValueDefinitions` in `modules/language/sources/printing/Printer.kt` checks descriptions only). `SchemaPrinter` never does this.
- Object values in documents print multiline, one field per line, with a comma ending every line except the last; list values print inline.
- Field arguments carrying descriptions print multiline with the description above the argument. More than three arguments also forces the multiline form even without descriptions (`writeArgumentDefinitions` in `Printer.kt`) — `SchemaPrinter` wraps only for descriptions.

**A round-trip test cannot catch a spacing defect.** `PrinterTests.testRoundtripKitchenSinkSchema` parses, prints, re-parses and compares with `equalsNode` — GraphQL ignores whitespace *between* tokens, so `schema  @onSchema` round-trips green. Every whitespace fix needs an exact-string assertion next to it.

Related: ../language/printer-lossiness.md, ../language/schema-printing.md.
