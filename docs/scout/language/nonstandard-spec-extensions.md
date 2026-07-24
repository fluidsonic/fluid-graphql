# Non-standard GraphQL extensions this library supports

Deviations from the released GraphQL specifications that look like errors to spec-trained eyes.

**The `@optional` directive.** `modules/language/sources/GLanguage.kt` (the `defaultOptionalDirective` property) defines a non-standard `@optional` directive that allows omitting a value for a non-null argument or input field (referencing graphql-spec issue #872). Crucially, its semantics are unconditional: `GNode.WithDefaultValue.isRequired()` (`modules/language/sources/model/nodes/GNode.kt`) returns false for any definition carrying a directive named `optional`, regardless of the `supportOptional` flag on the `GSchema` factory. That flag only controls whether the `@optional` directive definition is registered in `directiveDefinitions` — the requiredness behavior (and therefore `GSchema.validateValue`'s missing-field check) honors the directive everywhere.

**Fragment variable definitions.** The parser accepts the experimental `fragment frag($a: Int) on Query { ... }` syntax; `GFragmentDefinition` implements `GNode.WithVariableDefinitions`, and child traversal, `equalsNode`, and validation rules (variable-name exclusivity, variable-type validity) all treat fragments as variable-defining scopes. This is not in any released spec — do not mistake it for a copy-paste from `GOperationDefinition`.

**Input-object fields are modeled as "arguments".** Spec "input object fields" are `GArgument`/`GArgumentDefinition` throughout: `GObjectValue.arguments`, `GInputObjectType.argumentDefinitions`, and the ObjectField* validation rules consume argument-named APIs while their messages say "field". Spec-guided searches for field-named symbols find nothing.

Related: gschema-quirks.md.
