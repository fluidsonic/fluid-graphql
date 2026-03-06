# fluid-graphql vs graphql-js: Comparison Report

**fluid-graphql** v0.15.0 (Kotlin/JVM) vs **graphql-js** (JavaScript reference implementation)
**GraphQL Spec:** October 2021 edition

This report covers spec compliance gaps, missing utilities, structural differences, algorithmic differences, and known bugs/FIXMEs. Subscriptions are an intentional deferral and are noted in Section 3.

---

## Section 1: Spec Compliance Gaps

### 1.1 Missing Validation Rules — SDL/Schema Validation

**Priority: High**

graphql-js has 12 SDL-specific validation rules that fluid-graphql lacks entirely. fluid-graphql has no concept of schema validation separate from query validation; these rules validate the schema definition document itself.

| Rule | Description |
|------|-------------|
| `LoneSchemaDefinitionRule` | Only one schema definition allowed in a document |
| `UniqueOperationTypesRule` | No duplicate query/mutation/subscription root type assignments |
| `UniqueTypeNamesRule` | No duplicate type names in schema |
| `UniqueEnumValueNamesRule` | No duplicate enum value names |
| `UniqueFieldDefinitionNamesRule` | No duplicate field definition names |
| `UniqueArgumentDefinitionNamesRule` | No duplicate argument definition names |
| `UniqueDirectiveNamesRule` | No duplicate directive definition names |
| `PossibleTypeExtensionsRule` | Type extensions must target existing types |
| `KnownArgumentNamesOnDirectivesRule` | Arguments on directive definitions must be known (SDL variant) |
| `ProvidedRequiredArgumentsOnDirectivesRule` | Required directive args must be provided (SDL variant) |
| `KnownTypeNamesRule` | All referenced type names must exist in schema |
| `MaxIntrospectionDepthRule` | DOS protection against deeply nested introspection queries (recommended, not required by spec) |

### 1.2 Missing @oneOf Directive

**Priority: High**

- **graphql-js:** `@oneOf` directive on `InputObject` types; `GraphQLInputObjectType.isOneOf = true`. Validates during input coercion that exactly one field is provided with a non-null value (lines 146–167 of `coerceInputValue.ts`).
- **fluid-graphql:** Not implemented anywhere.

### 1.3 Input Coercion: Undefined Field Detection

**Priority: High**

- **graphql-js:** Explicitly errors when input objects contain fields not defined in the type (lines 129–144 of `coerceInputValue.ts`).
- **fluid-graphql:** Does not iterate provided fields to check against the type definition; undefined fields silently pass through.

### 1.4 Input Coercion: Single-Error vs Multi-Error

**Priority: Medium**

- **graphql-js:** Collects up to `maxErrors` coercion errors before aborting, allowing multiple errors to be reported simultaneously.
- **fluid-graphql:** Throws (via `.throwException()`) on the first coercion error. The spec allows multiple errors to be reported simultaneously.

### 1.5 SelectionUnambiguityRule: Known False Negatives

**Priority: High**

File: `modules/execution/sources/validation/rules/SelectionUnambiguityRule.kt:4-6`

Three FIXMEs in the source:

1. **@skip/@include not considered** — fields may be in logical conflict but only one ever executes; the rule may incorrectly reject or pass these.
2. **Argument comparison doesn't normalize defaults** — two fields with the same default but different explicit-vs-default arguments may be flagged incorrectly or missed.
3. **No memoization** — potential O(n²) performance for large selection sets.

graphql-js `OverlappingFieldsCanBeMergedRule` uses an `areMutuallyExclusive` parameter threaded through recursion and memoizes field maps and fragment pair comparisons.

### 1.6 Printer: Incomplete String Escaping

**Priority: Medium**

File: `modules/language/sources/printing/Printer.kt:480`

- FIXME in source: String escaping not fully implemented — Unicode escapes and special character handling are incomplete.
- graphql-js printer handles the complete spec-mandated string escaping.

### 1.7 Abstract Type Resolution: No isTypeOf Support

**Priority: High**

File: `modules/execution/sources/execution/DefaultFieldSelectionExecutor.kt:254-256`

