package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ContextProvidingVisitorTests {

	@Test
	fun visitorProvidesContextToChildVisitors() {
		val document = GDocument(
			definitions = listOf(
				GOperationDefinition(
					type = GOperationType.query,
					selectionSet = GSelectionSet(
						selections = listOf(
							GFieldSelection(name = "field")
						)
					)
				)
			)
		)

		val schema = GSchema.parse("""
			|type Query { field: String }
		""".trimMargin()).valueWithoutErrorsOrThrow()

		val visitedNodes = mutableListOf<String>()
		val parentNodes = mutableListOf<String?>()

		val visitor = object : Visitor.Hierarchical<Unit, CapturingVisitorContext>() {
			override fun onAny(node: GNode, data: CapturingVisitorContext, visit: Visit) {
				visitedNodes += node::class.simpleName ?: "Unknown"
				parentNodes += data.parentNode?.let { it::class.simpleName }
			}
		}

		val context = CapturingVisitorContext(document, schema)
		document.accept(visitor.contextualize(context))

		// The document should be visited first with no parent
		assertTrue(visitedNodes.contains("GDocument"))
		assertEquals(actual = parentNodes[0], expected = null)

		// Subsequent nodes should have parent context set
		val operationIndex = visitedNodes.indexOf("GOperationDefinition")
		assertTrue(operationIndex > 0)
		assertEquals(actual = parentNodes[operationIndex], expected = "GDocument")
	}


	@Test
	fun visitChildrenCalledAutomatically() {
		// ContextProvidingVisitor calls visitChildren() if the inner visitor doesn't.
		val document = GDocument(
			definitions = listOf(
				GOperationDefinition(
					type = GOperationType.query,
					selectionSet = GSelectionSet(
						selections = listOf(
							GFieldSelection(name = "field")
						)
					)
				)
			)
		)

		val schema = GSchema.parse("""
			|type Query { field: String }
		""".trimMargin()).valueWithoutErrorsOrThrow()

		val visitedNodeTypes = mutableListOf<String>()

		// This visitor does NOT call visitChildren() — ContextProvidingVisitor should do it automatically.
		val visitor = object : Visitor.Hierarchical<Unit, CapturingVisitorContext>() {
			override fun onAny(node: GNode, data: CapturingVisitorContext, visit: Visit) {
				visitedNodeTypes += node::class.simpleName ?: "Unknown"
				// Deliberately NOT calling visit.visitChildren()
			}
		}

		val context = CapturingVisitorContext(document, schema)
		document.accept(visitor.contextualize(context))

		// Even though we didn't call visitChildren(), the ContextProvidingVisitor should
		// have called it for us, so we should see child nodes visited.
		assertTrue(visitedNodeTypes.size > 1, "Expected multiple nodes to be visited, got: $visitedNodeTypes")
		assertTrue(visitedNodeTypes.contains("GDocument"))
		assertTrue(visitedNodeTypes.contains("GOperationDefinition"))
		assertTrue(visitedNodeTypes.contains("GSelectionSet"))
		assertTrue(visitedNodeTypes.contains("GFieldSelection"))
	}


	@Test
	fun skipChildrenPreventsAutomaticVisitChildren() {
		val document = GDocument(
			definitions = listOf(
				GOperationDefinition(
					type = GOperationType.query,
					selectionSet = GSelectionSet(
						selections = listOf(
							GFieldSelection(name = "field")
						)
					)
				)
			)
		)

		val schema = GSchema.parse("""
			|type Query { field: String }
		""".trimMargin()).valueWithoutErrorsOrThrow()

		val visitedNodeTypes = mutableListOf<String>()

		// This visitor skips children at the document level.
		val visitor = object : Visitor.Hierarchical<Unit, CapturingVisitorContext>() {
			override fun onAny(node: GNode, data: CapturingVisitorContext, visit: Visit) {
				visitedNodeTypes += node::class.simpleName ?: "Unknown"
				if (node is GDocument) {
					visit.skipChildren()
				}
				// For non-document nodes, ContextProvidingVisitor calls visitChildren() automatically
			}
		}

		val context = CapturingVisitorContext(document, schema)
		document.accept(visitor.contextualize(context))

		// Only the document should be visited since we skipped its children
		assertEquals(actual = visitedNodeTypes, expected = listOf("GDocument"))
	}


	@Test
	fun contextScopedToSubtree() {
		// Verify that context (parent node, related operation, etc.) is correctly
		// scoped so that visiting one subtree doesn't leak context into another.
		val field1 = GFieldSelection(name = "field1")
		val field2 = GFieldSelection(name = "field2")

		val document = GDocument(
			definitions = listOf(
				GOperationDefinition(
					type = GOperationType.query,
					selectionSet = GSelectionSet(
						selections = listOf(field1, field2)
					)
				)
			)
		)

		val schema = GSchema.parse("""
			|type Query { field1: String, field2: String }
		""".trimMargin()).valueWithoutErrorsOrThrow()

		val fieldParents = mutableMapOf<String, String?>()

		val visitor = object : Visitor.Hierarchical<Unit, CapturingVisitorContext>() {
			override fun onAny(node: GNode, data: CapturingVisitorContext, visit: Visit) {
				if (node is GFieldSelection) {
					fieldParents[node.name] = data.parentNode?.let { it::class.simpleName }
				}
			}
		}

		val context = CapturingVisitorContext(document, schema)
		document.accept(visitor.contextualize(context))

		// Both fields should have GSelectionSet as their parent, not each other
		assertEquals(actual = fieldParents["field1"], expected = "GSelectionSet")
		assertEquals(actual = fieldParents["field2"], expected = "GSelectionSet")
	}


	@Test
	fun explicitVisitChildrenNotCalledTwice() {
		// If the inner visitor calls visitChildren() explicitly, the ContextProvidingVisitor
		// should not call it a second time.
		val document = GDocument(
			definitions = listOf(
				GOperationDefinition(
					type = GOperationType.query,
					selectionSet = GSelectionSet(
						selections = listOf(
							GFieldSelection(name = "field")
						)
					)
				)
			)
		)

		val schema = GSchema.parse("""
			|type Query { field: String }
		""".trimMargin()).valueWithoutErrorsOrThrow()

		var visitCount = 0

		val visitor = object : Visitor.Hierarchical<Unit, CapturingVisitorContext>() {
			override fun onAny(node: GNode, data: CapturingVisitorContext, visit: Visit) {
				if (node is GFieldSelection) {
					visitCount++
				}
				// Explicitly call visitChildren()
				visit.visitChildren()
			}
		}

		val context = CapturingVisitorContext(document, schema)
		document.accept(visitor.contextualize(context))

		// The field should be visited exactly once -- explicit visitChildren should not cause double visits
		assertEquals(actual = visitCount, expected = 1)
	}


	private class CapturingVisitorContext(document: GDocument, schema: GSchema) : VisitorContext(document, schema)
}
