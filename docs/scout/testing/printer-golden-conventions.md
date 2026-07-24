# Printer formatting rules locked by golden-string tests

Undocumented formatting contracts any new golden-string test (dsl or printer) must reproduce exactly; recovered only by diffing DSL inputs against expected strings across `modules/dsl/tests/`.

- `GDocument.toString()` output ends with a trailing newline; schema `toString()` output does not — an invisible difference that produces baffling failures.
- Definitions in a printed document are separated by TWO blank lines; type definitions in a printed schema by one.
- An enum where any value has a description prints blank lines between ALL its values; directives alone do NOT trigger this (`writeEnumValueDefinitions` in `modules/language/sources/printing/Printer.kt` checks descriptions only). A plain enum prints values on consecutive lines (see the enum cases in `SchemaBuilderTests.enumType_withDescriptionsAndDeprecation` and `SchemaTests`).
- Object values in documents print multiline, one field per line, with a comma ending every line except the last (no comma after the final field); list values print inline.
- Field arguments carrying descriptions print in a multiline parenthesized form with the description above the argument (`SchemaBuilderTests.fieldDescriptions`). More than three arguments also forces the multiline form even without descriptions (`writeArgumentDefinitions` in `Printer.kt`).

Practical workflow: these tests are exact-string comparisons, so author expectations by running the test and copying actual output rather than hand-formatting.

Remember the printer is also lossy — some AST content never prints at all; see ../language/printer-lossiness.md before concluding a golden string is wrong.
