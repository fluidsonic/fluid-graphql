# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/).


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
