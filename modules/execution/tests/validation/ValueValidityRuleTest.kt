package testing

import io.fluidsonic.graphql.ValueValidityRule
import kotlin.test.Test

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
			""",
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
			""",
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
			""",
		)
	}
}
