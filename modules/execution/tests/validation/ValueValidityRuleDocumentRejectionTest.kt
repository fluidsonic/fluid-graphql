package testing

import io.fluidsonic.graphql.ValueValidityRule
import kotlin.test.Test

class ValueValidityRuleDocumentRejectionTest {

	@Test
	fun testRejectsValuesOfIncorrectTypeInDocument() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = variableDefaultValueErrors + argumentValueErrors + inputObjectFieldValueErrors,
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
			""",
		)
	}
}

// The three blocks below must stay in this order: the rule reports variable definition defaults
// first, then the field argument values, then the values of the input object argument's fields.

private val variableDefaultValueErrors = listOf(
	valueValidityRuleDocumentBooleanVariableErrors,
	valueValidityRuleDocumentEnumVariableErrors,
	valueValidityRuleDocumentFloatVariableErrors,
	valueValidityRuleDocumentIdVariableErrors,
	valueValidityRuleDocumentInputObjectVariableErrors,
	valueValidityRuleDocumentIntVariableErrors,
	valueValidityRuleDocumentIntListVariableErrors,
	valueValidityRuleDocumentNonNullIntListVariableErrors,
	valueValidityRuleDocumentCustomScalarVariableErrors,
	valueValidityRuleDocumentStringVariableErrors,
).flatten()

private val argumentValueErrors = listOf(
	valueValidityRuleDocumentBooleanArgumentErrors,
	valueValidityRuleDocumentEnumArgumentErrors,
	valueValidityRuleDocumentFloatArgumentErrors,
	valueValidityRuleDocumentIdArgumentErrors,
	valueValidityRuleDocumentInputObjectArgumentErrors,
	valueValidityRuleDocumentIntArgumentErrors,
	valueValidityRuleDocumentIntListArgumentErrors,
	valueValidityRuleDocumentNonNullIntListArgumentErrors,
	valueValidityRuleDocumentCustomScalarArgumentErrors,
	valueValidityRuleDocumentStringArgumentErrors,
	valueValidityRuleDocumentInputObjectMissingFieldErrors,
).flatten()

private val inputObjectFieldValueErrors = listOf(
	valueValidityRuleDocumentBooleanInputFieldErrors,
	valueValidityRuleDocumentEnumInputFieldErrors,
	valueValidityRuleDocumentFloatInputFieldErrors,
	valueValidityRuleDocumentIdInputFieldErrors,
	valueValidityRuleDocumentInputObjectInputFieldErrors,
	valueValidityRuleDocumentIntInputFieldErrors,
	valueValidityRuleDocumentIntListInputFieldErrors,
	valueValidityRuleDocumentNonNullIntListInputFieldErrors,
	valueValidityRuleDocumentCustomScalarInputFieldErrors,
	valueValidityRuleDocumentStringInputFieldErrors,
).flatten()
