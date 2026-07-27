package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `String` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `String`, in the order the rule reports them. */
internal val valueValidityRuleDocumentStringVariableErrors = listOf(
	"""
		String cannot represent a non string value: true

		<document>:107:27
		106 |    ${'$'}variable105: Scalar! = []
		107 |    ${'$'}variable106: String = true
		    |                           ^
		108 |    ${'$'}variable107: String = VALUE
	""",
	"""
		String cannot represent a non string value: VALUE

		<document>:108:27
		107 |    ${'$'}variable106: String = true
		108 |    ${'$'}variable107: String = VALUE
		    |                           ^
		109 |    ${'$'}variable108: String = 1
	""",
	"""
		String cannot represent a non string value: 1

		<document>:109:27
		108 |    ${'$'}variable107: String = VALUE
		109 |    ${'$'}variable108: String = 1
		    |                           ^
		110 |    ${'$'}variable109: String = 1.0
	""",
	"""
		String cannot represent a non string value: 1.0

		<document>:110:27
		109 |    ${'$'}variable108: String = 1
		110 |    ${'$'}variable109: String = 1.0
		    |                           ^
		111 |    ${'$'}variable110: String = {}
	""",
	"""
		String cannot represent a non string value: {}

		<document>:111:27
		110 |    ${'$'}variable109: String = 1.0
		111 |    ${'$'}variable110: String = {}
		    |                           ^
		112 |    ${'$'}variable111: String = []
	""",
	"""
		String cannot represent a non string value: []

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
		String cannot represent a non string value: true

		<document>:114:28
		113 |    ${'$'}variable112: String! = null
		114 |    ${'$'}variable113: String! = true
		    |                            ^
		115 |    ${'$'}variable114: String! = VALUE
	""",
	"""
		String cannot represent a non string value: VALUE

		<document>:115:28
		114 |    ${'$'}variable113: String! = true
		115 |    ${'$'}variable114: String! = VALUE
		    |                            ^
		116 |    ${'$'}variable115: String! = 1
	""",
	"""
		String cannot represent a non string value: 1

		<document>:116:28
		115 |    ${'$'}variable114: String! = VALUE
		116 |    ${'$'}variable115: String! = 1
		    |                            ^
		117 |    ${'$'}variable116: String! = 1.0
	""",
	"""
		String cannot represent a non string value: 1.0

		<document>:117:28
		116 |    ${'$'}variable115: String! = 1
		117 |    ${'$'}variable116: String! = 1.0
		    |                            ^
		118 |    ${'$'}variable117: String! = {}
	""",
	"""
		String cannot represent a non string value: {}

		<document>:118:28
		117 |    ${'$'}variable116: String! = 1.0
		118 |    ${'$'}variable117: String! = {}
		    |                            ^
		119 |    ${'$'}variable118: String! = []
	""",
	"""
		String cannot represent a non string value: []

		<document>:119:28
		118 |    ${'$'}variable117: String! = {}
		119 |    ${'$'}variable118: String! = []
		    |                            ^
		120 | ) {
	""",
)

/** Errors reported for field argument values of type `String`, in the order the rule reports them. */
internal val valueValidityRuleDocumentStringArgumentErrors = listOf(
	"""
		String cannot represent a non string value: true

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
		String cannot represent a non string value: VALUE

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
		String cannot represent a non string value: 1

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
		String cannot represent a non string value: 1.0

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
		String cannot represent a non string value: {}

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
		String cannot represent a non string value: []

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
		String cannot represent a non string value: true

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
		String cannot represent a non string value: VALUE

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
		String cannot represent a non string value: 1

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
		String cannot represent a non string value: 1.0

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
		String cannot represent a non string value: {}

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
		String cannot represent a non string value: []

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
)

/** Errors reported for input object field values of type `String`, in the order the rule reports them. */
internal val valueValidityRuleDocumentStringInputFieldErrors = listOf(
	"""
		String cannot represent a non string value: true

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
		String cannot represent a non string value: VALUE

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
		String cannot represent a non string value: 1

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
		String cannot represent a non string value: 1.0

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
		String cannot represent a non string value: {}

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
		String cannot represent a non string value: []

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
		String cannot represent a non string value: true

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
		String cannot represent a non string value: VALUE

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
		String cannot represent a non string value: 1

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
		String cannot represent a non string value: 1.0

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
		String cannot represent a non string value: {}

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
		String cannot represent a non string value: []

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
	""",
)
