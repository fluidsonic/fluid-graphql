# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**fluid-graphql** is a Kotlin/JVM GraphQL library (package: `io.fluidsonic.graphql`). Version 0.15.0.

## Git

Run `git` directly without `-C` — the working directory is always the project root.

## Shell Commands

- Do not use `cat` or `$()` shell substitution in Bash commands
- `cd` must always be used alone — never combined with other commands in the same Bash call
- "remember that" means persist the information to CLAUDE.md
- Never use project-external MEMORY.md files — always persist to CLAUDE.md

## Build Commands

```bash
./gradlew build              # Build all modules and run tests
./gradlew check              # Run all tests
./gradlew jvmTest            # Run all tests
./gradlew :fluid-graphql-language:jvmTest   # Run tests for a single module
./gradlew :fluid-graphql-execution:jvmTest  # Run execution module tests
```

Uses the `io.fluidsonic.gradle` plugin (v2.0.2) for build configuration. Gradle 9.1.0. Use `./gradlew dependencyUpdates` (versions plugin) to check for dependency updates.

## Architecture

Three modules with a clear dependency chain:

```
language  <--  dsl  <--  execution
                ^-----------┘
```

### `modules/language` — Core GraphQL type system and parsing
- **AST model** (`sources/model/nodes/`): `GNode` is the sealed base class for all AST nodes. Key types: `GDocument`, `GValue`, `GSelection`, `GDefinition`, all GraphQL type definitions (`GObjectType`, `GScalarType`, etc.)
- **Schema** (`sources/model/GSchema.kt`): Holds resolved types, directive definitions, and query/mutation/subscription root types
- **Parser** (`sources/parsing/`): Hand-written recursive descent parser (Lexer → Parser) producing AST nodes. Entry point: `GDocument.parse()`
- **Printer** (`sources/printing/`): AST-to-string serialization
- **Visitors** (`sources/visitors/`): Visitor pattern for AST traversal with `Visitor`, `Visitor.Typed`, `Visitor.Hierarchical` base classes, `NodeWalker`, and `VisitCoordinator`
- **GResult** (`sources/model/GResult.kt`): Sealed `Success`/`Failure` result type used throughout for error handling (not exceptions)

### `modules/dsl` — Kotlin DSL for building GraphQL schemas and documents
- `GraphQL.schema { ... }` is the entry point for schema building via `GSchemaBuilder`
- `GraphQL.document { ... }` for building query documents via `GraphQLDocumentBuilder`
- Uses `@DslMarker` annotations for scope control
- Depends on `language`

### `modules/execution` — Query execution engine
- **Executor** (`sources/execution/`): `GExecutor` interface with `GExecutor.default()` factory. Parses, validates, and executes queries against a schema
- **Validation** (`sources/validation/`): Rule-based validation (e.g., `ScalarLeavesRule`, `FragmentCycleDetectionRule`). Each rule is a `Visitor`
- **Resolution** (`sources/resolution/`): `GFieldResolver`, `GRootResolver` for resolving fields to values
- **Conversion** (`sources/conversion/`): Input/output coercers for type conversion between GraphQL and Kotlin values
- **Introspection** (`sources/introspection/`): Built-in introspection query support
- Depends on both `language` and `dsl`; uses `kotlinx-coroutines`

## Code Conventions

- All public API is in the single package `io.fluidsonic.graphql`
- GraphQL types/nodes are prefixed with `G` (e.g., `GNode`, `GDocument`, `GSchema`, `GExecutor`)
- DSL builder types are prefixed with `GraphQL` (e.g., `GraphQLDocumentBuilder`)
- Tests go in `tests/`; spec compliance tests in `tests/spec/`
- Tests use `kotlin.test` framework (not JUnit directly)
- Internal API is annotated with `@InternalGraphqlApi`
- Source directories use flat `sources/` instead of the standard `src/main/kotlin/` layout
- Tab indentation
- Spec test helpers: `assertValidationRule(rule, errors, document, schema?)` and `assertExecution(schema, document, ..., expected)` in `modules/execution/tests/utility/`
