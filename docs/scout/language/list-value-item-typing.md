# VisitorContext types list items by the list's item type

`VisitorContext` (`modules/language/sources/visitors/VisitorContext.kt`) tracks the type each value is checked against. It matters for any rule that inspects values inside list literals, such as `VariablesInAllowedPositionRule` and `ObjectFieldRequirementRule`.

**A `GListValue` gives its items the element type.** `applyTypeState` handles `is GListValue` by setting `relatedType` to the element type of the nullable list type. Where the expected type is not a list, the item takes the nullable type itself. This follows graphql-js `TypeInfo`. Items therefore see `Int!` for `[$v]` against `[Int!]`, so a non-null variable is accepted there, and objects inside lists get their input object type.

**`relatedArgumentDefinition` is not cleared inside a list.** Items still see the enclosing argument's definition, including its default value. A rule that needs "list items never have a default" must check `parentNode is GListValue` itself, as `VariablesInAllowedPositionRule` does when it computes `locationDefaultValue`.

Related: visitor-traversal.md, visitor-api-surface.md.
