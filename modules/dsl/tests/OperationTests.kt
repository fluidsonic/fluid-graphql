package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class OperationTests {

	@Test
	fun testAnonymousMutation() {
		assertEquals(
			actual = GraphQL.mutation {
				__typename()
			}.toString(),
			expected = """
				mutation {
					__typename
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testAnonymousQuery() {
		assertEquals(
			actual = GraphQL.query {
				__typename()
			}.toString(),
			expected = """
				{
					__typename
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testAnonymousSubscription() {
		assertEquals(
			actual = GraphQL.subscription {
				__typename()
			}.toString(),
			expected = """
				subscription {
					__typename
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testFieldArguments() {
		assertEquals(
			actual = GraphQL.query {
				"foo" {
					arguments { "a" to 1; "b" to null }
				}
			}.toString(),
			expected = """
				{
					foo(a: 1, b: null)
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testFragments() {
		assertEquals(
			actual = GraphQL.query {
				val Type by type
				val fragment1 = GFragmentRef("fragment1")

				fragment(fragment1)
				fragment1()
				fragment("fragment1")
				fragment(fragment1) {
					directives { "cool"() }
				}
				fragment1 {
					directives { "cool"() }
				}
				fragment("fragment1") {
					directives { "cool"() }
				}

				on(Type) {
					__typename()
				}
				on("Type") {
					directives { "foo"() }

					__typename()
				}
			}.toString(),
			expected = """
				{
					...fragment1
					...fragment1
					...fragment1
					...fragment1 @cool
					...fragment1 @cool
					...fragment1 @cool
					... on Type {
						__typename
					}
					... on Type @foo {
						__typename
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testNamedMutation() {
		assertEquals(
			actual = GraphQL.mutation("Foo") {
				__typename()
			}.toString(),
			expected = """
				mutation Foo {
					__typename
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testNamedQuery() {
		assertEquals(
			actual = GraphQL.query("Foo") {
				__typename()
			}.toString(),
			expected = """
				query Foo {
					__typename
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testNamedSubscription() {
		assertEquals(
			actual = GraphQL.subscription("Foo") {
				__typename()
			}.toString(),
			expected = """
				subscription Foo {
					__typename
				}

			""".trimIndent(),
		)
	}


	@Test
	fun testVariables() {
		assertEquals(
			actual = GraphQL.query {
				val Type by type
				val variable1 = variable("variable1", Type)
				val variable2 = variable("variable2", "Type")
				val variable3 by variable(Type)
				val variable4 by variable("Type")
				val variable5 = variable("variable5", Type) {
					default(null)
				}
				val variable6 = variable("variable6", "Type") {
					default(1)
				}
				val variable7 by variable(Type) {
					default(list { add(1); add(2) })
				}
				val variable8 by variable("Type") {
					directives { "foo" { arguments { "a" to 2 } } }
				}

				"field" {
					directives { "skip"() }
					arguments {
						obj {
							"a" to variable1
							"b" to variable2
							"c" to variable3
							"d" to variable4
						}
					}
				}
				"field2"()
				"field3" {
					arguments {
						list {
							add(variable5)
							add(variable6)
							add(variable7)
							add(variable8)
						}
					}
				}
			}.toString(),
			expected = """
				query (${'$'}variable1: Type, ${'$'}variable2: Type, ${'$'}variable3: Type, ${'$'}variable4: Type, ${'$'}variable5: Type = null, ${'$'}variable6: Type = 1, ${'$'}variable7: Type = [1, 2], ${'$'}variable8: Type @foo(a: 2)) {
					field @skip
					field2
					field3
				}

			""".trimIndent(),
		)
	}
}
