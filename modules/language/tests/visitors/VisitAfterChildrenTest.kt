package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GName
import io.fluidsonic.graphql.GNode
import io.fluidsonic.graphql.GOperationDefinition
import io.fluidsonic.graphql.Visit
import io.fluidsonic.graphql.Visitor
import io.fluidsonic.graphql.accept
import io.fluidsonic.graphql.parallelize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Pins the contract of `Visit.afterChildren`, which is what a rule must use instead of code after `visitChildren()`
// now that the parallel traversal descends in a loop rather than from within the visitors' frames.
class VisitAfterChildrenTest {

	private val document = GDocument.parse("{ field }").valueWithoutErrorsOrThrow()

	@Test
	fun testRunsAfterTheNodesSubtree() {
		val events = mutableListOf<String>()

		document.accept(RecordingVisitor(name = "A", events = events))

		assertEquals(
			actual = events,
			expected = listOf(
				"enter A GDocument",
				"enter A GOperationDefinition",
				"enter A GSelectionSet",
				"enter A GFieldSelection",
				"enter A GName",
				"after0 A GName",
				"after0 A GFieldSelection",
				"after0 A GSelectionSet",
				"after0 A GOperationDefinition",
				"after0 A GDocument",
			),
		)
	}

	@Test
	fun testRunsInRegistrationOrderWithinOneVisitor() {
		val events = mutableListOf<String>()

		document.accept(RecordingVisitor(name = "A", events = events, blockCount = 3, recordsNodes = { it is GName }))

		assertEquals(
			actual = events,
			expected = listOf("enter A GName", "after0 A GName", "after1 A GName", "after2 A GName"),
		)
	}

	@Test
	fun testRunsInVisitingOrderOfAParallelVisit() {
		val events = mutableListOf<String>()
		val visitors = listOf("A", "B", "C").map { name ->
			RecordingVisitor(name = name, events = events, recordsNodes = { it is GName })
		}

		document.accept(visitors.parallelize(), data = null)

		// `visitInParallel` calls its visitors' `leave` in the same order it calls their `enter`.
		assertEquals(
			actual = events,
			expected = listOf(
				"enter A GName",
				"enter B GName",
				"enter C GName",
				"after0 A GName",
				"after0 B GName",
				"after0 C GName",
			),
		)
	}

	@Test
	fun testDoesNotRunForTheNodeWhoseChildrenTheVisitorSkipped() {
		val events = mutableListOf<String>()
		val skipping = RecordingVisitor(name = "A", events = events, skipsChildrenAt = { it is GOperationDefinition })
		val descending = RecordingVisitor(name = "B", events = events)

		document.accept(listOf(skipping, descending).parallelize(), data = null)

		// Skipping ends A's business with the operation, so it gets no block for it even though B goes on to
		// traverse the operation's children. A did not skip the document's children, so it does leave the document.
		assertEquals(
			actual = events,
			expected = listOf(
				"enter A GDocument",
				"enter B GDocument",
				"enter A GOperationDefinition",
				"enter B GOperationDefinition",
				"enter B GSelectionSet",
				"enter B GFieldSelection",
				"enter B GName",
				"after0 B GName",
				"after0 B GFieldSelection",
				"after0 B GSelectionSet",
				"after0 B GOperationDefinition",
				"after0 A GDocument",
				"after0 B GDocument",
			),
		)
	}

	@Test
	fun testDoesNotRunForAVisitorThatAbortedWhileOthersCarryOn() {
		val events = mutableListOf<String>()
		val aborting = RecordingVisitor(name = "A", events = events, abortsAt = { it is GOperationDefinition })
		val descending = RecordingVisitor(name = "B", events = events)

		document.accept(listOf(aborting, descending).parallelize(), data = null)

		// Aborting drops A's pending blocks for every node still open — including the document it entered before.
		assertEquals(
			actual = events,
			expected = listOf(
				"enter A GDocument",
				"enter B GDocument",
				"enter A GOperationDefinition",
				"enter B GOperationDefinition",
				"enter B GSelectionSet",
				"enter B GFieldSelection",
				"enter B GName",
				"after0 B GName",
				"after0 B GFieldSelection",
				"after0 B GSelectionSet",
				"after0 B GOperationDefinition",
				"after0 B GDocument",
			),
		)
	}

	@Test
	fun testDoesNotRunOnceEveryVisitorHasAborted() {
		val events = mutableListOf<String>()
		val visitors = listOf("A", "B").map { name ->
			RecordingVisitor(name = name, events = events, abortsAt = { it is GOperationDefinition })
		}

		document.accept(visitors.parallelize(), data = null)

		assertEquals(
			actual = events,
			expected = listOf("enter A GDocument", "enter B GDocument", "enter A GOperationDefinition", "enter B GOperationDefinition"),
		)
	}

	@Test
	fun testDoesNotRunForANodeWhoseChildrenASingleVisitorSkipped() {
		val events = mutableListOf<String>()

		document.accept(RecordingVisitor(name = "A", events = events, skipsChildrenAt = { it is GOperationDefinition }))

		assertEquals(
			actual = events,
			expected = listOf("enter A GDocument", "enter A GOperationDefinition", "after0 A GDocument"),
		)
	}

	@Test
	fun testDoesNotRunWhenAnExceptionUnwindsTheVisit() {
		val events = mutableListOf<String>()
		val failing = RecordingVisitor(name = "A", events = events, throwsAt = { it is GName })

		assertFailsWith<IllegalStateException> { document.accept(listOf(failing).parallelize(), data = null) }
		assertFailsWith<IllegalStateException> { document.accept(failing) }

		assertEquals(actual = events.filter { it.startsWith("after") }, expected = emptyList())
	}

	@Test
	fun testRejectsUsingTheVisitFromWithinTheBlock() {
		val visitor = object : Visitor.WithoutData<Unit>() {

			override fun onNode(node: GNode, visit: Visit) {
				visit.afterChildren { visit.visitChildren() }
			}
		}

		assertFailsWith<IllegalStateException> { document.accept(listOf(visitor).parallelize(), data = null) }
	}
}

private class RecordingVisitor(
	private val name: String,
	private val events: MutableList<String>,
	private val blockCount: Int = 1,
	private val recordsNodes: (node: GNode) -> Boolean = { true },
	private val abortsAt: (node: GNode) -> Boolean = { false },
	private val skipsChildrenAt: (node: GNode) -> Boolean = { false },
	private val throwsAt: (node: GNode) -> Boolean = { false },
) : Visitor.WithoutData<Unit>() {

	override fun onNode(node: GNode, visit: Visit) {
		val label = node::class.simpleName

		if (recordsNodes(node)) {
			events += "enter $name $label"

			repeat(blockCount) { index ->
				visit.afterChildren { events += "after$index $name $label" }
			}
		}

		when {
			throwsAt(node) -> error("Failing at $label.")
			abortsAt(node) -> visit.abort()
			skipsChildrenAt(node) -> visit.skipChildren()
			else -> Unit
		}
	}
}
