# Two printers that deliberately disagree: Printer prints the AST, printSchema prints the schema

`printSchema`/`printType` versus `Printer`; matters for every golden string and for anyone who reads `GSchema.toString()` as a faithful copy of the source document.

`modules/language/sources/printing/SchemaPrinter.kt` (`printSchema`, `printType`) prints a *resolved* `GSchema` and matches graphql-js's `printSchema()` byte for byte. `Printer.kt` prints a `GNode` as written and must stay lossless. Their whitespace differs on purpose — the `SchemaPrinter` KDoc says so — notably: `Printer` wraps an argument list when it has descriptions **or more than three arguments**, `SchemaPrinter` only on descriptions; `Printer` blank-line-separates enum values whenever any value has a description, `SchemaPrinter` never does; `SchemaPrinter` prints every description as a block string and every default object value inline (`SdlTextPrinter.printValue`), where `Printer` puts one field per line.

**`GSchema.toString()` routes to `printSchema` with a tab indent, so it filters.** It drops built-in scalars, the introspection types, the five specified directive *definitions*, and every *applied* directive except `@deprecated`, `@oneOf` and `@specifiedBy` — a custom `@hello` on a type simply vanishes. It also emits directive definitions before types, regardless of document order, and prints a `schema { … }` block only for a description or a non-conventional root type name. `printType` is the exception that prints whatever it is handed, built-ins and introspection types included.

Pitfall: a golden test asserting `schema.toString()` pins the public shape, not the parsed document. Use `Printer.print(schema.document)` to see the latter.

Related: printer-lossiness.md, ../testing/printer-golden-conventions.md.
