package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


class NodeWalkerTest {

	@Test
	fun `walks a document`() {
		val intValue = GIntValue(1)
		val queryTypeRef = GTypeRef("Query")

		val argument = GArgument(name = "argument", value = intValue)
		val directive = GDirective(name = "directive")

		val fieldSelection = GFieldSelection(
			name = "field",
			arguments = listOf(argument)
		)
		val selectionSet = GSelectionSet(
			selections = listOf(fieldSelection)
		)
		val variableDefinition = GVariableDefinition(
			name = "variable",
			type = GIntTypeRef,
			defaultValue = intValue
		)
		val fragmentDefinition = GFragmentDefinition(
			directives = listOf(directive),
			name = "fragment",
			typeCondition = queryTypeRef,
			selectionSet = selectionSet,
			variableDefinitions = listOf(variableDefinition)
		)
		val operationDefinition = GOperationDefinition(
			directives = listOf(directive),
			type = GOperationType.query,
			name = "query",
			selectionSet = selectionSet,
			variableDefinitions = listOf(variableDefinition)
		)
		val document = GDocument(
			definitions = listOf(
				fragmentDefinition,
				operationDefinition
			)
		)

		val walker = document.walk()

		assertSame(expected = walker.child, actual = document)
		assertSame(expected = walker.parent, actual = null)
		assertFalse(walker.ascend())

		with(walker) {
			assertDescend(toChild = document)
			assertNextChild(parent = document, child = fragmentDefinition)
			assertDescend(toChild = fragmentDefinition)
			assertNextChild(parent = fragmentDefinition, child = fragmentDefinition.nameNode)
			assertNextChild(parent = fragmentDefinition, child = variableDefinition)
			assertDescend(toChild = variableDefinition)
			assertNextChild(parent = variableDefinition, child = variableDefinition.nameNode)
			assertNextChild(parent = variableDefinition, child = GIntTypeRef)
			assertDescend(toChild = GIntTypeRef)
			assertNextChild(parent = GIntTypeRef, child = GIntTypeRef.nameNode)
			assertAscend(toParent = variableDefinition, toChild = GIntTypeRef)
			assertNextChild(parent = variableDefinition, child = intValue)
			assertAscend(toParent = fragmentDefinition, toChild = variableDefinition)
			assertNextChild(parent = fragmentDefinition, child = queryTypeRef)
			assertNextChild(parent = fragmentDefinition, child = directive)
			assertDescend(toChild = directive)
			assertNextChild(parent = directive, child = directive.nameNode)
			assertAscend(toParent = fragmentDefinition, toChild = directive)
			assertNextChild(parent = fragmentDefinition, child = selectionSet)
			assertDescend(toChild = selectionSet)
			assertNextChild(parent = selectionSet, child = fieldSelection)
			assertDescend(toChild = fieldSelection)
			assertNextChild(parent = fieldSelection, child = fieldSelection.nameNode)
			assertNextChild(parent = fieldSelection, child = argument)
			assertDescend(toChild = argument)
			assertNextChild(parent = argument, child = argument.nameNode)
			assertNextChild(parent = argument, child = intValue)
			assertAscend(toParent = fieldSelection, toChild = argument)
			assertAscend(toParent = selectionSet, toChild = fieldSelection)
			assertAscend(toParent = fragmentDefinition, toChild = selectionSet)
			assertAscend(toParent = document, toChild = fragmentDefinition)

			assertNextChild(parent = document, child = operationDefinition)
			assertDescend(toChild = operationDefinition)
			assertNextChild(parent = operationDefinition, child = operationDefinition.nameNode)
			assertNextChild(parent = operationDefinition, child = variableDefinition)
			assertDescend(toChild = variableDefinition)
			assertNextChild(parent = variableDefinition, child = variableDefinition.nameNode)
			assertNextChild(parent = variableDefinition, child = GIntTypeRef)
			assertDescend(toChild = GIntTypeRef)
			assertNextChild(parent = GIntTypeRef, child = GIntTypeRef.nameNode)
			assertAscend(toParent = variableDefinition, toChild = GIntTypeRef)
			assertAscend(toParent = operationDefinition, toChild = variableDefinition, checkEndOfChildren = false)
			assertAscend(toParent = document, toChild = operationDefinition, checkEndOfChildren = false)
			assertAscend(toParent = null, toChild = document)
		}

		assertNull(walker.nextChild())
		assertFalse(walker.ascend())
		assertSame(expected = walker.child, actual = document)
		assertSame(expected = walker.parent, actual = null)

//		walker.skipChildren() // ignored before entering root node
//
//		assertEquals(actual = walker.depth, expected = 0)
//		assertTrue(actual = walker.hasNext())
//		assertFalse(actual = walker.nextIsChild())
//		assertFalse(actual = walker.nextIsSibling())
//		assertNull(actual = walker.current)
//
//		assertNext(walker, depth = 1, hasNext = true, nextIsChild = true, node = document)
//		assertNext(walker, depth = 2, hasNext = true, nextIsChild = true, node = fragmentDefinition)
//		assertNext(walker, depth = 3, hasNext = true, nextIsSibling = true, node = fragmentDefinition.nameNode)
//		assertNext(walker, depth = 3, hasNext = true, nextIsChild = true, node = variableDefinition)
//		assertNext(walker, depth = 4, hasNext = true, nextIsSibling = true, node = variableDefinition.nameNode)
//		assertNext(walker, depth = 4, hasNext = true, nextIsChild = true, node = GIntTypeRef)
//		assertNext(walker, depth = 5, hasNext = true, node = GIntTypeRef.nameNode)
//		assertNext(walker, depth = 4, hasNext = true, node = intValue)
//		assertNext(walker, depth = 3, hasNext = true, nextIsChild = true, node = queryTypeRef)
//		assertNext(walker, depth = 4, hasNext = true, node = queryTypeRef.nameNode)
//		assertNext(walker, depth = 3, hasNext = true, nextIsChild = true, node = directive)
//		assertNext(walker, depth = 4, hasNext = true, node = directive.nameNode)
//		assertNext(walker, depth = 3, hasNext = true, nextIsChild = true, node = selectionSet)
//		assertNext(walker, depth = 4, hasNext = true, nextIsChild = true, node = fieldSelection)
//		assertNext(walker, depth = 5, hasNext = true, nextIsSibling = true, node = fieldSelection.nameNode)
//		assertNext(walker, depth = 5, hasNext = true, nextIsChild = true, node = argument)
//		assertNext(walker, depth = 6, hasNext = true, nextIsSibling = true, node = argument.nameNode)
//		assertNext(walker, depth = 6, hasNext = true, node = intValue)
//
//		assertNext(walker, depth = 2, hasNext = true, nextIsChild = true, node = operationDefinition)
//		assertNext(walker, depth = 3, hasNext = true, nextIsSibling = true, node = operationDefinition.nameNode)
//		assertNext(walker, depth = 3, hasNext = true, nextIsChild = true, node = variableDefinition)
//		assertNext(walker, depth = 4, hasNext = true, nextIsSibling = true, node = variableDefinition.nameNode)
//		assertNext(walker, depth = 4, hasNext = true, nextIsChild = true, node = GIntTypeRef)
//		walker.skipChildren()
//		assertFalse(walker.nextIsChild())
//		assertNext(walker, depth = 4, hasNext = true, node = intValue)
//		assertNext(walker, depth = 3, hasNext = true, nextIsChild = true, node = directive)
//		assertNext(walker, depth = 4, hasNext = true, node = directive.nameNode)
//		assertNext(walker, depth = 3, hasNext = true, nextIsChild = true, node = selectionSet)
//		walker.skipChildren()
//		assertFalse(walker.hasNext())
//		assertFalse(walker.nextIsChild())
//
//		assertNext(walker, node = null, depth = 0, hasNext = false)
//		walker.skipChildren() // ignored after leaving root node
//		assertNext(walker, node = null, depth = 0, hasNext = false)
	}


//	private fun assertNext(walker: NodeWalker, node: GAst?, depth: Int, hasNext: Boolean, nextIsChild: Boolean = false, nextIsSibling: Boolean = false) {
//		assertSame(actual = walker.next(), expected = node, message = "walker.next()")
//		assertEquals(actual = walker.depth, expected = depth, message = "walker.depth")
//		assertEquals(actual = walker.hasNext(), expected = hasNext, message = "walker.hasNext()")
//		assertEquals(actual = walker.nextIsChild(), expected = nextIsChild, message = "walker.nextIsChild()")
//		assertEquals(actual = walker.nextIsSibling(), expected = nextIsSibling, message = "walker.nextIsSibling()")
//		assertSame(actual = walker.current, expected = node, message = "walker.current")
//	}


	private fun NodeWalker.assertAscend(toParent: GAst?, toChild: GAst, checkEndOfChildren: Boolean = true) {
		if (checkEndOfChildren) {
			assertNull(nextChild(), message = "walker.nextChild()")
			assertNull(this.child, message = "walker.child")
			assertFalse(descend(), message = "walker.descend()")
		}

		assertTrue(ascend(), message = "walker.ascend()")
		assertSame(expected = this.parent, actual = toParent, message = "walker.parent")
		assertSame(expected = this.child, actual = toChild, message = "walker.child")
	}


	private fun NodeWalker.assertDescend(toChild: GAst) {
		assertTrue(descend(), message = "walker.descend()")
		assertNull(this.child, message = "walker.child")
		assertSame(expected = this.parent, actual = toChild, message = "walker.parent")
	}


	private fun NodeWalker.assertNextChild(parent: GAst, child: GAst?) {
		assertSame(expected = child, actual = nextChild(), message = "walker.nextChild()")
		assertSame(expected = parent, actual = this.parent, message = "walker.parent")
		assertSame(expected = child, actual = this.child, message = "walker.child")
	}
}
