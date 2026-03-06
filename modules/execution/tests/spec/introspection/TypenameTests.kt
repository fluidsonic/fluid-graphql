package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §4 — Meta-fields: __typename
class TypenameTests {

	@Test
	fun testTypenameOnQuery() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __typename
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		assertEquals(expected = "Query", actual = data["__typename"])
	}


	@Test
	fun testTypenameOnObjectField() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { Any() } }
			}
			Object<Any>(MyObject) {
				field("id" of String) { resolve { "1" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  obj {
			    __typename
			    id
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val obj = data["obj"] as Map<String, Any?>
		assertEquals(expected = "MyObject", actual = obj["__typename"])
	}


	@Test
	fun testTypenameAlongsideOtherFields() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __typename
			  foo
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		assertEquals(expected = "Query", actual = data["__typename"])
		assertEquals(expected = "bar", actual = data["foo"])
	}


	@Test
	fun testTypenameOnUnionMember() = runTest {
		val schema = GraphQL.schema {
			val Cat by type
			val Dog by type
			val Pet by type
			Query {
				field("pet" of Pet) { resolve { Cat() } }
			}
			Object<Cat>(Cat) {
				field("meows" of Boolean) { resolve { it.meows } }
			}
			Object<Dog>(Dog) {
				field("barks" of Boolean) { resolve { it.barks } }
			}
			Union(Pet with Cat or Dog)
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  pet {
			    ... on Cat { __typename meows }
			    ... on Dog { __typename barks }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val pet = data["pet"] as Map<String, Any?>
		assertEquals(expected = "Cat", actual = pet["__typename"])
	}


	@Test
	fun testTypenameOnInterfaceImplementor() = runTest {
		val schema = GraphQL.schema {
			val Named by type
			val Dog by type
			Query {
				field("named" of Named) { resolve { DogImpl() } }
			}
			Interface(Named) {
				field("name" of String)
			}
			Object<DogImpl>(Dog implements Named) {
				field("name" of String) { resolve { it.name } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  named {
			    ... on Dog { __typename name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val named = data["named"] as Map<String, Any?>
		assertEquals(expected = "Dog", actual = named["__typename"])
	}


	private class Cat(val meows: Boolean = true)
	private class Dog(val barks: Boolean = true)
	private class DogImpl(val name: String = "Buddy")
}
