# VisitorContext types list items by the list's item type

`VisitorContext` (`modules/language/sources/visitors/VisitorContext.kt`) tracks the type each value is checked against. It matters for any rule that inspects values inside list literals, such as `VariablesInAllowedPositionRule`.

**A `GListValue` gives its items the element type.** `applyTypeState` handles `is GListValue` by setting `relatedType` to the element type of the nullable list type. Where the expected type is not a list, the item takes the nullable type itself. This follows graphql-js `TypeInfo`. Items therefore see `Int!` for `[$v]` against `[Int!]`, so a non-null variable is accepted there, and objects inside lists get their input object type.

The list's own type also becomes the items' `relatedParentType`, as graphql-js `getParentInputType` does. The enclosing input object is not the parent of a list item. So a nullable variable as an item of a OneOf field's list passes `VariablesInAllowedPositionRule`, whose OneOf check reads `relatedParentType` through `usage.parentType`.

**`relatedArgumentDefinition` is not cleared inside a list.** Items still see the enclosing argument's definition, including its default value. A rule that needs "list items never have a default" must check `parentNode is GListValue` itself, as `VariablesInAllowedPositionRule` does when it computes `locationDefaultValue`.

Related: visitor-traversal.md, visitor-api-surface.md.
