package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ValueValidityRuleTest {

	@Test
	fun testAcceptsAnyResolvedValueForCustomerScalar() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|query someQuery(
				|   ${'$'}variable1: Scalar = true
				|   ${'$'}variable2: Scalar = null
				|   ${'$'}variable3: Scalar = 1.0
				|   ${'$'}variable4: Scalar = "string"
				|   ${'$'}variable5: Scalar = enum
				|   ${'$'}variable6: Scalar = {}
				|   ${'$'}variable7: Scalar = [true]
				|   ${'$'}variable8: Scalar = [null]
				|   ${'$'}variable9: Scalar = [1.0]
				|   ${'$'}variable10: Scalar = ["string"]
				|   ${'$'}variable11: Scalar = [enum]
				|   ${'$'}variable12: Scalar = [{}]
				|) {
				|   fun(
				|      argument1: true
				|      argument2: null
				|      argument3: 1.0
				|      argument4: "string"
				|      argument5: enum
				|      argument6: {}
				|      argument7: [true]
				|      argument8: [null]
				|      argument9: [1.0]
				|      argument10: ["string"]
				|      argument11: [enum]
				|      argument12: [{}]
				|      argument13: {
				|         field1: true
				|         field2: null
				|         field3: 1.0
				|         field4: "string"
				|         field5: enum
				|         field6: {}
				|         field7: [true]
				|         field8: [null]
				|         field9: [1.0]
				|         field10: ["string"]
				|         field11: [enum]
				|         field12: [{}]
				|      }
				|   )
				|}
			""",
			schema = """
				|type Query {
				|   fun(
				|      argument1: Scalar = true
				|      argument2: Scalar = null
				|      argument3: Scalar = 1.0
				|      argument4: Scalar = "string"
				|      argument5: Scalar = enum
				|      argument6: Scalar = {}
				|      argument7: Scalar = [true]
				|      argument8: Scalar = [null]
				|      argument9: Scalar = [1.0]
				|      argument10: Scalar = ["string"]
				|      argument11: Scalar = [enum]
				|      argument12: Scalar = [{}]
				|      argument13: Input
				|   ): String
				|}
				|
				|input Input {
				|   field1: Scalar = true
				|   field2: Scalar = null
				|   field3: Scalar = 1.0
				|   field4: Scalar = "string"
				|   field5: Scalar = enum
				|   field6: Scalar = {}
				|   field7: Scalar = [true]
				|   field8: Scalar = [null]
				|   field9: Scalar = [1.0]
				|   field10: Scalar = ["string"]
				|   field11: Scalar = [enum]
				|   field12: Scalar = [{}]
				|}
				|
				|scalar Scalar
			"""
		)
	}

	@Test
	fun testAcceptsValuesOfCorrectTypeInDocument() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|query someQuery(
				|   ${'$'}variable1: Boolean = null
				|   ${'$'}variable2: Boolean = true
				|   ${'$'}variable3: Boolean = false
				|   ${'$'}variable4: Boolean! = true
				|   ${'$'}variable5: Boolean! = false
				|   ${'$'}variable6: Enum = null
				|   ${'$'}variable7: Enum = VALUE
				|   ${'$'}variable8: Enum! = VALUE
				|   ${'$'}variable9: Float = null
				|   ${'$'}variable0: Float = 1
				|   ${'$'}variable11: Float = 1.0
				|   ${'$'}variable12: Float! = 1
				|   ${'$'}variable13: Float! = 1.0
				|   ${'$'}variable14: ID = null
				|   ${'$'}variable15: ID = 1
				|   ${'$'}variable16: ID = "string"
				|   ${'$'}variable17: ID! = 1
				|   ${'$'}variable18: ID! = "string"
				|   ${'$'}variable19: Input = null
				|   ${'$'}variable20: Input = {}
				|   ${'$'}variable21: Input! = {}
				|   ${'$'}variable22: Int = null
				|   ${'$'}variable23: Int = 1
				|   ${'$'}variable24: Int! = 1
				|   ${'$'}variable25: [Int] = null
				|   ${'$'}variable26: [Int] = []
				|   ${'$'}variable27: [Int] = [1]
				|   ${'$'}variable28: [Int] = 1
				|   ${'$'}variable29: [Int]! = []
				|   ${'$'}variable30: [Int]! = [null]
				|   ${'$'}variable31: [Int]! = [1]
				|   ${'$'}variable32: [Int]! = 1
				|   ${'$'}variable33: [Int!] = null
				|   ${'$'}variable34: [Int!] = []
				|   ${'$'}variable35: [Int!] = [1]
				|   ${'$'}variable36: [Int!] = 1
				|   ${'$'}variable37: [Int!]! = []
				|   ${'$'}variable38: [Int!]! = [1]
				|   ${'$'}variable39: [Int!]! = 1
				|   ${'$'}variable40: Scalar = null
				|   ${'$'}variable41: Scalar = true
				|   ${'$'}variable42: Scalar = 1
				|   ${'$'}variable43: Scalar = 1.0
				|   ${'$'}variable44: Scalar! = true
				|   ${'$'}variable45: Scalar! = 1
				|   ${'$'}variable46: Scalar! = 1.0
				|   ${'$'}variable47: String = null
				|   ${'$'}variable48: String = "string"
				|   ${'$'}variable49: String! = "string"
				|) {
				|   fun(
				|      argument1: null
				|      argument2: true
				|      argument3: false
				|      argument4: true
				|      argument5: false
				|      argument6: null
				|      argument7: VALUE
				|      argument8: VALUE
				|      argument9: null
				|      argument0: 1
				|      argument11: 1.0
				|      argument12: 1
				|      argument13: 1.0
				|      argument14: null
				|      argument15: 1
				|      argument16: "string"
				|      argument17: 1
				|      argument18: "string"
				|      argument19: null
				|      argument20: {}
				|      argument21: {
				|         field1: null
				|         field2: true
				|         field3: false
				|         field4: true
				|         field5: false
				|         field6: null
				|         field7: VALUE
				|         field8: VALUE
				|         field9: null
				|         field0: 1
				|         field11: 1.0
				|         field12: 1
				|         field13: 1.0
				|         field14: null
				|         field15: 1
				|         field16: "string"
				|         field17: 1
				|         field18: "string"
				|         field19: null
				|         field20: {}
				|         field21: {}
				|         field22: null
				|         field23: 1
				|         field24: 1
				|         field25: null
				|         field26: []
				|         field27: [1]
				|         field28: 1
				|         field29: []
				|         field30: [null]
				|         field31: [1]
				|         field32: 1
				|         field33: null
				|         field34: []
				|         field35: [1]
				|         field36: 1
				|         field37: []
				|         field38: [1]
				|         field39: 1
				|         field40: null
				|         field40: null
				|         field41: true
				|         field42: 1
				|         field43: 1.0
				|         field44: true
				|         field45: 1
				|         field46: 1.0
				|         field47: null
				|         field48: "string"
				|         field49: "string"
				|      }
				|      argument22: null
				|      argument23: 1
				|      argument24: 1
				|      argument25: null
				|      argument26: []
				|      argument27: [1]
				|      argument28: 1
				|      argument29: []
				|      argument30: [null]
				|      argument31: [1]
				|      argument32: 1
				|      argument33: null
				|      argument34: []
				|      argument35: [1]
				|      argument36: 1
				|      argument37: []
				|      argument38: [1]
				|      argument39: 1
				|      argument40: null
				|      argument41: true
				|      argument42: 1
				|      argument43: 1.0
				|      argument44: true
				|      argument45: 1
				|      argument46: 1.0
				|      argument47: null
				|      argument48: "string"
				|      argument49: "string"
				|   )
				|}
			""",
			schema = """
				|type Query {
				|   fun(
				|      argument1: Boolean = null
				|      argument2: Boolean = true
				|      argument3: Boolean = false
				|      argument4: Boolean! = true
				|      argument5: Boolean! = false
				|      argument6: Enum = null
				|      argument7: Enum = VALUE
				|      argument8: Enum! = VALUE
				|      argument9: Float = null
				|      argument0: Float = 1
				|      argument11: Float = 1.0
				|      argument12: Float! = 1
				|      argument13: Float! = 1.0
				|      argument14: ID = null
				|      argument15: ID = 1
				|      argument16: ID = "string"
				|      argument17: ID! = 1
				|      argument18: ID! = "string"
				|      argument19: Input = null
				|      argument20: Input = {}
				|      argument21: Input! = {}
				|      argument22: Int = null
				|      argument23: Int = 1
				|      argument24: Int! = 1
				|      argument25: [Int] = null
				|      argument26: [Int] = []
				|      argument27: [Int] = [1]
				|      argument28: [Int] = 1
				|      argument29: [Int]! = []
				|      argument30: [Int]! = [null]
				|      argument31: [Int]! = [1]
				|      argument32: [Int]! = 1
				|      argument33: [Int!] = null
				|      argument34: [Int!] = []
				|      argument35: [Int!] = [1]
				|      argument36: [Int!] = 1
				|      argument37: [Int!]! = []
				|      argument38: [Int!]! = [1]
				|      argument39: [Int!]! = 1
				|      argument40: Scalar = null
				|      argument41: Scalar = true
				|      argument42: Scalar = 1
				|      argument43: Scalar = 1.0
				|      argument44: Scalar! = true
				|      argument45: Scalar! = 1
				|      argument46: Scalar! = 1.0
				|      argument47: String = null
				|      argument48: String = "string"
				|      argument49: String! = "string"
				|   ): String
				|}
				|
				|input Input {
				|   field1: Boolean = null
				|   field2: Boolean = true
				|   field3: Boolean = false
				|   field4: Boolean! = true
				|   field5: Boolean! = false
				|   field6: Enum = null
				|   field7: Enum = VALUE
				|   field8: Enum! = VALUE
				|   field9: Float = null
				|   field0: Float = 1
				|   field11: Float = 1.0
				|   field12: Float! = 1
				|   field13: Float! = 1.0
				|   field14: ID = null
				|   field15: ID = 1
				|   field16: ID = "string"
				|   field17: ID! = 1
				|   field18: ID! = "string"
				|   field19: Input = null
				|   field20: Input = {}
				|   field21: Input! = {}
				|   field22: Int = null
				|   field23: Int = 1
				|   field24: Int! = 1
				|   field25: [Int] = null
				|   field26: [Int] = []
				|   field27: [Int] = [1]
				|   field28: [Int] = 1
				|   field29: [Int]! = []
				|   field30: [Int]! = [null]
				|   field31: [Int]! = [1]
				|   field32: [Int]! = 1
				|   field33: [Int!] = null
				|   field34: [Int!] = []
				|   field35: [Int!] = [1]
				|   field36: [Int!] = 1
				|   field37: [Int!]! = []
				|   field38: [Int!]! = [1]
				|   field39: [Int!]! = 1
				|   field40: Scalar = null
				|   field41: Scalar = true
				|   field42: Scalar = 1
				|   field43: Scalar = 1.0
				|   field44: Scalar! = true
				|   field45: Scalar! = 1
				|   field46: Scalar! = 1.0
				|   field47: String = null
				|   field48: String = "string"
				|   field49: String! = "string"
				|}
				|
				|enum Enum { VALUE }
				|scalar Scalar
			"""
		)
	}


	@Test
	fun testAcceptsValuesOfCorrectTypeInSchema() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|type Query {
				|   fun(
				|      argument1: Boolean = null
				|      argument2: Boolean = true
				|      argument3: Boolean = false
				|      argument4: Boolean! = true
				|      argument5: Boolean! = false
				|      argument6: Enum = null
				|      argument7: Enum = VALUE
				|      argument8: Enum! = VALUE
				|      argument9: Float = null
				|      argument0: Float = 1
				|      argument11: Float = 1.0
				|      argument12: Float! = 1
				|      argument13: Float! = 1.0
				|      argument14: ID = null
				|      argument15: ID = 1
				|      argument16: ID = "string"
				|      argument17: ID! = 1
				|      argument18: ID! = "string"
				|      argument19: Input = null
				|      argument20: Input = {}
				|      argument21: Input! = {}
				|      argument22: Int = null
				|      argument23: Int = 1
				|      argument24: Int! = 1
				|      argument25: [Int] = null
				|      argument26: [Int] = []
				|      argument27: [Int] = [1]
				|      argument28: [Int] = 1
				|      argument29: [Int]! = []
				|      argument30: [Int]! = [null]
				|      argument31: [Int]! = [1]
				|      argument32: [Int]! = 1
				|      argument33: [Int!] = null
				|      argument34: [Int!] = []
				|      argument35: [Int!] = [1]
				|      argument36: [Int!] = 1
				|      argument37: [Int!]! = []
				|      argument38: [Int!]! = [1]
				|      argument39: [Int!]! = 1
				|      argument40: String = null
				|      argument41: String = "string"
				|      argument42: String! = "string"
				|   ): String
				|}
				|
				|input Input {
				|   field1: Boolean = null
				|   field2: Boolean = true
				|   field3: Boolean = false
				|   field4: Boolean! = true
				|   field5: Boolean! = false
				|   field6: Enum = null
				|   field7: Enum = VALUE
				|   field8: Enum! = VALUE
				|   field9: Float = null
				|   field0: Float = 1
				|   field11: Float = 1.0
				|   field12: Float! = 1
				|   field13: Float! = 1.0
				|   field14: ID = null
				|   field15: ID = 1
				|   field16: ID = "string"
				|   field17: ID! = 1
				|   field18: ID! = "string"
				|   field19: Input = null
				|   field20: Input = {}
				|   field21: Input! = {}
				|   field22: Int = null
				|   field23: Int = 1
				|   field24: Int! = 1
				|   field25: [Int] = null
				|   field26: [Int] = []
				|   field27: [Int] = [1]
				|   field28: [Int] = 1
				|   field29: [Int]! = []
				|   field30: [Int]! = [null]
				|   field31: [Int]! = [1]
				|   field32: [Int]! = 1
				|   field33: [Int!] = null
				|   field34: [Int!] = []
				|   field35: [Int!] = [1]
				|   field36: [Int!] = 1
				|   field37: [Int!]! = []
				|   field38: [Int!]! = [1]
				|   field39: [Int!]! = 1
				|   field40: String = null
				|   field41: String = "string"
				|   field42: String! = "string"
				|}
				|
				|enum Enum { VALUE }
				|scalar Scalar
			"""
		)
	}


	@Test
	fun testRejectsValuesOfIncorrectTypeInDocument() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf(
				"""
					Type 'Boolean' does not allow value 'VALUE'.

					<document>:2:26
					1 | query someQuery(
					2 |    ${'$'}variable1: Boolean = VALUE
					  |                          ^
					3 |    ${'$'}variable2: Boolean = 1.0
				""",
				"""
					Type 'Boolean' does not allow value '1.0'.

					<document>:3:26
					2 |    ${'$'}variable1: Boolean = VALUE
					3 |    ${'$'}variable2: Boolean = 1.0
					  |                          ^
					4 |    ${'$'}variable3: Boolean = 1
				""",
				"""
					Type 'Boolean' does not allow value '1'.

					<document>:4:26
					3 |    ${'$'}variable2: Boolean = 1.0
					4 |    ${'$'}variable3: Boolean = 1
					  |                          ^
					5 |    ${'$'}variable4: Boolean = []
				""",
				"""
					Type 'Boolean' does not allow a list value.

					<document>:5:26
					4 |    ${'$'}variable3: Boolean = 1
					5 |    ${'$'}variable4: Boolean = []
					  |                          ^
					6 |    ${'$'}variable5: Boolean = {}
				""",
				"""
					Type 'Boolean' does not allow an input object value.

					<document>:6:26
					5 |    ${'$'}variable4: Boolean = []
					6 |    ${'$'}variable5: Boolean = {}
					  |                          ^
					7 |    ${'$'}variable6: Boolean = ""
				""",
				"""
					Type 'Boolean' does not allow value '""'.

					<document>:7:26
					6 |    ${'$'}variable5: Boolean = {}
					7 |    ${'$'}variable6: Boolean = ""
					  |                          ^
					8 |    ${'$'}variable7: Boolean! = null
				""",
				"""
					Type 'Boolean!' does not allow value 'null'.

					<document>:8:27
					7 |    ${'$'}variable6: Boolean = ""
					8 |    ${'$'}variable7: Boolean! = null
					  |                           ^
					9 |    ${'$'}variable8: Boolean! = VALUE
				""",
				"""
					Type 'Boolean!' does not allow value 'VALUE'.

					<document>:9:27
					 8 |    ${'$'}variable7: Boolean! = null
					 9 |    ${'$'}variable8: Boolean! = VALUE
					   |                           ^
					10 |    ${'$'}variable9: Boolean! = 1.0
				""",
				"""
					Type 'Boolean!' does not allow value '1.0'.

					<document>:10:27
					 9 |    ${'$'}variable8: Boolean! = VALUE
					10 |    ${'$'}variable9: Boolean! = 1.0
					   |                           ^
					11 |    ${'$'}variable10: Boolean! = 1
				""",
				"""
					Type 'Boolean!' does not allow value '1'.

					<document>:11:28
					10 |    ${'$'}variable9: Boolean! = 1.0
					11 |    ${'$'}variable10: Boolean! = 1
					   |                            ^
					12 |    ${'$'}variable11: Boolean! = []
				""",
				"""
					Type 'Boolean!' does not allow a list value.

					<document>:12:28
					11 |    ${'$'}variable10: Boolean! = 1
					12 |    ${'$'}variable11: Boolean! = []
					   |                            ^
					13 |    ${'$'}variable12: Boolean! = {}
				""",
				"""
					Type 'Boolean!' does not allow an input object value.

					<document>:13:28
					12 |    ${'$'}variable11: Boolean! = []
					13 |    ${'$'}variable12: Boolean! = {}
					   |                            ^
					14 |    ${'$'}variable13: Boolean! = ""
				""",
				"""
					Type 'Boolean!' does not allow value '""'.

					<document>:14:28
					13 |    ${'$'}variable12: Boolean! = {}
					14 |    ${'$'}variable13: Boolean! = ""
					   |                            ^
					15 |    ${'$'}variable14: Enum = true
				""",
				"""
					Type 'Enum' does not allow value 'true'.

					<document>:15:24
					14 |    ${'$'}variable13: Boolean! = ""
					15 |    ${'$'}variable14: Enum = true
					   |                        ^
					16 |    ${'$'}variable15: Enum = value
				""",
				"""
					Type 'Enum' does not allow value 'value'.

					<document>:16:24
					15 |    ${'$'}variable14: Enum = true
					16 |    ${'$'}variable15: Enum = value
					   |                        ^
					17 |    ${'$'}variable16: Enum = 1.0
				""",
				"""
					Type 'Enum' does not allow value '1.0'.

					<document>:17:24
					16 |    ${'$'}variable15: Enum = value
					17 |    ${'$'}variable16: Enum = 1.0
					   |                        ^
					18 |    ${'$'}variable17: Enum = 1
				""",
				"""
					Type 'Enum' does not allow value '1'.

					<document>:18:24
					17 |    ${'$'}variable16: Enum = 1.0
					18 |    ${'$'}variable17: Enum = 1
					   |                        ^
					19 |    ${'$'}variable18: Enum = []
				""",
				"""
					Type 'Enum' does not allow a list value.

					<document>:19:24
					18 |    ${'$'}variable17: Enum = 1
					19 |    ${'$'}variable18: Enum = []
					   |                        ^
					20 |    ${'$'}variable19: Enum = {}
				""",
				"""
					Type 'Enum' does not allow an input object value.

					<document>:20:24
					19 |    ${'$'}variable18: Enum = []
					20 |    ${'$'}variable19: Enum = {}
					   |                        ^
					21 |    ${'$'}variable20: Enum = ""
				""",
				"""
					Type 'Enum' does not allow value '""'.

					<document>:21:24
					20 |    ${'$'}variable19: Enum = {}
					21 |    ${'$'}variable20: Enum = ""
					   |                        ^
					22 |    ${'$'}variable21: Enum! = null
				""",
				"""
					Type 'Enum!' does not allow value 'null'.

					<document>:22:25
					21 |    ${'$'}variable20: Enum = ""
					22 |    ${'$'}variable21: Enum! = null
					   |                         ^
					23 |    ${'$'}variable22: Enum! = value
				""",
				"""
					Type 'Enum!' does not allow value 'value'.

					<document>:23:25
					22 |    ${'$'}variable21: Enum! = null
					23 |    ${'$'}variable22: Enum! = value
					   |                         ^
					24 |    ${'$'}variable23: Enum! = true
				""",
				"""
					Type 'Enum!' does not allow value 'true'.

					<document>:24:25
					23 |    ${'$'}variable22: Enum! = value
					24 |    ${'$'}variable23: Enum! = true
					   |                         ^
					25 |    ${'$'}variable24: Enum! = 1.0
				""",
				"""
					Type 'Enum!' does not allow value '1.0'.

					<document>:25:25
					24 |    ${'$'}variable23: Enum! = true
					25 |    ${'$'}variable24: Enum! = 1.0
					   |                         ^
					26 |    ${'$'}variable25: Enum! = 1
				""",
				"""
					Type 'Enum!' does not allow value '1'.

					<document>:26:25
					25 |    ${'$'}variable24: Enum! = 1.0
					26 |    ${'$'}variable25: Enum! = 1
					   |                         ^
					27 |    ${'$'}variable26: Enum! = []
				""",
				"""
					Type 'Enum!' does not allow a list value.

					<document>:27:25
					26 |    ${'$'}variable25: Enum! = 1
					27 |    ${'$'}variable26: Enum! = []
					   |                         ^
					28 |    ${'$'}variable27: Enum! = {}
				""",
				"""
					Type 'Enum!' does not allow an input object value.

					<document>:28:25
					27 |    ${'$'}variable26: Enum! = []
					28 |    ${'$'}variable27: Enum! = {}
					   |                         ^
					29 |    ${'$'}variable28: Enum! = ""
				""",
				"""
					Type 'Enum!' does not allow value '""'.

					<document>:29:25
					28 |    ${'$'}variable27: Enum! = {}
					29 |    ${'$'}variable28: Enum! = ""
					   |                         ^
					30 |    ${'$'}variable29: Float = true
				""",
				"""
					Type 'Float' does not allow value 'true'.

					<document>:30:25
					29 |    ${'$'}variable28: Enum! = ""
					30 |    ${'$'}variable29: Float = true
					   |                         ^
					31 |    ${'$'}variable30: Float = VALUE
				""",
				"""
					Type 'Float' does not allow value 'VALUE'.

					<document>:31:25
					30 |    ${'$'}variable29: Float = true
					31 |    ${'$'}variable30: Float = VALUE
					   |                         ^
					32 |    ${'$'}variable31: Float = []
				""",
				"""
					Type 'Float' does not allow a list value.

					<document>:32:25
					31 |    ${'$'}variable30: Float = VALUE
					32 |    ${'$'}variable31: Float = []
					   |                         ^
					33 |    ${'$'}variable32: Float = {}
				""",
				"""
					Type 'Float' does not allow an input object value.

					<document>:33:25
					32 |    ${'$'}variable31: Float = []
					33 |    ${'$'}variable32: Float = {}
					   |                         ^
					34 |    ${'$'}variable33: Float = ""
				""",
				"""
					Type 'Float' does not allow value '""'.

					<document>:34:25
					33 |    ${'$'}variable32: Float = {}
					34 |    ${'$'}variable33: Float = ""
					   |                         ^
					35 |    ${'$'}variable34: Float! = null
				""",
				"""
					Type 'Float!' does not allow value 'null'.

					<document>:35:26
					34 |    ${'$'}variable33: Float = ""
					35 |    ${'$'}variable34: Float! = null
					   |                          ^
					36 |    ${'$'}variable35: Float! = true
				""",
				"""
					Type 'Float!' does not allow value 'true'.

					<document>:36:26
					35 |    ${'$'}variable34: Float! = null
					36 |    ${'$'}variable35: Float! = true
					   |                          ^
					37 |    ${'$'}variable36: Float! = VALUE
				""",
				"""
					Type 'Float!' does not allow value 'VALUE'.

					<document>:37:26
					36 |    ${'$'}variable35: Float! = true
					37 |    ${'$'}variable36: Float! = VALUE
					   |                          ^
					38 |    ${'$'}variable37: Float! = []
				""",
				"""
					Type 'Float!' does not allow a list value.

					<document>:38:26
					37 |    ${'$'}variable36: Float! = VALUE
					38 |    ${'$'}variable37: Float! = []
					   |                          ^
					39 |    ${'$'}variable38: Float! = {}
				""",
				"""
					Type 'Float!' does not allow an input object value.

					<document>:39:26
					38 |    ${'$'}variable37: Float! = []
					39 |    ${'$'}variable38: Float! = {}
					   |                          ^
					40 |    ${'$'}variable39: Float! = ""
				""",
				"""
					Type 'Float!' does not allow value '""'.

					<document>:40:26
					39 |    ${'$'}variable38: Float! = {}
					40 |    ${'$'}variable39: Float! = ""
					   |                          ^
					41 |    ${'$'}variable40: ID = true
				""",
				"""
					Type 'ID' does not allow value 'true'.

					<document>:41:22
					40 |    ${'$'}variable39: Float! = ""
					41 |    ${'$'}variable40: ID = true
					   |                      ^
					42 |    ${'$'}variable41: ID = 1.0
				""",
				"""
					Type 'ID' does not allow value '1.0'.

					<document>:42:22
					41 |    ${'$'}variable40: ID = true
					42 |    ${'$'}variable41: ID = 1.0
					   |                      ^
					43 |    ${'$'}variable44: ID = VALUE
				""",
				"""
					Type 'ID' does not allow value 'VALUE'.

					<document>:43:22
					42 |    ${'$'}variable41: ID = 1.0
					43 |    ${'$'}variable44: ID = VALUE
					   |                      ^
					44 |    ${'$'}variable42: ID = []
				""",
				"""
					Type 'ID' does not allow a list value.

					<document>:44:22
					43 |    ${'$'}variable44: ID = VALUE
					44 |    ${'$'}variable42: ID = []
					   |                      ^
					45 |    ${'$'}variable43: ID = {}
				""",
				"""
					Type 'ID' does not allow an input object value.

					<document>:45:22
					44 |    ${'$'}variable42: ID = []
					45 |    ${'$'}variable43: ID = {}
					   |                      ^
					46 |    ${'$'}variable45: ID! = null
				""",
				"""
					Type 'ID!' does not allow value 'null'.

					<document>:46:23
					45 |    ${'$'}variable43: ID = {}
					46 |    ${'$'}variable45: ID! = null
					   |                       ^
					47 |    ${'$'}variable46: ID! = true
				""",
				"""
					Type 'ID!' does not allow value 'true'.

					<document>:47:23
					46 |    ${'$'}variable45: ID! = null
					47 |    ${'$'}variable46: ID! = true
					   |                       ^
					48 |    ${'$'}variable47: ID! = 1.0
				""",
				"""
					Type 'ID!' does not allow value '1.0'.

					<document>:48:23
					47 |    ${'$'}variable46: ID! = true
					48 |    ${'$'}variable47: ID! = 1.0
					   |                       ^
					49 |    ${'$'}variable48: ID! = VALUE
				""",
				"""
					Type 'ID!' does not allow value 'VALUE'.

					<document>:49:23
					48 |    ${'$'}variable47: ID! = 1.0
					49 |    ${'$'}variable48: ID! = VALUE
					   |                       ^
					50 |    ${'$'}variable49: ID! = []
				""",
				"""
					Type 'ID!' does not allow a list value.

					<document>:50:23
					49 |    ${'$'}variable48: ID! = VALUE
					50 |    ${'$'}variable49: ID! = []
					   |                       ^
					51 |    ${'$'}variable50: ID! = {}
				""",
				"""
					Type 'ID!' does not allow an input object value.

					<document>:51:23
					50 |    ${'$'}variable49: ID! = []
					51 |    ${'$'}variable50: ID! = {}
					   |                       ^
					52 |    ${'$'}variable51: Input = true
				""",
				"""
					Type 'Input' does not allow value 'true'.

					<document>:52:25
					51 |    ${'$'}variable50: ID! = {}
					52 |    ${'$'}variable51: Input = true
					   |                         ^
					53 |    ${'$'}variable52: Input = VALUE
				""",
				"""
					Type 'Input' does not allow value 'VALUE'.

					<document>:53:25
					52 |    ${'$'}variable51: Input = true
					53 |    ${'$'}variable52: Input = VALUE
					   |                         ^
					54 |    ${'$'}variable53: Input = 1.0
				""",
				"""
					Type 'Input' does not allow value '1.0'.

					<document>:54:25
					53 |    ${'$'}variable52: Input = VALUE
					54 |    ${'$'}variable53: Input = 1.0
					   |                         ^
					55 |    ${'$'}variable54: Input = 1
				""",
				"""
					Type 'Input' does not allow value '1'.

					<document>:55:25
					54 |    ${'$'}variable53: Input = 1.0
					55 |    ${'$'}variable54: Input = 1
					   |                         ^
					56 |    ${'$'}variable55: Input = []
				""",
				"""
					Type 'Input' does not allow a list value.

					<document>:56:25
					55 |    ${'$'}variable54: Input = 1
					56 |    ${'$'}variable55: Input = []
					   |                         ^
					57 |    ${'$'}variable56: Input = ""
				""",
				"""
					Type 'Input' does not allow value '""'.

					<document>:57:25
					56 |    ${'$'}variable55: Input = []
					57 |    ${'$'}variable56: Input = ""
					   |                         ^
					58 |    ${'$'}variable57: Input! = null
				""",
				"""
					Type 'Input!' does not allow value 'null'.

					<document>:58:26
					57 |    ${'$'}variable56: Input = ""
					58 |    ${'$'}variable57: Input! = null
					   |                          ^
					59 |    ${'$'}variable58: Input! = true
				""",
				"""
					Type 'Input!' does not allow value 'true'.

					<document>:59:26
					58 |    ${'$'}variable57: Input! = null
					59 |    ${'$'}variable58: Input! = true
					   |                          ^
					60 |    ${'$'}variable59: Input! = VALUE
				""",
				"""
					Type 'Input!' does not allow value 'VALUE'.

					<document>:60:26
					59 |    ${'$'}variable58: Input! = true
					60 |    ${'$'}variable59: Input! = VALUE
					   |                          ^
					61 |    ${'$'}variable60: Input! = 1.0
				""",
				"""
					Type 'Input!' does not allow value '1.0'.

					<document>:61:26
					60 |    ${'$'}variable59: Input! = VALUE
					61 |    ${'$'}variable60: Input! = 1.0
					   |                          ^
					62 |    ${'$'}variable61: Input! = 1
				""",
				"""
					Type 'Input!' does not allow value '1'.

					<document>:62:26
					61 |    ${'$'}variable60: Input! = 1.0
					62 |    ${'$'}variable61: Input! = 1
					   |                          ^
					63 |    ${'$'}variable62: Input! = []
				""",
				"""
					Type 'Input!' does not allow a list value.

					<document>:63:26
					62 |    ${'$'}variable61: Input! = 1
					63 |    ${'$'}variable62: Input! = []
					   |                          ^
					64 |    ${'$'}variable63: Input! = ""
				""",
				"""
					Type 'Input!' does not allow value '""'.

					<document>:64:26
					63 |    ${'$'}variable62: Input! = []
					64 |    ${'$'}variable63: Input! = ""
					   |                          ^
					65 |    ${'$'}variable64: Int = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:65:23
					64 |    ${'$'}variable63: Input! = ""
					65 |    ${'$'}variable64: Int = true
					   |                       ^
					66 |    ${'$'}variable65: Int = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:66:23
					65 |    ${'$'}variable64: Int = true
					66 |    ${'$'}variable65: Int = VALUE
					   |                       ^
					67 |    ${'$'}variable66: Int = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:67:23
					66 |    ${'$'}variable65: Int = VALUE
					67 |    ${'$'}variable66: Int = 1.0
					   |                       ^
					68 |    ${'$'}variable67: Int = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:68:23
					67 |    ${'$'}variable66: Int = 1.0
					68 |    ${'$'}variable67: Int = {}
					   |                       ^
					69 |    ${'$'}variable68: Int = []
				""",
				"""
					Type 'Int' does not allow a list value.

					<document>:69:23
					68 |    ${'$'}variable67: Int = {}
					69 |    ${'$'}variable68: Int = []
					   |                       ^
					70 |    ${'$'}variable69: Int = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:70:23
					69 |    ${'$'}variable68: Int = []
					70 |    ${'$'}variable69: Int = ""
					   |                       ^
					71 |    ${'$'}variable70: Int! = null
				""",
				"""
					Type 'Int!' does not allow value 'null'.

					<document>:71:24
					70 |    ${'$'}variable69: Int = ""
					71 |    ${'$'}variable70: Int! = null
					   |                        ^
					72 |    ${'$'}variable71: Int! = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:72:24
					71 |    ${'$'}variable70: Int! = null
					72 |    ${'$'}variable71: Int! = true
					   |                        ^
					73 |    ${'$'}variable72: Int! = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:73:24
					72 |    ${'$'}variable71: Int! = true
					73 |    ${'$'}variable72: Int! = VALUE
					   |                        ^
					74 |    ${'$'}variable73: Int! = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:74:24
					73 |    ${'$'}variable72: Int! = VALUE
					74 |    ${'$'}variable73: Int! = 1.0
					   |                        ^
					75 |    ${'$'}variable74: Int! = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:75:24
					74 |    ${'$'}variable73: Int! = 1.0
					75 |    ${'$'}variable74: Int! = {}
					   |                        ^
					76 |    ${'$'}variable75: Int! = []
				""",
				"""
					Type 'Int!' does not allow a list value.

					<document>:76:24
					75 |    ${'$'}variable74: Int! = {}
					76 |    ${'$'}variable75: Int! = []
					   |                        ^
					77 |    ${'$'}variable76: Int! = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:77:24
					76 |    ${'$'}variable75: Int! = []
					77 |    ${'$'}variable76: Int! = ""
					   |                        ^
					78 |    ${'$'}variable77: [Int] = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:78:25
					77 |    ${'$'}variable76: Int! = ""
					78 |    ${'$'}variable77: [Int] = true
					   |                         ^
					79 |    ${'$'}variable78: [Int] = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:79:25
					78 |    ${'$'}variable77: [Int] = true
					79 |    ${'$'}variable78: [Int] = VALUE
					   |                         ^
					80 |    ${'$'}variable79: [Int] = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:80:25
					79 |    ${'$'}variable78: [Int] = VALUE
					80 |    ${'$'}variable79: [Int] = 1.0
					   |                         ^
					81 |    ${'$'}variable80: [Int] = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:81:25
					80 |    ${'$'}variable79: [Int] = 1.0
					81 |    ${'$'}variable80: [Int] = {}
					   |                         ^
					82 |    ${'$'}variable81: [Int] = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:82:25
					81 |    ${'$'}variable80: [Int] = {}
					82 |    ${'$'}variable81: [Int] = ""
					   |                         ^
					83 |    ${'$'}variable82: [Int]! = null
				""",
				"""
					Type '[Int]!' does not allow value 'null'.

					<document>:83:26
					82 |    ${'$'}variable81: [Int] = ""
					83 |    ${'$'}variable82: [Int]! = null
					   |                          ^
					84 |    ${'$'}variable83: [Int]! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:84:26
					83 |    ${'$'}variable82: [Int]! = null
					84 |    ${'$'}variable83: [Int]! = true
					   |                          ^
					85 |    ${'$'}variable84: [Int]! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:85:26
					84 |    ${'$'}variable83: [Int]! = true
					85 |    ${'$'}variable84: [Int]! = VALUE
					   |                          ^
					86 |    ${'$'}variable85: [Int]! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:86:26
					85 |    ${'$'}variable84: [Int]! = VALUE
					86 |    ${'$'}variable85: [Int]! = 1.0
					   |                          ^
					87 |    ${'$'}variable86: [Int]! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:87:26
					86 |    ${'$'}variable85: [Int]! = 1.0
					87 |    ${'$'}variable86: [Int]! = {}
					   |                          ^
					88 |    ${'$'}variable87: [Int]! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:88:26
					87 |    ${'$'}variable86: [Int]! = {}
					88 |    ${'$'}variable87: [Int]! = ""
					   |                          ^
					89 |    ${'$'}variable88: [Int!] = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:89:26
					88 |    ${'$'}variable87: [Int]! = ""
					89 |    ${'$'}variable88: [Int!] = true
					   |                          ^
					90 |    ${'$'}variable89: [Int!] = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:90:26
					89 |    ${'$'}variable88: [Int!] = true
					90 |    ${'$'}variable89: [Int!] = VALUE
					   |                          ^
					91 |    ${'$'}variable90: [Int!] = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:91:26
					90 |    ${'$'}variable89: [Int!] = VALUE
					91 |    ${'$'}variable90: [Int!] = 1.0
					   |                          ^
					92 |    ${'$'}variable91: [Int!] = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:92:26
					91 |    ${'$'}variable90: [Int!] = 1.0
					92 |    ${'$'}variable91: [Int!] = {}
					   |                          ^
					93 |    ${'$'}variable92: [Int!] = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:93:26
					92 |    ${'$'}variable91: [Int!] = {}
					93 |    ${'$'}variable92: [Int!] = ""
					   |                          ^
					94 |    ${'$'}variable93: [Int!]! = null
				""",
				"""
					Type '[Int!]!' does not allow value 'null'.

					<document>:94:27
					93 |    ${'$'}variable92: [Int!] = ""
					94 |    ${'$'}variable93: [Int!]! = null
					   |                           ^
					95 |    ${'$'}variable94: [Int!]! = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:95:27
					94 |    ${'$'}variable93: [Int!]! = null
					95 |    ${'$'}variable94: [Int!]! = true
					   |                           ^
					96 |    ${'$'}variable95: [Int!]! = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:96:27
					95 |    ${'$'}variable94: [Int!]! = true
					96 |    ${'$'}variable95: [Int!]! = VALUE
					   |                           ^
					97 |    ${'$'}variable96: [Int!]! = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:97:27
					96 |    ${'$'}variable95: [Int!]! = VALUE
					97 |    ${'$'}variable96: [Int!]! = 1.0
					   |                           ^
					98 |    ${'$'}variable97: [Int!]! = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:98:27
					97 |    ${'$'}variable96: [Int!]! = 1.0
					98 |    ${'$'}variable97: [Int!]! = {}
					   |                           ^
					99 |    ${'$'}variable98: [Int!]! = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:99:27
					 98 |    ${'$'}variable97: [Int!]! = {}
					 99 |    ${'$'}variable98: [Int!]! = ""
					    |                           ^
					100 |    ${'$'}variable99: Scalar = VALUE
				""",
				"""
					Type 'Scalar!' does not allow value 'null'.

					<document>:103:28
					102 |    ${'$'}variable101: Scalar = []
					103 |    ${'$'}variable102: Scalar! = null
					    |                            ^
					104 |    ${'$'}variable103: Scalar! = VALUE
				""",
				"""
					Type 'String' does not allow value 'true'.

					<document>:107:27
					106 |    ${'$'}variable105: Scalar! = []
					107 |    ${'$'}variable106: String = true
					    |                           ^
					108 |    ${'$'}variable107: String = VALUE
				""",
				"""
					Type 'String' does not allow value 'VALUE'.

					<document>:108:27
					107 |    ${'$'}variable106: String = true
					108 |    ${'$'}variable107: String = VALUE
					    |                           ^
					109 |    ${'$'}variable108: String = 1
				""",
				"""
					Type 'String' does not allow value '1'.

					<document>:109:27
					108 |    ${'$'}variable107: String = VALUE
					109 |    ${'$'}variable108: String = 1
					    |                           ^
					110 |    ${'$'}variable109: String = 1.0
				""",
				"""
					Type 'String' does not allow value '1.0'.

					<document>:110:27
					109 |    ${'$'}variable108: String = 1
					110 |    ${'$'}variable109: String = 1.0
					    |                           ^
					111 |    ${'$'}variable110: String = {}
				""",
				"""
					Type 'String' does not allow an input object value.

					<document>:111:27
					110 |    ${'$'}variable109: String = 1.0
					111 |    ${'$'}variable110: String = {}
					    |                           ^
					112 |    ${'$'}variable111: String = []
				""",
				"""
					Type 'String' does not allow a list value.

					<document>:112:27
					111 |    ${'$'}variable110: String = {}
					112 |    ${'$'}variable111: String = []
					    |                           ^
					113 |    ${'$'}variable112: String! = null
				""",
				"""
					Type 'String!' does not allow value 'null'.

					<document>:113:28
					112 |    ${'$'}variable111: String = []
					113 |    ${'$'}variable112: String! = null
					    |                            ^
					114 |    ${'$'}variable113: String! = true
				""",
				"""
					Type 'String!' does not allow value 'true'.

					<document>:114:28
					113 |    ${'$'}variable112: String! = null
					114 |    ${'$'}variable113: String! = true
					    |                            ^
					115 |    ${'$'}variable114: String! = VALUE
				""",
				"""
					Type 'String!' does not allow value 'VALUE'.

					<document>:115:28
					114 |    ${'$'}variable113: String! = true
					115 |    ${'$'}variable114: String! = VALUE
					    |                            ^
					116 |    ${'$'}variable115: String! = 1
				""",
				"""
					Type 'String!' does not allow value '1'.

					<document>:116:28
					115 |    ${'$'}variable114: String! = VALUE
					116 |    ${'$'}variable115: String! = 1
					    |                            ^
					117 |    ${'$'}variable116: String! = 1.0
				""",
				"""
					Type 'String!' does not allow value '1.0'.

					<document>:117:28
					116 |    ${'$'}variable115: String! = 1
					117 |    ${'$'}variable116: String! = 1.0
					    |                            ^
					118 |    ${'$'}variable117: String! = {}
				""",
				"""
					Type 'String!' does not allow an input object value.

					<document>:118:28
					117 |    ${'$'}variable116: String! = 1.0
					118 |    ${'$'}variable117: String! = {}
					    |                            ^
					119 |    ${'$'}variable118: String! = []
				""",
				"""
					Type 'String!' does not allow a list value.

					<document>:119:28
					118 |    ${'$'}variable117: String! = {}
					119 |    ${'$'}variable118: String! = []
					    |                            ^
					120 | ) {
				""",
				"""
					Type 'Boolean' does not allow value 'VALUE'.

					<document>:122:18
					121 |    fun(
					122 |       argument1: VALUE
					    |                  ^
					123 |       argument2: 1.0

					<document>:3:18
					2 |    fun(
					3 |       argument1: Boolean = VALUE
					  |                  ^
					4 |       argument2: Boolean = 1.0
				""",
				"""
					Type 'Boolean' does not allow value '1.0'.

					<document>:123:18
					122 |       argument1: VALUE
					123 |       argument2: 1.0
					    |                  ^
					124 |       argument3: 1

					<document>:4:18
					3 |       argument1: Boolean = VALUE
					4 |       argument2: Boolean = 1.0
					  |                  ^
					5 |       argument3: Boolean = 1
				""",
				"""
					Type 'Boolean' does not allow value '1'.

					<document>:124:18
					123 |       argument2: 1.0
					124 |       argument3: 1
					    |                  ^
					125 |       argument4: []

					<document>:5:18
					4 |       argument2: Boolean = 1.0
					5 |       argument3: Boolean = 1
					  |                  ^
					6 |       argument4: Boolean = []
				""",
				"""
					Type 'Boolean' does not allow a list value.

					<document>:125:18
					124 |       argument3: 1
					125 |       argument4: []
					    |                  ^
					126 |       argument5: {}

					<document>:6:18
					5 |       argument3: Boolean = 1
					6 |       argument4: Boolean = []
					  |                  ^
					7 |       argument5: Boolean = {}
				""",
				"""
					Type 'Boolean' does not allow an input object value.

					<document>:126:18
					125 |       argument4: []
					126 |       argument5: {}
					    |                  ^
					127 |       argument6: ""

					<document>:7:18
					6 |       argument4: Boolean = []
					7 |       argument5: Boolean = {}
					  |                  ^
					8 |       argument6: Boolean = ""
				""",
				"""
					Type 'Boolean' does not allow value '""'.

					<document>:127:18
					126 |       argument5: {}
					127 |       argument6: ""
					    |                  ^
					128 |       argument7: null

					<document>:8:18
					7 |       argument5: Boolean = {}
					8 |       argument6: Boolean = ""
					  |                  ^
					9 |       argument7: Boolean! = null
				""",
				"""
					Type 'Boolean' does not allow value 'null'.

					<document>:128:18
					127 |       argument6: ""
					128 |       argument7: null
					    |                  ^
					129 |       argument8: VALUE

					<document>:9:18
					 8 |       argument6: Boolean = ""
					 9 |       argument7: Boolean! = null
					   |                  ^
					10 |       argument8: Boolean! = VALUE
				""",
				"""
					Type 'Boolean' does not allow value 'VALUE'.

					<document>:129:18
					128 |       argument7: null
					129 |       argument8: VALUE
					    |                  ^
					130 |       argument9: 1.0

					<document>:10:18
					 9 |       argument7: Boolean! = null
					10 |       argument8: Boolean! = VALUE
					   |                  ^
					11 |       argument9: Boolean! = 1.0
				""",
				"""
					Type 'Boolean' does not allow value '1.0'.

					<document>:130:18
					129 |       argument8: VALUE
					130 |       argument9: 1.0
					    |                  ^
					131 |       argument10: 1

					<document>:11:18
					10 |       argument8: Boolean! = VALUE
					11 |       argument9: Boolean! = 1.0
					   |                  ^
					12 |       argument10: Boolean! = 1
				""",
				"""
					Type 'Boolean' does not allow value '1'.

					<document>:131:19
					130 |       argument9: 1.0
					131 |       argument10: 1
					    |                   ^
					132 |       argument11: []

					<document>:12:19
					11 |       argument9: Boolean! = 1.0
					12 |       argument10: Boolean! = 1
					   |                   ^
					13 |       argument11: Boolean! = []
				""",
				"""
					Type 'Boolean' does not allow a list value.

					<document>:132:19
					131 |       argument10: 1
					132 |       argument11: []
					    |                   ^
					133 |       argument12: {}

					<document>:13:19
					12 |       argument10: Boolean! = 1
					13 |       argument11: Boolean! = []
					   |                   ^
					14 |       argument12: Boolean! = {}
				""",
				"""
					Type 'Boolean' does not allow an input object value.

					<document>:133:19
					132 |       argument11: []
					133 |       argument12: {}
					    |                   ^
					134 |       argument13: ""

					<document>:14:19
					13 |       argument11: Boolean! = []
					14 |       argument12: Boolean! = {}
					   |                   ^
					15 |       argument13: Boolean! = ""
				""",
				"""
					Type 'Boolean' does not allow value '""'.

					<document>:134:19
					133 |       argument12: {}
					134 |       argument13: ""
					    |                   ^
					135 |       argument14: true

					<document>:15:19
					14 |       argument12: Boolean! = {}
					15 |       argument13: Boolean! = ""
					   |                   ^
					16 |       argument14: Enum = true
				""",
				"""
					Type 'Enum' does not allow value 'true'.

					<document>:135:19
					134 |       argument13: ""
					135 |       argument14: true
					    |                   ^
					136 |       argument15: value

					<document>:16:19
					15 |       argument13: Boolean! = ""
					16 |       argument14: Enum = true
					   |                   ^
					17 |       argument15: Enum = value
				""",
				"""
					Type 'Enum' does not allow value 'value'.

					<document>:136:19
					135 |       argument14: true
					136 |       argument15: value
					    |                   ^
					137 |       argument16: 1.0

					<document>:17:19
					16 |       argument14: Enum = true
					17 |       argument15: Enum = value
					   |                   ^
					18 |       argument16: Enum = 1.0
				""",
				"""
					Type 'Enum' does not allow value '1.0'.

					<document>:137:19
					136 |       argument15: value
					137 |       argument16: 1.0
					    |                   ^
					138 |       argument17: 1

					<document>:18:19
					17 |       argument15: Enum = value
					18 |       argument16: Enum = 1.0
					   |                   ^
					19 |       argument17: Enum = 1
				""",
				"""
					Type 'Enum' does not allow value '1'.

					<document>:138:19
					137 |       argument16: 1.0
					138 |       argument17: 1
					    |                   ^
					139 |       argument18: []

					<document>:19:19
					18 |       argument16: Enum = 1.0
					19 |       argument17: Enum = 1
					   |                   ^
					20 |       argument18: Enum = []
				""",
				"""
					Type 'Enum' does not allow a list value.

					<document>:139:19
					138 |       argument17: 1
					139 |       argument18: []
					    |                   ^
					140 |       argument19: {}

					<document>:20:19
					19 |       argument17: Enum = 1
					20 |       argument18: Enum = []
					   |                   ^
					21 |       argument19: Enum = {}
				""",
				"""
					Type 'Enum' does not allow an input object value.

					<document>:140:19
					139 |       argument18: []
					140 |       argument19: {}
					    |                   ^
					141 |       argument20: ""

					<document>:21:19
					20 |       argument18: Enum = []
					21 |       argument19: Enum = {}
					   |                   ^
					22 |       argument20: Enum = ""
				""",
				"""
					Type 'Enum' does not allow value '""'.

					<document>:141:19
					140 |       argument19: {}
					141 |       argument20: ""
					    |                   ^
					142 |       argument21: null

					<document>:22:19
					21 |       argument19: Enum = {}
					22 |       argument20: Enum = ""
					   |                   ^
					23 |       argument21: Enum! = null
				""",
				"""
					Type 'Enum' does not allow value 'null'.

					<document>:142:19
					141 |       argument20: ""
					142 |       argument21: null
					    |                   ^
					143 |       argument22: value

					<document>:23:19
					22 |       argument20: Enum = ""
					23 |       argument21: Enum! = null
					   |                   ^
					24 |       argument22: Enum! = value
				""",
				"""
					Type 'Enum' does not allow value 'value'.

					<document>:143:19
					142 |       argument21: null
					143 |       argument22: value
					    |                   ^
					144 |       argument23: true

					<document>:24:19
					23 |       argument21: Enum! = null
					24 |       argument22: Enum! = value
					   |                   ^
					25 |       argument23: Enum! = true
				""",
				"""
					Type 'Enum' does not allow value 'true'.

					<document>:144:19
					143 |       argument22: value
					144 |       argument23: true
					    |                   ^
					145 |       argument24: 1.0

					<document>:25:19
					24 |       argument22: Enum! = value
					25 |       argument23: Enum! = true
					   |                   ^
					26 |       argument24: Enum! = 1.0
				""",
				"""
					Type 'Enum' does not allow value '1.0'.

					<document>:145:19
					144 |       argument23: true
					145 |       argument24: 1.0
					    |                   ^
					146 |       argument25: 1

					<document>:26:19
					25 |       argument23: Enum! = true
					26 |       argument24: Enum! = 1.0
					   |                   ^
					27 |       argument25: Enum! = 1
				""",
				"""
					Type 'Enum' does not allow value '1'.

					<document>:146:19
					145 |       argument24: 1.0
					146 |       argument25: 1
					    |                   ^
					147 |       argument26: []

					<document>:27:19
					26 |       argument24: Enum! = 1.0
					27 |       argument25: Enum! = 1
					   |                   ^
					28 |       argument26: Enum! = []
				""",
				"""
					Type 'Enum' does not allow a list value.

					<document>:147:19
					146 |       argument25: 1
					147 |       argument26: []
					    |                   ^
					148 |       argument27: {}

					<document>:28:19
					27 |       argument25: Enum! = 1
					28 |       argument26: Enum! = []
					   |                   ^
					29 |       argument27: Enum! = {}
				""",
				"""
					Type 'Enum' does not allow an input object value.

					<document>:148:19
					147 |       argument26: []
					148 |       argument27: {}
					    |                   ^
					149 |       argument28: ""

					<document>:29:19
					28 |       argument26: Enum! = []
					29 |       argument27: Enum! = {}
					   |                   ^
					30 |       argument28: Enum! = ""
				""",
				"""
					Type 'Enum' does not allow value '""'.

					<document>:149:19
					148 |       argument27: {}
					149 |       argument28: ""
					    |                   ^
					150 |       argument29: true

					<document>:30:19
					29 |       argument27: Enum! = {}
					30 |       argument28: Enum! = ""
					   |                   ^
					31 |       argument29: Float = true
				""",
				"""
					Type 'Float' does not allow value 'true'.

					<document>:150:19
					149 |       argument28: ""
					150 |       argument29: true
					    |                   ^
					151 |       argument30: VALUE

					<document>:31:19
					30 |       argument28: Enum! = ""
					31 |       argument29: Float = true
					   |                   ^
					32 |       argument30: Float = VALUE
				""",
				"""
					Type 'Float' does not allow value 'VALUE'.

					<document>:151:19
					150 |       argument29: true
					151 |       argument30: VALUE
					    |                   ^
					152 |       argument31: []

					<document>:32:19
					31 |       argument29: Float = true
					32 |       argument30: Float = VALUE
					   |                   ^
					33 |       argument31: Float = []
				""",
				"""
					Type 'Float' does not allow a list value.

					<document>:152:19
					151 |       argument30: VALUE
					152 |       argument31: []
					    |                   ^
					153 |       argument32: {}

					<document>:33:19
					32 |       argument30: Float = VALUE
					33 |       argument31: Float = []
					   |                   ^
					34 |       argument32: Float = {}
				""",
				"""
					Type 'Float' does not allow an input object value.

					<document>:153:19
					152 |       argument31: []
					153 |       argument32: {}
					    |                   ^
					154 |       argument33: ""

					<document>:34:19
					33 |       argument31: Float = []
					34 |       argument32: Float = {}
					   |                   ^
					35 |       argument33: Float = ""
				""",
				"""
					Type 'Float' does not allow value '""'.

					<document>:154:19
					153 |       argument32: {}
					154 |       argument33: ""
					    |                   ^
					155 |       argument34: null

					<document>:35:19
					34 |       argument32: Float = {}
					35 |       argument33: Float = ""
					   |                   ^
					36 |       argument34: Float! = null
				""",
				"""
					Type 'Float' does not allow value 'null'.

					<document>:155:19
					154 |       argument33: ""
					155 |       argument34: null
					    |                   ^
					156 |       argument35: true

					<document>:36:19
					35 |       argument33: Float = ""
					36 |       argument34: Float! = null
					   |                   ^
					37 |       argument35: Float! = true
				""",
				"""
					Type 'Float' does not allow value 'true'.

					<document>:156:19
					155 |       argument34: null
					156 |       argument35: true
					    |                   ^
					157 |       argument36: VALUE

					<document>:37:19
					36 |       argument34: Float! = null
					37 |       argument35: Float! = true
					   |                   ^
					38 |       argument36: Float! = VALUE
				""",
				"""
					Type 'Float' does not allow value 'VALUE'.

					<document>:157:19
					156 |       argument35: true
					157 |       argument36: VALUE
					    |                   ^
					158 |       argument37: []

					<document>:38:19
					37 |       argument35: Float! = true
					38 |       argument36: Float! = VALUE
					   |                   ^
					39 |       argument37: Float! = []
				""",
				"""
					Type 'Float' does not allow a list value.

					<document>:158:19
					157 |       argument36: VALUE
					158 |       argument37: []
					    |                   ^
					159 |       argument38: {}

					<document>:39:19
					38 |       argument36: Float! = VALUE
					39 |       argument37: Float! = []
					   |                   ^
					40 |       argument38: Float! = {}
				""",
				"""
					Type 'Float' does not allow an input object value.

					<document>:159:19
					158 |       argument37: []
					159 |       argument38: {}
					    |                   ^
					160 |       argument39: ""

					<document>:40:19
					39 |       argument37: Float! = []
					40 |       argument38: Float! = {}
					   |                   ^
					41 |       argument39: Float! = ""
				""",
				"""
					Type 'Float' does not allow value '""'.

					<document>:160:19
					159 |       argument38: {}
					160 |       argument39: ""
					    |                   ^
					161 |       argument40: true

					<document>:41:19
					40 |       argument38: Float! = {}
					41 |       argument39: Float! = ""
					   |                   ^
					42 |       argument40: ID = true
				""",
				"""
					Type 'ID' does not allow value 'true'.

					<document>:161:19
					160 |       argument39: ""
					161 |       argument40: true
					    |                   ^
					162 |       argument41: 1.0

					<document>:42:19
					41 |       argument39: Float! = ""
					42 |       argument40: ID = true
					   |                   ^
					43 |       argument41: ID = 1.0
				""",
				"""
					Type 'ID' does not allow value '1.0'.

					<document>:162:19
					161 |       argument40: true
					162 |       argument41: 1.0
					    |                   ^
					163 |       argument44: VALUE

					<document>:43:19
					42 |       argument40: ID = true
					43 |       argument41: ID = 1.0
					   |                   ^
					44 |       argument44: ID = VALUE
				""",
				"""
					Type 'ID' does not allow value 'VALUE'.

					<document>:163:19
					162 |       argument41: 1.0
					163 |       argument44: VALUE
					    |                   ^
					164 |       argument42: []

					<document>:44:19
					43 |       argument41: ID = 1.0
					44 |       argument44: ID = VALUE
					   |                   ^
					45 |       argument42: ID = []
				""",
				"""
					Type 'ID' does not allow a list value.

					<document>:164:19
					163 |       argument44: VALUE
					164 |       argument42: []
					    |                   ^
					165 |       argument43: {}

					<document>:45:19
					44 |       argument44: ID = VALUE
					45 |       argument42: ID = []
					   |                   ^
					46 |       argument43: ID = {}
				""",
				"""
					Type 'ID' does not allow an input object value.

					<document>:165:19
					164 |       argument42: []
					165 |       argument43: {}
					    |                   ^
					166 |       argument45: null

					<document>:46:19
					45 |       argument42: ID = []
					46 |       argument43: ID = {}
					   |                   ^
					47 |       argument45: ID! = null
				""",
				"""
					Type 'ID' does not allow value 'null'.

					<document>:166:19
					165 |       argument43: {}
					166 |       argument45: null
					    |                   ^
					167 |       argument46: true

					<document>:47:19
					46 |       argument43: ID = {}
					47 |       argument45: ID! = null
					   |                   ^
					48 |       argument46: ID! = true
				""",
				"""
					Type 'ID' does not allow value 'true'.

					<document>:167:19
					166 |       argument45: null
					167 |       argument46: true
					    |                   ^
					168 |       argument47: 1.0

					<document>:48:19
					47 |       argument45: ID! = null
					48 |       argument46: ID! = true
					   |                   ^
					49 |       argument47: ID! = 1.0
				""",
				"""
					Type 'ID' does not allow value '1.0'.

					<document>:168:19
					167 |       argument46: true
					168 |       argument47: 1.0
					    |                   ^
					169 |       argument48: VALUE

					<document>:49:19
					48 |       argument46: ID! = true
					49 |       argument47: ID! = 1.0
					   |                   ^
					50 |       argument48: ID! = VALUE
				""",
				"""
					Type 'ID' does not allow value 'VALUE'.

					<document>:169:19
					168 |       argument47: 1.0
					169 |       argument48: VALUE
					    |                   ^
					170 |       argument49: []

					<document>:50:19
					49 |       argument47: ID! = 1.0
					50 |       argument48: ID! = VALUE
					   |                   ^
					51 |       argument49: ID! = []
				""",
				"""
					Type 'ID' does not allow a list value.

					<document>:170:19
					169 |       argument48: VALUE
					170 |       argument49: []
					    |                   ^
					171 |       argument50: {}

					<document>:51:19
					50 |       argument48: ID! = VALUE
					51 |       argument49: ID! = []
					   |                   ^
					52 |       argument50: ID! = {}
				""",
				"""
					Type 'ID' does not allow an input object value.

					<document>:171:19
					170 |       argument49: []
					171 |       argument50: {}
					    |                   ^
					172 |       argument51: true

					<document>:52:19
					51 |       argument49: ID! = []
					52 |       argument50: ID! = {}
					   |                   ^
					53 |       argument51: Input = true
				""",
				"""
					Type 'Input' does not allow value 'true'.

					<document>:172:19
					171 |       argument50: {}
					172 |       argument51: true
					    |                   ^
					173 |       argument52: VALUE

					<document>:53:19
					52 |       argument50: ID! = {}
					53 |       argument51: Input = true
					   |                   ^
					54 |       argument52: Input = VALUE
				""",
				"""
					Type 'Input' does not allow value 'VALUE'.

					<document>:173:19
					172 |       argument51: true
					173 |       argument52: VALUE
					    |                   ^
					174 |       argument53: 1.0

					<document>:54:19
					53 |       argument51: Input = true
					54 |       argument52: Input = VALUE
					   |                   ^
					55 |       argument53: Input = 1.0
				""",
				"""
					Type 'Input' does not allow value '1.0'.

					<document>:174:19
					173 |       argument52: VALUE
					174 |       argument53: 1.0
					    |                   ^
					175 |       argument54: 1

					<document>:55:19
					54 |       argument52: Input = VALUE
					55 |       argument53: Input = 1.0
					   |                   ^
					56 |       argument54: Input = 1
				""",
				"""
					Type 'Input' does not allow value '1'.

					<document>:175:19
					174 |       argument53: 1.0
					175 |       argument54: 1
					    |                   ^
					176 |       argument55: []

					<document>:56:19
					55 |       argument53: Input = 1.0
					56 |       argument54: Input = 1
					   |                   ^
					57 |       argument55: Input = []
				""",
				"""
					Type 'Input' does not allow a list value.

					<document>:176:19
					175 |       argument54: 1
					176 |       argument55: []
					    |                   ^
					177 |       argument56: ""

					<document>:57:19
					56 |       argument54: Input = 1
					57 |       argument55: Input = []
					   |                   ^
					58 |       argument56: Input = ""
				""",
				"""
					Type 'Input' does not allow value '""'.

					<document>:177:19
					176 |       argument55: []
					177 |       argument56: ""
					    |                   ^
					178 |       argument57: null

					<document>:58:19
					57 |       argument55: Input = []
					58 |       argument56: Input = ""
					   |                   ^
					59 |       argument57: Input! = null
				""",
				"""
					Type 'Input' does not allow value 'null'.

					<document>:178:19
					177 |       argument56: ""
					178 |       argument57: null
					    |                   ^
					179 |       argument58: true

					<document>:59:19
					58 |       argument56: Input = ""
					59 |       argument57: Input! = null
					   |                   ^
					60 |       argument58: Input! = true
				""",
				"""
					Type 'Input' does not allow value 'true'.

					<document>:179:19
					178 |       argument57: null
					179 |       argument58: true
					    |                   ^
					180 |       argument59: VALUE

					<document>:60:19
					59 |       argument57: Input! = null
					60 |       argument58: Input! = true
					   |                   ^
					61 |       argument59: Input! = VALUE
				""",
				"""
					Type 'Input' does not allow value 'VALUE'.

					<document>:180:19
					179 |       argument58: true
					180 |       argument59: VALUE
					    |                   ^
					181 |       argument60: 1.0

					<document>:61:19
					60 |       argument58: Input! = true
					61 |       argument59: Input! = VALUE
					   |                   ^
					62 |       argument60: Input! = 1.0
				""",
				"""
					Type 'Input' does not allow value '1.0'.

					<document>:181:19
					180 |       argument59: VALUE
					181 |       argument60: 1.0
					    |                   ^
					182 |       argument61: 1

					<document>:62:19
					61 |       argument59: Input! = VALUE
					62 |       argument60: Input! = 1.0
					   |                   ^
					63 |       argument61: Input! = 1
				""",
				"""
					Type 'Input' does not allow value '1'.

					<document>:182:19
					181 |       argument60: 1.0
					182 |       argument61: 1
					    |                   ^
					183 |       argument62: []

					<document>:63:19
					62 |       argument60: Input! = 1.0
					63 |       argument61: Input! = 1
					   |                   ^
					64 |       argument62: Input! = []
				""",
				"""
					Type 'Input' does not allow a list value.

					<document>:183:19
					182 |       argument61: 1
					183 |       argument62: []
					    |                   ^
					184 |       argument63: ""

					<document>:64:19
					63 |       argument61: Input! = 1
					64 |       argument62: Input! = []
					   |                   ^
					65 |       argument63: Input! = ""
				""",
				"""
					Type 'Input' does not allow value '""'.

					<document>:184:19
					183 |       argument62: []
					184 |       argument63: ""
					    |                   ^
					185 |       argument64: true

					<document>:65:19
					64 |       argument62: Input! = []
					65 |       argument63: Input! = ""
					   |                   ^
					66 |       argument64: Int = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:185:19
					184 |       argument63: ""
					185 |       argument64: true
					    |                   ^
					186 |       argument65: VALUE

					<document>:66:19
					65 |       argument63: Input! = ""
					66 |       argument64: Int = true
					   |                   ^
					67 |       argument65: Int = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:186:19
					185 |       argument64: true
					186 |       argument65: VALUE
					    |                   ^
					187 |       argument66: 1.0

					<document>:67:19
					66 |       argument64: Int = true
					67 |       argument65: Int = VALUE
					   |                   ^
					68 |       argument66: Int = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:187:19
					186 |       argument65: VALUE
					187 |       argument66: 1.0
					    |                   ^
					188 |       argument67: {}

					<document>:68:19
					67 |       argument65: Int = VALUE
					68 |       argument66: Int = 1.0
					   |                   ^
					69 |       argument67: Int = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:188:19
					187 |       argument66: 1.0
					188 |       argument67: {}
					    |                   ^
					189 |       argument68: []

					<document>:69:19
					68 |       argument66: Int = 1.0
					69 |       argument67: Int = {}
					   |                   ^
					70 |       argument68: Int = []
				""",
				"""
					Type 'Int' does not allow a list value.

					<document>:189:19
					188 |       argument67: {}
					189 |       argument68: []
					    |                   ^
					190 |       argument69: ""

					<document>:70:19
					69 |       argument67: Int = {}
					70 |       argument68: Int = []
					   |                   ^
					71 |       argument69: Int = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:190:19
					189 |       argument68: []
					190 |       argument69: ""
					    |                   ^
					191 |       argument70: null

					<document>:71:19
					70 |       argument68: Int = []
					71 |       argument69: Int = ""
					   |                   ^
					72 |       argument70: Int! = null
				""",
				"""
					Type 'Int' does not allow value 'null'.

					<document>:191:19
					190 |       argument69: ""
					191 |       argument70: null
					    |                   ^
					192 |       argument71: true

					<document>:72:19
					71 |       argument69: Int = ""
					72 |       argument70: Int! = null
					   |                   ^
					73 |       argument71: Int! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:192:19
					191 |       argument70: null
					192 |       argument71: true
					    |                   ^
					193 |       argument72: VALUE

					<document>:73:19
					72 |       argument70: Int! = null
					73 |       argument71: Int! = true
					   |                   ^
					74 |       argument72: Int! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:193:19
					192 |       argument71: true
					193 |       argument72: VALUE
					    |                   ^
					194 |       argument73: 1.0

					<document>:74:19
					73 |       argument71: Int! = true
					74 |       argument72: Int! = VALUE
					   |                   ^
					75 |       argument73: Int! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:194:19
					193 |       argument72: VALUE
					194 |       argument73: 1.0
					    |                   ^
					195 |       argument74: {}

					<document>:75:19
					74 |       argument72: Int! = VALUE
					75 |       argument73: Int! = 1.0
					   |                   ^
					76 |       argument74: Int! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:195:19
					194 |       argument73: 1.0
					195 |       argument74: {}
					    |                   ^
					196 |       argument75: []

					<document>:76:19
					75 |       argument73: Int! = 1.0
					76 |       argument74: Int! = {}
					   |                   ^
					77 |       argument75: Int! = []
				""",
				"""
					Type 'Int' does not allow a list value.

					<document>:196:19
					195 |       argument74: {}
					196 |       argument75: []
					    |                   ^
					197 |       argument76: ""

					<document>:77:19
					76 |       argument74: Int! = {}
					77 |       argument75: Int! = []
					   |                   ^
					78 |       argument76: Int! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:197:19
					196 |       argument75: []
					197 |       argument76: ""
					    |                   ^
					198 |       argument77: true

					<document>:78:19
					77 |       argument75: Int! = []
					78 |       argument76: Int! = ""
					   |                   ^
					79 |       argument77: [Int] = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:198:19
					197 |       argument76: ""
					198 |       argument77: true
					    |                   ^
					199 |       argument78: VALUE

					<document>:79:19
					78 |       argument76: Int! = ""
					79 |       argument77: [Int] = true
					   |                   ^
					80 |       argument78: [Int] = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:199:19
					198 |       argument77: true
					199 |       argument78: VALUE
					    |                   ^
					200 |       argument79: 1.0

					<document>:80:19
					79 |       argument77: [Int] = true
					80 |       argument78: [Int] = VALUE
					   |                   ^
					81 |       argument79: [Int] = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:200:19
					199 |       argument78: VALUE
					200 |       argument79: 1.0
					    |                   ^
					201 |       argument80: {}

					<document>:81:19
					80 |       argument78: [Int] = VALUE
					81 |       argument79: [Int] = 1.0
					   |                   ^
					82 |       argument80: [Int] = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:201:19
					200 |       argument79: 1.0
					201 |       argument80: {}
					    |                   ^
					202 |       argument81: ""

					<document>:82:19
					81 |       argument79: [Int] = 1.0
					82 |       argument80: [Int] = {}
					   |                   ^
					83 |       argument81: [Int] = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:202:19
					201 |       argument80: {}
					202 |       argument81: ""
					    |                   ^
					203 |       argument82: null

					<document>:83:19
					82 |       argument80: [Int] = {}
					83 |       argument81: [Int] = ""
					   |                   ^
					84 |       argument82: [Int]! = null
				""",
				"""
					Type 'Int' does not allow value 'null'.

					<document>:203:19
					202 |       argument81: ""
					203 |       argument82: null
					    |                   ^
					204 |       argument83: true

					<document>:84:19
					83 |       argument81: [Int] = ""
					84 |       argument82: [Int]! = null
					   |                   ^
					85 |       argument83: [Int]! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:204:19
					203 |       argument82: null
					204 |       argument83: true
					    |                   ^
					205 |       argument84: VALUE

					<document>:85:19
					84 |       argument82: [Int]! = null
					85 |       argument83: [Int]! = true
					   |                   ^
					86 |       argument84: [Int]! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:205:19
					204 |       argument83: true
					205 |       argument84: VALUE
					    |                   ^
					206 |       argument85: 1.0

					<document>:86:19
					85 |       argument83: [Int]! = true
					86 |       argument84: [Int]! = VALUE
					   |                   ^
					87 |       argument85: [Int]! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:206:19
					205 |       argument84: VALUE
					206 |       argument85: 1.0
					    |                   ^
					207 |       argument86: {}

					<document>:87:19
					86 |       argument84: [Int]! = VALUE
					87 |       argument85: [Int]! = 1.0
					   |                   ^
					88 |       argument86: [Int]! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:207:19
					206 |       argument85: 1.0
					207 |       argument86: {}
					    |                   ^
					208 |       argument87: ""

					<document>:88:19
					87 |       argument85: [Int]! = 1.0
					88 |       argument86: [Int]! = {}
					   |                   ^
					89 |       argument87: [Int]! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:208:19
					207 |       argument86: {}
					208 |       argument87: ""
					    |                   ^
					209 |       argument88: true

					<document>:89:19
					88 |       argument86: [Int]! = {}
					89 |       argument87: [Int]! = ""
					   |                   ^
					90 |       argument88: [Int!] = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:209:19
					208 |       argument87: ""
					209 |       argument88: true
					    |                   ^
					210 |       argument89: VALUE

					<document>:90:19
					89 |       argument87: [Int]! = ""
					90 |       argument88: [Int!] = true
					   |                   ^
					91 |       argument89: [Int!] = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:210:19
					209 |       argument88: true
					210 |       argument89: VALUE
					    |                   ^
					211 |       argument90: 1.0

					<document>:91:19
					90 |       argument88: [Int!] = true
					91 |       argument89: [Int!] = VALUE
					   |                   ^
					92 |       argument90: [Int!] = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:211:19
					210 |       argument89: VALUE
					211 |       argument90: 1.0
					    |                   ^
					212 |       argument91: {}

					<document>:92:19
					91 |       argument89: [Int!] = VALUE
					92 |       argument90: [Int!] = 1.0
					   |                   ^
					93 |       argument91: [Int!] = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:212:19
					211 |       argument90: 1.0
					212 |       argument91: {}
					    |                   ^
					213 |       argument92: ""

					<document>:93:19
					92 |       argument90: [Int!] = 1.0
					93 |       argument91: [Int!] = {}
					   |                   ^
					94 |       argument92: [Int!] = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:213:19
					212 |       argument91: {}
					213 |       argument92: ""
					    |                   ^
					214 |       argument93: null

					<document>:94:19
					93 |       argument91: [Int!] = {}
					94 |       argument92: [Int!] = ""
					   |                   ^
					95 |       argument93: [Int!]! = null
				""",
				"""
					Type 'Int' does not allow value 'null'.

					<document>:214:19
					213 |       argument92: ""
					214 |       argument93: null
					    |                   ^
					215 |       argument94: true

					<document>:95:19
					94 |       argument92: [Int!] = ""
					95 |       argument93: [Int!]! = null
					   |                   ^
					96 |       argument94: [Int!]! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:215:19
					214 |       argument93: null
					215 |       argument94: true
					    |                   ^
					216 |       argument95: VALUE

					<document>:96:19
					95 |       argument93: [Int!]! = null
					96 |       argument94: [Int!]! = true
					   |                   ^
					97 |       argument95: [Int!]! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:216:19
					215 |       argument94: true
					216 |       argument95: VALUE
					    |                   ^
					217 |       argument96: 1.0

					<document>:97:19
					96 |       argument94: [Int!]! = true
					97 |       argument95: [Int!]! = VALUE
					   |                   ^
					98 |       argument96: [Int!]! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:217:19
					216 |       argument95: VALUE
					217 |       argument96: 1.0
					    |                   ^
					218 |       argument97: {}

					<document>:98:19
					97 |       argument95: [Int!]! = VALUE
					98 |       argument96: [Int!]! = 1.0
					   |                   ^
					99 |       argument97: [Int!]! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:218:19
					217 |       argument96: 1.0
					218 |       argument97: {}
					    |                   ^
					219 |       argument98: ""

					<document>:99:19
					 98 |       argument96: [Int!]! = 1.0
					 99 |       argument97: [Int!]! = {}
					    |                   ^
					100 |       argument98: [Int!]! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:219:19
					218 |       argument97: {}
					219 |       argument98: ""
					    |                   ^
					220 |       argument99: VALUE

					<document>:100:19
					 99 |       argument97: [Int!]! = {}
					100 |       argument98: [Int!]! = ""
					    |                   ^
					101 |       argument99: Scalar = VALUE
				""",
				"""
					Type 'Scalar' does not allow value 'null'.

					<document>:223:20
					222 |       argument101: []
					223 |       argument102: null
					    |                    ^
					224 |       argument103: VALUE

					<document>:104:20
					103 |       argument101: Scalar = []
					104 |       argument102: Scalar! = null
					    |                    ^
					105 |       argument103: Scalar! = VALUE
				""",
				"""
					Type 'String' does not allow value 'true'.

					<document>:227:20
					226 |       argument105: []
					227 |       argument106: true
					    |                    ^
					228 |       argument107: VALUE

					<document>:108:20
					107 |       argument105: Scalar! = []
					108 |       argument106: String = true
					    |                    ^
					109 |       argument107: String = VALUE
				""",
				"""
					Type 'String' does not allow value 'VALUE'.

					<document>:228:20
					227 |       argument106: true
					228 |       argument107: VALUE
					    |                    ^
					229 |       argument108: 1

					<document>:109:20
					108 |       argument106: String = true
					109 |       argument107: String = VALUE
					    |                    ^
					110 |       argument108: String = 1
				""",
				"""
					Type 'String' does not allow value '1'.

					<document>:229:20
					228 |       argument107: VALUE
					229 |       argument108: 1
					    |                    ^
					230 |       argument109: 1.0

					<document>:110:20
					109 |       argument107: String = VALUE
					110 |       argument108: String = 1
					    |                    ^
					111 |       argument109: String = 1.0
				""",
				"""
					Type 'String' does not allow value '1.0'.

					<document>:230:20
					229 |       argument108: 1
					230 |       argument109: 1.0
					    |                    ^
					231 |       argument110: {}

					<document>:111:20
					110 |       argument108: String = 1
					111 |       argument109: String = 1.0
					    |                    ^
					112 |       argument110: String = {}
				""",
				"""
					Type 'String' does not allow an input object value.

					<document>:231:20
					230 |       argument109: 1.0
					231 |       argument110: {}
					    |                    ^
					232 |       argument111: []

					<document>:112:20
					111 |       argument109: String = 1.0
					112 |       argument110: String = {}
					    |                    ^
					113 |       argument111: String = []
				""",
				"""
					Type 'String' does not allow a list value.

					<document>:232:20
					231 |       argument110: {}
					232 |       argument111: []
					    |                    ^
					233 |       argument112: null

					<document>:113:20
					112 |       argument110: String = {}
					113 |       argument111: String = []
					    |                    ^
					114 |       argument112: String! = null
				""",
				"""
					Type 'String' does not allow value 'null'.

					<document>:233:20
					232 |       argument111: []
					233 |       argument112: null
					    |                    ^
					234 |       argument113: true

					<document>:114:20
					113 |       argument111: String = []
					114 |       argument112: String! = null
					    |                    ^
					115 |       argument113: String! = true
				""",
				"""
					Type 'String' does not allow value 'true'.

					<document>:234:20
					233 |       argument112: null
					234 |       argument113: true
					    |                    ^
					235 |       argument114: VALUE

					<document>:115:20
					114 |       argument112: String! = null
					115 |       argument113: String! = true
					    |                    ^
					116 |       argument114: String! = VALUE
				""",
				"""
					Type 'String' does not allow value 'VALUE'.

					<document>:235:20
					234 |       argument113: true
					235 |       argument114: VALUE
					    |                    ^
					236 |       argument115: 1

					<document>:116:20
					115 |       argument113: String! = true
					116 |       argument114: String! = VALUE
					    |                    ^
					117 |       argument115: String! = 1
				""",
				"""
					Type 'String' does not allow value '1'.

					<document>:236:20
					235 |       argument114: VALUE
					236 |       argument115: 1
					    |                    ^
					237 |       argument116: 1.0

					<document>:117:20
					116 |       argument114: String! = VALUE
					117 |       argument115: String! = 1
					    |                    ^
					118 |       argument116: String! = 1.0
				""",
				"""
					Type 'String' does not allow value '1.0'.

					<document>:237:20
					236 |       argument115: 1
					237 |       argument116: 1.0
					    |                    ^
					238 |       argument117: {}

					<document>:118:20
					117 |       argument115: String! = 1
					118 |       argument116: String! = 1.0
					    |                    ^
					119 |       argument117: String! = {}
				""",
				"""
					Type 'String' does not allow an input object value.

					<document>:238:20
					237 |       argument116: 1.0
					238 |       argument117: {}
					    |                    ^
					239 |       argument118: []

					<document>:119:20
					118 |       argument116: String! = 1.0
					119 |       argument117: String! = {}
					    |                    ^
					120 |       argument118: String! = []
				""",
				"""
					Type 'String' does not allow a list value.

					<document>:239:20
					238 |       argument117: {}
					239 |       argument118: []
					    |                    ^
					240 |       argument119: {

					<document>:120:20
					119 |       argument117: String! = {}
					120 |       argument118: String! = []
					    |                    ^
					121 |       argument119: Input!
				""",
				"""
					Required field 'field119' of type 'Input' is missing.

					<document>:240:20
					239 |       argument118: []
					240 |       argument119: {
					    |                    ^
					241 |          field1: VALUE

					<document>:244:4
					243 |    field118: String! = []
					244 |    field119: String!
					    |    ^
					245 | }
				""",
				"""
					Type 'Boolean' does not allow value 'VALUE'.

					<document>:241:18
					240 |       argument119: {
					241 |          field1: VALUE
					    |                  ^
					242 |          field2: 1.0

					<document>:126:12
					125 | input Input {
					126 |    field1: Boolean = VALUE
					    |            ^
					127 |    field2: Boolean = 1.0
				""",
				"""
					Type 'Boolean' does not allow value '1.0'.

					<document>:242:18
					241 |          field1: VALUE
					242 |          field2: 1.0
					    |                  ^
					243 |          field3: 1

					<document>:127:12
					126 |    field1: Boolean = VALUE
					127 |    field2: Boolean = 1.0
					    |            ^
					128 |    field3: Boolean = 1
				""",
				"""
					Type 'Boolean' does not allow value '1'.

					<document>:243:18
					242 |          field2: 1.0
					243 |          field3: 1
					    |                  ^
					244 |          field4: []

					<document>:128:12
					127 |    field2: Boolean = 1.0
					128 |    field3: Boolean = 1
					    |            ^
					129 |    field4: Boolean = []
				""",
				"""
					Type 'Boolean' does not allow a list value.

					<document>:244:18
					243 |          field3: 1
					244 |          field4: []
					    |                  ^
					245 |          field5: {}

					<document>:129:12
					128 |    field3: Boolean = 1
					129 |    field4: Boolean = []
					    |            ^
					130 |    field5: Boolean = {}
				""",
				"""
					Type 'Boolean' does not allow an input object value.

					<document>:245:18
					244 |          field4: []
					245 |          field5: {}
					    |                  ^
					246 |          field6: ""

					<document>:130:12
					129 |    field4: Boolean = []
					130 |    field5: Boolean = {}
					    |            ^
					131 |    field6: Boolean = ""
				""",
				"""
					Type 'Boolean' does not allow value '""'.

					<document>:246:18
					245 |          field5: {}
					246 |          field6: ""
					    |                  ^
					247 |          field7: null

					<document>:131:12
					130 |    field5: Boolean = {}
					131 |    field6: Boolean = ""
					    |            ^
					132 |    field7: Boolean! = null
				""",
				"""
					Type 'Boolean' does not allow value 'null'.

					<document>:247:18
					246 |          field6: ""
					247 |          field7: null
					    |                  ^
					248 |          field8: VALUE

					<document>:132:12
					131 |    field6: Boolean = ""
					132 |    field7: Boolean! = null
					    |            ^
					133 |    field8: Boolean! = VALUE
				""",
				"""
					Type 'Boolean' does not allow value 'VALUE'.

					<document>:248:18
					247 |          field7: null
					248 |          field8: VALUE
					    |                  ^
					249 |          field9: 1.0

					<document>:133:12
					132 |    field7: Boolean! = null
					133 |    field8: Boolean! = VALUE
					    |            ^
					134 |    field9: Boolean! = 1.0
				""",
				"""
					Type 'Boolean' does not allow value '1.0'.

					<document>:249:18
					248 |          field8: VALUE
					249 |          field9: 1.0
					    |                  ^
					250 |          field10: 1

					<document>:134:12
					133 |    field8: Boolean! = VALUE
					134 |    field9: Boolean! = 1.0
					    |            ^
					135 |    field10: Boolean! = 1
				""",
				"""
					Type 'Boolean' does not allow value '1'.

					<document>:250:19
					249 |          field9: 1.0
					250 |          field10: 1
					    |                   ^
					251 |          field11: []

					<document>:135:13
					134 |    field9: Boolean! = 1.0
					135 |    field10: Boolean! = 1
					    |             ^
					136 |    field11: Boolean! = []
				""",
				"""
					Type 'Boolean' does not allow a list value.

					<document>:251:19
					250 |          field10: 1
					251 |          field11: []
					    |                   ^
					252 |          field12: {}

					<document>:136:13
					135 |    field10: Boolean! = 1
					136 |    field11: Boolean! = []
					    |             ^
					137 |    field12: Boolean! = {}
				""",
				"""
					Type 'Boolean' does not allow an input object value.

					<document>:252:19
					251 |          field11: []
					252 |          field12: {}
					    |                   ^
					253 |          field13: ""

					<document>:137:13
					136 |    field11: Boolean! = []
					137 |    field12: Boolean! = {}
					    |             ^
					138 |    field13: Boolean! = ""
				""",
				"""
					Type 'Boolean' does not allow value '""'.

					<document>:253:19
					252 |          field12: {}
					253 |          field13: ""
					    |                   ^
					254 |          field14: true

					<document>:138:13
					137 |    field12: Boolean! = {}
					138 |    field13: Boolean! = ""
					    |             ^
					139 |    field14: Enum = true
				""",
				"""
					Type 'Enum' does not allow value 'true'.

					<document>:254:19
					253 |          field13: ""
					254 |          field14: true
					    |                   ^
					255 |          field15: value

					<document>:139:13
					138 |    field13: Boolean! = ""
					139 |    field14: Enum = true
					    |             ^
					140 |    field15: Enum = value
				""",
				"""
					Type 'Enum' does not allow value 'value'.

					<document>:255:19
					254 |          field14: true
					255 |          field15: value
					    |                   ^
					256 |          field16: 1.0

					<document>:140:13
					139 |    field14: Enum = true
					140 |    field15: Enum = value
					    |             ^
					141 |    field16: Enum = 1.0
				""",
				"""
					Type 'Enum' does not allow value '1.0'.

					<document>:256:19
					255 |          field15: value
					256 |          field16: 1.0
					    |                   ^
					257 |          field17: 1

					<document>:141:13
					140 |    field15: Enum = value
					141 |    field16: Enum = 1.0
					    |             ^
					142 |    field17: Enum = 1
				""",
				"""
					Type 'Enum' does not allow value '1'.

					<document>:257:19
					256 |          field16: 1.0
					257 |          field17: 1
					    |                   ^
					258 |          field18: []

					<document>:142:13
					141 |    field16: Enum = 1.0
					142 |    field17: Enum = 1
					    |             ^
					143 |    field18: Enum = []
				""",
				"""
					Type 'Enum' does not allow a list value.

					<document>:258:19
					257 |          field17: 1
					258 |          field18: []
					    |                   ^
					259 |          field19: {}

					<document>:143:13
					142 |    field17: Enum = 1
					143 |    field18: Enum = []
					    |             ^
					144 |    field19: Enum = {}
				""",
				"""
					Type 'Enum' does not allow an input object value.

					<document>:259:19
					258 |          field18: []
					259 |          field19: {}
					    |                   ^
					260 |          field20: ""

					<document>:144:13
					143 |    field18: Enum = []
					144 |    field19: Enum = {}
					    |             ^
					145 |    field20: Enum = ""
				""",
				"""
					Type 'Enum' does not allow value '""'.

					<document>:260:19
					259 |          field19: {}
					260 |          field20: ""
					    |                   ^
					261 |          field21: null

					<document>:145:13
					144 |    field19: Enum = {}
					145 |    field20: Enum = ""
					    |             ^
					146 |    field21: Enum! = null
				""",
				"""
					Type 'Enum' does not allow value 'null'.

					<document>:261:19
					260 |          field20: ""
					261 |          field21: null
					    |                   ^
					262 |          field22: value

					<document>:146:13
					145 |    field20: Enum = ""
					146 |    field21: Enum! = null
					    |             ^
					147 |    field22: Enum! = value
				""",
				"""
					Type 'Enum' does not allow value 'value'.

					<document>:262:19
					261 |          field21: null
					262 |          field22: value
					    |                   ^
					263 |          field23: true

					<document>:147:13
					146 |    field21: Enum! = null
					147 |    field22: Enum! = value
					    |             ^
					148 |    field23: Enum! = true
				""",
				"""
					Type 'Enum' does not allow value 'true'.

					<document>:263:19
					262 |          field22: value
					263 |          field23: true
					    |                   ^
					264 |          field24: 1.0

					<document>:148:13
					147 |    field22: Enum! = value
					148 |    field23: Enum! = true
					    |             ^
					149 |    field24: Enum! = 1.0
				""",
				"""
					Type 'Enum' does not allow value '1.0'.

					<document>:264:19
					263 |          field23: true
					264 |          field24: 1.0
					    |                   ^
					265 |          field25: 1

					<document>:149:13
					148 |    field23: Enum! = true
					149 |    field24: Enum! = 1.0
					    |             ^
					150 |    field25: Enum! = 1
				""",
				"""
					Type 'Enum' does not allow value '1'.

					<document>:265:19
					264 |          field24: 1.0
					265 |          field25: 1
					    |                   ^
					266 |          field26: []

					<document>:150:13
					149 |    field24: Enum! = 1.0
					150 |    field25: Enum! = 1
					    |             ^
					151 |    field26: Enum! = []
				""",
				"""
					Type 'Enum' does not allow a list value.

					<document>:266:19
					265 |          field25: 1
					266 |          field26: []
					    |                   ^
					267 |          field27: {}

					<document>:151:13
					150 |    field25: Enum! = 1
					151 |    field26: Enum! = []
					    |             ^
					152 |    field27: Enum! = {}
				""",
				"""
					Type 'Enum' does not allow an input object value.

					<document>:267:19
					266 |          field26: []
					267 |          field27: {}
					    |                   ^
					268 |          field28: ""

					<document>:152:13
					151 |    field26: Enum! = []
					152 |    field27: Enum! = {}
					    |             ^
					153 |    field28: Enum! = ""
				""",
				"""
					Type 'Enum' does not allow value '""'.

					<document>:268:19
					267 |          field27: {}
					268 |          field28: ""
					    |                   ^
					269 |          field29: true

					<document>:153:13
					152 |    field27: Enum! = {}
					153 |    field28: Enum! = ""
					    |             ^
					154 |    field29: Float = true
				""",
				"""
					Type 'Float' does not allow value 'true'.

					<document>:269:19
					268 |          field28: ""
					269 |          field29: true
					    |                   ^
					270 |          field30: VALUE

					<document>:154:13
					153 |    field28: Enum! = ""
					154 |    field29: Float = true
					    |             ^
					155 |    field30: Float = VALUE
				""",
				"""
					Type 'Float' does not allow value 'VALUE'.

					<document>:270:19
					269 |          field29: true
					270 |          field30: VALUE
					    |                   ^
					271 |          field31: []

					<document>:155:13
					154 |    field29: Float = true
					155 |    field30: Float = VALUE
					    |             ^
					156 |    field31: Float = []
				""",
				"""
					Type 'Float' does not allow a list value.

					<document>:271:19
					270 |          field30: VALUE
					271 |          field31: []
					    |                   ^
					272 |          field32: {}

					<document>:156:13
					155 |    field30: Float = VALUE
					156 |    field31: Float = []
					    |             ^
					157 |    field32: Float = {}
				""",
				"""
					Type 'Float' does not allow an input object value.

					<document>:272:19
					271 |          field31: []
					272 |          field32: {}
					    |                   ^
					273 |          field33: ""

					<document>:157:13
					156 |    field31: Float = []
					157 |    field32: Float = {}
					    |             ^
					158 |    field33: Float = ""
				""",
				"""
					Type 'Float' does not allow value '""'.

					<document>:273:19
					272 |          field32: {}
					273 |          field33: ""
					    |                   ^
					274 |          field34: null

					<document>:158:13
					157 |    field32: Float = {}
					158 |    field33: Float = ""
					    |             ^
					159 |    field34: Float! = null
				""",
				"""
					Type 'Float' does not allow value 'null'.

					<document>:274:19
					273 |          field33: ""
					274 |          field34: null
					    |                   ^
					275 |          field35: true

					<document>:159:13
					158 |    field33: Float = ""
					159 |    field34: Float! = null
					    |             ^
					160 |    field35: Float! = true
				""",
				"""
					Type 'Float' does not allow value 'true'.

					<document>:275:19
					274 |          field34: null
					275 |          field35: true
					    |                   ^
					276 |          field36: VALUE

					<document>:160:13
					159 |    field34: Float! = null
					160 |    field35: Float! = true
					    |             ^
					161 |    field36: Float! = VALUE
				""",
				"""
					Type 'Float' does not allow value 'VALUE'.

					<document>:276:19
					275 |          field35: true
					276 |          field36: VALUE
					    |                   ^
					277 |          field37: []

					<document>:161:13
					160 |    field35: Float! = true
					161 |    field36: Float! = VALUE
					    |             ^
					162 |    field37: Float! = []
				""",
				"""
					Type 'Float' does not allow a list value.

					<document>:277:19
					276 |          field36: VALUE
					277 |          field37: []
					    |                   ^
					278 |          field38: {}

					<document>:162:13
					161 |    field36: Float! = VALUE
					162 |    field37: Float! = []
					    |             ^
					163 |    field38: Float! = {}
				""",
				"""
					Type 'Float' does not allow an input object value.

					<document>:278:19
					277 |          field37: []
					278 |          field38: {}
					    |                   ^
					279 |          field39: ""

					<document>:163:13
					162 |    field37: Float! = []
					163 |    field38: Float! = {}
					    |             ^
					164 |    field39: Float! = ""
				""",
				"""
					Type 'Float' does not allow value '""'.

					<document>:279:19
					278 |          field38: {}
					279 |          field39: ""
					    |                   ^
					280 |          field40: true

					<document>:164:13
					163 |    field38: Float! = {}
					164 |    field39: Float! = ""
					    |             ^
					165 |    field40: ID = true
				""",
				"""
					Type 'ID' does not allow value 'true'.

					<document>:280:19
					279 |          field39: ""
					280 |          field40: true
					    |                   ^
					281 |          field41: 1.0

					<document>:165:13
					164 |    field39: Float! = ""
					165 |    field40: ID = true
					    |             ^
					166 |    field41: ID = 1.0
				""",
				"""
					Type 'ID' does not allow value '1.0'.

					<document>:281:19
					280 |          field40: true
					281 |          field41: 1.0
					    |                   ^
					282 |          field44: VALUE

					<document>:166:13
					165 |    field40: ID = true
					166 |    field41: ID = 1.0
					    |             ^
					167 |    field44: ID = VALUE
				""",
				"""
					Type 'ID' does not allow value 'VALUE'.

					<document>:282:19
					281 |          field41: 1.0
					282 |          field44: VALUE
					    |                   ^
					283 |          field42: []

					<document>:167:13
					166 |    field41: ID = 1.0
					167 |    field44: ID = VALUE
					    |             ^
					168 |    field42: ID = []
				""",
				"""
					Type 'ID' does not allow a list value.

					<document>:283:19
					282 |          field44: VALUE
					283 |          field42: []
					    |                   ^
					284 |          field43: {}

					<document>:168:13
					167 |    field44: ID = VALUE
					168 |    field42: ID = []
					    |             ^
					169 |    field43: ID = {}
				""",
				"""
					Type 'ID' does not allow an input object value.

					<document>:284:19
					283 |          field42: []
					284 |          field43: {}
					    |                   ^
					285 |          field45: null

					<document>:169:13
					168 |    field42: ID = []
					169 |    field43: ID = {}
					    |             ^
					170 |    field45: ID! = null
				""",
				"""
					Type 'ID' does not allow value 'null'.

					<document>:285:19
					284 |          field43: {}
					285 |          field45: null
					    |                   ^
					286 |          field46: true

					<document>:170:13
					169 |    field43: ID = {}
					170 |    field45: ID! = null
					    |             ^
					171 |    field46: ID! = true
				""",
				"""
					Type 'ID' does not allow value 'true'.

					<document>:286:19
					285 |          field45: null
					286 |          field46: true
					    |                   ^
					287 |          field47: 1.0

					<document>:171:13
					170 |    field45: ID! = null
					171 |    field46: ID! = true
					    |             ^
					172 |    field47: ID! = 1.0
				""",
				"""
					Type 'ID' does not allow value '1.0'.

					<document>:287:19
					286 |          field46: true
					287 |          field47: 1.0
					    |                   ^
					288 |          field48: VALUE

					<document>:172:13
					171 |    field46: ID! = true
					172 |    field47: ID! = 1.0
					    |             ^
					173 |    field48: ID! = VALUE
				""",
				"""
					Type 'ID' does not allow value 'VALUE'.

					<document>:288:19
					287 |          field47: 1.0
					288 |          field48: VALUE
					    |                   ^
					289 |          field49: []

					<document>:173:13
					172 |    field47: ID! = 1.0
					173 |    field48: ID! = VALUE
					    |             ^
					174 |    field49: ID! = []
				""",
				"""
					Type 'ID' does not allow a list value.

					<document>:289:19
					288 |          field48: VALUE
					289 |          field49: []
					    |                   ^
					290 |          field50: {}

					<document>:174:13
					173 |    field48: ID! = VALUE
					174 |    field49: ID! = []
					    |             ^
					175 |    field50: ID! = {}
				""",
				"""
					Type 'ID' does not allow an input object value.

					<document>:290:19
					289 |          field49: []
					290 |          field50: {}
					    |                   ^
					291 |          field51: true

					<document>:175:13
					174 |    field49: ID! = []
					175 |    field50: ID! = {}
					    |             ^
					176 |    field51: Input = true
				""",
				"""
					Type 'Input' does not allow value 'true'.

					<document>:291:19
					290 |          field50: {}
					291 |          field51: true
					    |                   ^
					292 |          field52: VALUE

					<document>:176:13
					175 |    field50: ID! = {}
					176 |    field51: Input = true
					    |             ^
					177 |    field52: Input = VALUE
				""",
				"""
					Type 'Input' does not allow value 'VALUE'.

					<document>:292:19
					291 |          field51: true
					292 |          field52: VALUE
					    |                   ^
					293 |          field53: 1.0

					<document>:177:13
					176 |    field51: Input = true
					177 |    field52: Input = VALUE
					    |             ^
					178 |    field53: Input = 1.0
				""",
				"""
					Type 'Input' does not allow value '1.0'.

					<document>:293:19
					292 |          field52: VALUE
					293 |          field53: 1.0
					    |                   ^
					294 |          field54: 1

					<document>:178:13
					177 |    field52: Input = VALUE
					178 |    field53: Input = 1.0
					    |             ^
					179 |    field54: Input = 1
				""",
				"""
					Type 'Input' does not allow value '1'.

					<document>:294:19
					293 |          field53: 1.0
					294 |          field54: 1
					    |                   ^
					295 |          field55: []

					<document>:179:13
					178 |    field53: Input = 1.0
					179 |    field54: Input = 1
					    |             ^
					180 |    field55: Input = []
				""",
				"""
					Type 'Input' does not allow a list value.

					<document>:295:19
					294 |          field54: 1
					295 |          field55: []
					    |                   ^
					296 |          field56: ""

					<document>:180:13
					179 |    field54: Input = 1
					180 |    field55: Input = []
					    |             ^
					181 |    field56: Input = ""
				""",
				"""
					Type 'Input' does not allow value '""'.

					<document>:296:19
					295 |          field55: []
					296 |          field56: ""
					    |                   ^
					297 |          field57: null

					<document>:181:13
					180 |    field55: Input = []
					181 |    field56: Input = ""
					    |             ^
					182 |    field57: Input! = null
				""",
				"""
					Type 'Input' does not allow value 'null'.

					<document>:297:19
					296 |          field56: ""
					297 |          field57: null
					    |                   ^
					298 |          field58: true

					<document>:182:13
					181 |    field56: Input = ""
					182 |    field57: Input! = null
					    |             ^
					183 |    field58: Input! = true
				""",
				"""
					Type 'Input' does not allow value 'true'.

					<document>:298:19
					297 |          field57: null
					298 |          field58: true
					    |                   ^
					299 |          field59: VALUE

					<document>:183:13
					182 |    field57: Input! = null
					183 |    field58: Input! = true
					    |             ^
					184 |    field59: Input! = VALUE
				""",
				"""
					Type 'Input' does not allow value 'VALUE'.

					<document>:299:19
					298 |          field58: true
					299 |          field59: VALUE
					    |                   ^
					300 |          field60: 1.0

					<document>:184:13
					183 |    field58: Input! = true
					184 |    field59: Input! = VALUE
					    |             ^
					185 |    field60: Input! = 1.0
				""",
				"""
					Type 'Input' does not allow value '1.0'.

					<document>:300:19
					299 |          field59: VALUE
					300 |          field60: 1.0
					    |                   ^
					301 |          field61: 1

					<document>:185:13
					184 |    field59: Input! = VALUE
					185 |    field60: Input! = 1.0
					    |             ^
					186 |    field61: Input! = 1
				""",
				"""
					Type 'Input' does not allow value '1'.

					<document>:301:19
					300 |          field60: 1.0
					301 |          field61: 1
					    |                   ^
					302 |          field62: []

					<document>:186:13
					185 |    field60: Input! = 1.0
					186 |    field61: Input! = 1
					    |             ^
					187 |    field62: Input! = []
				""",
				"""
					Type 'Input' does not allow a list value.

					<document>:302:19
					301 |          field61: 1
					302 |          field62: []
					    |                   ^
					303 |          field63: ""

					<document>:187:13
					186 |    field61: Input! = 1
					187 |    field62: Input! = []
					    |             ^
					188 |    field63: Input! = ""
				""",
				"""
					Type 'Input' does not allow value '""'.

					<document>:303:19
					302 |          field62: []
					303 |          field63: ""
					    |                   ^
					304 |          field64: true

					<document>:188:13
					187 |    field62: Input! = []
					188 |    field63: Input! = ""
					    |             ^
					189 |    field64: Int = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:304:19
					303 |          field63: ""
					304 |          field64: true
					    |                   ^
					305 |          field65: VALUE

					<document>:189:13
					188 |    field63: Input! = ""
					189 |    field64: Int = true
					    |             ^
					190 |    field65: Int = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:305:19
					304 |          field64: true
					305 |          field65: VALUE
					    |                   ^
					306 |          field66: 1.0

					<document>:190:13
					189 |    field64: Int = true
					190 |    field65: Int = VALUE
					    |             ^
					191 |    field66: Int = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:306:19
					305 |          field65: VALUE
					306 |          field66: 1.0
					    |                   ^
					307 |          field67: {}

					<document>:191:13
					190 |    field65: Int = VALUE
					191 |    field66: Int = 1.0
					    |             ^
					192 |    field67: Int = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:307:19
					306 |          field66: 1.0
					307 |          field67: {}
					    |                   ^
					308 |          field68: []

					<document>:192:13
					191 |    field66: Int = 1.0
					192 |    field67: Int = {}
					    |             ^
					193 |    field68: Int = []
				""",
				"""
					Type 'Int' does not allow a list value.

					<document>:308:19
					307 |          field67: {}
					308 |          field68: []
					    |                   ^
					309 |          field69: ""

					<document>:193:13
					192 |    field67: Int = {}
					193 |    field68: Int = []
					    |             ^
					194 |    field69: Int = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:309:19
					308 |          field68: []
					309 |          field69: ""
					    |                   ^
					310 |          field70: null

					<document>:194:13
					193 |    field68: Int = []
					194 |    field69: Int = ""
					    |             ^
					195 |    field70: Int! = null
				""",
				"""
					Type 'Int' does not allow value 'null'.

					<document>:310:19
					309 |          field69: ""
					310 |          field70: null
					    |                   ^
					311 |          field71: true

					<document>:195:13
					194 |    field69: Int = ""
					195 |    field70: Int! = null
					    |             ^
					196 |    field71: Int! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:311:19
					310 |          field70: null
					311 |          field71: true
					    |                   ^
					312 |          field72: VALUE

					<document>:196:13
					195 |    field70: Int! = null
					196 |    field71: Int! = true
					    |             ^
					197 |    field72: Int! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:312:19
					311 |          field71: true
					312 |          field72: VALUE
					    |                   ^
					313 |          field73: 1.0

					<document>:197:13
					196 |    field71: Int! = true
					197 |    field72: Int! = VALUE
					    |             ^
					198 |    field73: Int! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:313:19
					312 |          field72: VALUE
					313 |          field73: 1.0
					    |                   ^
					314 |          field74: {}

					<document>:198:13
					197 |    field72: Int! = VALUE
					198 |    field73: Int! = 1.0
					    |             ^
					199 |    field74: Int! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:314:19
					313 |          field73: 1.0
					314 |          field74: {}
					    |                   ^
					315 |          field75: []

					<document>:199:13
					198 |    field73: Int! = 1.0
					199 |    field74: Int! = {}
					    |             ^
					200 |    field75: Int! = []
				""",
				"""
					Type 'Int' does not allow a list value.

					<document>:315:19
					314 |          field74: {}
					315 |          field75: []
					    |                   ^
					316 |          field76: ""

					<document>:200:13
					199 |    field74: Int! = {}
					200 |    field75: Int! = []
					    |             ^
					201 |    field76: Int! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:316:19
					315 |          field75: []
					316 |          field76: ""
					    |                   ^
					317 |          field77: true

					<document>:201:13
					200 |    field75: Int! = []
					201 |    field76: Int! = ""
					    |             ^
					202 |    field77: [Int] = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:317:19
					316 |          field76: ""
					317 |          field77: true
					    |                   ^
					318 |          field78: VALUE

					<document>:202:13
					201 |    field76: Int! = ""
					202 |    field77: [Int] = true
					    |             ^
					203 |    field78: [Int] = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:318:19
					317 |          field77: true
					318 |          field78: VALUE
					    |                   ^
					319 |          field79: 1.0

					<document>:203:13
					202 |    field77: [Int] = true
					203 |    field78: [Int] = VALUE
					    |             ^
					204 |    field79: [Int] = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:319:19
					318 |          field78: VALUE
					319 |          field79: 1.0
					    |                   ^
					320 |          field80: {}

					<document>:204:13
					203 |    field78: [Int] = VALUE
					204 |    field79: [Int] = 1.0
					    |             ^
					205 |    field80: [Int] = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:320:19
					319 |          field79: 1.0
					320 |          field80: {}
					    |                   ^
					321 |          field81: ""

					<document>:205:13
					204 |    field79: [Int] = 1.0
					205 |    field80: [Int] = {}
					    |             ^
					206 |    field81: [Int] = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:321:19
					320 |          field80: {}
					321 |          field81: ""
					    |                   ^
					322 |          field82: null

					<document>:206:13
					205 |    field80: [Int] = {}
					206 |    field81: [Int] = ""
					    |             ^
					207 |    field82: [Int]! = null
				""",
				"""
					Type 'Int' does not allow value 'null'.

					<document>:322:19
					321 |          field81: ""
					322 |          field82: null
					    |                   ^
					323 |          field83: true

					<document>:207:13
					206 |    field81: [Int] = ""
					207 |    field82: [Int]! = null
					    |             ^
					208 |    field83: [Int]! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:323:19
					322 |          field82: null
					323 |          field83: true
					    |                   ^
					324 |          field84: VALUE

					<document>:208:13
					207 |    field82: [Int]! = null
					208 |    field83: [Int]! = true
					    |             ^
					209 |    field84: [Int]! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:324:19
					323 |          field83: true
					324 |          field84: VALUE
					    |                   ^
					325 |          field85: 1.0

					<document>:209:13
					208 |    field83: [Int]! = true
					209 |    field84: [Int]! = VALUE
					    |             ^
					210 |    field85: [Int]! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:325:19
					324 |          field84: VALUE
					325 |          field85: 1.0
					    |                   ^
					326 |          field86: {}

					<document>:210:13
					209 |    field84: [Int]! = VALUE
					210 |    field85: [Int]! = 1.0
					    |             ^
					211 |    field86: [Int]! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:326:19
					325 |          field85: 1.0
					326 |          field86: {}
					    |                   ^
					327 |          field87: ""

					<document>:211:13
					210 |    field85: [Int]! = 1.0
					211 |    field86: [Int]! = {}
					    |             ^
					212 |    field87: [Int]! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:327:19
					326 |          field86: {}
					327 |          field87: ""
					    |                   ^
					328 |          field88: true

					<document>:212:13
					211 |    field86: [Int]! = {}
					212 |    field87: [Int]! = ""
					    |             ^
					213 |    field88: [Int!] = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:328:19
					327 |          field87: ""
					328 |          field88: true
					    |                   ^
					329 |          field89: VALUE

					<document>:213:13
					212 |    field87: [Int]! = ""
					213 |    field88: [Int!] = true
					    |             ^
					214 |    field89: [Int!] = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:329:19
					328 |          field88: true
					329 |          field89: VALUE
					    |                   ^
					330 |          field90: 1.0

					<document>:214:13
					213 |    field88: [Int!] = true
					214 |    field89: [Int!] = VALUE
					    |             ^
					215 |    field90: [Int!] = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:330:19
					329 |          field89: VALUE
					330 |          field90: 1.0
					    |                   ^
					331 |          field91: {}

					<document>:215:13
					214 |    field89: [Int!] = VALUE
					215 |    field90: [Int!] = 1.0
					    |             ^
					216 |    field91: [Int!] = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:331:19
					330 |          field90: 1.0
					331 |          field91: {}
					    |                   ^
					332 |          field92: ""

					<document>:216:13
					215 |    field90: [Int!] = 1.0
					216 |    field91: [Int!] = {}
					    |             ^
					217 |    field92: [Int!] = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:332:19
					331 |          field91: {}
					332 |          field92: ""
					    |                   ^
					333 |          field93: null

					<document>:217:13
					216 |    field91: [Int!] = {}
					217 |    field92: [Int!] = ""
					    |             ^
					218 |    field93: [Int!]! = null
				""",
				"""
					Type 'Int' does not allow value 'null'.

					<document>:333:19
					332 |          field92: ""
					333 |          field93: null
					    |                   ^
					334 |          field94: true

					<document>:218:13
					217 |    field92: [Int!] = ""
					218 |    field93: [Int!]! = null
					    |             ^
					219 |    field94: [Int!]! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:334:19
					333 |          field93: null
					334 |          field94: true
					    |                   ^
					335 |          field95: VALUE

					<document>:219:13
					218 |    field93: [Int!]! = null
					219 |    field94: [Int!]! = true
					    |             ^
					220 |    field95: [Int!]! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:335:19
					334 |          field94: true
					335 |          field95: VALUE
					    |                   ^
					336 |          field96: 1.0

					<document>:220:13
					219 |    field94: [Int!]! = true
					220 |    field95: [Int!]! = VALUE
					    |             ^
					221 |    field96: [Int!]! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:336:19
					335 |          field95: VALUE
					336 |          field96: 1.0
					    |                   ^
					337 |          field97: {}

					<document>:221:13
					220 |    field95: [Int!]! = VALUE
					221 |    field96: [Int!]! = 1.0
					    |             ^
					222 |    field97: [Int!]! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:337:19
					336 |          field96: 1.0
					337 |          field97: {}
					    |                   ^
					338 |          field98: ""

					<document>:222:13
					221 |    field96: [Int!]! = 1.0
					222 |    field97: [Int!]! = {}
					    |             ^
					223 |    field98: [Int!]! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:338:19
					337 |          field97: {}
					338 |          field98: ""
					    |                   ^
					339 |          field99: VALUE

					<document>:223:13
					222 |    field97: [Int!]! = {}
					223 |    field98: [Int!]! = ""
					    |             ^
					224 |    field99: Scalar = VALUE
				""",
				"""
					Type 'Scalar' does not allow value 'null'.

					<document>:342:20
					341 |          field101: []
					342 |          field102: null
					    |                    ^
					343 |          field103: VALUE

					<document>:227:14
					226 |    field101: Scalar = []
					227 |    field102: Scalar! = null
					    |              ^
					228 |    field103: Scalar! = VALUE
				""",
				"""
					Type 'String' does not allow value 'true'.

					<document>:346:20
					345 |          field105: []
					346 |          field106: true
					    |                    ^
					347 |          field107: VALUE

					<document>:231:14
					230 |    field105: Scalar! = []
					231 |    field106: String = true
					    |              ^
					232 |    field107: String = VALUE
				""",
				"""
					Type 'String' does not allow value 'VALUE'.

					<document>:347:20
					346 |          field106: true
					347 |          field107: VALUE
					    |                    ^
					348 |          field108: 1

					<document>:232:14
					231 |    field106: String = true
					232 |    field107: String = VALUE
					    |              ^
					233 |    field108: String = 1
				""",
				"""
					Type 'String' does not allow value '1'.

					<document>:348:20
					347 |          field107: VALUE
					348 |          field108: 1
					    |                    ^
					349 |          field109: 1.0

					<document>:233:14
					232 |    field107: String = VALUE
					233 |    field108: String = 1
					    |              ^
					234 |    field109: String = 1.0
				""",
				"""
					Type 'String' does not allow value '1.0'.

					<document>:349:20
					348 |          field108: 1
					349 |          field109: 1.0
					    |                    ^
					350 |          field110: {}

					<document>:234:14
					233 |    field108: String = 1
					234 |    field109: String = 1.0
					    |              ^
					235 |    field110: String = {}
				""",
				"""
					Type 'String' does not allow an input object value.

					<document>:350:20
					349 |          field109: 1.0
					350 |          field110: {}
					    |                    ^
					351 |          field111: []

					<document>:235:14
					234 |    field109: String = 1.0
					235 |    field110: String = {}
					    |              ^
					236 |    field111: String = []
				""",
				"""
					Type 'String' does not allow a list value.

					<document>:351:20
					350 |          field110: {}
					351 |          field111: []
					    |                    ^
					352 |          field112: null

					<document>:236:14
					235 |    field110: String = {}
					236 |    field111: String = []
					    |              ^
					237 |    field112: String! = null
				""",
				"""
					Type 'String' does not allow value 'null'.

					<document>:352:20
					351 |          field111: []
					352 |          field112: null
					    |                    ^
					353 |          field113: true

					<document>:237:14
					236 |    field111: String = []
					237 |    field112: String! = null
					    |              ^
					238 |    field113: String! = true
				""",
				"""
					Type 'String' does not allow value 'true'.

					<document>:353:20
					352 |          field112: null
					353 |          field113: true
					    |                    ^
					354 |          field114: VALUE

					<document>:238:14
					237 |    field112: String! = null
					238 |    field113: String! = true
					    |              ^
					239 |    field114: String! = VALUE
				""",
				"""
					Type 'String' does not allow value 'VALUE'.

					<document>:354:20
					353 |          field113: true
					354 |          field114: VALUE
					    |                    ^
					355 |          field115: 1

					<document>:239:14
					238 |    field113: String! = true
					239 |    field114: String! = VALUE
					    |              ^
					240 |    field115: String! = 1
				""",
				"""
					Type 'String' does not allow value '1'.

					<document>:355:20
					354 |          field114: VALUE
					355 |          field115: 1
					    |                    ^
					356 |          field116: 1.0

					<document>:240:14
					239 |    field114: String! = VALUE
					240 |    field115: String! = 1
					    |              ^
					241 |    field116: String! = 1.0
				""",
				"""
					Type 'String' does not allow value '1.0'.

					<document>:356:20
					355 |          field115: 1
					356 |          field116: 1.0
					    |                    ^
					357 |          field117: {}

					<document>:241:14
					240 |    field115: String! = 1
					241 |    field116: String! = 1.0
					    |              ^
					242 |    field117: String! = {}
				""",
				"""
					Type 'String' does not allow an input object value.

					<document>:357:20
					356 |          field116: 1.0
					357 |          field117: {}
					    |                    ^
					358 |          field118: []

					<document>:242:14
					241 |    field116: String! = 1.0
					242 |    field117: String! = {}
					    |              ^
					243 |    field118: String! = []
				""",
				"""
					Type 'String' does not allow a list value.

					<document>:358:20
					357 |          field117: {}
					358 |          field118: []
					    |                    ^
					359 |       }

					<document>:243:14
					242 |    field117: String! = {}
					243 |    field118: String! = []
					    |              ^
					244 |    field119: String!
				"""
			),
			document = """
				|query someQuery(
				|   ${'$'}variable1: Boolean = VALUE
				|   ${'$'}variable2: Boolean = 1.0
				|   ${'$'}variable3: Boolean = 1
				|   ${'$'}variable4: Boolean = []
				|   ${'$'}variable5: Boolean = {}
				|   ${'$'}variable6: Boolean = ""
				|   ${'$'}variable7: Boolean! = null
				|   ${'$'}variable8: Boolean! = VALUE
				|   ${'$'}variable9: Boolean! = 1.0
				|   ${'$'}variable10: Boolean! = 1
				|   ${'$'}variable11: Boolean! = []
				|   ${'$'}variable12: Boolean! = {}
				|   ${'$'}variable13: Boolean! = ""
				|   ${'$'}variable14: Enum = true
				|   ${'$'}variable15: Enum = value
				|   ${'$'}variable16: Enum = 1.0
				|   ${'$'}variable17: Enum = 1
				|   ${'$'}variable18: Enum = []
				|   ${'$'}variable19: Enum = {}
				|   ${'$'}variable20: Enum = ""
				|   ${'$'}variable21: Enum! = null
				|   ${'$'}variable22: Enum! = value
				|   ${'$'}variable23: Enum! = true
				|   ${'$'}variable24: Enum! = 1.0
				|   ${'$'}variable25: Enum! = 1
				|   ${'$'}variable26: Enum! = []
				|   ${'$'}variable27: Enum! = {}
				|   ${'$'}variable28: Enum! = ""
				|   ${'$'}variable29: Float = true
				|   ${'$'}variable30: Float = VALUE
				|   ${'$'}variable31: Float = []
				|   ${'$'}variable32: Float = {}
				|   ${'$'}variable33: Float = ""
				|   ${'$'}variable34: Float! = null
				|   ${'$'}variable35: Float! = true
				|   ${'$'}variable36: Float! = VALUE
				|   ${'$'}variable37: Float! = []
				|   ${'$'}variable38: Float! = {}
				|   ${'$'}variable39: Float! = ""
				|   ${'$'}variable40: ID = true
				|   ${'$'}variable41: ID = 1.0
				|   ${'$'}variable44: ID = VALUE
				|   ${'$'}variable42: ID = []
				|   ${'$'}variable43: ID = {}
				|   ${'$'}variable45: ID! = null
				|   ${'$'}variable46: ID! = true
				|   ${'$'}variable47: ID! = 1.0
				|   ${'$'}variable48: ID! = VALUE
				|   ${'$'}variable49: ID! = []
				|   ${'$'}variable50: ID! = {}
				|   ${'$'}variable51: Input = true
				|   ${'$'}variable52: Input = VALUE
				|   ${'$'}variable53: Input = 1.0
				|   ${'$'}variable54: Input = 1
				|   ${'$'}variable55: Input = []
				|   ${'$'}variable56: Input = ""
				|   ${'$'}variable57: Input! = null
				|   ${'$'}variable58: Input! = true
				|   ${'$'}variable59: Input! = VALUE
				|   ${'$'}variable60: Input! = 1.0
				|   ${'$'}variable61: Input! = 1
				|   ${'$'}variable62: Input! = []
				|   ${'$'}variable63: Input! = ""
				|   ${'$'}variable64: Int = true
				|   ${'$'}variable65: Int = VALUE
				|   ${'$'}variable66: Int = 1.0
				|   ${'$'}variable67: Int = {}
				|   ${'$'}variable68: Int = []
				|   ${'$'}variable69: Int = ""
				|   ${'$'}variable70: Int! = null
				|   ${'$'}variable71: Int! = true
				|   ${'$'}variable72: Int! = VALUE
				|   ${'$'}variable73: Int! = 1.0
				|   ${'$'}variable74: Int! = {}
				|   ${'$'}variable75: Int! = []
				|   ${'$'}variable76: Int! = ""
				|   ${'$'}variable77: [Int] = true
				|   ${'$'}variable78: [Int] = VALUE
				|   ${'$'}variable79: [Int] = 1.0
				|   ${'$'}variable80: [Int] = {}
				|   ${'$'}variable81: [Int] = ""
				|   ${'$'}variable82: [Int]! = null
				|   ${'$'}variable83: [Int]! = true
				|   ${'$'}variable84: [Int]! = VALUE
				|   ${'$'}variable85: [Int]! = 1.0
				|   ${'$'}variable86: [Int]! = {}
				|   ${'$'}variable87: [Int]! = ""
				|   ${'$'}variable88: [Int!] = true
				|   ${'$'}variable89: [Int!] = VALUE
				|   ${'$'}variable90: [Int!] = 1.0
				|   ${'$'}variable91: [Int!] = {}
				|   ${'$'}variable92: [Int!] = ""
				|   ${'$'}variable93: [Int!]! = null
				|   ${'$'}variable94: [Int!]! = true
				|   ${'$'}variable95: [Int!]! = VALUE
				|   ${'$'}variable96: [Int!]! = 1.0
				|   ${'$'}variable97: [Int!]! = {}
				|   ${'$'}variable98: [Int!]! = ""
				|   ${'$'}variable99: Scalar = VALUE
				|   ${'$'}variable100: Scalar = {}
				|   ${'$'}variable101: Scalar = []
				|   ${'$'}variable102: Scalar! = null
				|   ${'$'}variable103: Scalar! = VALUE
				|   ${'$'}variable104: Scalar! = {}
				|   ${'$'}variable105: Scalar! = []
				|   ${'$'}variable106: String = true
				|   ${'$'}variable107: String = VALUE
				|   ${'$'}variable108: String = 1
				|   ${'$'}variable109: String = 1.0
				|   ${'$'}variable110: String = {}
				|   ${'$'}variable111: String = []
				|   ${'$'}variable112: String! = null
				|   ${'$'}variable113: String! = true
				|   ${'$'}variable114: String! = VALUE
				|   ${'$'}variable115: String! = 1
				|   ${'$'}variable116: String! = 1.0
				|   ${'$'}variable117: String! = {}
				|   ${'$'}variable118: String! = []
				|) {
				|   fun(
				|      argument1: VALUE
				|      argument2: 1.0
				|      argument3: 1
				|      argument4: []
				|      argument5: {}
				|      argument6: ""
				|      argument7: null
				|      argument8: VALUE
				|      argument9: 1.0
				|      argument10: 1
				|      argument11: []
				|      argument12: {}
				|      argument13: ""
				|      argument14: true
				|      argument15: value
				|      argument16: 1.0
				|      argument17: 1
				|      argument18: []
				|      argument19: {}
				|      argument20: ""
				|      argument21: null
				|      argument22: value
				|      argument23: true
				|      argument24: 1.0
				|      argument25: 1
				|      argument26: []
				|      argument27: {}
				|      argument28: ""
				|      argument29: true
				|      argument30: VALUE
				|      argument31: []
				|      argument32: {}
				|      argument33: ""
				|      argument34: null
				|      argument35: true
				|      argument36: VALUE
				|      argument37: []
				|      argument38: {}
				|      argument39: ""
				|      argument40: true
				|      argument41: 1.0
				|      argument44: VALUE
				|      argument42: []
				|      argument43: {}
				|      argument45: null
				|      argument46: true
				|      argument47: 1.0
				|      argument48: VALUE
				|      argument49: []
				|      argument50: {}
				|      argument51: true
				|      argument52: VALUE
				|      argument53: 1.0
				|      argument54: 1
				|      argument55: []
				|      argument56: ""
				|      argument57: null
				|      argument58: true
				|      argument59: VALUE
				|      argument60: 1.0
				|      argument61: 1
				|      argument62: []
				|      argument63: ""
				|      argument64: true
				|      argument65: VALUE
				|      argument66: 1.0
				|      argument67: {}
				|      argument68: []
				|      argument69: ""
				|      argument70: null
				|      argument71: true
				|      argument72: VALUE
				|      argument73: 1.0
				|      argument74: {}
				|      argument75: []
				|      argument76: ""
				|      argument77: true
				|      argument78: VALUE
				|      argument79: 1.0
				|      argument80: {}
				|      argument81: ""
				|      argument82: null
				|      argument83: true
				|      argument84: VALUE
				|      argument85: 1.0
				|      argument86: {}
				|      argument87: ""
				|      argument88: true
				|      argument89: VALUE
				|      argument90: 1.0
				|      argument91: {}
				|      argument92: ""
				|      argument93: null
				|      argument94: true
				|      argument95: VALUE
				|      argument96: 1.0
				|      argument97: {}
				|      argument98: ""
				|      argument99: VALUE
				|      argument100: {}
				|      argument101: []
				|      argument102: null
				|      argument103: VALUE
				|      argument104: {}
				|      argument105: []
				|      argument106: true
				|      argument107: VALUE
				|      argument108: 1
				|      argument109: 1.0
				|      argument110: {}
				|      argument111: []
				|      argument112: null
				|      argument113: true
				|      argument114: VALUE
				|      argument115: 1
				|      argument116: 1.0
				|      argument117: {}
				|      argument118: []
				|      argument119: {
				|         field1: VALUE
				|         field2: 1.0
				|         field3: 1
				|         field4: []
				|         field5: {}
				|         field6: ""
				|         field7: null
				|         field8: VALUE
				|         field9: 1.0
				|         field10: 1
				|         field11: []
				|         field12: {}
				|         field13: ""
				|         field14: true
				|         field15: value
				|         field16: 1.0
				|         field17: 1
				|         field18: []
				|         field19: {}
				|         field20: ""
				|         field21: null
				|         field22: value
				|         field23: true
				|         field24: 1.0
				|         field25: 1
				|         field26: []
				|         field27: {}
				|         field28: ""
				|         field29: true
				|         field30: VALUE
				|         field31: []
				|         field32: {}
				|         field33: ""
				|         field34: null
				|         field35: true
				|         field36: VALUE
				|         field37: []
				|         field38: {}
				|         field39: ""
				|         field40: true
				|         field41: 1.0
				|         field44: VALUE
				|         field42: []
				|         field43: {}
				|         field45: null
				|         field46: true
				|         field47: 1.0
				|         field48: VALUE
				|         field49: []
				|         field50: {}
				|         field51: true
				|         field52: VALUE
				|         field53: 1.0
				|         field54: 1
				|         field55: []
				|         field56: ""
				|         field57: null
				|         field58: true
				|         field59: VALUE
				|         field60: 1.0
				|         field61: 1
				|         field62: []
				|         field63: ""
				|         field64: true
				|         field65: VALUE
				|         field66: 1.0
				|         field67: {}
				|         field68: []
				|         field69: ""
				|         field70: null
				|         field71: true
				|         field72: VALUE
				|         field73: 1.0
				|         field74: {}
				|         field75: []
				|         field76: ""
				|         field77: true
				|         field78: VALUE
				|         field79: 1.0
				|         field80: {}
				|         field81: ""
				|         field82: null
				|         field83: true
				|         field84: VALUE
				|         field85: 1.0
				|         field86: {}
				|         field87: ""
				|         field88: true
				|         field89: VALUE
				|         field90: 1.0
				|         field91: {}
				|         field92: ""
				|         field93: null
				|         field94: true
				|         field95: VALUE
				|         field96: 1.0
				|         field97: {}
				|         field98: ""
				|         field99: VALUE
				|         field100: {}
				|         field101: []
				|         field102: null
				|         field103: VALUE
				|         field104: {}
				|         field105: []
				|         field106: true
				|         field107: VALUE
				|         field108: 1
				|         field109: 1.0
				|         field110: {}
				|         field111: []
				|         field112: null
				|         field113: true
				|         field114: VALUE
				|         field115: 1
				|         field116: 1.0
				|         field117: {}
				|         field118: []
				|      }
				|   )
				|}
			""",
			schema = """
				|type Query {
				|   fun(
				|      argument1: Boolean = VALUE
				|      argument2: Boolean = 1.0
				|      argument3: Boolean = 1
				|      argument4: Boolean = []
				|      argument5: Boolean = {}
				|      argument6: Boolean = ""
				|      argument7: Boolean! = null
				|      argument8: Boolean! = VALUE
				|      argument9: Boolean! = 1.0
				|      argument10: Boolean! = 1
				|      argument11: Boolean! = []
				|      argument12: Boolean! = {}
				|      argument13: Boolean! = ""
				|      argument14: Enum = true
				|      argument15: Enum = value
				|      argument16: Enum = 1.0
				|      argument17: Enum = 1
				|      argument18: Enum = []
				|      argument19: Enum = {}
				|      argument20: Enum = ""
				|      argument21: Enum! = null
				|      argument22: Enum! = value
				|      argument23: Enum! = true
				|      argument24: Enum! = 1.0
				|      argument25: Enum! = 1
				|      argument26: Enum! = []
				|      argument27: Enum! = {}
				|      argument28: Enum! = ""
				|      argument29: Float = true
				|      argument30: Float = VALUE
				|      argument31: Float = []
				|      argument32: Float = {}
				|      argument33: Float = ""
				|      argument34: Float! = null
				|      argument35: Float! = true
				|      argument36: Float! = VALUE
				|      argument37: Float! = []
				|      argument38: Float! = {}
				|      argument39: Float! = ""
				|      argument40: ID = true
				|      argument41: ID = 1.0
				|      argument44: ID = VALUE
				|      argument42: ID = []
				|      argument43: ID = {}
				|      argument45: ID! = null
				|      argument46: ID! = true
				|      argument47: ID! = 1.0
				|      argument48: ID! = VALUE
				|      argument49: ID! = []
				|      argument50: ID! = {}
				|      argument51: Input = true
				|      argument52: Input = VALUE
				|      argument53: Input = 1.0
				|      argument54: Input = 1
				|      argument55: Input = []
				|      argument56: Input = ""
				|      argument57: Input! = null
				|      argument58: Input! = true
				|      argument59: Input! = VALUE
				|      argument60: Input! = 1.0
				|      argument61: Input! = 1
				|      argument62: Input! = []
				|      argument63: Input! = ""
				|      argument64: Int = true
				|      argument65: Int = VALUE
				|      argument66: Int = 1.0
				|      argument67: Int = {}
				|      argument68: Int = []
				|      argument69: Int = ""
				|      argument70: Int! = null
				|      argument71: Int! = true
				|      argument72: Int! = VALUE
				|      argument73: Int! = 1.0
				|      argument74: Int! = {}
				|      argument75: Int! = []
				|      argument76: Int! = ""
				|      argument77: [Int] = true
				|      argument78: [Int] = VALUE
				|      argument79: [Int] = 1.0
				|      argument80: [Int] = {}
				|      argument81: [Int] = ""
				|      argument82: [Int]! = null
				|      argument83: [Int]! = true
				|      argument84: [Int]! = VALUE
				|      argument85: [Int]! = 1.0
				|      argument86: [Int]! = {}
				|      argument87: [Int]! = ""
				|      argument88: [Int!] = true
				|      argument89: [Int!] = VALUE
				|      argument90: [Int!] = 1.0
				|      argument91: [Int!] = {}
				|      argument92: [Int!] = ""
				|      argument93: [Int!]! = null
				|      argument94: [Int!]! = true
				|      argument95: [Int!]! = VALUE
				|      argument96: [Int!]! = 1.0
				|      argument97: [Int!]! = {}
				|      argument98: [Int!]! = ""
				|      argument99: Scalar = VALUE
				|      argument100: Scalar = {}
				|      argument101: Scalar = []
				|      argument102: Scalar! = null
				|      argument103: Scalar! = VALUE
				|      argument104: Scalar! = {}
				|      argument105: Scalar! = []
				|      argument106: String = true
				|      argument107: String = VALUE
				|      argument108: String = 1
				|      argument109: String = 1.0
				|      argument110: String = {}
				|      argument111: String = []
				|      argument112: String! = null
				|      argument113: String! = true
				|      argument114: String! = VALUE
				|      argument115: String! = 1
				|      argument116: String! = 1.0
				|      argument117: String! = {}
				|      argument118: String! = []
				|      argument119: Input!
				|   ): String
				|}
				|
				|input Input {
				|   field1: Boolean = VALUE
				|   field2: Boolean = 1.0
				|   field3: Boolean = 1
				|   field4: Boolean = []
				|   field5: Boolean = {}
				|   field6: Boolean = ""
				|   field7: Boolean! = null
				|   field8: Boolean! = VALUE
				|   field9: Boolean! = 1.0
				|   field10: Boolean! = 1
				|   field11: Boolean! = []
				|   field12: Boolean! = {}
				|   field13: Boolean! = ""
				|   field14: Enum = true
				|   field15: Enum = value
				|   field16: Enum = 1.0
				|   field17: Enum = 1
				|   field18: Enum = []
				|   field19: Enum = {}
				|   field20: Enum = ""
				|   field21: Enum! = null
				|   field22: Enum! = value
				|   field23: Enum! = true
				|   field24: Enum! = 1.0
				|   field25: Enum! = 1
				|   field26: Enum! = []
				|   field27: Enum! = {}
				|   field28: Enum! = ""
				|   field29: Float = true
				|   field30: Float = VALUE
				|   field31: Float = []
				|   field32: Float = {}
				|   field33: Float = ""
				|   field34: Float! = null
				|   field35: Float! = true
				|   field36: Float! = VALUE
				|   field37: Float! = []
				|   field38: Float! = {}
				|   field39: Float! = ""
				|   field40: ID = true
				|   field41: ID = 1.0
				|   field44: ID = VALUE
				|   field42: ID = []
				|   field43: ID = {}
				|   field45: ID! = null
				|   field46: ID! = true
				|   field47: ID! = 1.0
				|   field48: ID! = VALUE
				|   field49: ID! = []
				|   field50: ID! = {}
				|   field51: Input = true
				|   field52: Input = VALUE
				|   field53: Input = 1.0
				|   field54: Input = 1
				|   field55: Input = []
				|   field56: Input = ""
				|   field57: Input! = null
				|   field58: Input! = true
				|   field59: Input! = VALUE
				|   field60: Input! = 1.0
				|   field61: Input! = 1
				|   field62: Input! = []
				|   field63: Input! = ""
				|   field64: Int = true
				|   field65: Int = VALUE
				|   field66: Int = 1.0
				|   field67: Int = {}
				|   field68: Int = []
				|   field69: Int = ""
				|   field70: Int! = null
				|   field71: Int! = true
				|   field72: Int! = VALUE
				|   field73: Int! = 1.0
				|   field74: Int! = {}
				|   field75: Int! = []
				|   field76: Int! = ""
				|   field77: [Int] = true
				|   field78: [Int] = VALUE
				|   field79: [Int] = 1.0
				|   field80: [Int] = {}
				|   field81: [Int] = ""
				|   field82: [Int]! = null
				|   field83: [Int]! = true
				|   field84: [Int]! = VALUE
				|   field85: [Int]! = 1.0
				|   field86: [Int]! = {}
				|   field87: [Int]! = ""
				|   field88: [Int!] = true
				|   field89: [Int!] = VALUE
				|   field90: [Int!] = 1.0
				|   field91: [Int!] = {}
				|   field92: [Int!] = ""
				|   field93: [Int!]! = null
				|   field94: [Int!]! = true
				|   field95: [Int!]! = VALUE
				|   field96: [Int!]! = 1.0
				|   field97: [Int!]! = {}
				|   field98: [Int!]! = ""
				|   field99: Scalar = VALUE
				|   field100: Scalar = {}
				|   field101: Scalar = []
				|   field102: Scalar! = null
				|   field103: Scalar! = VALUE
				|   field104: Scalar! = {}
				|   field105: Scalar! = []
				|   field106: String = true
				|   field107: String = VALUE
				|   field108: String = 1
				|   field109: String = 1.0
				|   field110: String = {}
				|   field111: String = []
				|   field112: String! = null
				|   field113: String! = true
				|   field114: String! = VALUE
				|   field115: String! = 1
				|   field116: String! = 1.0
				|   field117: String! = {}
				|   field118: String! = []
				|   field119: String!
				|}
				|
				|enum Enum { VALUE }
				|scalar Scalar
			"""
		)
	}


	@Test
	fun testRejectsValuesOfIncorrectTypeInSchema() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf(
				"""
					Type 'Boolean' does not allow value 'VALUE'.

					<document>:3:28
					2 |    fun(
					3 |       argument1: Boolean = VALUE
					  |                            ^
					4 |       argument2: Boolean = 1.0
				""",
				"""
					Type 'Boolean' does not allow value '1.0'.

					<document>:4:28
					3 |       argument1: Boolean = VALUE
					4 |       argument2: Boolean = 1.0
					  |                            ^
					5 |       argument3: Boolean = 1
				""",
				"""
					Type 'Boolean' does not allow value '1'.

					<document>:5:28
					4 |       argument2: Boolean = 1.0
					5 |       argument3: Boolean = 1
					  |                            ^
					6 |       argument4: Boolean = []
				""",
				"""
					Type 'Boolean' does not allow a list value.

					<document>:6:28
					5 |       argument3: Boolean = 1
					6 |       argument4: Boolean = []
					  |                            ^
					7 |       argument5: Boolean = {}
				""",
				"""
					Type 'Boolean' does not allow an input object value.

					<document>:7:28
					6 |       argument4: Boolean = []
					7 |       argument5: Boolean = {}
					  |                            ^
					8 |       argument6: Boolean = ""
				""",
				"""
					Type 'Boolean' does not allow value '""'.

					<document>:8:28
					7 |       argument5: Boolean = {}
					8 |       argument6: Boolean = ""
					  |                            ^
					9 |       argument7: Boolean! = null
				""",
				"""
					Type 'Boolean!' does not allow value 'null'.

					<document>:9:29
					 8 |       argument6: Boolean = ""
					 9 |       argument7: Boolean! = null
					   |                             ^
					10 |       argument8: Boolean! = VALUE
				""",
				"""
					Type 'Boolean!' does not allow value 'VALUE'.

					<document>:10:29
					 9 |       argument7: Boolean! = null
					10 |       argument8: Boolean! = VALUE
					   |                             ^
					11 |       argument9: Boolean! = 1.0
				""",
				"""
					Type 'Boolean!' does not allow value '1.0'.

					<document>:11:29
					10 |       argument8: Boolean! = VALUE
					11 |       argument9: Boolean! = 1.0
					   |                             ^
					12 |       argument10: Boolean! = 1
				""",
				"""
					Type 'Boolean!' does not allow value '1'.

					<document>:12:30
					11 |       argument9: Boolean! = 1.0
					12 |       argument10: Boolean! = 1
					   |                              ^
					13 |       argument11: Boolean! = []
				""",
				"""
					Type 'Boolean!' does not allow a list value.

					<document>:13:30
					12 |       argument10: Boolean! = 1
					13 |       argument11: Boolean! = []
					   |                              ^
					14 |       argument12: Boolean! = {}
				""",
				"""
					Type 'Boolean!' does not allow an input object value.

					<document>:14:30
					13 |       argument11: Boolean! = []
					14 |       argument12: Boolean! = {}
					   |                              ^
					15 |       argument13: Boolean! = ""
				""",
				"""
					Type 'Boolean!' does not allow value '""'.

					<document>:15:30
					14 |       argument12: Boolean! = {}
					15 |       argument13: Boolean! = ""
					   |                              ^
					16 |       argument14: Enum = true
				""",
				"""
					Type 'Enum' does not allow value 'true'.

					<document>:16:26
					15 |       argument13: Boolean! = ""
					16 |       argument14: Enum = true
					   |                          ^
					17 |       argument15: Enum = value
				""",
				"""
					Type 'Enum' does not allow value 'value'.

					<document>:17:26
					16 |       argument14: Enum = true
					17 |       argument15: Enum = value
					   |                          ^
					18 |       argument16: Enum = 1.0
				""",
				"""
					Type 'Enum' does not allow value '1.0'.

					<document>:18:26
					17 |       argument15: Enum = value
					18 |       argument16: Enum = 1.0
					   |                          ^
					19 |       argument17: Enum = 1
				""",
				"""
					Type 'Enum' does not allow value '1'.

					<document>:19:26
					18 |       argument16: Enum = 1.0
					19 |       argument17: Enum = 1
					   |                          ^
					20 |       argument18: Enum = []
				""",
				"""
					Type 'Enum' does not allow a list value.

					<document>:20:26
					19 |       argument17: Enum = 1
					20 |       argument18: Enum = []
					   |                          ^
					21 |       argument19: Enum = {}
				""",
				"""
					Type 'Enum' does not allow an input object value.

					<document>:21:26
					20 |       argument18: Enum = []
					21 |       argument19: Enum = {}
					   |                          ^
					22 |       argument20: Enum = ""
				""",
				"""
					Type 'Enum' does not allow value '""'.

					<document>:22:26
					21 |       argument19: Enum = {}
					22 |       argument20: Enum = ""
					   |                          ^
					23 |       argument21: Enum! = null
				""",
				"""
					Type 'Enum!' does not allow value 'null'.

					<document>:23:27
					22 |       argument20: Enum = ""
					23 |       argument21: Enum! = null
					   |                           ^
					24 |       argument22: Enum! = value
				""",
				"""
					Type 'Enum!' does not allow value 'value'.

					<document>:24:27
					23 |       argument21: Enum! = null
					24 |       argument22: Enum! = value
					   |                           ^
					25 |       argument23: Enum! = true
				""",
				"""
					Type 'Enum!' does not allow value 'true'.

					<document>:25:27
					24 |       argument22: Enum! = value
					25 |       argument23: Enum! = true
					   |                           ^
					26 |       argument24: Enum! = 1.0
				""",
				"""
					Type 'Enum!' does not allow value '1.0'.

					<document>:26:27
					25 |       argument23: Enum! = true
					26 |       argument24: Enum! = 1.0
					   |                           ^
					27 |       argument25: Enum! = 1
				""",
				"""
					Type 'Enum!' does not allow value '1'.

					<document>:27:27
					26 |       argument24: Enum! = 1.0
					27 |       argument25: Enum! = 1
					   |                           ^
					28 |       argument26: Enum! = []
				""",
				"""
					Type 'Enum!' does not allow a list value.

					<document>:28:27
					27 |       argument25: Enum! = 1
					28 |       argument26: Enum! = []
					   |                           ^
					29 |       argument27: Enum! = {}
				""",
				"""
					Type 'Enum!' does not allow an input object value.

					<document>:29:27
					28 |       argument26: Enum! = []
					29 |       argument27: Enum! = {}
					   |                           ^
					30 |       argument28: Enum! = ""
				""",
				"""
					Type 'Enum!' does not allow value '""'.

					<document>:30:27
					29 |       argument27: Enum! = {}
					30 |       argument28: Enum! = ""
					   |                           ^
					31 |       argument29: Float = true
				""",
				"""
					Type 'Float' does not allow value 'true'.

					<document>:31:27
					30 |       argument28: Enum! = ""
					31 |       argument29: Float = true
					   |                           ^
					32 |       argument30: Float = VALUE
				""",
				"""
					Type 'Float' does not allow value 'VALUE'.

					<document>:32:27
					31 |       argument29: Float = true
					32 |       argument30: Float = VALUE
					   |                           ^
					33 |       argument31: Float = []
				""",
				"""
					Type 'Float' does not allow a list value.

					<document>:33:27
					32 |       argument30: Float = VALUE
					33 |       argument31: Float = []
					   |                           ^
					34 |       argument32: Float = {}
				""",
				"""
					Type 'Float' does not allow an input object value.

					<document>:34:27
					33 |       argument31: Float = []
					34 |       argument32: Float = {}
					   |                           ^
					35 |       argument33: Float = ""
				""",
				"""
					Type 'Float' does not allow value '""'.

					<document>:35:27
					34 |       argument32: Float = {}
					35 |       argument33: Float = ""
					   |                           ^
					36 |       argument34: Float! = null
				""",
				"""
					Type 'Float!' does not allow value 'null'.

					<document>:36:28
					35 |       argument33: Float = ""
					36 |       argument34: Float! = null
					   |                            ^
					37 |       argument35: Float! = true
				""",
				"""
					Type 'Float!' does not allow value 'true'.

					<document>:37:28
					36 |       argument34: Float! = null
					37 |       argument35: Float! = true
					   |                            ^
					38 |       argument36: Float! = VALUE
				""",
				"""
					Type 'Float!' does not allow value 'VALUE'.

					<document>:38:28
					37 |       argument35: Float! = true
					38 |       argument36: Float! = VALUE
					   |                            ^
					39 |       argument37: Float! = []
				""",
				"""
					Type 'Float!' does not allow a list value.

					<document>:39:28
					38 |       argument36: Float! = VALUE
					39 |       argument37: Float! = []
					   |                            ^
					40 |       argument38: Float! = {}
				""",
				"""
					Type 'Float!' does not allow an input object value.

					<document>:40:28
					39 |       argument37: Float! = []
					40 |       argument38: Float! = {}
					   |                            ^
					41 |       argument39: Float! = ""
				""",
				"""
					Type 'Float!' does not allow value '""'.

					<document>:41:28
					40 |       argument38: Float! = {}
					41 |       argument39: Float! = ""
					   |                            ^
					42 |       argument40: ID = true
				""",
				"""
					Type 'ID' does not allow value 'true'.

					<document>:42:24
					41 |       argument39: Float! = ""
					42 |       argument40: ID = true
					   |                        ^
					43 |       argument41: ID = 1.0
				""",
				"""
					Type 'ID' does not allow value '1.0'.

					<document>:43:24
					42 |       argument40: ID = true
					43 |       argument41: ID = 1.0
					   |                        ^
					44 |       argument44: ID = VALUE
				""",
				"""
					Type 'ID' does not allow value 'VALUE'.

					<document>:44:24
					43 |       argument41: ID = 1.0
					44 |       argument44: ID = VALUE
					   |                        ^
					45 |       argument42: ID = []
				""",
				"""
					Type 'ID' does not allow a list value.

					<document>:45:24
					44 |       argument44: ID = VALUE
					45 |       argument42: ID = []
					   |                        ^
					46 |       argument43: ID = {}
				""",
				"""
					Type 'ID' does not allow an input object value.

					<document>:46:24
					45 |       argument42: ID = []
					46 |       argument43: ID = {}
					   |                        ^
					47 |       argument45: ID! = null
				""",
				"""
					Type 'ID!' does not allow value 'null'.

					<document>:47:25
					46 |       argument43: ID = {}
					47 |       argument45: ID! = null
					   |                         ^
					48 |       argument46: ID! = true
				""",
				"""
					Type 'ID!' does not allow value 'true'.

					<document>:48:25
					47 |       argument45: ID! = null
					48 |       argument46: ID! = true
					   |                         ^
					49 |       argument47: ID! = 1.0
				""",
				"""
					Type 'ID!' does not allow value '1.0'.

					<document>:49:25
					48 |       argument46: ID! = true
					49 |       argument47: ID! = 1.0
					   |                         ^
					50 |       argument48: ID! = VALUE
				""",
				"""
					Type 'ID!' does not allow value 'VALUE'.

					<document>:50:25
					49 |       argument47: ID! = 1.0
					50 |       argument48: ID! = VALUE
					   |                         ^
					51 |       argument49: ID! = []
				""",
				"""
					Type 'ID!' does not allow a list value.

					<document>:51:25
					50 |       argument48: ID! = VALUE
					51 |       argument49: ID! = []
					   |                         ^
					52 |       argument50: ID! = {}
				""",
				"""
					Type 'ID!' does not allow an input object value.

					<document>:52:25
					51 |       argument49: ID! = []
					52 |       argument50: ID! = {}
					   |                         ^
					53 |       argument51: Input = true
				""",
				"""
					Type 'Input' does not allow value 'true'.

					<document>:53:27
					52 |       argument50: ID! = {}
					53 |       argument51: Input = true
					   |                           ^
					54 |       argument52: Input = VALUE
				""",
				"""
					Type 'Input' does not allow value 'VALUE'.

					<document>:54:27
					53 |       argument51: Input = true
					54 |       argument52: Input = VALUE
					   |                           ^
					55 |       argument53: Input = 1.0
				""",
				"""
					Type 'Input' does not allow value '1.0'.

					<document>:55:27
					54 |       argument52: Input = VALUE
					55 |       argument53: Input = 1.0
					   |                           ^
					56 |       argument54: Input = 1
				""",
				"""
					Type 'Input' does not allow value '1'.

					<document>:56:27
					55 |       argument53: Input = 1.0
					56 |       argument54: Input = 1
					   |                           ^
					57 |       argument55: Input = []
				""",
				"""
					Type 'Input' does not allow a list value.

					<document>:57:27
					56 |       argument54: Input = 1
					57 |       argument55: Input = []
					   |                           ^
					58 |       argument56: Input = ""
				""",
				"""
					Type 'Input' does not allow value '""'.

					<document>:58:27
					57 |       argument55: Input = []
					58 |       argument56: Input = ""
					   |                           ^
					59 |       argument57: Input! = null
				""",
				"""
					Type 'Input!' does not allow value 'null'.

					<document>:59:28
					58 |       argument56: Input = ""
					59 |       argument57: Input! = null
					   |                            ^
					60 |       argument58: Input! = true
				""",
				"""
					Type 'Input!' does not allow value 'true'.

					<document>:60:28
					59 |       argument57: Input! = null
					60 |       argument58: Input! = true
					   |                            ^
					61 |       argument59: Input! = VALUE
				""",
				"""
					Type 'Input!' does not allow value 'VALUE'.

					<document>:61:28
					60 |       argument58: Input! = true
					61 |       argument59: Input! = VALUE
					   |                            ^
					62 |       argument60: Input! = 1.0
				""",
				"""
					Type 'Input!' does not allow value '1.0'.

					<document>:62:28
					61 |       argument59: Input! = VALUE
					62 |       argument60: Input! = 1.0
					   |                            ^
					63 |       argument61: Input! = 1
				""",
				"""
					Type 'Input!' does not allow value '1'.

					<document>:63:28
					62 |       argument60: Input! = 1.0
					63 |       argument61: Input! = 1
					   |                            ^
					64 |       argument62: Input! = []
				""",
				"""
					Type 'Input!' does not allow a list value.

					<document>:64:28
					63 |       argument61: Input! = 1
					64 |       argument62: Input! = []
					   |                            ^
					65 |       argument63: Input! = ""
				""",
				"""
					Type 'Input!' does not allow value '""'.

					<document>:65:28
					64 |       argument62: Input! = []
					65 |       argument63: Input! = ""
					   |                            ^
					66 |       argument64: Int = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:66:25
					65 |       argument63: Input! = ""
					66 |       argument64: Int = true
					   |                         ^
					67 |       argument65: Int = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:67:25
					66 |       argument64: Int = true
					67 |       argument65: Int = VALUE
					   |                         ^
					68 |       argument66: Int = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:68:25
					67 |       argument65: Int = VALUE
					68 |       argument66: Int = 1.0
					   |                         ^
					69 |       argument67: Int = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:69:25
					68 |       argument66: Int = 1.0
					69 |       argument67: Int = {}
					   |                         ^
					70 |       argument68: Int = []
				""",
				"""
					Type 'Int' does not allow a list value.

					<document>:70:25
					69 |       argument67: Int = {}
					70 |       argument68: Int = []
					   |                         ^
					71 |       argument69: Int = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:71:25
					70 |       argument68: Int = []
					71 |       argument69: Int = ""
					   |                         ^
					72 |       argument70: Int! = null
				""",
				"""
					Type 'Int!' does not allow value 'null'.

					<document>:72:26
					71 |       argument69: Int = ""
					72 |       argument70: Int! = null
					   |                          ^
					73 |       argument71: Int! = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:73:26
					72 |       argument70: Int! = null
					73 |       argument71: Int! = true
					   |                          ^
					74 |       argument72: Int! = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:74:26
					73 |       argument71: Int! = true
					74 |       argument72: Int! = VALUE
					   |                          ^
					75 |       argument73: Int! = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:75:26
					74 |       argument72: Int! = VALUE
					75 |       argument73: Int! = 1.0
					   |                          ^
					76 |       argument74: Int! = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:76:26
					75 |       argument73: Int! = 1.0
					76 |       argument74: Int! = {}
					   |                          ^
					77 |       argument75: Int! = []
				""",
				"""
					Type 'Int!' does not allow a list value.

					<document>:77:26
					76 |       argument74: Int! = {}
					77 |       argument75: Int! = []
					   |                          ^
					78 |       argument76: Int! = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:78:26
					77 |       argument75: Int! = []
					78 |       argument76: Int! = ""
					   |                          ^
					79 |       argument77: [Int] = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:79:27
					78 |       argument76: Int! = ""
					79 |       argument77: [Int] = true
					   |                           ^
					80 |       argument78: [Int] = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:80:27
					79 |       argument77: [Int] = true
					80 |       argument78: [Int] = VALUE
					   |                           ^
					81 |       argument79: [Int] = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:81:27
					80 |       argument78: [Int] = VALUE
					81 |       argument79: [Int] = 1.0
					   |                           ^
					82 |       argument80: [Int] = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:82:27
					81 |       argument79: [Int] = 1.0
					82 |       argument80: [Int] = {}
					   |                           ^
					83 |       argument81: [Int] = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:83:27
					82 |       argument80: [Int] = {}
					83 |       argument81: [Int] = ""
					   |                           ^
					84 |       argument82: [Int]! = null
				""",
				"""
					Type '[Int]!' does not allow value 'null'.

					<document>:84:28
					83 |       argument81: [Int] = ""
					84 |       argument82: [Int]! = null
					   |                            ^
					85 |       argument83: [Int]! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:85:28
					84 |       argument82: [Int]! = null
					85 |       argument83: [Int]! = true
					   |                            ^
					86 |       argument84: [Int]! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:86:28
					85 |       argument83: [Int]! = true
					86 |       argument84: [Int]! = VALUE
					   |                            ^
					87 |       argument85: [Int]! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:87:28
					86 |       argument84: [Int]! = VALUE
					87 |       argument85: [Int]! = 1.0
					   |                            ^
					88 |       argument86: [Int]! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:88:28
					87 |       argument85: [Int]! = 1.0
					88 |       argument86: [Int]! = {}
					   |                            ^
					89 |       argument87: [Int]! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:89:28
					88 |       argument86: [Int]! = {}
					89 |       argument87: [Int]! = ""
					   |                            ^
					90 |       argument88: [Int!] = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:90:28
					89 |       argument87: [Int]! = ""
					90 |       argument88: [Int!] = true
					   |                            ^
					91 |       argument89: [Int!] = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:91:28
					90 |       argument88: [Int!] = true
					91 |       argument89: [Int!] = VALUE
					   |                            ^
					92 |       argument90: [Int!] = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:92:28
					91 |       argument89: [Int!] = VALUE
					92 |       argument90: [Int!] = 1.0
					   |                            ^
					93 |       argument91: [Int!] = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:93:28
					92 |       argument90: [Int!] = 1.0
					93 |       argument91: [Int!] = {}
					   |                            ^
					94 |       argument92: [Int!] = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:94:28
					93 |       argument91: [Int!] = {}
					94 |       argument92: [Int!] = ""
					   |                            ^
					95 |       argument93: [Int!]! = null
				""",
				"""
					Type '[Int!]!' does not allow value 'null'.

					<document>:95:29
					94 |       argument92: [Int!] = ""
					95 |       argument93: [Int!]! = null
					   |                             ^
					96 |       argument94: [Int!]! = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:96:29
					95 |       argument93: [Int!]! = null
					96 |       argument94: [Int!]! = true
					   |                             ^
					97 |       argument95: [Int!]! = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:97:29
					96 |       argument94: [Int!]! = true
					97 |       argument95: [Int!]! = VALUE
					   |                             ^
					98 |       argument96: [Int!]! = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:98:29
					97 |       argument95: [Int!]! = VALUE
					98 |       argument96: [Int!]! = 1.0
					   |                             ^
					99 |       argument97: [Int!]! = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:99:29
					 98 |       argument96: [Int!]! = 1.0
					 99 |       argument97: [Int!]! = {}
					    |                             ^
					100 |       argument98: [Int!]! = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:100:29
					 99 |       argument97: [Int!]! = {}
					100 |       argument98: [Int!]! = ""
					    |                             ^
					101 |       argument99: Scalar = VALUE
				""",
				"""
					Type 'Scalar!' does not allow value 'null'.

					<document>:104:30
					103 |       argument101: Scalar = []
					104 |       argument102: Scalar! = null
					    |                              ^
					105 |       argument103: Scalar! = VALUE
				""",
				"""
					Type 'String' does not allow value 'true'.

					<document>:108:29
					107 |       argument105: Scalar! = []
					108 |       argument106: String = true
					    |                             ^
					109 |       argument107: String = VALUE
				""",
				"""
					Type 'String' does not allow value 'VALUE'.

					<document>:109:29
					108 |       argument106: String = true
					109 |       argument107: String = VALUE
					    |                             ^
					110 |       argument108: String = 1
				""",
				"""
					Type 'String' does not allow value '1'.

					<document>:110:29
					109 |       argument107: String = VALUE
					110 |       argument108: String = 1
					    |                             ^
					111 |       argument109: String = 1.0
				""",
				"""
					Type 'String' does not allow value '1.0'.

					<document>:111:29
					110 |       argument108: String = 1
					111 |       argument109: String = 1.0
					    |                             ^
					112 |       argument110: String = {}
				""",
				"""
					Type 'String' does not allow an input object value.

					<document>:112:29
					111 |       argument109: String = 1.0
					112 |       argument110: String = {}
					    |                             ^
					113 |       argument111: String = []
				""",
				"""
					Type 'String' does not allow a list value.

					<document>:113:29
					112 |       argument110: String = {}
					113 |       argument111: String = []
					    |                             ^
					114 |       argument112: String! = null
				""",
				"""
					Type 'String!' does not allow value 'null'.

					<document>:114:30
					113 |       argument111: String = []
					114 |       argument112: String! = null
					    |                              ^
					115 |       argument113: String! = true
				""",
				"""
					Type 'String!' does not allow value 'true'.

					<document>:115:30
					114 |       argument112: String! = null
					115 |       argument113: String! = true
					    |                              ^
					116 |       argument114: String! = VALUE
				""",
				"""
					Type 'String!' does not allow value 'VALUE'.

					<document>:116:30
					115 |       argument113: String! = true
					116 |       argument114: String! = VALUE
					    |                              ^
					117 |       argument115: String! = 1
				""",
				"""
					Type 'String!' does not allow value '1'.

					<document>:117:30
					116 |       argument114: String! = VALUE
					117 |       argument115: String! = 1
					    |                              ^
					118 |       argument116: String! = 1.0
				""",
				"""
					Type 'String!' does not allow value '1.0'.

					<document>:118:30
					117 |       argument115: String! = 1
					118 |       argument116: String! = 1.0
					    |                              ^
					119 |       argument117: String! = {}
				""",
				"""
					Type 'String!' does not allow an input object value.

					<document>:119:30
					118 |       argument116: String! = 1.0
					119 |       argument117: String! = {}
					    |                              ^
					120 |       argument118: String! = []
				""",
				"""
					Type 'String!' does not allow a list value.

					<document>:120:30
					119 |       argument117: String! = {}
					120 |       argument118: String! = []
					    |                              ^
					121 |       argument119: Input!
				""",
				"""
					Type 'Boolean' does not allow value 'VALUE'.

					<document>:126:22
					125 | input Input {
					126 |    field1: Boolean = VALUE
					    |                      ^
					127 |    field2: Boolean = 1.0
				""",
				"""
					Type 'Boolean' does not allow value '1.0'.

					<document>:127:22
					126 |    field1: Boolean = VALUE
					127 |    field2: Boolean = 1.0
					    |                      ^
					128 |    field3: Boolean = 1
				""",
				"""
					Type 'Boolean' does not allow value '1'.

					<document>:128:22
					127 |    field2: Boolean = 1.0
					128 |    field3: Boolean = 1
					    |                      ^
					129 |    field4: Boolean = []
				""",
				"""
					Type 'Boolean' does not allow a list value.

					<document>:129:22
					128 |    field3: Boolean = 1
					129 |    field4: Boolean = []
					    |                      ^
					130 |    field5: Boolean = {}
				""",
				"""
					Type 'Boolean' does not allow an input object value.

					<document>:130:22
					129 |    field4: Boolean = []
					130 |    field5: Boolean = {}
					    |                      ^
					131 |    field6: Boolean = ""
				""",
				"""
					Type 'Boolean' does not allow value '""'.

					<document>:131:22
					130 |    field5: Boolean = {}
					131 |    field6: Boolean = ""
					    |                      ^
					132 |    field7: Boolean! = null
				""",
				"""
					Type 'Boolean!' does not allow value 'null'.

					<document>:132:23
					131 |    field6: Boolean = ""
					132 |    field7: Boolean! = null
					    |                       ^
					133 |    field8: Boolean! = VALUE
				""",
				"""
					Type 'Boolean!' does not allow value 'VALUE'.

					<document>:133:23
					132 |    field7: Boolean! = null
					133 |    field8: Boolean! = VALUE
					    |                       ^
					134 |    field9: Boolean! = 1.0
				""",
				"""
					Type 'Boolean!' does not allow value '1.0'.

					<document>:134:23
					133 |    field8: Boolean! = VALUE
					134 |    field9: Boolean! = 1.0
					    |                       ^
					135 |    field10: Boolean! = 1
				""",
				"""
					Type 'Boolean!' does not allow value '1'.

					<document>:135:24
					134 |    field9: Boolean! = 1.0
					135 |    field10: Boolean! = 1
					    |                        ^
					136 |    field11: Boolean! = []
				""",
				"""
					Type 'Boolean!' does not allow a list value.

					<document>:136:24
					135 |    field10: Boolean! = 1
					136 |    field11: Boolean! = []
					    |                        ^
					137 |    field12: Boolean! = {}
				""",
				"""
					Type 'Boolean!' does not allow an input object value.

					<document>:137:24
					136 |    field11: Boolean! = []
					137 |    field12: Boolean! = {}
					    |                        ^
					138 |    field13: Boolean! = ""
				""",
				"""
					Type 'Boolean!' does not allow value '""'.

					<document>:138:24
					137 |    field12: Boolean! = {}
					138 |    field13: Boolean! = ""
					    |                        ^
					139 |    field14: Enum = true
				""",
				"""
					Type 'Enum' does not allow value 'true'.

					<document>:139:20
					138 |    field13: Boolean! = ""
					139 |    field14: Enum = true
					    |                    ^
					140 |    field15: Enum = value
				""",
				"""
					Type 'Enum' does not allow value 'value'.

					<document>:140:20
					139 |    field14: Enum = true
					140 |    field15: Enum = value
					    |                    ^
					141 |    field16: Enum = 1.0
				""",
				"""
					Type 'Enum' does not allow value '1.0'.

					<document>:141:20
					140 |    field15: Enum = value
					141 |    field16: Enum = 1.0
					    |                    ^
					142 |    field17: Enum = 1
				""",
				"""
					Type 'Enum' does not allow value '1'.

					<document>:142:20
					141 |    field16: Enum = 1.0
					142 |    field17: Enum = 1
					    |                    ^
					143 |    field18: Enum = []
				""",
				"""
					Type 'Enum' does not allow a list value.

					<document>:143:20
					142 |    field17: Enum = 1
					143 |    field18: Enum = []
					    |                    ^
					144 |    field19: Enum = {}
				""",
				"""
					Type 'Enum' does not allow an input object value.

					<document>:144:20
					143 |    field18: Enum = []
					144 |    field19: Enum = {}
					    |                    ^
					145 |    field20: Enum = ""
				""",
				"""
					Type 'Enum' does not allow value '""'.

					<document>:145:20
					144 |    field19: Enum = {}
					145 |    field20: Enum = ""
					    |                    ^
					146 |    field21: Enum! = null
				""",
				"""
					Type 'Enum!' does not allow value 'null'.

					<document>:146:21
					145 |    field20: Enum = ""
					146 |    field21: Enum! = null
					    |                     ^
					147 |    field22: Enum! = value
				""",
				"""
					Type 'Enum!' does not allow value 'value'.

					<document>:147:21
					146 |    field21: Enum! = null
					147 |    field22: Enum! = value
					    |                     ^
					148 |    field23: Enum! = true
				""",
				"""
					Type 'Enum!' does not allow value 'true'.

					<document>:148:21
					147 |    field22: Enum! = value
					148 |    field23: Enum! = true
					    |                     ^
					149 |    field24: Enum! = 1.0
				""",
				"""
					Type 'Enum!' does not allow value '1.0'.

					<document>:149:21
					148 |    field23: Enum! = true
					149 |    field24: Enum! = 1.0
					    |                     ^
					150 |    field25: Enum! = 1
				""",
				"""
					Type 'Enum!' does not allow value '1'.

					<document>:150:21
					149 |    field24: Enum! = 1.0
					150 |    field25: Enum! = 1
					    |                     ^
					151 |    field26: Enum! = []
				""",
				"""
					Type 'Enum!' does not allow a list value.

					<document>:151:21
					150 |    field25: Enum! = 1
					151 |    field26: Enum! = []
					    |                     ^
					152 |    field27: Enum! = {}
				""",
				"""
					Type 'Enum!' does not allow an input object value.

					<document>:152:21
					151 |    field26: Enum! = []
					152 |    field27: Enum! = {}
					    |                     ^
					153 |    field28: Enum! = ""
				""",
				"""
					Type 'Enum!' does not allow value '""'.

					<document>:153:21
					152 |    field27: Enum! = {}
					153 |    field28: Enum! = ""
					    |                     ^
					154 |    field29: Float = true
				""",
				"""
					Type 'Float' does not allow value 'true'.

					<document>:154:21
					153 |    field28: Enum! = ""
					154 |    field29: Float = true
					    |                     ^
					155 |    field30: Float = VALUE
				""",
				"""
					Type 'Float' does not allow value 'VALUE'.

					<document>:155:21
					154 |    field29: Float = true
					155 |    field30: Float = VALUE
					    |                     ^
					156 |    field31: Float = []
				""",
				"""
					Type 'Float' does not allow a list value.

					<document>:156:21
					155 |    field30: Float = VALUE
					156 |    field31: Float = []
					    |                     ^
					157 |    field32: Float = {}
				""",
				"""
					Type 'Float' does not allow an input object value.

					<document>:157:21
					156 |    field31: Float = []
					157 |    field32: Float = {}
					    |                     ^
					158 |    field33: Float = ""
				""",
				"""
					Type 'Float' does not allow value '""'.

					<document>:158:21
					157 |    field32: Float = {}
					158 |    field33: Float = ""
					    |                     ^
					159 |    field34: Float! = null
				""",
				"""
					Type 'Float!' does not allow value 'null'.

					<document>:159:22
					158 |    field33: Float = ""
					159 |    field34: Float! = null
					    |                      ^
					160 |    field35: Float! = true
				""",
				"""
					Type 'Float!' does not allow value 'true'.

					<document>:160:22
					159 |    field34: Float! = null
					160 |    field35: Float! = true
					    |                      ^
					161 |    field36: Float! = VALUE
				""",
				"""
					Type 'Float!' does not allow value 'VALUE'.

					<document>:161:22
					160 |    field35: Float! = true
					161 |    field36: Float! = VALUE
					    |                      ^
					162 |    field37: Float! = []
				""",
				"""
					Type 'Float!' does not allow a list value.

					<document>:162:22
					161 |    field36: Float! = VALUE
					162 |    field37: Float! = []
					    |                      ^
					163 |    field38: Float! = {}
				""",
				"""
					Type 'Float!' does not allow an input object value.

					<document>:163:22
					162 |    field37: Float! = []
					163 |    field38: Float! = {}
					    |                      ^
					164 |    field39: Float! = ""
				""",
				"""
					Type 'Float!' does not allow value '""'.

					<document>:164:22
					163 |    field38: Float! = {}
					164 |    field39: Float! = ""
					    |                      ^
					165 |    field40: ID = true
				""",
				"""
					Type 'ID' does not allow value 'true'.

					<document>:165:18
					164 |    field39: Float! = ""
					165 |    field40: ID = true
					    |                  ^
					166 |    field41: ID = 1.0
				""",
				"""
					Type 'ID' does not allow value '1.0'.

					<document>:166:18
					165 |    field40: ID = true
					166 |    field41: ID = 1.0
					    |                  ^
					167 |    field44: ID = VALUE
				""",
				"""
					Type 'ID' does not allow value 'VALUE'.

					<document>:167:18
					166 |    field41: ID = 1.0
					167 |    field44: ID = VALUE
					    |                  ^
					168 |    field42: ID = []
				""",
				"""
					Type 'ID' does not allow a list value.

					<document>:168:18
					167 |    field44: ID = VALUE
					168 |    field42: ID = []
					    |                  ^
					169 |    field43: ID = {}
				""",
				"""
					Type 'ID' does not allow an input object value.

					<document>:169:18
					168 |    field42: ID = []
					169 |    field43: ID = {}
					    |                  ^
					170 |    field45: ID! = null
				""",
				"""
					Type 'ID!' does not allow value 'null'.

					<document>:170:19
					169 |    field43: ID = {}
					170 |    field45: ID! = null
					    |                   ^
					171 |    field46: ID! = true
				""",
				"""
					Type 'ID!' does not allow value 'true'.

					<document>:171:19
					170 |    field45: ID! = null
					171 |    field46: ID! = true
					    |                   ^
					172 |    field47: ID! = 1.0
				""",
				"""
					Type 'ID!' does not allow value '1.0'.

					<document>:172:19
					171 |    field46: ID! = true
					172 |    field47: ID! = 1.0
					    |                   ^
					173 |    field48: ID! = VALUE
				""",
				"""
					Type 'ID!' does not allow value 'VALUE'.

					<document>:173:19
					172 |    field47: ID! = 1.0
					173 |    field48: ID! = VALUE
					    |                   ^
					174 |    field49: ID! = []
				""",
				"""
					Type 'ID!' does not allow a list value.

					<document>:174:19
					173 |    field48: ID! = VALUE
					174 |    field49: ID! = []
					    |                   ^
					175 |    field50: ID! = {}
				""",
				"""
					Type 'ID!' does not allow an input object value.

					<document>:175:19
					174 |    field49: ID! = []
					175 |    field50: ID! = {}
					    |                   ^
					176 |    field51: Input = true
				""",
				"""
					Type 'Input' does not allow value 'true'.

					<document>:176:21
					175 |    field50: ID! = {}
					176 |    field51: Input = true
					    |                     ^
					177 |    field52: Input = VALUE
				""",
				"""
					Type 'Input' does not allow value 'VALUE'.

					<document>:177:21
					176 |    field51: Input = true
					177 |    field52: Input = VALUE
					    |                     ^
					178 |    field53: Input = 1.0
				""",
				"""
					Type 'Input' does not allow value '1.0'.

					<document>:178:21
					177 |    field52: Input = VALUE
					178 |    field53: Input = 1.0
					    |                     ^
					179 |    field54: Input = 1
				""",
				"""
					Type 'Input' does not allow value '1'.

					<document>:179:21
					178 |    field53: Input = 1.0
					179 |    field54: Input = 1
					    |                     ^
					180 |    field55: Input = []
				""",
				"""
					Type 'Input' does not allow a list value.

					<document>:180:21
					179 |    field54: Input = 1
					180 |    field55: Input = []
					    |                     ^
					181 |    field56: Input = ""
				""",
				"""
					Type 'Input' does not allow value '""'.

					<document>:181:21
					180 |    field55: Input = []
					181 |    field56: Input = ""
					    |                     ^
					182 |    field57: Input! = null
				""",
				"""
					Type 'Input!' does not allow value 'null'.

					<document>:182:22
					181 |    field56: Input = ""
					182 |    field57: Input! = null
					    |                      ^
					183 |    field58: Input! = true
				""",
				"""
					Type 'Input!' does not allow value 'true'.

					<document>:183:22
					182 |    field57: Input! = null
					183 |    field58: Input! = true
					    |                      ^
					184 |    field59: Input! = VALUE
				""",
				"""
					Type 'Input!' does not allow value 'VALUE'.

					<document>:184:22
					183 |    field58: Input! = true
					184 |    field59: Input! = VALUE
					    |                      ^
					185 |    field60: Input! = 1.0
				""",
				"""
					Type 'Input!' does not allow value '1.0'.

					<document>:185:22
					184 |    field59: Input! = VALUE
					185 |    field60: Input! = 1.0
					    |                      ^
					186 |    field61: Input! = 1
				""",
				"""
					Type 'Input!' does not allow value '1'.

					<document>:186:22
					185 |    field60: Input! = 1.0
					186 |    field61: Input! = 1
					    |                      ^
					187 |    field62: Input! = []
				""",
				"""
					Type 'Input!' does not allow a list value.

					<document>:187:22
					186 |    field61: Input! = 1
					187 |    field62: Input! = []
					    |                      ^
					188 |    field63: Input! = ""
				""",
				"""
					Type 'Input!' does not allow value '""'.

					<document>:188:22
					187 |    field62: Input! = []
					188 |    field63: Input! = ""
					    |                      ^
					189 |    field64: Int = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:189:19
					188 |    field63: Input! = ""
					189 |    field64: Int = true
					    |                   ^
					190 |    field65: Int = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:190:19
					189 |    field64: Int = true
					190 |    field65: Int = VALUE
					    |                   ^
					191 |    field66: Int = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:191:19
					190 |    field65: Int = VALUE
					191 |    field66: Int = 1.0
					    |                   ^
					192 |    field67: Int = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:192:19
					191 |    field66: Int = 1.0
					192 |    field67: Int = {}
					    |                   ^
					193 |    field68: Int = []
				""",
				"""
					Type 'Int' does not allow a list value.

					<document>:193:19
					192 |    field67: Int = {}
					193 |    field68: Int = []
					    |                   ^
					194 |    field69: Int = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:194:19
					193 |    field68: Int = []
					194 |    field69: Int = ""
					    |                   ^
					195 |    field70: Int! = null
				""",
				"""
					Type 'Int!' does not allow value 'null'.

					<document>:195:20
					194 |    field69: Int = ""
					195 |    field70: Int! = null
					    |                    ^
					196 |    field71: Int! = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:196:20
					195 |    field70: Int! = null
					196 |    field71: Int! = true
					    |                    ^
					197 |    field72: Int! = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:197:20
					196 |    field71: Int! = true
					197 |    field72: Int! = VALUE
					    |                    ^
					198 |    field73: Int! = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:198:20
					197 |    field72: Int! = VALUE
					198 |    field73: Int! = 1.0
					    |                    ^
					199 |    field74: Int! = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:199:20
					198 |    field73: Int! = 1.0
					199 |    field74: Int! = {}
					    |                    ^
					200 |    field75: Int! = []
				""",
				"""
					Type 'Int!' does not allow a list value.

					<document>:200:20
					199 |    field74: Int! = {}
					200 |    field75: Int! = []
					    |                    ^
					201 |    field76: Int! = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:201:20
					200 |    field75: Int! = []
					201 |    field76: Int! = ""
					    |                    ^
					202 |    field77: [Int] = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:202:21
					201 |    field76: Int! = ""
					202 |    field77: [Int] = true
					    |                     ^
					203 |    field78: [Int] = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:203:21
					202 |    field77: [Int] = true
					203 |    field78: [Int] = VALUE
					    |                     ^
					204 |    field79: [Int] = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:204:21
					203 |    field78: [Int] = VALUE
					204 |    field79: [Int] = 1.0
					    |                     ^
					205 |    field80: [Int] = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:205:21
					204 |    field79: [Int] = 1.0
					205 |    field80: [Int] = {}
					    |                     ^
					206 |    field81: [Int] = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:206:21
					205 |    field80: [Int] = {}
					206 |    field81: [Int] = ""
					    |                     ^
					207 |    field82: [Int]! = null
				""",
				"""
					Type '[Int]!' does not allow value 'null'.

					<document>:207:22
					206 |    field81: [Int] = ""
					207 |    field82: [Int]! = null
					    |                      ^
					208 |    field83: [Int]! = true
				""",
				"""
					Type 'Int' does not allow value 'true'.

					<document>:208:22
					207 |    field82: [Int]! = null
					208 |    field83: [Int]! = true
					    |                      ^
					209 |    field84: [Int]! = VALUE
				""",
				"""
					Type 'Int' does not allow value 'VALUE'.

					<document>:209:22
					208 |    field83: [Int]! = true
					209 |    field84: [Int]! = VALUE
					    |                      ^
					210 |    field85: [Int]! = 1.0
				""",
				"""
					Type 'Int' does not allow value '1.0'.

					<document>:210:22
					209 |    field84: [Int]! = VALUE
					210 |    field85: [Int]! = 1.0
					    |                      ^
					211 |    field86: [Int]! = {}
				""",
				"""
					Type 'Int' does not allow an input object value.

					<document>:211:22
					210 |    field85: [Int]! = 1.0
					211 |    field86: [Int]! = {}
					    |                      ^
					212 |    field87: [Int]! = ""
				""",
				"""
					Type 'Int' does not allow value '""'.

					<document>:212:22
					211 |    field86: [Int]! = {}
					212 |    field87: [Int]! = ""
					    |                      ^
					213 |    field88: [Int!] = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:213:22
					212 |    field87: [Int]! = ""
					213 |    field88: [Int!] = true
					    |                      ^
					214 |    field89: [Int!] = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:214:22
					213 |    field88: [Int!] = true
					214 |    field89: [Int!] = VALUE
					    |                      ^
					215 |    field90: [Int!] = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:215:22
					214 |    field89: [Int!] = VALUE
					215 |    field90: [Int!] = 1.0
					    |                      ^
					216 |    field91: [Int!] = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:216:22
					215 |    field90: [Int!] = 1.0
					216 |    field91: [Int!] = {}
					    |                      ^
					217 |    field92: [Int!] = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:217:22
					216 |    field91: [Int!] = {}
					217 |    field92: [Int!] = ""
					    |                      ^
					218 |    field93: [Int!]! = null
				""",
				"""
					Type '[Int!]!' does not allow value 'null'.

					<document>:218:23
					217 |    field92: [Int!] = ""
					218 |    field93: [Int!]! = null
					    |                       ^
					219 |    field94: [Int!]! = true
				""",
				"""
					Type 'Int!' does not allow value 'true'.

					<document>:219:23
					218 |    field93: [Int!]! = null
					219 |    field94: [Int!]! = true
					    |                       ^
					220 |    field95: [Int!]! = VALUE
				""",
				"""
					Type 'Int!' does not allow value 'VALUE'.

					<document>:220:23
					219 |    field94: [Int!]! = true
					220 |    field95: [Int!]! = VALUE
					    |                       ^
					221 |    field96: [Int!]! = 1.0
				""",
				"""
					Type 'Int!' does not allow value '1.0'.

					<document>:221:23
					220 |    field95: [Int!]! = VALUE
					221 |    field96: [Int!]! = 1.0
					    |                       ^
					222 |    field97: [Int!]! = {}
				""",
				"""
					Type 'Int!' does not allow an input object value.

					<document>:222:23
					221 |    field96: [Int!]! = 1.0
					222 |    field97: [Int!]! = {}
					    |                       ^
					223 |    field98: [Int!]! = ""
				""",
				"""
					Type 'Int!' does not allow value '""'.

					<document>:223:23
					222 |    field97: [Int!]! = {}
					223 |    field98: [Int!]! = ""
					    |                       ^
					224 |    field99: Scalar = VALUE
				""",
				"""
					Type 'Scalar!' does not allow value 'null'.

					<document>:227:24
					226 |    field101: Scalar = []
					227 |    field102: Scalar! = null
					    |                        ^
					228 |    field103: Scalar! = VALUE
				""",
				"""
					Type 'String' does not allow value 'true'.

					<document>:231:23
					230 |    field105: Scalar! = []
					231 |    field106: String = true
					    |                       ^
					232 |    field107: String = VALUE
				""",
				"""
					Type 'String' does not allow value 'VALUE'.

					<document>:232:23
					231 |    field106: String = true
					232 |    field107: String = VALUE
					    |                       ^
					233 |    field108: String = 1
				""",
				"""
					Type 'String' does not allow value '1'.

					<document>:233:23
					232 |    field107: String = VALUE
					233 |    field108: String = 1
					    |                       ^
					234 |    field109: String = 1.0
				""",
				"""
					Type 'String' does not allow value '1.0'.

					<document>:234:23
					233 |    field108: String = 1
					234 |    field109: String = 1.0
					    |                       ^
					235 |    field110: String = {}
				""",
				"""
					Type 'String' does not allow an input object value.

					<document>:235:23
					234 |    field109: String = 1.0
					235 |    field110: String = {}
					    |                       ^
					236 |    field111: String = []
				""",
				"""
					Type 'String' does not allow a list value.

					<document>:236:23
					235 |    field110: String = {}
					236 |    field111: String = []
					    |                       ^
					237 |    field112: String! = null
				""",
				"""
					Type 'String!' does not allow value 'null'.

					<document>:237:24
					236 |    field111: String = []
					237 |    field112: String! = null
					    |                        ^
					238 |    field113: String! = true
				""",
				"""
					Type 'String!' does not allow value 'true'.

					<document>:238:24
					237 |    field112: String! = null
					238 |    field113: String! = true
					    |                        ^
					239 |    field114: String! = VALUE
				""",
				"""
					Type 'String!' does not allow value 'VALUE'.

					<document>:239:24
					238 |    field113: String! = true
					239 |    field114: String! = VALUE
					    |                        ^
					240 |    field115: String! = 1
				""",
				"""
					Type 'String!' does not allow value '1'.

					<document>:240:24
					239 |    field114: String! = VALUE
					240 |    field115: String! = 1
					    |                        ^
					241 |    field116: String! = 1.0
				""",
				"""
					Type 'String!' does not allow value '1.0'.

					<document>:241:24
					240 |    field115: String! = 1
					241 |    field116: String! = 1.0
					    |                        ^
					242 |    field117: String! = {}
				""",
				"""
					Type 'String!' does not allow an input object value.

					<document>:242:24
					241 |    field116: String! = 1.0
					242 |    field117: String! = {}
					    |                        ^
					243 |    field118: String! = []
				""",
				"""
					Type 'String!' does not allow a list value.

					<document>:243:24
					242 |    field117: String! = {}
					243 |    field118: String! = []
					    |                        ^
					244 | }
				"""
			),
			document = """
				|type Query {
				|   fun(
				|      argument1: Boolean = VALUE
				|      argument2: Boolean = 1.0
				|      argument3: Boolean = 1
				|      argument4: Boolean = []
				|      argument5: Boolean = {}
				|      argument6: Boolean = ""
				|      argument7: Boolean! = null
				|      argument8: Boolean! = VALUE
				|      argument9: Boolean! = 1.0
				|      argument10: Boolean! = 1
				|      argument11: Boolean! = []
				|      argument12: Boolean! = {}
				|      argument13: Boolean! = ""
				|      argument14: Enum = true
				|      argument15: Enum = value
				|      argument16: Enum = 1.0
				|      argument17: Enum = 1
				|      argument18: Enum = []
				|      argument19: Enum = {}
				|      argument20: Enum = ""
				|      argument21: Enum! = null
				|      argument22: Enum! = value
				|      argument23: Enum! = true
				|      argument24: Enum! = 1.0
				|      argument25: Enum! = 1
				|      argument26: Enum! = []
				|      argument27: Enum! = {}
				|      argument28: Enum! = ""
				|      argument29: Float = true
				|      argument30: Float = VALUE
				|      argument31: Float = []
				|      argument32: Float = {}
				|      argument33: Float = ""
				|      argument34: Float! = null
				|      argument35: Float! = true
				|      argument36: Float! = VALUE
				|      argument37: Float! = []
				|      argument38: Float! = {}
				|      argument39: Float! = ""
				|      argument40: ID = true
				|      argument41: ID = 1.0
				|      argument44: ID = VALUE
				|      argument42: ID = []
				|      argument43: ID = {}
				|      argument45: ID! = null
				|      argument46: ID! = true
				|      argument47: ID! = 1.0
				|      argument48: ID! = VALUE
				|      argument49: ID! = []
				|      argument50: ID! = {}
				|      argument51: Input = true
				|      argument52: Input = VALUE
				|      argument53: Input = 1.0
				|      argument54: Input = 1
				|      argument55: Input = []
				|      argument56: Input = ""
				|      argument57: Input! = null
				|      argument58: Input! = true
				|      argument59: Input! = VALUE
				|      argument60: Input! = 1.0
				|      argument61: Input! = 1
				|      argument62: Input! = []
				|      argument63: Input! = ""
				|      argument64: Int = true
				|      argument65: Int = VALUE
				|      argument66: Int = 1.0
				|      argument67: Int = {}
				|      argument68: Int = []
				|      argument69: Int = ""
				|      argument70: Int! = null
				|      argument71: Int! = true
				|      argument72: Int! = VALUE
				|      argument73: Int! = 1.0
				|      argument74: Int! = {}
				|      argument75: Int! = []
				|      argument76: Int! = ""
				|      argument77: [Int] = true
				|      argument78: [Int] = VALUE
				|      argument79: [Int] = 1.0
				|      argument80: [Int] = {}
				|      argument81: [Int] = ""
				|      argument82: [Int]! = null
				|      argument83: [Int]! = true
				|      argument84: [Int]! = VALUE
				|      argument85: [Int]! = 1.0
				|      argument86: [Int]! = {}
				|      argument87: [Int]! = ""
				|      argument88: [Int!] = true
				|      argument89: [Int!] = VALUE
				|      argument90: [Int!] = 1.0
				|      argument91: [Int!] = {}
				|      argument92: [Int!] = ""
				|      argument93: [Int!]! = null
				|      argument94: [Int!]! = true
				|      argument95: [Int!]! = VALUE
				|      argument96: [Int!]! = 1.0
				|      argument97: [Int!]! = {}
				|      argument98: [Int!]! = ""
				|      argument99: Scalar = VALUE
				|      argument100: Scalar = {}
				|      argument101: Scalar = []
				|      argument102: Scalar! = null
				|      argument103: Scalar! = VALUE
				|      argument104: Scalar! = {}
				|      argument105: Scalar! = []
				|      argument106: String = true
				|      argument107: String = VALUE
				|      argument108: String = 1
				|      argument109: String = 1.0
				|      argument110: String = {}
				|      argument111: String = []
				|      argument112: String! = null
				|      argument113: String! = true
				|      argument114: String! = VALUE
				|      argument115: String! = 1
				|      argument116: String! = 1.0
				|      argument117: String! = {}
				|      argument118: String! = []
				|      argument119: Input!
				|   ): String
				|}
				|
				|input Input {
				|   field1: Boolean = VALUE
				|   field2: Boolean = 1.0
				|   field3: Boolean = 1
				|   field4: Boolean = []
				|   field5: Boolean = {}
				|   field6: Boolean = ""
				|   field7: Boolean! = null
				|   field8: Boolean! = VALUE
				|   field9: Boolean! = 1.0
				|   field10: Boolean! = 1
				|   field11: Boolean! = []
				|   field12: Boolean! = {}
				|   field13: Boolean! = ""
				|   field14: Enum = true
				|   field15: Enum = value
				|   field16: Enum = 1.0
				|   field17: Enum = 1
				|   field18: Enum = []
				|   field19: Enum = {}
				|   field20: Enum = ""
				|   field21: Enum! = null
				|   field22: Enum! = value
				|   field23: Enum! = true
				|   field24: Enum! = 1.0
				|   field25: Enum! = 1
				|   field26: Enum! = []
				|   field27: Enum! = {}
				|   field28: Enum! = ""
				|   field29: Float = true
				|   field30: Float = VALUE
				|   field31: Float = []
				|   field32: Float = {}
				|   field33: Float = ""
				|   field34: Float! = null
				|   field35: Float! = true
				|   field36: Float! = VALUE
				|   field37: Float! = []
				|   field38: Float! = {}
				|   field39: Float! = ""
				|   field40: ID = true
				|   field41: ID = 1.0
				|   field44: ID = VALUE
				|   field42: ID = []
				|   field43: ID = {}
				|   field45: ID! = null
				|   field46: ID! = true
				|   field47: ID! = 1.0
				|   field48: ID! = VALUE
				|   field49: ID! = []
				|   field50: ID! = {}
				|   field51: Input = true
				|   field52: Input = VALUE
				|   field53: Input = 1.0
				|   field54: Input = 1
				|   field55: Input = []
				|   field56: Input = ""
				|   field57: Input! = null
				|   field58: Input! = true
				|   field59: Input! = VALUE
				|   field60: Input! = 1.0
				|   field61: Input! = 1
				|   field62: Input! = []
				|   field63: Input! = ""
				|   field64: Int = true
				|   field65: Int = VALUE
				|   field66: Int = 1.0
				|   field67: Int = {}
				|   field68: Int = []
				|   field69: Int = ""
				|   field70: Int! = null
				|   field71: Int! = true
				|   field72: Int! = VALUE
				|   field73: Int! = 1.0
				|   field74: Int! = {}
				|   field75: Int! = []
				|   field76: Int! = ""
				|   field77: [Int] = true
				|   field78: [Int] = VALUE
				|   field79: [Int] = 1.0
				|   field80: [Int] = {}
				|   field81: [Int] = ""
				|   field82: [Int]! = null
				|   field83: [Int]! = true
				|   field84: [Int]! = VALUE
				|   field85: [Int]! = 1.0
				|   field86: [Int]! = {}
				|   field87: [Int]! = ""
				|   field88: [Int!] = true
				|   field89: [Int!] = VALUE
				|   field90: [Int!] = 1.0
				|   field91: [Int!] = {}
				|   field92: [Int!] = ""
				|   field93: [Int!]! = null
				|   field94: [Int!]! = true
				|   field95: [Int!]! = VALUE
				|   field96: [Int!]! = 1.0
				|   field97: [Int!]! = {}
				|   field98: [Int!]! = ""
				|   field99: Scalar = VALUE
				|   field100: Scalar = {}
				|   field101: Scalar = []
				|   field102: Scalar! = null
				|   field103: Scalar! = VALUE
				|   field104: Scalar! = {}
				|   field105: Scalar! = []
				|   field106: String = true
				|   field107: String = VALUE
				|   field108: String = 1
				|   field109: String = 1.0
				|   field110: String = {}
				|   field111: String = []
				|   field112: String! = null
				|   field113: String! = true
				|   field114: String! = VALUE
				|   field115: String! = 1
				|   field116: String! = 1.0
				|   field117: String! = {}
				|   field118: String! = []
				|}
				|
				|enum Enum { VALUE }
				|scalar Scalar
			"""
		)
	}
}
