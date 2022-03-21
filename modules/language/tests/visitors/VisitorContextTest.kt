package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class VisitorContextTest {

	// FIXME Add more cases.
	@Test
	fun testProvidesCorrectInformation() {
		val stringValue = GStringValue("value")
		val stringArgument = GArgument(name = "string", value = stringValue)
		val scalarObjectValue = GObjectValue(listOf(stringArgument))
		val scalarArgument = GArgument(name = "scalar", value = scalarObjectValue)
		val objectValue = GObjectValue(listOf(stringArgument, scalarArgument))
		val inputArgument = GArgument(name = "input", value = objectValue)
		val directive = GDirective(name = "directive", arguments = listOf(stringArgument, inputArgument))
		val fieldSelection = GFieldSelection(name = "field", arguments = listOf(stringArgument, inputArgument))
		val defaultValue = GStringValue("defaultValue")
		val variableDefinition = GVariableDefinition(name = "variable", type = GStringTypeRef, defaultValue = defaultValue)
		val fragmentFieldSelection = GFieldSelection(name = "fragmentStringField")
		val fragmentSelection = GFragmentSelection("stringFieldFragment")
		val selectionSet = GSelectionSet(selections = listOf(fieldSelection, fragmentSelection))
		val operationDefinition = GOperationDefinition(
			type = GOperationType.query,
			selectionSet = selectionSet,
			directives = listOf(directive),
			variableDefinitions = listOf(variableDefinition)
		)
		val fragmentSelectionSet = GSelectionSet(listOf(fragmentFieldSelection))
		val fragmentDefinition = GFragmentDefinition(name = "stringFieldFragment", typeCondition = GNamedTypeRef("Result"), fragmentSelectionSet)
		val document = GDocument(definitions = listOf(operationDefinition, fragmentDefinition))

		/*
		 * query ($variable: String = "defaultValue") @directive(string: "value", input: {
		 * 	string: "value",
		 * 	scalar: {
		 * 		string: "value"
		 * 	}
		 * }) {
		 * 	field(string: "value", input: {
		 * 		string: "value",
		 * 		scalar: {
		 * 			string: "value"
		 * 		}
		 * 	})
		 * 	...stringFieldFragment
		 * }
		 * fragment stringFieldFragment on Result {
		 * 	fragmentStringField
		 * }
		 */

		val schema = GSchema.parse(
			"""
			|directive @directive(string: String!, input: Input!) on QUERY
			|scalar Scalar
			|input Input { string: String!, scalar: Scalar }
			|type Query { field(string: String!, input: Input!): String!, fragmentField: String! }
			|type Result {string: String!, input: Input!, fragmentStringField: String!}
		""".trimMargin()
		).valueWithoutErrorsOrThrow()

		val directiveDefinition = schema.directiveDefinition("directive")!!
		val directiveStringArgumentDefinition = directiveDefinition.argumentDefinition("string")!!
		val directiveInputArgumentDefinition = directiveDefinition.argumentDefinition("input")!!
		val queryType = schema.rootTypeForOperationType(GOperationType.query)!!
		val inputType = schema.resolveType("Input") as GInputObjectType
		val scalarType = schema.resolveType("Scalar") as GCustomScalarType
		val resultType = schema.resolveType("Result") as GObjectType
		val fieldDefinition = queryType.fieldDefinition("field")!!
		val fieldStringArgumentDefinition = fieldDefinition.argumentDefinition("string")!!
		val fieldInputArgumentDefinition = fieldDefinition.argumentDefinition("input")!!
		val inputScalarArgumentDefinition = inputType.argumentDefinition("scalar")!!
		val inputStringArgumentDefinition = inputType.argumentDefinition("string")!!
		val fragmentFieldDefinition = resultType.fieldDefinition("fragmentStringField")
		val nonNullInputType = GNonNullType(inputType)
		val nonNullStringType = GNonNullType(GStringType)

		val context = CapturingContext(document, schema)
		document.accept(CapturingVisitor().contextualize(context))

		val actualElements = context.elements
		val expectedElements = listOf(
			CapturedElement(
				node = document
			),
			CapturedElement(
				node = operationDefinition,
				parentNode = document,
				relatedOperationDefinition = operationDefinition,
				relatedType = queryType
			),
			CapturedElement(
				node = variableDefinition,
				parentNode = operationDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = GStringType
			),
			CapturedElement(
				node = variableDefinition.nameNode,
				parentNode = variableDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = GStringType
			),
			CapturedElement(
				node = variableDefinition.type,
				parentNode = variableDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = GStringType,
			),
			CapturedElement(
				node = GStringTypeRef.nameNode,
				parentNode = variableDefinition.type,
				relatedOperationDefinition = operationDefinition,
				relatedType = GStringType
			),
			CapturedElement(
				node = defaultValue,
				parentNode = variableDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = GStringType
			),
			CapturedElement(
				node = directive,
				parentNode = operationDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition
			),
			CapturedElement(
				node = directive.nameNode,
				parentNode = directive,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition
			),
			CapturedElement(
				node = stringArgument,
				parentNode = directive,
				relatedArgumentDefinition = directiveStringArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = nonNullStringType,
			),
			CapturedElement(
				node = stringArgument.nameNode,
				parentNode = stringArgument,
				relatedArgumentDefinition = directiveStringArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = nonNullStringType,
			),
			CapturedElement(
				node = stringValue,
				parentNode = stringArgument,
				relatedArgumentDefinition = directiveStringArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = nonNullStringType,
			),
			CapturedElement(
				node = inputArgument,
				parentNode = directive,
				relatedArgumentDefinition = directiveInputArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = nonNullInputType
			),
			CapturedElement(
				node = inputArgument.nameNode,
				parentNode = inputArgument,
				relatedArgumentDefinition = directiveInputArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = nonNullInputType
			),
			CapturedElement(
				node = objectValue,
				parentNode = inputArgument,
				relatedArgumentDefinition = directiveInputArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedType = nonNullInputType
			),
			CapturedElement(
				node = stringArgument,
				parentNode = objectValue,
				relatedArgumentDefinition = inputStringArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = stringArgument.nameNode,
				parentNode = stringArgument,
				relatedArgumentDefinition = inputStringArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = stringValue,
				parentNode = stringArgument,
				relatedArgumentDefinition = inputStringArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = scalarArgument,
				parentNode = objectValue,
				relatedArgumentDefinition = inputScalarArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedType = scalarType
			),
			CapturedElement(
				node = scalarArgument.nameNode,
				parentNode = scalarArgument,
				relatedArgumentDefinition = inputScalarArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedType = scalarType
			),
			CapturedElement(
				node = scalarObjectValue,
				parentNode = scalarArgument,
				relatedArgumentDefinition = inputScalarArgumentDefinition,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedType = scalarType
			),
			CapturedElement(
				node = stringArgument,
				parentNode = scalarObjectValue,
				relatedArgumentDefinition = null,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = scalarType,
			),
			CapturedElement(
				node = stringArgument.nameNode,
				parentNode = stringArgument,
				relatedArgumentDefinition = null,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = scalarType,
			),
			CapturedElement(
				node = stringValue,
				parentNode = stringArgument,
				relatedArgumentDefinition = null,
				relatedDirective = directive,
				relatedDirectiveDefinition = directiveDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = scalarType,
			),
			CapturedElement(
				node = selectionSet,
				parentNode = operationDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelectionSet = selectionSet
			),
			CapturedElement(
				node = fieldSelection,
				parentNode = selectionSet,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = fieldSelection.nameNode,
				parentNode = fieldSelection,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = stringArgument,
				parentNode = fieldSelection,
				relatedArgumentDefinition = fieldStringArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = stringArgument.nameNode,
				parentNode = stringArgument,
				relatedArgumentDefinition = fieldStringArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = stringValue,
				parentNode = stringArgument,
				relatedArgumentDefinition = fieldStringArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = inputArgument,
				parentNode = fieldSelection,
				relatedArgumentDefinition = fieldInputArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullInputType
			),
			CapturedElement(
				node = inputArgument.nameNode,
				parentNode = inputArgument,
				relatedArgumentDefinition = fieldInputArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullInputType
			),
			CapturedElement(
				node = objectValue,
				parentNode = inputArgument,
				relatedArgumentDefinition = fieldInputArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullInputType
			),
			CapturedElement(
				node = stringArgument,
				parentNode = objectValue,
				relatedArgumentDefinition = inputStringArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = stringArgument.nameNode,
				parentNode = stringArgument,
				relatedArgumentDefinition = inputStringArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = stringValue,
				parentNode = stringArgument,
				relatedArgumentDefinition = inputStringArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = scalarArgument,
				parentNode = objectValue,
				relatedArgumentDefinition = inputScalarArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = scalarType
			),
			CapturedElement(
				node = scalarArgument.nameNode,
				parentNode = scalarArgument,
				relatedArgumentDefinition = inputScalarArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = scalarType
			),
			CapturedElement(
				node = scalarObjectValue,
				parentNode = scalarArgument,
				relatedArgumentDefinition = inputScalarArgumentDefinition,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = inputType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
				relatedType = scalarType
			),
			CapturedElement(
				node = stringArgument,
				parentNode = scalarObjectValue,
				relatedArgumentDefinition = null,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = scalarType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
			),
			CapturedElement(
				node = stringArgument.nameNode,
				parentNode = stringArgument,
				relatedArgumentDefinition = null,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = scalarType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
			),
			CapturedElement(
				node = stringValue,
				parentNode = stringArgument,
				relatedArgumentDefinition = null,
				relatedFieldDefinition = fieldDefinition,
				relatedFieldSelection = fieldSelection,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = scalarType,
				relatedSelection = fieldSelection,
				relatedSelectionSet = selectionSet,
			),
			CapturedElement(
				node = fragmentSelection,
				parentNode = selectionSet,
				relatedFragmentDefinition = fragmentDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fragmentSelection,
				relatedSelectionSet = selectionSet,
				relatedType = resultType
			),
			CapturedElement(
				node = fragmentSelection.nameNode,
				parentNode = fragmentSelection,
				relatedFragmentDefinition = fragmentDefinition,
				relatedOperationDefinition = operationDefinition,
				relatedParentType = queryType,
				relatedSelection = fragmentSelection,
				relatedSelectionSet = selectionSet,
				relatedType = resultType
			),
			CapturedElement(
				node = fragmentDefinition,
				parentNode = document,
				relatedFragmentDefinition = fragmentDefinition,
				relatedParentType = resultType,
				relatedSelectionSet = selectionSet,
				relatedType = resultType
			),
			CapturedElement(
				node = fragmentDefinition.nameNode,
				parentNode = fragmentDefinition,
				relatedFragmentDefinition = fragmentDefinition,
				relatedParentType = resultType,
				relatedSelectionSet = selectionSet,
				relatedType = resultType
			),
			CapturedElement(
				node = fragmentDefinition.typeCondition,
				parentNode = fragmentDefinition,
				relatedFragmentDefinition = fragmentDefinition,
				relatedParentType = resultType,
				relatedSelectionSet = selectionSet,
				relatedType = resultType
			),
			CapturedElement(
				node = fragmentDefinition.typeCondition.nameNode,
				parentNode = fragmentDefinition.typeCondition,
				relatedFragmentDefinition = fragmentDefinition,
				relatedParentType = resultType,
				relatedSelectionSet = selectionSet,
				relatedType = resultType
			),
			CapturedElement(
				node = fragmentSelectionSet,
				parentNode = fragmentDefinition,
				relatedFragmentDefinition = fragmentDefinition,
				relatedParentSelectionSet = selectionSet,
				relatedParentType = resultType,
				relatedSelectionSet = fragmentSelectionSet
			),
			CapturedElement(
				node = fragmentFieldSelection,
				parentNode = fragmentSelectionSet,
				relatedFieldDefinition = fragmentFieldDefinition,
				relatedFieldSelection = fragmentFieldSelection,
				relatedFragmentDefinition = fragmentDefinition,
				relatedParentSelectionSet = selectionSet,
				relatedParentType = resultType,
				relatedSelection = fragmentFieldSelection,
				relatedSelectionSet = fragmentSelectionSet,
				relatedType = nonNullStringType
			),
			CapturedElement(
				node = fragmentFieldSelection.nameNode,
				parentNode = fragmentFieldSelection,
				relatedFieldDefinition = fragmentFieldDefinition,
				relatedFieldSelection = fragmentFieldSelection,
				relatedFragmentDefinition = fragmentDefinition,
				relatedParentSelectionSet = selectionSet,
				relatedParentType = resultType,
				relatedSelection = fragmentFieldSelection,
				relatedSelectionSet = fragmentSelectionSet,
				relatedType = nonNullStringType
			)
		)

		assertEquals(expected = expectedElements, actual = actualElements)
	}


	@Suppress("EqualsOrHashCode")
	private data class CapturedElement(
		val node: GNode,
		val parentNode: GNode? = null,
		val relatedArgumentDefinition: GArgumentDefinition? = null,
		val relatedDirective: GDirective? = null,
		val relatedDirectiveDefinition: GDirectiveDefinition? = null,
		val relatedFieldDefinition: GFieldDefinition? = null,
		val relatedFieldSelection: GFieldSelection? = null,
		val relatedFragmentDefinition: GFragmentDefinition? = null,
		val relatedOperationDefinition: GOperationDefinition? = null,
		val relatedParentSelectionSet: GSelectionSet? = null,
		val relatedParentType: GType? = null,
		val relatedSelection: GSelection? = null,
		val relatedSelectionSet: GSelectionSet? = null,
		val relatedType: GType? = null,
	) {


		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other !is CapturedElement) return false

			if (!node.equalsNode(other.node)) return false
			if (!parentNode.equalsNode(other.parentNode)) return false
			if (!relatedArgumentDefinition.equalsNode(other.relatedArgumentDefinition)) return false
			if (!relatedDirective.equalsNode(other.relatedDirective)) return false
			if (!relatedDirectiveDefinition.equalsNode(other.relatedDirectiveDefinition)) return false
			if (!relatedFieldDefinition.equalsNode(other.relatedFieldDefinition)) return false
			if (!relatedFieldSelection.equalsNode(other.relatedFieldSelection)) return false
			if (!relatedFragmentDefinition.equalsNode(other.relatedFragmentDefinition)) return false
			if (!relatedOperationDefinition.equalsNode(other.relatedOperationDefinition)) return false
			if (!relatedParentSelectionSet.equalsNode(other.relatedParentSelectionSet)) return false
			if (!relatedParentType.equalsNode(other.relatedParentType)) return false
			if (!relatedSelection.equalsNode(other.relatedSelection)) return false
			if (!relatedSelectionSet.equalsNode(other.relatedSelectionSet)) return false
			if (!relatedType.equalsNode(other.relatedType)) return false

			return true
		}


		override fun toString(): String {
			return "CapturedElement(\n" +
				"\tnode=${identityOf(node)}, \n" +
				parentNode?.let { "\tparentNode=${identityOf(parentNode)}, \n" }.orEmpty() +
				relatedArgumentDefinition?.let { "\trelatedArgumentDefinition=${identityOf(relatedArgumentDefinition)}, \n" }.orEmpty() +
				relatedDirective?.let { "\trelatedDirective=${identityOf(relatedDirective)}, \n" }.orEmpty() +
				relatedDirectiveDefinition?.let { "\trelatedDirectiveDefinition=${identityOf(relatedDirectiveDefinition)}, \n" }.orEmpty() +
				relatedFieldDefinition?.let { "\trelatedFieldDefinition=${identityOf(relatedFieldDefinition)}, \n" }.orEmpty() +
				relatedFieldSelection?.let { "\trelatedFieldSelection=${identityOf(relatedFieldSelection)}, \n" }.orEmpty() +
				relatedFragmentDefinition?.let { "\trelatedFragmentDefinition=${identityOf(relatedFragmentDefinition)}, \n" }.orEmpty() +
				relatedOperationDefinition?.let { "\trelatedOperationDefinition=${identityOf(relatedOperationDefinition)}, \n" }.orEmpty() +
				relatedParentSelectionSet?.let { "\trelatedParentSelectionSet=${identityOf(relatedParentSelectionSet)}, \n" }.orEmpty() +
				relatedParentType?.let { "\trelatedParentType=${identityOf(relatedParentType)}, \n" }.orEmpty() +
				relatedSelection?.let { "\trelatedSelection=${identityOf(relatedSelection)}, \n" }.orEmpty() +
				relatedSelectionSet?.let { "\trelatedSelectionSet=${identityOf(relatedSelectionSet)}, \n" }.orEmpty() +
				relatedType?.let { "\trelatedType=${identityOf(relatedType)}\n" }.orEmpty() +
				")"
		}
	}


	private class CapturingContext(document: GDocument, schema: GSchema) : VisitorContext(document, schema) {

		val elements = mutableListOf<CapturedElement>()
	}


	private class CapturingVisitor : Visitor.Hierarchical<Unit, CapturingContext>() {

		override fun onAny(node: GNode, data: CapturingContext, visit: Visit) {
			data.elements += CapturedElement(
				node = node,
				parentNode = data.parentNode,
				relatedArgumentDefinition = data.relatedArgumentDefinition,
				relatedDirective = data.relatedDirective,
				relatedDirectiveDefinition = data.relatedDirectiveDefinition,
				relatedFieldDefinition = data.relatedFieldDefinition,
				relatedFieldSelection = data.relatedFieldSelection,
				relatedFragmentDefinition = data.relatedFragmentDefinition,
				relatedOperationDefinition = data.relatedOperationDefinition,
				relatedParentSelectionSet = data.relatedParentSelectionSet,
				relatedParentType = data.relatedParentType,
				relatedSelection = data.relatedSelection,
				relatedSelectionSet = data.relatedSelectionSet,
				relatedType = data.relatedType,
			)
		}
	}
}