- **graphql-js:** After resolving an abstract type, calls `returnType.isTypeOf(result, context, info)` to validate the resolved concrete type is correct; also provides a `defaultTypeResolver` that checks `value.__typename` then iterates possible types calling `isTypeOf`.
- **fluid-graphql:** No `isTypeOf` mechanism. Type resolution relies solely on Kotlin `isInstance()` check. FIXME noted in source.

### 1.8 Abstract Type Resolution: ensureValidRuntimeType Checks

**Priority: High**

graphql-js validates 7 conditions after type resolution:
1. Result must not be null/undefined
2. Result must not be a `GraphQLObjectType` instance (must be a string name)
3. Result must be a string
4. Type name must exist in schema
5. Type must be an object type (not interface/union)
6. Type must be a subtype of the declared return type

fluid-graphql has no equivalent validation — incorrect type resolutions produce runtime errors rather than proper GraphQL errors.

### 1.9 Lexer: Invalid Character Handling

**Priority: Low**

File: `modules/language/sources/parsing/Lexer.kt:241`

- FIXME in source: Does not throw a proper error at end-of-input in all cases.
- graphql-js raises a well-formed `GraphQLSyntaxError` in all end-of-input scenarios.

---

## Section 2: Missing Utilities

graphql-js ships `src/utilities/` with ~27 utilities. fluid-graphql has a parser and printer but lacks higher-level utilities.

### 2.1 Schema Building from Parsed Document

**Priority: High**

- **graphql-js:** `buildASTSchema(documentNode)` converts a parsed `DocumentNode` into a fully resolved `GraphQLSchema`.
- **fluid-graphql:** No equivalent. `GSchema.kt` constructor requires manually passing pre-constructed type lists. `GDocument.parse()` can parse SDL, and `GSchema` can be constructed manually, but there is no automated bridge from a parsed document to a resolved schema.

### 2.2 Schema Building from Introspection

**Priority: Medium**

- **graphql-js:** `buildClientSchema(introspectionResult)` reconstructs a `GraphQLSchema` from an introspection query response.
- **fluid-graphql:** Not implemented. Useful for tooling and client-side type checking.

### 2.3 Schema Extension from Parsed Document

**Priority: Medium**

- **graphql-js:** `extendSchema(schema, documentNode)` merges type extensions and new definitions from a `DocumentNode` into an existing `GraphQLSchema`.
- **fluid-graphql:** `GTypeExtension` AST nodes exist and parse correctly, but there is no utility to apply them to an existing `GSchema`. Extensions in documents are silently ignored during schema construction.

### 2.4 Type-Aware AST Visitor

**Priority: High**

- **graphql-js:** `TypeInfo` class + `visitWithTypeInfo(visitor, typeInfo)` — allows tracking the current type, parent type, input type, and argument during AST traversal.
- **fluid-graphql:** No equivalent. Validation rules that need type context must implement their own type tracking. This is a foundational building block for many tools.

### 2.5 Type Comparison Utilities

**Priority: Medium**

- **graphql-js:** `isEqualType(a, b)`, `isTypeSubTypeOf(schema, maybeSubType, superType)`, `doTypesOverlap(schema, typeA, typeB)`.
- **fluid-graphql:** No equivalent standalone utilities. `GSchema.getPossibleTypes()` exists but no subtype/overlap checks.

### 2.6 Breaking Change Detection

**Priority: Low**

- **graphql-js:** `findBreakingChanges(oldSchema, newSchema)`, `findDangerousChanges(oldSchema, newSchema)`.
- **fluid-graphql:** Not implemented. Useful for CI/CD schema evolution validation.

### 2.7 Document Manipulation Utilities

**Priority: Low**

- **graphql-js:** `separateOperations(document)`, `concatAST(documents[])`, `stripIgnoredCharacters(source)`, `getOperationAST(document, operationName)`.
- **fluid-graphql:** Not implemented. These are convenience utilities for tooling.

### 2.8 Schema → Introspection Result

**Priority: Medium**

- **graphql-js:** `introspectionFromSchema(schema)` — converts a schema directly to an introspection result without executing a query.
- **fluid-graphql:** Can only obtain introspection by executing a `__schema` query via `GExecutor`.

### 2.9 Generate Introspection Query

**Priority: Low**

- **graphql-js:** `getIntrospectionQuery(options)` — produces the standard introspection query string, with options to include/exclude deprecated fields, descriptions, etc.
- **fluid-graphql:** No equivalent. Introspection support is hardcoded within the executor.

