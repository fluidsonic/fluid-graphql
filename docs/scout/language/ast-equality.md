# AST equality is two-tier; type relations require the same GSchema instance

How to compare `GNode` values correctly; matters for any code using `==`, `equalsNode`, or `isSubtypeOf`.

Structural comparison must go through `GNode.equalsNode(other, includingOrigin)` (`modules/language/sources/model/nodes/GNode.kt`). Most node classes (`GFieldSelection`, `GDocument`, `GObjectType`, ...) do not override `equals()`, so `==` on them is reference identity. Only leaf-ish nodes override `equals()` — `GName`, the `G*Value` types, the `*Ref` types, `GPath` — and those always ignore origin and extensions.

The tiers diverge subtly: `GStringValue.equals` ignores `isBlock` while `GStringValue.equalsNode` compares it, so two GStringValues that are `==` can fail `equalsNode` even with `includingOrigin = false`.

`GType.isSupertypeOf`/`isSubtypeOf` mix two mechanisms: direct type comparison is instance identity (`===`, or default `==` for scalars), while interface implementation and union membership are checked by name against `GNamedTypeRef` lists (`GInterfaceType.isSupertypeOf`, `GUnionType.isSupertypeOf`). So identity-based checks only work between `GType` instances resolved from the same `GSchema` — object, enum, input-object, and custom scalar types from two separately parsed schemas are unrelated (`isSubtypeOf` returns false negatives) — but built-in scalars (`GBooleanType` etc.) are shared singleton objects that match everywhere, and name-based interface/union membership succeeds even across schemas.

Pitfall: code that mixes ASTs or schemas from separate parses will silently misbehave in both directions — false negatives on direct type identity, and `==` on value nodes giving false positives against expectations formed from a different parse. Tests compare golden ASTs via `equalsNode(includingOrigin = true)` with special origin wiring — see ../testing/ast-golden-tests.md.
