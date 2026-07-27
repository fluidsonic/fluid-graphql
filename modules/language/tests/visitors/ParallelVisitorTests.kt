package testing
import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GNode
import io.fluidsonic.graphql.GOperationDefinition
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.Visit
import io.fluidsonic.graphql.Visitor
import io.fluidsonic.graphql.VisitorContext
import io.fluidsonic.graphql.accept
import io.fluidsonic.graphql.contextualize
import io.fluidsonic.graphql.parallelize
import io.fluidsonic.graphql.parallelizeContextualized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParallelVisitorTests {

	@Test
	fun emptyVisitorListProducesNoOp() {
		val document = GDocument.parse(
			"""
			|query { field }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		// An empty list of visitors should produce a noOp visitor that does nothing
		val visitor = emptyList<Visitor<Unit, StackCollectingVisitor.Data>>().parallelize()
		// Should not throw
		document.accept(visitor, data = StackCollectingVisitor.Data())
	}

	@Test
	fun singleVisitorSeesAllNodes() {
		val visitorTarget = StackCollectingVisitor.Target()
		val singleVisitor = StackCollectingVisitor(target = visitorTarget)

		val document = GDocument.parse(
			"""
			|query { field }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		document.accept(listOf(singleVisitor).parallelize(), data = StackCollectingVisitor.Data())

		val nodeTypes = visitorTarget.stacks.map { it.last().substringBefore("(") }

		assertTrue(nodeTypes.contains("Document"))
		assertTrue(nodeTypes.contains("OperationDefinition"))
		assertTrue(nodeTypes.contains("SelectionSet"))
		assertTrue(nodeTypes.contains("FieldSelection"))
	}

	@Test
	fun allVisitorsAbort() {
		// When ALL visitors abort, the parallel visitor should stop traversal.
		val visitorTarget = StackCollectingVisitor.Target()
		val visitors = List(3) { index ->
			StackCollectingVisitor(
				suffix = ".${'A' + index}",
				target = visitorTarget,
				abortsInNode = { it is GDocument },
			)
		}

		val document = GDocument.parse(
			"""
			|query { field(arg: 1) { nested } }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		document.accept(visitors.parallelize(), data = StackCollectingVisitor.Data())

		// All visitors abort at Document, so we should only see Document visits
		val nodeTypes = visitorTarget.stacks.map { it.last().substringBefore(".") }
		assertTrue(nodeTypes.all { it.startsWith("Document") })
		assertEquals(actual = visitorTarget.stacks.size, expected = 3)
	}

	@Test
	fun eachVisitorSeesAllNodes() {
		val visitorTarget = StackCollectingVisitor.Target()
		val visitors = List(2) { index ->
			StackCollectingVisitor(
				suffix = ".${'A' + index}",
				target = visitorTarget,
			)
		}

		val document = GDocument.parse(
			"""
			|query { field }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		document.accept(visitors.parallelize(), data = StackCollectingVisitor.Data())

		// Extract node types each visitor saw
		val allLastEntries = visitorTarget.stacks.map { it.last() }

		val visitorANodeTypes = allLastEntries
			.filter { ".A" in it }
			.map { it.substringBefore(".") }
			.sorted()

		val visitorBNodeTypes = allLastEntries
			.filter { ".B" in it }
			.map { it.substringBefore(".") }
			.sorted()

		// Both visitors should see the same node types
		assertEquals(actual = visitorANodeTypes, expected = visitorBNodeTypes)
	}

	@Test
	fun skipChildrenInOneVisitorDoesNotAffectOthers() {
		// Visitor A skips children at OperationDefinition, Visitor B does not.
		val visitorTarget = StackCollectingVisitor.Target()
		val visitors = List(2) { index ->
			StackCollectingVisitor(
				suffix = ".${'A' + index}",
				target = visitorTarget,
				skipsChildrenInNode = {
					index == 0 && it is GOperationDefinition
				},
			)
		}

		val document = GDocument.parse(
			"""
			|query { field }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		document.accept(visitors.parallelize(), data = StackCollectingVisitor.Data())

		// Each stack snapshot's last element is the most recently visited node.
		// Filter by suffix to get nodes each visitor visited.
		val allLastEntries = visitorTarget.stacks.map { it.last() }
		val visitorAEntries = allLastEntries.filter { ".A" in it }
		val visitorBEntries = allLastEntries.filter { ".B" in it }

		val visitorANodeTypes = visitorAEntries.map { it.substringBefore(".") }.distinct()
		val visitorBNodeTypes = visitorBEntries.map { it.substringBefore(".") }.distinct()

		assertTrue(
			visitorANodeTypes.contains("Document"),
			"Visitor A should see Document, got: $visitorANodeTypes from entries: $visitorAEntries (all: $allLastEntries)",
		)
		assertTrue(visitorANodeTypes.contains("OperationDefinition"), "Visitor A should see OperationDefinition")
		assertFalse(visitorANodeTypes.contains("SelectionSet"), "Visitor A should not see SelectionSet after skipChildren")
		assertFalse(visitorANodeTypes.contains("FieldSelection"), "Visitor A should not see FieldSelection after skipChildren")

		assertTrue(visitorBNodeTypes.contains("Document"), "Visitor B should see Document")
		assertTrue(visitorBNodeTypes.contains("OperationDefinition"), "Visitor B should see OperationDefinition")
		assertTrue(visitorBNodeTypes.contains("SelectionSet"), "Visitor B should see SelectionSet")
		assertTrue(visitorBNodeTypes.contains("FieldSelection"), "Visitor B should see FieldSelection")
	}

	@Test
	fun lastVisitorSkippingASubtreeDoesNotHideItFromTheOthers() {
		// The recursive coordinator dispatched a node's visitors in a `do … while (childIndex < children.size)` loop
		// and only descended from within a visitor's `visitChildren()`. When the LAST visitor was already skipping a
		// subtree, its dispatch returned immediately, the loop ended with the cursor past the last visitor, and the
		// descent never happened — dropping the subtree for *every* visitor rather than for the skipping one alone.
		val visitorTarget = StackCollectingVisitor.Target()
		val visitors = List(2) { index ->
			StackCollectingVisitor(
				suffix = ".${'A' + index}",
				target = visitorTarget,
				skipsChildrenInNode = {
					index == 1 && it is GOperationDefinition
				},
			)
		}

		val document = GDocument.parse(
			"""
			|query { field { nested } }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		document.accept(visitors.parallelize(), data = StackCollectingVisitor.Data())

		val visitorANodeTypes = visitorTarget.stacks
			.map { it.last() }
			.filter { ".A(" in it }
			.map { it.substringBefore(".") }

		// Both levels below the operation must still reach visitor A. The bug left it with the outer selection set
		// and nothing beneath.
		assertEquals(actual = visitorANodeTypes.count { it == "SelectionSet" }, expected = 2)
		assertEquals(actual = visitorANodeTypes.count { it == "FieldSelection" }, expected = 2)

		// And the skipping visitor must still see nothing below the operation it skipped.
		val visitorBNodeTypes = visitorTarget.stacks
			.map { it.last() }
			.filter { ".B(" in it }
			.map { it.substringBefore(".") }

		assertEquals(actual = visitorBNodeTypes, expected = listOf("Document", "OperationDefinition"))
	}

	@Test
	fun abortInOneVisitorDoesNotAffectOthers() {
		// Visitor A aborts at Document, Visitor B continues normally.
		val visitorTarget = StackCollectingVisitor.Target()
		val visitors = List(2) { index ->
			StackCollectingVisitor(
				suffix = ".${'A' + index}",
				target = visitorTarget,
				abortsInNode = {
					index == 0 && it is GDocument
				},
			)
		}

		val document = GDocument.parse(
			"""
			|query { field }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		document.accept(visitors.parallelize(), data = StackCollectingVisitor.Data())

		// Extract all visited node entries per visitor by checking the suffix in the entry name
		val allLastEntries = visitorTarget.stacks.map { it.last() }

		val visitorANodeTypes = allLastEntries
			.filter { it.startsWith("Document.A") || it.contains(".A(") }
			.map { it.substringBefore(".") }
			.distinct()

		val visitorBNodeTypes = allLastEntries
			.filter { it.startsWith("Document.B") || it.contains(".B(") }
			.map { it.substringBefore(".") }
			.distinct()

		// Visitor A aborted at Document, so it should only see Document
		assertTrue(visitorANodeTypes.contains("Document"), "Visitor A should see Document, got $visitorANodeTypes from $allLastEntries")
		assertFalse(visitorANodeTypes.contains("OperationDefinition"), "Visitor A should not see deeper nodes after abort")

		// Visitor B should see all nodes
		assertTrue(visitorBNodeTypes.contains("Document"), "Visitor B should see Document")
		assertTrue(visitorBNodeTypes.contains("OperationDefinition"), "Visitor B should see OperationDefinition")
	}

	@Test
	fun parallelVisitorsTraverseFragmentDefinitions() {
		val visitorTarget = StackCollectingVisitor.Target()
		val visitors = List(2) { index ->
			StackCollectingVisitor(
				suffix = ".${'A' + index}",
				target = visitorTarget,
			)
		}

		val document = GDocument.parse(
			"""
			|fragment F on Query { field }
			|query { ...F }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		document.accept(visitors.parallelize(), data = StackCollectingVisitor.Data())

		// Both visitors should see the FragmentDefinition
		val visitorANodes = visitorTarget.stacks
			.map { it.last() }
			.filter { it.contains(".A)") }
			.map { it.substringBefore(".") }

		val visitorBNodes = visitorTarget.stacks
			.map { it.last() }
			.filter { it.contains(".B)") }
			.map { it.substringBefore(".") }

		assertTrue(visitorANodes.contains("FragmentDefinition"))
		assertTrue(visitorBNodes.contains("FragmentDefinition"))
		assertTrue(visitorANodes.contains("FragmentSelection"))
		assertTrue(visitorBNodes.contains("FragmentSelection"))
	}

	@Test
	fun contextIsAdvancedOncePerNodeForEveryVisitor() {
		val document = GDocument.parse(
			"""
			|query { obj { x } }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		val schema = GSchema.parse(
			"""
			|type Query { obj: Obj }
			|type Obj { x: String }
			""".trimMargin(),
		).valueWithoutErrorsOrThrow()

		fun observingVisitor(observations: MutableList<String>) = object : Visitor<Unit, VisitorContext>() {

			override fun onNode(node: GNode, data: VisitorContext, visit: Visit) {
				observations += "${node::class.simpleName} " +
					"parent=${data.parentNode?.let { it::class.simpleName }} " +
					"relatedFieldDefinition=${data.relatedFieldDefinition?.name}"
			}
		}

		val observationsPerVisitor = List(3) { mutableListOf<String>() }
		document.accept(
			visitor = observationsPerVisitor.map(::observingVisitor).parallelizeContextualized(),
			data = VisitorContext(document, schema),
		)

		// Every visitor must observe the very same context for the very same node. A per-visitor context advance
		// would make each visitor after the first see the node as its own parent.
		assertEquals(actual = observationsPerVisitor[1], expected = observationsPerVisitor[0])
		assertEquals(actual = observationsPerVisitor[2], expected = observationsPerVisitor[0])

		// A single contextualized visitor is the reference for "advanced exactly once per node". Matching it for
		// every node rules out both a missing advance and a double advance anywhere in the document.
		val referenceObservations = mutableListOf<String>()
		document.accept(observingVisitor(referenceObservations).contextualize(VisitorContext(document, schema)))

		assertEquals(actual = observationsPerVisitor[0], expected = referenceObservations)

		// Guard the reference itself: the context must be advanced for more than just the root.
		assertEquals(actual = referenceObservations.first(), expected = "GDocument parent=null relatedFieldDefinition=null")
		assertTrue(
			"GFieldSelection parent=GSelectionSet relatedFieldDefinition=obj" in referenceObservations,
			"Expected the outer field selection to resolve against Query, got: $referenceObservations",
		)
		assertTrue(
			"GFieldSelection parent=GSelectionSet relatedFieldDefinition=x" in referenceObservations,
			"Expected the nested field selection to resolve against Obj, got: $referenceObservations",
		)
	}
}
