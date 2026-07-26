package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.Object
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Gate C — an unknown field, an undefined fragment and an unresolvable type condition are all
// skipped: their response key is absent, not present-and-null.
// Verified against graphql@17.0.2: `{ known unknownField }` -> {"data":{"known":"K"}}.
//
// Every fixture uses the `execute(document: GDocument)` overload on purpose. The `documentSource`
// overloads validate, which would turn these into request errors instead.
class SkippedSelectionTests {

	private data class ObjData(val dummy: String = "")

	private val schema = GraphQL.schema {
		val Obj by type

		Object<ObjData>(Obj) {
			field("a" of String) { resolve { "A" } }
		}

		Query {
			field("known" of String) { resolve { "K" } }
			field("o" of Obj) { resolve { ObjData() } }
		}

		Mutation {
			field("known" of String) { resolve { "K" } }
			field("o" of Obj) { resolve { ObjData() } }
		}
	}

	private suspend fun execute(documentSource: String): Map<String, Any?> {
		val executor = GExecutor.default(schema = schema)
		val document = GDocument.parse(documentSource).valueOrThrow()

		return executor.serializeResult(executor.execute(document))
	}

	private suspend fun assertSkipped(selection: String, expectedData: Map<String, Any?>) {
		assertEquals(actual = execute("{ $selection }"), expected = mapOf("data" to expectedData))
		assertEquals(actual = execute("mutation { $selection }"), expected = mapOf("data" to expectedData))
	}

	// The fixture that actually matters: it is the only one reaching the field-dispatch path.
	@Test
	fun testUnknownField_isSkipped() = runTest {
		assertSkipped("known unknownField", mapOf("known" to "K"))
	}

	@Test
	fun testUnknownIntrospectionField_isSkipped() = runTest {
		assertSkipped("__foo", emptyMap())
	}

	@Test
	fun testUndefinedFragment_isSkipped() = runTest {
		assertSkipped("known ...Missing", mapOf("known" to "K"))
	}

	@Test
	fun testInlineFragmentOnUndefinedType_isSkipped() = runTest {
		assertSkipped("known ... on Undefined { known }", mapOf("known" to "K"))
	}

	// A *named* fragment whose type condition does not resolve must behave exactly like the inline case
	// above. Upstream makes no distinction: `collectFields` routes both through
	// `doesFragmentConditionMatch`, which returns false whenever `typeFromAST` yields nothing.
	//
	// Verified against graphql@17.0.2 — both forms yield {"data":{"known":"K"}} with no errors:
	//   { known ...F } fragment F on Undefined { known }
	//   { known ... on Undefined { known } }
	@Test
	fun testNamedFragmentOnUndefinedType_isSkipped() = runTest {
		assertEquals(
			actual = execute("{ known ...F } fragment F on Undefined { known }"),
			expected = mapOf("data" to mapOf("known" to "K")),
		)
		assertEquals(
			actual = execute("mutation { known ...F } fragment F on Undefined { known }"),
			expected = mapOf("data" to mapOf("known" to "K")),
		)
	}

	@Test
	fun testUnknownNestedField_isSkipped() = runTest {
		assertSkipped("o { unknownNested }", mapOf("o" to emptyMap<String, Any?>()))
	}

	@Test
	fun testUnknownFieldAloneLeavesDataEmpty() = runTest {
		assertSkipped("unknownField", emptyMap())
	}
}
