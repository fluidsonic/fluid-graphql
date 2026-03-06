package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §4.2 — Type Introspection
class TypeIntrospectionTests {

	@Test
	fun testObjectTypeKind() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Query") {
			    kind
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertEquals(expected = "OBJECT", actual = type["kind"])
	}


	@Test
	fun testScalarTypeKind() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "String") {
			    kind
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertEquals(expected = "SCALAR", actual = type["kind"])
	}


	@Test
	fun testInterfaceTypeKind() = runTest {
		val schema = GraphQL.schema {
			val MyInterface by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			Interface(MyInterface) {
				field("name" of String)
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyInterface") {
			    kind
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertEquals(expected = "INTERFACE", actual = type["kind"])
	}


	@Test
	fun testUnionTypeKind() = runTest {
		val schema = GraphQL.schema {
			val TypeA by type
			val TypeB by type
			val MyUnion by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			Object<Any>(TypeA) {
				field("id" of String) { resolve { "" } }
			}
			Object<Any>(TypeB) {
				field("id" of String) { resolve { "" } }
			}
			Union(MyUnion with TypeA or TypeB)
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyUnion") {
			    kind
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertEquals(expected = "UNION", actual = type["kind"])
	}


	@Test
	fun testEnumTypeKind() = runTest {
		val schema = GraphQL.schema {
			val MyEnum by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			Enum(MyEnum) {
				value("A")
				value("B")
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyEnum") {
			    kind
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertEquals(expected = "ENUM", actual = type["kind"])
	}


	@Test
	fun testInputObjectTypeKind() = runTest {
		val schema = GraphQL.schema {
			val MyInput by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			InputObject(MyInput) {
				argument("id" of String)
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyInput") {
			    kind
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertEquals(expected = "INPUT_OBJECT", actual = type["kind"])
	}


	@Test
	fun testListTypeKind() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("items" of List(String)) { resolve { emptyList<String>() } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Query the type of the `items` field via __schema types; use __type on Query then fields
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Query") {
			    fields {
			      name
			      type {
			        kind
			        ofType { name kind }
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val itemsField = fields.first { it["name"] == "items" }
		@Suppress("UNCHECKED_CAST")
		val fieldType = itemsField["type"] as Map<String, Any?>
		assertEquals(expected = "LIST", actual = fieldType["kind"])
		@Suppress("UNCHECKED_CAST")
		val ofType = fieldType["ofType"] as Map<String, Any?>
		assertEquals(expected = "String", actual = ofType["name"])
	}


	@Test
	fun testNonNullTypeKind() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("required" of !String) { resolve { "hello" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Query") {
			    fields {
			      name
			      type {
			        kind
			        ofType { name kind }
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val requiredField = fields.first { it["name"] == "required" }
		@Suppress("UNCHECKED_CAST")
		val fieldType = requiredField["type"] as Map<String, Any?>
		assertEquals(expected = "NON_NULL", actual = fieldType["kind"])
		@Suppress("UNCHECKED_CAST")
		val ofType = fieldType["ofType"] as Map<String, Any?>
		assertEquals(expected = "String", actual = ofType["name"])
	}


	@Test
	fun testObjectTypeFields() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("id" of String) { resolve { "" } }
				field("name" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      type { name }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val fieldNames = fields.map { it["name"] }
		assertTrue(fieldNames.contains("id"), "Expected 'id' field")
		assertTrue(fieldNames.contains("name"), "Expected 'name' field")
	}


	@Test
	fun testScalarTypeFieldsNull() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "String") {
			    fields { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertNull(type["fields"])
	}


	@Test
	fun testObjectTypeInterfaces() = runTest {
		val schema = GraphQL.schema {
			val MyInterface by type
			val MyObject by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			Interface(MyInterface) {
				field("name" of String)
			}
			Object<Any>(MyObject implements MyInterface) {
				field("name" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    interfaces { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val interfaces = type["interfaces"] as List<Map<String, Any?>>
		val interfaceNames = interfaces.map { it["name"] }
		assertTrue(interfaceNames.contains("MyInterface"), "Expected 'MyInterface' in interfaces")
	}


	@Test
	fun testInterfacePossibleTypes() = runTest {
		val schema = GraphQL.schema {
			val MyInterface by type
			val ImplA by type
			val ImplB by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			Interface(MyInterface) {
				field("name" of String)
			}
			Object<Any>(ImplA implements MyInterface) {
				field("name" of String) { resolve { "" } }
			}
			Object<Any>(ImplB implements MyInterface) {
				field("name" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyInterface") {
			    possibleTypes { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val possibleTypes = type["possibleTypes"] as List<Map<String, Any?>>
		val names = possibleTypes.map { it["name"] }
		assertTrue(names.contains("ImplA"), "Expected 'ImplA' in possibleTypes")
		assertTrue(names.contains("ImplB"), "Expected 'ImplB' in possibleTypes")
	}


	@Test
	fun testUnionPossibleTypes() = runTest {
		val schema = GraphQL.schema {
			val TypeA by type
			val TypeB by type
			val MyUnion by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			Object<Any>(TypeA) {
				field("id" of String) { resolve { "" } }
			}
			Object<Any>(TypeB) {
				field("id" of String) { resolve { "" } }
			}
			Union(MyUnion with TypeA or TypeB)
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyUnion") {
			    possibleTypes { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val possibleTypes = type["possibleTypes"] as List<Map<String, Any?>>
		val names = possibleTypes.map { it["name"] }
		assertTrue(names.contains("TypeA"), "Expected 'TypeA' in possibleTypes")
		assertTrue(names.contains("TypeB"), "Expected 'TypeB' in possibleTypes")
	}


	@Test
	fun testEnumValues() = runTest {
		val schema = GraphQL.schema {
			val Status by type
			Query {
				field("status" of Status) { resolve { "ACTIVE" } }
			}
			Enum(Status) {
				value("ACTIVE")
				value("INACTIVE")
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Status") {
			    enumValues { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val enumValues = type["enumValues"] as List<Map<String, Any?>>
		val names = enumValues.map { it["name"] }
		assertTrue(names.contains("ACTIVE"), "Expected 'ACTIVE' in enumValues")
		assertTrue(names.contains("INACTIVE"), "Expected 'INACTIVE' in enumValues")
	}


	@Test
	fun testInputObjectInputFields() = runTest {
		val schema = GraphQL.schema {
			val MyInput by type
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			InputObject(MyInput) {
				argument("username" of String)
				argument("email" of String)
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyInput") {
			    inputFields { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val inputFields = type["inputFields"] as List<Map<String, Any?>>
		val names = inputFields.map { it["name"] }
		assertTrue(names.contains("username"), "Expected 'username' in inputFields")
		assertTrue(names.contains("email"), "Expected 'email' in inputFields")
	}


	@Test
	fun testListTypeOfType() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("items" of List(String)) { resolve { emptyList<String>() } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Query") {
			    fields {
			      name
			      type {
			        kind
			        ofType { name kind }
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val itemsField = fields.first { it["name"] == "items" }
		@Suppress("UNCHECKED_CAST")
		val fieldType = itemsField["type"] as Map<String, Any?>
		assertEquals(expected = "LIST", actual = fieldType["kind"])
		@Suppress("UNCHECKED_CAST")
		val ofType = fieldType["ofType"] as Map<String, Any?>
		assertEquals(expected = "String", actual = ofType["name"])
	}


	@Test
	fun testNonNullTypeOfType() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("required" of !String) { resolve { "hello" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Query") {
			    fields {
			      name
			      type {
			        kind
			        ofType { name kind }
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val requiredField = fields.first { it["name"] == "required" }
		@Suppress("UNCHECKED_CAST")
		val fieldType = requiredField["type"] as Map<String, Any?>
		assertEquals(expected = "NON_NULL", actual = fieldType["kind"])
		@Suppress("UNCHECKED_CAST")
		val ofType = fieldType["ofType"] as Map<String, Any?>
		assertEquals(expected = "String", actual = ofType["name"])
	}


	@Test
	fun testFieldsIncludeDeprecated() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("active" of String) { resolve { "" } }
				field("old" of String) {
					deprecated("Use active instead")
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields(includeDeprecated: true) { name isDeprecated }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val fieldNames = fields.map { it["name"] }
		assertTrue(fieldNames.contains("active"))
		assertTrue(fieldNames.contains("old"))
	}
}
