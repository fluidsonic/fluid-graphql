# Where the type system departs from the GraphQL specification

Deliberate non-standard extensions plus one naming divergence; both read as errors to specification-trained eyes.

**Fragment variable definitions.** The parser accepts the experimental `fragment frag($a: Int) on Query { ... }` syntax; `GFragmentDefinition` implements `GNode.WithVariableDefinitions`, and child traversal, `equalsNode`, and validation rules (variable-name exclusivity, variable-type validity) all treat fragments as variable-defining scopes. This is not in any released specification — do not mistake it for a copy-paste from `GOperationDefinition`.

**Input-object fields are modeled as "arguments".** Specification "input object fields" are `GArgument`/`GArgumentDefinition` throughout: `GObjectValue.arguments`, `GInputObjectType.argumentDefinitions`, and the ObjectField* validation rules consume argument-named APIs while their messages say "field". Specification-guided searches for field-named symbols find nothing.

**Removed in 0.19.0 — do not re-derive these from an older note.** The non-standard `@optional` directive, the `supportOptional` flag on the `GSchema` factory, and the directive-driven behaviour of `GNode.WithDefaultValue.isRequired()` are all gone; requiredness now follows solely from the declared type and default value. `GInputObjectType` is also no longer a subclass of `GCompositeType`, so the composite types are exactly the specification's three — which is why `FragmentTypeConditionValidityRule` now rejects a fragment conditioned on an input object, and `ValidatorRuleCoverageTest` uses one as that rule's fixture.

Related: gschema-quirks.md, type-system-extension-merging.md, builtin-scalar-instances.md.