### 2.10 Name Validation Utility

**Priority: Low**

- **graphql-js:** `assertValidName(name)`, `isValidNameError(name)` — validates GraphQL identifier rules.
- **fluid-graphql:** No standalone utility; validation may happen implicitly during parsing.

### 2.11 isSubType on Schema

**Priority: High**

- **graphql-js:** `GraphQLSchema.isSubType(abstractType, maybeSubType)` — cached subtype relationship checks used in `ensureValidRuntimeType`.
- **fluid-graphql:** `GSchema` has no `isSubType()` method. Absence is a contributing reason why fluid-graphql lacks runtime type validation (see gap 1.8).

---

## Section 3: Structural / Architectural Differences

### 3.1 No Schema Self-Validation

**Priority: High**

- **graphql-js:** `validateSchema(schema)` / `assertValidSchema(schema)` in `src/type/validate.ts`. Checks include: type names are valid, interface fields exist on implementing types, union members are object types, field arg types are input types, etc. Called automatically in `execute()` (line 53 of `execute.ts`).
- **fluid-graphql:** No schema validation. Invalid schemas may produce runtime errors instead of clear validation errors at schema-construction time.

### 3.2 Subscriptions: Intentional Deferral

**Priority: Low (noted for completeness)**

- **fluid-graphql:** Subscription operations are parsed and validated (including `SubscriptionRootFieldExclusivityRule`) but not executed.
- **graphql-js:** Full subscription execution via `subscribe()` returning `AsyncIterable<ExecutionResult>`.
- **Status:** Intentional deferral. Not a compliance gap to be fixed in the current roadmap.

### 3.3 Non-Standard @optional Directive

**Priority: Medium**

- fluid-graphql defines a custom `@optional` directive allowing absence of a non-null argument to be treated as acceptable.
- This directive does not exist in the GraphQL spec or graphql-js.
- Consumers depending on it cannot interoperate with standard tooling or validators.

### 3.4 Async-Only Execution

**Priority: Low**

- **graphql-js:** Both `execute()` (async Promise) and `executeSync()` (synchronous).
- **fluid-graphql:** Suspend-based coroutines only — no synchronous execution path.
- Not a spec compliance issue; JVM coroutines are idiomatic Kotlin.

### 3.5 Schema Description and Extensions Not Stored

**Priority: Low**

- **graphql-js:** `GraphQLSchema` stores `description`, `extensions`, `astNode`, `extensionASTNodes`.
- **fluid-graphql:** `GSchema` stores `document` and `types` but no top-level schema description or custom extensions.

### 3.6 Schema Coordinates Not Supported

**Priority: Low**

- **graphql-js:** Schema coordinate AST nodes (e.g., `Query.field`, `@directive(arg)`) for referencing schema elements by string. Utilities: `resolveSchemaCoordinate()`, `resolveASTSchemaCoordinate()`.
- **fluid-graphql:** Not implemented. Primarily useful for tooling and error messages.

### 3.7 Default Field Resolver Missing

**Priority: Medium**

File: `modules/execution/sources/execution/DefaultFieldSelectionExecutor.kt:254`

- FIXME in source: Default resolver not supported.
- **graphql-js:** `defaultFieldResolver` resolves a field by property name on the parent object; active unless overridden per-field.
- **fluid-graphql:** Always requires explicit field resolver; no automatic property-name fallback.

### 3.8 Error Formatting Utilities

**Priority: Low**

- **graphql-js:** `formatError(error)`, `printError(error)`, `locatedError(originalError, nodes, path)` as standalone exported functions.
- **fluid-graphql:** `GError` class handles these inline; no standalone utility functions.

---

## Section 4: Known Bugs / FIXMEs (Source-Referenced)

