# Where the type system departs from the GraphQL specification

Deliberate non-standard extensions plus one modeling divergence; all of them read as errors to spec-trained eyes.

**The `@optional` directive.** `modules/language/sources/GLanguage.kt` (the `defaultOptionalDirective` property) defines a non-standard `@optional` directive that allows omitting a value for a non-null argument or input field (referencing graphql-spec issue #872). Crucially, its semantics are unconditional: `GNode.WithDefaultValue.isRequired()` (`modules/language/sources/model/nodes/GNode.kt`) returns false for any definition carrying a directive named `optional`, regardless of the `supportOptional` flag on the `GSchema` factory. That flag only controls whether the `@optional` directive definition is registered in `directiveDefinitions` — the requiredness behavior (and therefore `GSchema.validateValue`'s missing-field check) honors the directive everywhere.

**Fragment variable definitions.** The parser accepts the experimental `fragment frag($a: Int) on Query { ... }` syntax; `GFragmentDefinition` implements `GNode.WithVariableDefinitions`, and child traversal, `equalsNode`, and validation rules (variable-name exclusivity, variable-type validity) all treat fragments as variable-defining scopes. This is not in any released spec — do not mistake it for a copy-paste from `GOperationDefinition`.

**Input-object fields are modeled as "arguments".** Spec "input object fields" are `GArgument`/`GArgumentDefinition` throughout: `GObjectValue.arguments`, `GInputObjectType.argumentDefinitions`, and the ObjectField* validation rules consume argument-named APIs while their messages say "field". Spec-guided searches for field-named symbols find nothing.

**`GInputObjectType` is a `GCompositeType`.** The specification's composite types are object, interface and union only, but here `GCompositeType` (`modules/language/sources/model/nodes/GNode.kt`) has `GInputObjectType` as a fourth subclass. Its own KDoc treats the class as "types that can have selection sets applied", so every rule gating on `as? GCompositeType` accepts an input-object type condition: `FragmentTypeConditionValidityRule` waves it through, and `FragmentSelectionPossibilityRule` then reports the misleading "will never match the unrelated type" rather than rejecting the condition as invalid. `ValidatorRuleCoverageTest` deliberately uses a scalar, not an input object, to trip the former for that reason.

Related: gschema-quirks.md, type-system-extension-merging.md.
