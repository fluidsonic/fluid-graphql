# fluid-graphql

[![Maven Central](https://img.shields.io/maven-central/v/io.fluidsonic.graphql/fluid-graphql?label=Maven%20Central)](https://search.maven.org/artifact/io.fluidsonic.graphql/fluid-graphql)
[![Tests](https://github.com/fluidsonic/fluid-graphql/workflows/Tests/badge.svg)](https://github.com/fluidsonic/fluid-graphql/actions?workflow=Tests)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20%20(JVM)-blue.svg)](https://github.com/JetBrains/kotlin/releases/v2.3.20)
[![#fluid-libraries Slack Channel](https://img.shields.io/badge/slack-%23fluid--libraries-543951.svg?label=Slack)](https://kotlinlang.slack.com/messages/C7UDFSVT2/)

A Kotlin/JVM GraphQL library for building schemas, executing queries, and parsing GraphQL documents.

## Installation

Add the umbrella artifact (includes all modules) to your Gradle build:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.fluidsonic.graphql:fluid-graphql:0.16.0")
}
```

Or depend on individual modules:

```kotlin
dependencies {
    // Core type system, parser, printer, AST
    implementation("io.fluidsonic.graphql:fluid-graphql-language:0.16.0")

    // Kotlin DSL for building schemas and documents
    implementation("io.fluidsonic.graphql:fluid-graphql-dsl:0.16.0")

    // Query execution engine and validation
    implementation("io.fluidsonic.graphql:fluid-graphql-execution:0.16.0")
}
```

## Quick Start

```kotlin
import io.fluidsonic.graphql.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val schema = GraphQL.schema {
        Query {
            field("hello" of !String) {
                resolve { "Hello, world!" }
            }
        }
    }

    val executor = GExecutor.default(schema = schema)
    val result = executor.execute("{ hello }")
    println(executor.serializeResult(result))
    // {data={hello=Hello, world!}}
}
```

## Schema Definition

Schemas are built with `GraphQL.schema { }`. The DSL provides type-safe builders for every GraphQL type.

### Object Types and Fields

```kotlin
val schema = GraphQL.schema {
    Query {
        field("user" of type("User")) {
            argument("id" of !String)
            resolve {
                val id = arguments["id"] as String
                User(id = id, name = "Alice")
            }
        }
    }

    Object(type("User")) {
        field("id" of !String)
        field("name" of !String)
    }
}
```

### Non-null and List Types

Use `!` to make a type non-null, and `List(...)` to wrap a type in a list:

```kotlin
Object(type("Post")) {
    field("id" of !String)          // String! — non-null
    field("tags" of List(!String))  // [String!] — list of non-null strings
    field("comments" of !List(!type("Comment")))  // [Comment!]! — non-null list of non-null
}
```

Built-in scalar type references available inside the DSL: `String`, `Int`, `Float`, `Boolean`, `ID`.

### Arguments with Default Values

```kotlin
Query {
    field("posts" of List(type("Post"))) {
        argument("limit" of Int) {
            default(value(10))
        }
        argument("status" of type("Status")) {
            default(enum("PUBLISHED"))
        }
    }
}
```

### Enums

```kotlin
val Status by type

Enum(Status) {
    value("DRAFT")
    value("PUBLISHED")
    value("ARCHIVED") {
        deprecated("Use DRAFT instead")
    }
}
```

### Interfaces

```kotlin
val Node by type
val Post by type
val Comment by type

Interface(Node) {
    field("id" of !ID)
}

Object(Post implements Node) {
    field("id" of !ID)
    field("title" of !String)
}

Object(Comment implements Node) {
    field("id" of !ID)
    field("body" of !String)
}
```

An object implementing multiple interfaces:

```kotlin
Object(type("BlogPost") implements type("Node") and type("Timestamped")) {
    field("id" of !ID)
    field("createdAt" of !String)
    field("title" of !String)
}
```

### Unions

```kotlin
val SearchResult by type
val Post by type
val User by type

Union(SearchResult with Post or User)
```

### Input Objects

```kotlin
val CreatePostInput by type

InputObject(CreatePostInput) {
    argument("title" of !String)
    argument("body" of !String)
    argument("tags" of List(!String))
}

Mutation {
    field("createPost" of !type("Post")) {
        argument("input" of !CreatePostInput)
    }
}
```

### Custom Scalars

```kotlin
val Date by type

Scalar(Date) {
    description("An ISO-8601 date string (e.g. 2024-01-15)")
}
```

See [Custom Scalar Coercion](#custom-scalar-coercion) for attaching coercers.

### Directives

```kotlin
Directive("auth") {
    description("Requires the caller to be authenticated")
    argument("role" of String) {
        default(value("USER"))
    }
    on(FIELD_DEFINITION or OBJECT)
}
```

### The `by type` delegation pattern

Use `val MyType by type` to derive a `GNamedTypeRef` from the property name. This avoids repeating string literals:

```kotlin
val schema = GraphQL.schema {
    val User by type
    val Post by type

    Query {
        field("user" of User) {
            argument("id" of !ID)
        }
        field("posts" of List(Post))
    }

    Object(User) {
        field("id" of !ID)
        field("name" of !String)
    }

    Object(Post) {
        field("id" of !ID)
        field("title" of !String)
    }
}
```

### Descriptions and Deprecation

```kotlin
Object(type("User")) {
    description("A registered user of the system")

    field("id" of !ID) {
        description("Unique identifier")
    }
    field("legacyId" of String) {
        deprecated("Use `id` instead")
    }
}
```

## Field Resolution

### Inline `resolve {}` blocks

The simplest way to attach a resolver is the `resolve { }` block on a field definition. The lambda receives the parent object as its argument:

```kotlin
Object(type("User")) {
    field("id" of !String) {
        resolve { parent: Any -> (parent as User).id }
    }
}
```

### Typed `Object<KotlinType>` with type-safe resolvers

Use the typed `Object<KotlinType>` overload for compile-time safety. The resolver lambda receives the correctly-typed parent:

```kotlin
data class User(val id: String, val name: String)

val schema = GraphQL.schema {
    val User by type

    Query {
        field("me" of User) {
            resolve { currentUser() }
        }
    }

    Object<User>(User) {
        field("id" of !String) {
            resolve { it.id }
        }
        field("name" of !String) {
            resolve { it.name }
        }
    }
}
```

### Accessing arguments in resolvers

The `GFieldResolverContext` receiver exposes `arguments` (a `Map<String, Any?>`), `fieldDefinition`, `parentType`, `path`, and the full `execution` context:

```kotlin
field("greeting" of !String) {
    argument("name" of !String)
    resolve {
        val name = arguments["name"] as String
        "Hello, $name!"
    }
}
```

### Accessing executor context extensions in resolvers

Per-request data (such as the current user) can be passed via `GExecutorContextExtensionSet` and read inside any resolver:

```kotlin
object CurrentUserKey : GExecutorContextExtensionKey<User>

field("me" of type("User")) {
    resolve {
        execution.extensions[CurrentUserKey]
    }
}
```

### Fallback field resolver

Provide a `fieldResolver` to `GExecutor.default` to handle fields that have no inline resolver — useful for property-based default resolution or middleware:

```kotlin
val executor = GExecutor.default(
    schema = schema,
    fieldResolver = GFieldResolver { parent ->
        // called for every field without an explicit resolver
        next()
    }
)
```

## Custom Scalar Coercion

Attach coercers directly on a `Scalar` definition using the three coercion hooks.

### `coerceNodeInput` — inline literal values

Called when the scalar value is provided inline in a query document (not as a variable):

```kotlin
val Date by type

Scalar(Date) {
    coerceNodeInput { input ->
        // input is the raw GValue AST node
        when (input) {
            is GStringValue -> LocalDate.parse(input.value)
            else -> GError("Expected a date string").throwException()
        }
    }
}
```

### `coerceVariableInput` — variable values

Called when the scalar value is provided via a query variable (already parsed from JSON):

```kotlin
Scalar(Date) {
    coerceVariableInput { input ->
        // input is the raw value from the variables map (e.g. String from JSON)
        LocalDate.parse(input as String)
    }
}
```

### `coerceOutput` — serializing resolver results

Called when a resolver returns a value of this scalar type, converting it to a serializable form:

```kotlin
Scalar(Date) {
    coerceOutput { input ->
        // input is the value returned by a resolver
        (input as LocalDate).toString()
    }
}
```

## Executing Queries

```kotlin
val executor = GExecutor.default(schema = schema)

// Execute from a query string (suspend function)
val result: GResult<Map<String, Any?>> = executor.execute("{ hello }")

// With operation name and variables
val result = executor.execute(
    documentSource = "query GetUser(\$id: ID!) { user(id: \$id) { name } }",
    operationName = "GetUser",
    variableValues = mapOf("id" to "42")
)

// Convert result to a plain map for JSON serialization
val response: Map<String, Any?> = executor.serializeResult(result)
// { "data": { ... }, "errors": [...] }
```

### Passing per-request context

Use `GExecutorContextExtensionSet` to attach request-scoped data (auth context, request ID, etc.):

```kotlin
object CurrentUserKey : GExecutorContextExtensionKey<User>

val extensions = GExecutorContextExtensionSet {
    set(CurrentUserKey, authenticatedUser)
}

val result = executor.execute(
    documentSource = "{ me { name } }",
    extensions = extensions
)
```

### Providing a root value

By default, the root value passed to top-level field resolvers is `Unit`. Use `GRootResolver` to provide a meaningful root object:

```kotlin
val executor = GExecutor.default(
    schema = schema,
    rootResolver = GRootResolver.constant(myRootObject)
)
```

## Validation

Validate a parsed document against a schema before executing it. Requires the `fluid-graphql-execution` module.

```kotlin
val document = GDocument.parse("{ user { id name } }").valueOrThrow()
val errors: List<GError> = document.validate(schema)

if (errors.isEmpty()) {
    println("Document is valid")
} else {
    errors.forEach { println(it.describe()) }
}
```

Validation covers the full GraphQL specification: field existence, argument types, fragment cycles, variable usage, directive locations, and more.

## Parsing and Printing

### Parsing

```kotlin
// Parse a query document
val result: GResult<GDocument> = GDocument.parse("{ hello }")
val document = result.valueOrThrow()

// Parse a schema SDL
val schemaResult: GResult<GDocument> = GDocument.parse("""
    type Query {
        hello: String
    }
""".trimIndent())
```

### Printing (serializing back to GraphQL SDL / query string)

Every `GNode` can be serialized back to a GraphQL string via `GNode.print(node)` or simply `toString()`:

```kotlin
val document = GDocument.parse("query { hello }").valueOrThrow()
println(document.toString())
// query {
//   hello
// }

// Custom indentation
println(GNode.print(document, indent = "    "))
```

## Error Handling

### `GResult<Value>`

Operations that can fail return a `GResult<Value>` — a sealed class with `Success` and `Failure` variants. A `Success` may still carry non-fatal errors alongside its value.

```kotlin
val result: GResult<GDocument> = GDocument.parse(source)

// Pattern match
when (result) {
    is GResult.Success -> println("Parsed: ${result.value}")
    is GResult.Failure -> println("Errors: ${result.errors}")
}

// Convenient extractors
val document = result.valueOrNull()          // null on failure
val document = result.valueOrThrow()         // throws GErrorException on failure
val document = result.valueWithoutErrorsOrNull()  // null if any errors present

// Transform
val names = result.mapValue { doc -> doc.definitions.map { it } }

// Chain
val validated = result.flatMapValue { doc ->
    val errors = doc.validate(schema)
    if (errors.isEmpty()) GResult.success(doc) else GResult.failure(errors)
}

// Early exit pattern
fun process(result: GResult<GDocument>): GResult<String> {
    val doc = result.ifErrors { errors -> return GResult.failure(errors) }
    return GResult.success(doc.toString())
}
```

### `GError`

`GError` is the standard error type, following the GraphQL response format:

```kotlin
val error = GError(
    message = "Not found",
    path = GPath.ofName("user"),
    extensions = mapOf("code" to "NOT_FOUND")
)

// Throw as an exception (caught by executor and turned into a response error)
error.throwException()

// Describe with source location context
println(error.describe())
```

### `GErrorException`

`GErrorException` bridges between the error-result model and Kotlin exceptions. It is thrown by `valueOrThrow()` and by `GError.throwException()`. Inside resolvers, throwing a `GErrorException` causes the executor to include its errors in the GraphQL response rather than propagating the exception.

```kotlin
throw GErrorException(GError("Something went wrong"))
// or equivalently:
GError("Something went wrong").throwException()
```

## Exception Handling

By default, exceptions thrown inside resolvers or coercers propagate out of `GExecutor.execute`. Provide a `GExceptionHandler` to translate them into GraphQL errors instead:

```kotlin
val executor = GExecutor.default(
    schema = schema,
    exceptionHandler = GExceptionHandler { exception ->
        when (exception) {
            is NotFoundException -> GError(
                message = exception.message ?: "Not found",
                extensions = mapOf("code" to "NOT_FOUND")
            )
            else -> GError("Internal server error")
        }
    }
)
```

The handler receives the exception via its `GExceptionHandlerContext` receiver, which also provides the `GExceptionOrigin` indicating whether the exception came from a field resolver, output coercer, input coercer, or root resolver.

Note: `GErrorException` (thrown via `GError.throwException()`) is never passed to the exception handler — it is always treated as a GraphQL error automatically.

## Node Extensions

`GNodeExtensionSet` provides a typed key-value map for attaching arbitrary metadata to any `GNode` without modifying the AST. This is the mechanism fluid-graphql itself uses to attach resolvers and coercers to schema nodes.

### Defining a key

```kotlin
data class AuthRequirement(val role: String)

object AuthRequirementKey : GNodeExtensionKey<AuthRequirement>
```

### Attaching metadata during schema construction

```kotlin
val schema = GraphQL.schema {
    Query {
        field("adminData" of !String) {
            extension(AuthRequirementKey, AuthRequirement(role = "ADMIN"))
            resolve { "secret" }
        }
    }
}
```

### Reading metadata from AST nodes

```kotlin
val fieldDef: GFieldDefinition = schema.queryType!!.fieldDefinition("adminData")!!
val auth: AuthRequirement? = fieldDef.extensions[AuthRequirementKey]
```

## Executor Context Extensions

`GExecutorContextExtensionKey` is the per-request equivalent of `GNodeExtensionKey`. Use it to pass request-scoped values (current user, trace context, etc.) into resolvers without threading parameters through every call.

### Defining a key

```kotlin
data class RequestContext(val userId: String, val isAdmin: Boolean)

object RequestContextKey : GExecutorContextExtensionKey<RequestContext>
```

### Passing extensions on each request

```kotlin
val extensions = GExecutorContextExtensionSet {
    set(RequestContextKey, RequestContext(userId = "42", isAdmin = false))
}

val result = executor.execute(
    documentSource = "{ me { name } }",
    extensions = extensions
)
```

### Reading extensions inside resolvers

```kotlin
Object<User>(type("User")) {
    field("isAdmin" of !Boolean) {
        resolve {
            val ctx = execution.extensions[RequestContextKey]
            ctx?.isAdmin ?: false
        }
    }
}
```

## Building Query Documents Programmatically

Use `GraphQL.document { }` to build a `GDocument` without writing a query string:

```kotlin
val document = GraphQL.document {
    query("GetUser") {
        "user" {
            argument("id", value("42"))
            "id"()
            "name"()
            "email"()
        }
    }
}

// Equivalent to:
// query GetUser {
//   user(id: "42") {
//     id
//     name
//     email
//   }
// }
```

### Mutations and subscriptions

```kotlin
val doc = GraphQL.document {
    mutation("CreatePost") {
        "createPost" {
            "id"()
            "title"()
        }
    }
}

GraphQL.document {
    subscription("OnMessage") {
        "messageAdded" {
            "id"()
            "body"()
        }
    }
}
```

### Fragment spreads and inline fragments

```kotlin
val doc = GraphQL.document {
    query {
        "search" {
            on("Post") {
                "title"()
            }
            on("User") {
                "name"()
            }
            fragment("CommonFields")
        }
    }

    fragment("CommonFields") {
        // on a particular type — set via the fragment builder
        "id"()
        "createdAt"()
    }
}
```

### Field aliases

```kotlin
GraphQL.document {
    query {
        "user"(alias = "currentUser") {
            "id"()
        }
    }
}
```

## Module Structure

```
io.fluidsonic.graphql

  fluid-graphql-language
    - GNode, GDocument, GSchema (AST model)
    - GDocument.parse(), GNode.print()
    - GResult, GError, GErrorException
    - GNodeExtensionKey, GNodeExtensionSet
    - Visitor, NodeWalker (AST traversal)

        ^
        |

  fluid-graphql-dsl
    - GraphQL.schema { } (GSchemaBuilder)
    - GraphQL.document { } (GraphQLDocumentBuilder)

        ^
        |

  fluid-graphql-execution
    - GExecutor.default()
    - GDocument.validate()
    - GFieldResolver, GRootResolver
    - GOutputCoercer, GNodeInputCoercer, GVariableInputCoercer
    - GExceptionHandler
    - GExecutorContextExtensionKey, GExecutorContextExtensionSet
```

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
