# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/).


## [0.18.0] - 2026-07-27

Validation now does what it claimed to. Several rules were partly or wholly inert, and the ones that
ran were unsafe on deeply nested or hostile input — which matters because `GDocument.validate` runs
on every request in a server. **Documents that passed validation before may now be rejected.** The
public API is unchanged, so nothing needs recompiling to pick this up.

### Added

- `extend type`, `extend interface`, `extend union`, `extend enum`, `extend input`, `extend scalar`
  and `extend schema` are merged into the schema. They were parsed, kept on `GSchema.document`, and
  then silently discarded, so extending a type had no effect at all. Members accumulate in document
  order, two extensions of one target both apply, and a member that collides with an existing one
  replaces it in place. Extending a type the document does not define, or a built-in scalar, is
  ignored rather than reported — reporting it needs schema validation, which does not exist yet.
- Validation stops after 100 errors and appends `Too many validation errors, error limit reached.
  Validation aborted.`, matching graphql-js. A document with thousands of violations is rejected
  either way and no client can act on the rest, so the remaining work is wasted.

### Changed

- **Breaking.** Field selection merging (§5.3.2) and leaf field selections (§5.3.3) report
  graphql-js's messages, error counts and error locations. Merging reports one error per conflicting
  pair, so three mutually conflicting fields now yield three errors rather than one. §5.3.3 had two
  overlapping rules reporting the same violation twice with different wording and now has one, so
  those documents produce one error instead of two. Anything matching on message text breaks.
- **Breaking.** Errors are reported in document order. They used to be grouped by rule, because each
  rule walked the document separately.
- `GSchema.toString()` reflects merged extensions rather than printing the unmerged document.
- Deeply nested documents validate. Nesting cost one call frame per rule per level, so validation
  gave out well before the parser did; the traversal is now iterative and the parser is the binding
  limit.
- Validation walks the document once for all rules instead of once per rule.

### Fixed

- Field selection merging checked only the first selection set of the first definition in a
  document. Conflicts nested inside a field, and everything in every later operation or fragment,
  went unreported.
- A fragment that spreads itself crashed validation with a `StackOverflowError` instead of being
  reported as a cycle — reachable from untrusted input.
- Reordering the fields of an input object argument was reported as a conflict, though a different
  field order is the same value.
- Two fields of distinct composite types were reported as having incompatible types, which the
  specification permits, and their sub-selections were then never checked for conflicts.
- Conflicting field names and arguments went unreported when the parent type or the field definition
  could not be resolved.
- Leaf and composite selection errors were not reported at all for wrapped types such as `Int!` or
  `[Dog]`; the surviving rule also now names the full wrapped type, as graphql-js does.
- A visitor that skipped a subtree could hide it from the other visitors, dropping validation of
  that subtree entirely, when it was the last one registered.


## [0.17.0] - 2026-07-26

Behaviour is now checked against graphql-js 17.0.2 by executing it, not by reading it. Several
long-standing divergences turned out to be the opposite of what was assumed.

### Added

- `GDocument.parse` accepts an optional `maxTokens` limit, aborting with a syntax error once a
  document exceeds it. There is no default, matching graphql-js; set it when parsing untrusted
  input, since parsing is recursive and no depth limit exists.
- String values accept the braced `\u{…}` escape and surrogate pairs. A braced escape denotes a
  Unicode scalar value directly and can therefore never form a surrogate pair.
- The `@oneOf` directive, with `__Type.isOneOf` alongside it so clients that discover the directive
  can read the flag. Enforced for literals, for variables, and during validation.
- `__Directive.isRepeatable`, which the model and parser already supported but neither output path
  exposed.
- `@deprecated` is now accepted on all five locations the specification lists, adding
  `ARGUMENT_DEFINITION`, `INPUT_FIELD_DEFINITION` and `DIRECTIVE_DEFINITION`.
- Validation rejects a nullable variable supplying a field of a `@oneOf` input object. The fields
  themselves must be nullable; a variable must not be, or it could carry `null` and break the
  exactly-one-non-null invariant.

### Changed

- **Breaking.** A failed request no longer always answers `"data": null`. Errors raised *before*
  execution begins — a malformed document, an unknown operation name, uncoercible variables, a
  failed root resolver, an operation the schema has no root type for, or a validation failure — omit
  the `"data"` key entirely, as *Response* requires. Errors raised *during* execution keep the key,
  set to `null` if the error reached the root. `GError.isRequestError` carries the distinction, so
  callers that build their own failures and hand them to `serializeResult` can too.
- **Breaking.** The two `execute(documentSource…)` overloads now parse, validate and execute,
  mirroring graphql-js's `graphql()`. `execute(document)` still skips validation, mirroring its
  `execute()` — its documentation previously claimed the opposite. Both `documentSource` overloads
  became abstract members of `GExecutor`, which breaks third-party implementers of that interface.
- Fields, fragment spreads and inline fragments the schema does not define are skipped instead of
  producing an error, leaving the response key absent rather than present-and-null.
- A subscription operation answers with an error instead of throwing.
- Variable coercion reports every invalid value rather than aborting at the first, capped at 50
  errors with a terminal notice — previously a single bad element ended the whole coercion.
- Failures caused by a broken schema or a misused internal API now throw instead of appearing in the
  response; only failures a client can actually cause are reported as GraphQL errors.
- Migrated to fluid-gradle 4.0.0 (Kotlin 2.4.10, Gradle 9.6.1, JDK 21+).
- Adopted ktlint and detekt for formatting and static analysis (enforced via `./gradlew check` in
  CI) and reformatted the codebase to match. No public API or behavior changes.

### Fixed

- A non-null field resolving to `null` crashed instead of nullifying its nearest nullable ancestor.
  A thrown resolver already worked; the plain-`null` path did not.
- The printer dropped directives applied to object types and object type extensions, wrote
  `union` where a union extension needed `extend union`, dropped `repeatable` from directive
  definitions, and mis-spaced directives on schema definitions and extensions. All five are
  observable in `GSchema.toString()`.
- Out-of-range float literals such as `1e400` crashed the parser on untrusted input; they are now
  syntax errors. graphql-js keeps float values as strings and so never hits this.
- Truncated `\` and `\u` escapes at end of input threw instead of reporting a syntax error, and the
  string scanners accumulated quadratically.
- A document declaring its own `@deprecated` ended up with two definitions of it.
- Walking sibling nodes was quadratic: 32,000 siblings cost 512 million child reads, now 32,000.
- Removed unnecessary non-null assertions and casts in spec test files to eliminate compilation
  warnings.
- Fixed Dokka documentation generation warnings by configuring cross-module link resolution.


## [0.16.0] - 2026-03-28

### Changed

- Migrated to fluid-gradle 3.0.0 (Kotlin 2.3.20, Gradle 9.4.1, JDK 21+).
- Updated GitHub Actions to latest versions.

### Added

- Comprehensive KDoc on all public API.
- Test coverage for DSL builders, execution conversion, and visitors.
