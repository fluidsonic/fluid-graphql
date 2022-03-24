package testing

import io.fluidsonic.graphql.*
import kotlin.test.*

class PrinterTest {

	@Test
	fun printsCompleteDocument() {
		val stringValue = GStringValue("value")
		val stringArgument = GArgument(name = "string", value = stringValue)

		val scalarObjectValue = GObjectValue(listOf(stringArgument))
		val scalarArgument = GArgument(name = "scalar", value = scalarObjectValue)

		val objectValue = GObjectValue(listOf(stringArgument, scalarArgument))
		val inputArgument = GArgument(name = "input", value = objectValue)

		val defaultValue = GStringValue("defaultValue")
		val variableDefinition = GVariableDefinition(name = "variable", type = GStringTypeRef, defaultValue = defaultValue)
		val variableRef = GVariableRef(name = "variable")
		val stringVariableArgument = GArgument(name = "stringVariableRef", value = variableRef)

		val enumValue = GEnumValue("ENUM")
		val enumArgument = GArgument(name = "enum", value = enumValue)

		val boolValue = GBooleanValue(value = true)
		val booleanArgument = GArgument(name = "boolean", value = boolValue)

		val intValue = GIntValue(100)
		val intArgument = GArgument(name = "int", value = intValue)

		val floatValue = GFloatValue(123.45)
		val floatArgument = GArgument(name = "float", value = floatValue)

		val listValue = GListValue(listOf(GIntValue(1), GIntValue(2)))
		val listArgument = GArgument(name = "list", value = listValue)

		val nullValue = GNullValue()
		val nullArgument = GArgument(name = "nullArgument", value = nullValue)

		val fieldSelection = GFieldSelection(
			name = "field", arguments = listOf(
				stringArgument,
				stringVariableArgument,
				enumArgument,
				booleanArgument,
				intArgument,
				floatArgument,
				listArgument,
				nullArgument,
				inputArgument
			)
		)
		val directive = GDirective(name = "directive", arguments = listOf(stringArgument, inputArgument))

		val fragmentFieldSelection = GFieldSelection(name = "fragmentString")
		val fragmentSelection = GFragmentSelection("stringFieldFragment")

		val inlineFragmentFieldSelection = GFieldSelection(name = "inlinefragmentString")
		val inlineFragmentSelectionSet = GSelectionSet(listOf(inlineFragmentFieldSelection))
		val typeCondition = GNamedTypeRef("Result")
		val inlineFragmentSelection = GInlineFragmentSelection(selectionSet = inlineFragmentSelectionSet, typeCondition = typeCondition)

		val selectionSet = GSelectionSet(selections = listOf(fieldSelection, fragmentSelection, inlineFragmentSelection))

		val operationDefinition = GOperationDefinition(
			type = GOperationType.query,
			selectionSet = selectionSet,
			directives = listOf(directive),
			variableDefinitions = listOf(variableDefinition)
		)

		val fragmentSelectionSet = GSelectionSet(listOf(fragmentFieldSelection))
		val fragmentDefinition = GFragmentDefinition(name = "stringFieldFragment", typeCondition = typeCondition, fragmentSelectionSet)

		val document = GDocument(definitions = listOf(operationDefinition, fragmentDefinition))

		val expected = """
		|query (${'$'}variable: String = "defaultValue") @directive(string: "value", input: {
		|	string: "value",
		|	scalar: {
		|		string: "value"
		|	}
		|}) {
		|	field(string: "value", stringVariableRef: ${'$'}variable, enum: ENUM, boolean: true, int: 100, float: 123.45, list: [1, 2], nullArgument: null, input: {
		|		string: "value",
		|		scalar: {
		|			string: "value"
		|		}
		|	})
		|	...stringFieldFragment
		|	... on Result {
		|		inlinefragmentString
		|	}
		|}
		|
		|
		|fragment stringFieldFragment on Result {
		|	fragmentString
		|}
		|
		""".trimMargin()

		assertEquals(expected, document.toString())
	}


	@Test
	fun usesShorthandSyntax() {
		val fieldSelection = GFieldSelection(name = "field")
		val selectionSet = GSelectionSet(selections = listOf(fieldSelection))
		val operationDefinition = GOperationDefinition(type = GOperationType.query, selectionSet = selectionSet)

		val document = GDocument(definitions = listOf(operationDefinition))
		val expected = """
		|{
    	|	field
        |}
		|
		""".trimMargin()
		assertEquals(expected, document.toString())

	}


}