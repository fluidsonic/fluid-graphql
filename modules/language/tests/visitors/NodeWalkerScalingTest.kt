package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GFieldSelection
import io.fluidsonic.graphql.GNode
import io.fluidsonic.graphql.GOperationDefinition
import io.fluidsonic.graphql.GOperationType
import io.fluidsonic.graphql.GSelection
import io.fluidsonic.graphql.GSelectionSet
import io.fluidsonic.graphql.walk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Gate E (walker half) — walking a node's children must cost O(n), not O(n²).
 *
 * The cost proxy is deterministic rather than wall-clock: the fixture hands the AST a `List<GSelection>` that
 * counts how many of its elements the traversal actually reads. Every element the walker looks at goes through
 * that list, so the counter *is* the work done — no timing, no JIT warm-up, no CI flakiness.
 *
 * The fixture gives every field a distinct response name (`b0 b1 b2 …`) on purpose. The n-fields-under-one-
 * response-name variant belongs to the validation half of gate E, where pairwise conflict detection is
 * quadratic by design.
 */
class NodeWalkerScalingTest {

	@Test
	fun testWalkReadsEachSiblingSelectionExactlyOnce() {
		val count = 1_000
		val selections = countingSelections(count = count)
		walkFully(documentWith(selections = selections))

		assertEquals(actual = selections.readCount, expected = count.toLong(), message = "selections read while walking")
	}

	@Test
	fun testWalkCostGrowsLinearlyWithSiblingSelectionCount() {
		val counts = listOf(8_000, 16_000, 32_000)
		val readCounts = counts.map { count ->
			val selections = countingSelections(count = count)
			walkFully(documentWith(selections = selections))

			selections.readCount
		}

		val series = counts.indices.joinToString(", ") { index -> "${counts[index]} → ${readCounts[index]}" }

		counts.forEachIndexed { index, count ->
			val limit = 4L * count

			assertTrue(
				readCounts[index] <= limit,
				message = "walking $count sibling selections read ${readCounts[index]} selections, expected at most $limit ($series)",
			)
		}

		for (index in 1..counts.lastIndex) {
			val ratio = readCounts[index].toDouble() / readCounts[index - 1].toDouble()

			assertTrue(
				ratio <= 2.5,
				message = "doubling ${counts[index - 1]} sibling selections to ${counts[index]} multiplied the reads by $ratio, " +
					"expected at most 2.5 ($series)",
			)
		}
	}

	private fun countingSelections(count: Int) = CountingSelectionList(
		selections = List(count) { index -> GFieldSelection(name = "b$index") },
	)

	private fun documentWith(selections: List<GSelection>) = GDocument(
		definitions = listOf(
			GOperationDefinition(
				type = GOperationType.query,
				selectionSet = GSelectionSet(selections = selections),
			),
		),
	)

	/** Depth-first traversal of the whole subtree below [node], visiting every child exactly once. */
	private fun walkFully(node: GNode) {
		val walker = node.walk()

		while (true) {
			if (walker.descend()) {
				continue
			}

			while (walker.nextChild() === null) {
				if (!walker.ascend()) {
					return
				}
			}
		}
	}

	/** A [List] that counts how many of its elements are read, whether by index or through its iterator. */
	private class CountingSelectionList(private val selections: List<GSelection>) : List<GSelection> by selections {

		var readCount = 0L
			private set

		override fun get(index: Int): GSelection {
			readCount += 1

			return selections[index]
		}

		override fun iterator(): Iterator<GSelection> = CountingIterator()

		private inner class CountingIterator : Iterator<GSelection> {

			private var index = 0

			override fun hasNext() = index < selections.size

			override fun next(): GSelection {
				if (!hasNext()) {
					throw NoSuchElementException()
				}

				readCount += 1
				val selection = selections[index]
				index += 1

				return selection
			}
		}
	}
}
