# graphql-js is the tiebreaker wherever the specification is silent

version: graphql@17.0.2 (as of this pass) — the reference implementation this library is checked against, not a dependency.

fluid-graphql is an intentional close port of graphql-js, in behaviour *and* in structure. Where the specification leaves something open, upstream's answer is the intended answer here and a divergence is treated as the defect to fix — not as a design choice of this library. Missing that framing is how agents produce confidently wrong findings about "bugs" that are faithful ports.

The parity is visible throughout: `modules/language/tests/utility/KitchenSinkQueryFixture.kt` and `KitchenSinkSchemaFixture.kt` are upstream fixtures; execution sources name upstream functions in comments where a decision was copied (`collectFields` and `doesFragmentConditionMatch` in `DefaultSelectionSetExecutor`, `executeField` in `DefaultFieldSelectionExecutor`, `maxCoercionErrors` in `VariableInputConverter`); many tests record a verified upstream answer inline, for example `ErrorClassificationTests`, `NullPropagationTests` and `MaxCoercionErrorsTests`.

Two traps when establishing what upstream does:

- **Run it, do not read it.** Reading upstream's source has twice yielded the opposite of what executing the same input actually returns. A claim about upstream behaviour is only settled by executing it.
- **Do not render the probe's answer through `JSON.stringify`.** It prints `Infinity` as `null` and omits an `undefined` property entirely, so a rejecting call and an accepting one that returns a non-finite number look identical. A probe rendered that way nearly produced the opposite conclusion about `Float`'s literal coercion of `1e400` — the single fact one deliberate divergence rests on (../execution/spec-deviations.md). Print `typeof` and the raw value instead.
- **Compare against the matching entry point.** `graphql()` validates first, `execute()` does not, so the same document legitimately gives two different answers upstream — exactly as it does here (see ../execution/validation-entry-points.md).

Related: spec-citations.md, ../execution/error-classification.md.