| Priority | File | Line | Issue |
|----------|------|------|-------|
| High | `execution/sources/validation/rules/SelectionUnambiguityRule.kt` | 4 | @skip/@include not considered in conflict detection |
| Medium | `execution/sources/validation/rules/SelectionUnambiguityRule.kt` | 27 | Default values not normalized in argument comparison |
| Medium | `execution/sources/execution/DefaultFieldSelectionExecutor.kt` | 254 | Default resolver not implemented |
| High | `execution/sources/conversion/GVariableInputCoercer.kt` | 4 | Coercer not called for List & null wrapper types |
| High | `execution/sources/conversion/GNodeInputCoercer.kt` | 4 | Coercer not called for List & null wrapper types |
| High | `execution/sources/conversion/OutputConverter.kt` | 4 | Dispatch to output coercers for non-null/list types incomplete |
| Low | `language/sources/parsing/Lexer.kt` | 241 | No proper error thrown at end-of-input in all cases |
| Medium | `language/sources/printing/Printer.kt` | 480 | String escaping, Unicode escapes, line wrapping incomplete |
| High | `language/sources/GLanguage.kt` | 15 | Default directives not auto-added to all schemas |
| Medium | `execution/sources/validation/Validator.kt` | 11 | Visitor contextualization incomplete |
| Medium | `execution/sources/validation/ValidationContext.kt` | 4 | No error limit or early abortion |
| Medium | `execution/sources/execution/DefaultExecutor.kt` | 4 | Exception handling incomplete |

---

## Section 5: Algorithmic Deep-Dives

### 5.1 Input Coercion: Spec Algorithm vs Implementation

The GraphQL spec defines a `CoerceVariableValues` algorithm. Key differences:

| Step | graphql-js | fluid-graphql |
|------|-----------|--------------|
| Error collection | Multiple errors (up to `maxErrors`) | Fails on first error |
| Undefined input object fields | Error reported | Silently ignored |
| @oneOf validation | Enforced | Not implemented |
| `parseValue` exceptions | Caught, wrapped as `GraphQLError` | Unclear behavior |
| Default value conversion | Via `valueFromAST()` AST→runtime | Via `NodeInputConverter` |

### 5.2 Output Coercion / Field Execution

Key algorithmic differences in `completeValue`:

| Step | graphql-js | fluid-graphql |
|------|-----------|--------------|
| Abstract type resolution | 7-step `ensureValidRuntimeType()` | Kotlin `isInstance()` only |
| `isTypeOf` support | Yes — validates resolved type | No `isTypeOf` mechanism |
| Default type resolver | `__typename` property + `isTypeOf` fallback | No default; throws if unresolved |
| List coercion | `isIterableObject()` check; per-item error handling | `Collection<*>` check only |
| Promise/async handling in lists | `containsPromise` flag + `Promise.all` | Coroutine flatten |
| Error propagation in non-null | `handleFieldError` re-throws | `flatMapErrors` propagation |

### 5.3 Field Conflict Detection Algorithm

| Aspect | graphql-js `OverlappingFieldsCanBeMergedRule` | fluid-graphql `SelectionUnambiguityRule` |
|--------|----------------------------------------------|------------------------------------------|
| @skip/@include awareness | Not explicit (separate concern) | FIXME noted |
| Memoization | Yes — field maps and fragment pair caches | No memoization |
| `mutuallyExclusive` tracking | Parameter threaded through recursion | Inlined per-comparison |
| Argument comparison | Via `stringifyValue()` normalization | Raw `GValue !=` comparison |
| Fragment-to-fragment comparison | 10-stage (A–J) algorithm | Simpler grouping by response name |

### 5.4 Variable Coercion

| Step | graphql-js | fluid-graphql |
|------|-----------|--------------|
| Input type validation | Explicit `isInputType()` check | Assumed from prior validation |
| Null + non-null variable | Explicit check (line 111–120 of `values.ts`) | Handled in `coerceValue()` wrapping |
| Error handling | Callback with collection | Throws on first error |

---

## Priority Summary

| Priority | Count | Key Items |
|----------|-------|-----------|
| **Critical** | 0 | — |
| **High** | ~12 | SDL validation rules, @oneOf, undefined input fields, `isTypeOf`, runtime type checks, `buildASTSchema`, `TypeInfo`, `isSubType`, schema self-validation, input coercer wrapper type dispatch, default directives |
| **Medium** | ~10 | Multi-error coercion, `SelectionUnambiguityRule` false negatives, printer escaping, schema extension, `buildClientSchema`, type comparison utils, `@optional` custom directive, default field resolver, `ValidationContext` error limit |
| **Low** | ~10 | Subscriptions (intentional), breaking change detection, document manipulation utilities, introspection query generator, name validation, schema coordinates, async-only execution, schema description storage, error formatting utilities |
