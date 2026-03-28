package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ParallelVisitorTests {

	@Test
	fun emptyVisitorListProducesNoOp() {
		val document = GDocument.parse("""
			|query { field }
		""".trimMargin()).valueWithoutErrorsOrThrow()

		// An empty list of visitors should produce a noOp visitor that does nothing
		val visitor = emptyList<Visitor<Unit, StackCollectingVisitor.Data>>().parallelize()
		// Should not throw
		document.accept(visitor, data = StackCollectingVisitor.Data())
	}


	@Test
	fun singleVisitorSeesAllNodes() {
		val visitorTarget = StackCollectingVisitor.Target()
		val singleVisitor = StackCollectingVisitor(target = visitorTarget)

		val document = GDocument.parse("""
			|query { field }
		""".trimMargin()).valueWithoutErrorsOrThrow()

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
				abortsInNode = { it is GDocument }
			)
		}

		val document = GDocument.parse("""
			|query { field(arg: 1) { nested } }
		""".trimMargin()).valueWithoutErrorsOrThrow()

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

		val document = GDocument.parse("""
			|query { field }
		""".trimMargin()).valueWithoutErrorsOrThrow()

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
				}
			)
		}

		val document = GDocument.parse("""
			|query { field }
		""".trimMargin()).valueWithoutErrorsOrThrow()

		document.accept(visitors.parallelize(), data = StackCollectingVisitor.Data())

		// Each stack snapshot's last element is the most recently visited node.
		// Filter by suffix to get nodes each visitor visited.
		val allLastEntries = visitorTarget.stacks.map { it.last() }
		val visitorAEntries = allLastEntries.filter { ".A" in it }
		val visitorBEntries = allLastEntries.filter { ".B" in it }

		val visitorANodeTypes = visitorAEntries.map { it.substringBefore(".") }.distinct()
		val visitorBNodeTypes = visitorBEntries.map { it.substringBefore(".") }.distinct()

		assertTrue(visitorANodeTypes.contains("Document"), "Visitor A should see Document, got: $visitorANodeTypes from entries: $visitorAEntries (all: $allLastEntries)")
		assertTrue(visitorANodeTypes.contains("OperationDefinition"), "Visitor A should see OperationDefinition")
		assertFalse(visitorANodeTypes.contains("SelectionSet"), "Visitor A should not see SelectionSet after skipChildren")
		assertFalse(visitorANodeTypes.contains("FieldSelection"), "Visitor A should not see FieldSelection after skipChildren")

		assertTrue(visitorBNodeTypes.contains("Document"), "Visitor B should see Document")
		assertTrue(visitorBNodeTypes.contains("OperationDefinition"), "Visitor B should see OperationDefinition")
		assertTrue(visitorBNodeTypes.contains("SelectionSet"), "Visitor B should see SelectionSet")
		assertTrue(visitorBNodeTypes.contains("FieldSelection"), "Visitor B should see FieldSelection")
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
				}
			)
		}

		val document = GDocument.parse("""
			|query { field }
		""".trimMargin()).valueWithoutErrorsOrThrow()

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

		val document = GDocument.parse("""
			|fragment F on Query { field }
			|query { ...F }
		""".trimMargin()).valueWithoutErrorsOrThrow()

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
}
