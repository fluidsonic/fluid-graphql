# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/).


## [0.19.0] - 2026-07-27

Scalar coercion moves onto the types themselves. It used to live in three parallel tables inside the
converters — one per direction — which had drifted apart from each other and from the fourth copy
inside schema validation, so a literal could pass validation and then fail at execution. There is
now one definition per scalar, and validation asks the scalar rather than re-deciding.
**Anyone with custom coercers must migrate**; coercers lose their receiver and the interfaces are
renamed. Behaviour was checked cell by cell against graphql-js 17.0.2 by executing it.

### Added

- `printSchema(schema)` and `printType(type)` produce SDL byte-identical to graphql-js at the
  default two-space indent. The library could parse and execute a schema but not print one.
- `GSchema.validate()` reports specification violations as data and `GSchema.assertValid()` throws.
  Five rules ship: unimplemented interface fields, undeclared transitive interfaces, root operation
  types, and the two OneOf input-field rules. Results are computed once per schema.
- `GScalarType` carries its own coercion as four open members — `coerceOutputValue`,
  `coerceInputValue`, `coerceInputLiteral` and `valueToLiteral` — mirroring graphql-js.
- `GSchema.description` exposes the description written on a `schema { … }` definition, and
  `__Schema.description` now answers it.
- `GLanguage.isReservedTypeName(name)` answers whether a name is one the schema factory refuses — the
  five built-in scalars and anything beginning with `__` — so a caller building a schema from names it
  does not control can check instead of catching. It reads the same set the factory enforces.

### Changed

- **Breaking.** Coercers are context-free single-argument functions. `GNodeInputCoercer`,
  `GVariableInputCoercer` and `GOutputCoercer` become `GInputLiteralCoercer`, `GInputValueCoercer`
  and `GOutputValueCoercer`; their receiver, the four coercer context types, `next()` and the
  executor-level fallback coercers on `GExecutor.default` are gone. There was never a chain to
  delegate to — only a re-entrancy flag. The six extension accessors are renamed to match. A coercer
  now raises a bare message and the converter supplies the field, argument or variable context.
- **Breaking.** The built-in scalars are classes rather than objects, and every schema owns its own
  instances, so `GIntType` becomes `GIntType()` and `GType.defaultTypes` becomes a function. A
  shared instance would carry one schema's attached coercer into every other schema in the process.
  `GScalarType` gains equality by name and runtime class, since reference identity is no longer
  stable across schemas.
- **Breaking.** Type names the specification reserves are refused at construction: the built-in
  scalar names, anything beginning with `__`, and two types sharing a name. Previously a
  `scalar Int` was accepted and silently discarded.
- **Breaking.** `@optional` and `supportOptional` are removed. They tracked a specification issue
  open since 2021, and the requiredness clause applied whether or not the directive was registered,
  so any schema using that name for its own purposes had required arguments silently reclassified.
- **Breaking.** `GInputObjectType` is no longer a `GCompositeType`, which the specification does not
  permit. Exhaustive `when` blocks that relied on it absorbing input objects will fail to compile.
- **Breaking.** Coercion follows graphql-js cell by cell, so its output-only laxness is reproduced:
  `Int` and `Float` accept a boolean and a `Number()`-parsed string on output but not on input,
  `Boolean` accepts any finite number, and `String` renders finite numbers and booleans.
- The introspection types are ordinary members of every schema, so `__schema.types` lists them and
  a fragment on `__Type` resolves through the same lookup as any user type. The executor no longer
  swaps in a second schema mid-request. Unreferenced built-in scalars are no longer listed, matching
  graphql-js; `Boolean` and `String` always are, because the built-in directives take arguments of
  those types.
- A scalar literal rejected by a coercer attached to the type is now a validation error rather than
  a field error, so it aborts the request before execution.
- `GSchema.toString()` delegates to `printSchema`, so it shows the schema's public shape rather than
  a copy of the source document: applied directives other than `@deprecated`, `@oneOf` and
  `@specifiedBy` are dropped, descriptions print as block strings, and directive definitions precede
  the types.
- Upgraded to fluid-gradle 4.1.0. `./gradlew check` now reports only failing tests and prints one
  `N tests, all passed` line per module, so a failure is no longer buried in output. This replaces
  the local test-logging workaround in the root build script, which is removed. Kotlin 2.4.10,
  Dokka 2.2.0 and the Gradle 9.6.1 wrapper are unchanged, so published artifacts are unaffected.

### Fixed

- `Int` passed a string through verbatim where an `Int` was declared, emitting a string into the
  response, and `Float` serialised `NaN` and the infinities into invalid JSON.
- An omitted nullable argument was rejected as though it were required, so `{ field }` failed
  whenever the schema declared `field(input: Input)` with a nullable input. It is now absent from
  the coerced arguments, as *Coercing Field Arguments* requires.
- A cancelled request was reported as a GraphQL error instead of unwinding. Field resolvers suspend,
  so cancellation reached the exception handler, which converted it into an error and left the
  caller's coroutine believing it was still running.
- `@deprecated(reason:)` was typed as a nullable `String` where the specification and graphql-js
  both declare `String!`, so an introspecting client saw a signature the server did not mean. Its
  description also had an unbalanced parenthesis.
- A field error raised *while completing* a value — a failed output coercion, or a non-null child that
  nullified itself — stopped propagating one level too early, so a non-null field kept a `null` the
  specification does not allow it to hold. `{ nonNullFloat }` returned `{"nonNullFloat": null}` where
  it must return `data: null`. A resolver that threw or returned null was already handled correctly;
  only errors originating inside completion were affected.
- Whether an input coercer's error carried a response `path` depended on how the coercer signalled the
  failure rather than on where it failed, and the two ways disagreed in opposite directions. A literal
  coercer raising a `GErrorException` produced an error with no path even though the response field was
  known, while a variable coercer's exception routed through a `GExceptionHandler` produced a path naming
  the *variable* — which is not a response field, and belongs on a request error that carries no `data` to
  index into. Both now follow the origin: a literal coercion failure carries its field, a variable
  coercion failure carries no path.
- Validation rebuilt a coercer's error from its message alone, so a scalar that rejected a literal with
  `extensions` — a machine-readable `code`, say — reached the client with the prose intact and the
  structure gone. The error is now kept whole and only its location added, which is the one thing a
  coercer cannot know. Execution already behaved this way, so the two paths now agree about what a
  client is told regardless of which entry point rejected the value.
- `__schema.types` listed each built-in scalar twice whenever a document also defined one.
- A fragment conditioned on an input-object type was reported as never matching its parent type
  rather than as conditioning on a non-composite type.
- Validation reported at most one bad leaf per value tree in some shapes; it now reports every one,
  as graphql-js does, including through a coercer that throws.


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
