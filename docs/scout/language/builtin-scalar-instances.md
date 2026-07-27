# Built-in scalars are per-schema instances, reference-gated, and unreachable for customisation

How `Boolean`/`Float`/`ID`/`Int`/`String` enter a `GSchema`; matters for type comparisons across schemas, for `__schema.types` expectations, and before trying to customise one.

`GType.defaultTypes()` (`modules/language/sources/model/nodes/GNode.kt`) is a **factory**: every call mints five fresh instances, and `GSchema.types` calls it per schema. The KDoc on `GScalarType` gives the reason — a coercer attaches to a *node* through its `GNodeExtensionSet`, so a shared singleton would leak one schema's coercer into every other schema in the process. Because reference identity is therefore useless, `GScalarType.equals` compares by **name and class** (a `GCustomScalarType` named `Int` stays distinct from a `GIntType`).

**Only `Int`, `Float` and `ID` are genuinely reference-gated.** `GSchema.types` includes a built-in only when `referencedTypeNames` finds it mentioned by some type or directive definition — but the built-in directives take `if: Boolean!` and `String` arguments, and the introspection types use both, so `Boolean` and `String` are listed in every schema regardless.

**You cannot attach a coercer to a built-in scalar through the public API.** `validateTypeNames` refuses any definition named after a built-in (`Cannot redefine built-in scalar type "…"`), and `GSchema.types` additionally filters such definitions out in favour of its own fresh instance. The internal `GSchema(document, allowsReservedTypeNames = true)` overload exists only to reach that filtering path.

Related: scalar-coercion.md, ast-equality.md, gschema-quirks.md.
