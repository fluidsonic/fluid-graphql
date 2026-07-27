package testing

import io.fluidsonic.graphql.ValueValidityRule
import kotlin.test.Test

class ValueValidityRuleSchemaRejectionTest {

	@Test
	fun testRejectsValuesOfIncorrectTypeInSchema() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = argumentDefaultValueErrors + inputFieldDefaultValueErrors,
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
			""",
		)
	}
}

// The two blocks below must stay in this order: the rule reports the argument definition
// defaults of `type Query` before the field definition defaults of `input Input`.

private val argumentDefaultValueErrors = listOf(
	valueValidityRuleSchemaBooleanArgumentErrors,
	valueValidityRuleSchemaEnumArgumentErrors,
	valueValidityRuleSchemaFloatArgumentErrors,
	valueValidityRuleSchemaIdArgumentErrors,
	valueValidityRuleSchemaInputObjectArgumentErrors,
	valueValidityRuleSchemaIntArgumentErrors,
	valueValidityRuleSchemaIntListArgumentErrors,
	valueValidityRuleSchemaNonNullIntListArgumentErrors,
	valueValidityRuleSchemaCustomScalarArgumentErrors,
	valueValidityRuleSchemaStringArgumentErrors,
).flatten()

private val inputFieldDefaultValueErrors = listOf(
	valueValidityRuleSchemaBooleanInputFieldErrors,
	valueValidityRuleSchemaEnumInputFieldErrors,
	valueValidityRuleSchemaFloatInputFieldErrors,
	valueValidityRuleSchemaIdInputFieldErrors,
	valueValidityRuleSchemaInputObjectInputFieldErrors,
	valueValidityRuleSchemaIntInputFieldErrors,
	valueValidityRuleSchemaIntListInputFieldErrors,
	valueValidityRuleSchemaNonNullIntListInputFieldErrors,
	valueValidityRuleSchemaCustomScalarInputFieldErrors,
	valueValidityRuleSchemaStringInputFieldErrors,
).flatten()
