package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `String` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `String`, in the order the rule reports them. */
internal val valueValidityRuleSchemaStringArgumentErrors = listOf(
	"""
		String cannot represent a non string value: true

		<document>:108:29
		107 |       argument105: Scalar! = []
		108 |       argument106: String = true
		    |                             ^
		109 |       argument107: String = VALUE
	""",
	"""
		String cannot represent a non string value: VALUE

		<document>:109:29
		108 |       argument106: String = true
		109 |       argument107: String = VALUE
		    |                             ^
		110 |       argument108: String = 1
	""",
	"""
		String cannot represent a non string value: 1

		<document>:110:29
		109 |       argument107: String = VALUE
		110 |       argument108: String = 1
		    |                             ^
		111 |       argument109: String = 1.0
	""",
	"""
		String cannot represent a non string value: 1.0

		<document>:111:29
		110 |       argument108: String = 1
		111 |       argument109: String = 1.0
		    |                             ^
		112 |       argument110: String = {}
	""",
	"""
		String cannot represent a non string value: {}

		<document>:112:29
		111 |       argument109: String = 1.0
		112 |       argument110: String = {}
		    |                             ^
		113 |       argument111: String = []
	""",
	"""
		String cannot represent a non string value: []

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
		String cannot represent a non string value: true

		<document>:115:30
		114 |       argument112: String! = null
		115 |       argument113: String! = true
		    |                              ^
		116 |       argument114: String! = VALUE
	""",
	"""
		String cannot represent a non string value: VALUE

		<document>:116:30
		115 |       argument113: String! = true
		116 |       argument114: String! = VALUE
		    |                              ^
		117 |       argument115: String! = 1
	""",
	"""
		String cannot represent a non string value: 1

		<document>:117:30
		116 |       argument114: String! = VALUE
		117 |       argument115: String! = 1
		    |                              ^
		118 |       argument116: String! = 1.0
	""",
	"""
		String cannot represent a non string value: 1.0

		<document>:118:30
		117 |       argument115: String! = 1
		118 |       argument116: String! = 1.0
		    |                              ^
		119 |       argument117: String! = {}
	""",
	"""
		String cannot represent a non string value: {}

		<document>:119:30
		118 |       argument116: String! = 1.0
		119 |       argument117: String! = {}
		    |                              ^
		120 |       argument118: String! = []
	""",
	"""
		String cannot represent a non string value: []

		<document>:120:30
		119 |       argument117: String! = {}
		120 |       argument118: String! = []
		    |                              ^
		121 |       argument119: Input!
	""",
)

/** Errors reported for input field definition default values of type `String`, in the order the rule reports them. */
internal val valueValidityRuleSchemaStringInputFieldErrors = listOf(
	"""
		String cannot represent a non string value: true

		<document>:231:23
		230 |    field105: Scalar! = []
		231 |    field106: String = true
		    |                       ^
		232 |    field107: String = VALUE
	""",
	"""
		String cannot represent a non string value: VALUE

		<document>:232:23
		231 |    field106: String = true
		232 |    field107: String = VALUE
		    |                       ^
		233 |    field108: String = 1
	""",
	"""
		String cannot represent a non string value: 1

		<document>:233:23
		232 |    field107: String = VALUE
		233 |    field108: String = 1
		    |                       ^
		234 |    field109: String = 1.0
	""",
	"""
		String cannot represent a non string value: 1.0

		<document>:234:23
		233 |    field108: String = 1
		234 |    field109: String = 1.0
		    |                       ^
		235 |    field110: String = {}
	""",
	"""
		String cannot represent a non string value: {}

		<document>:235:23
		234 |    field109: String = 1.0
		235 |    field110: String = {}
		    |                       ^
		236 |    field111: String = []
	""",
	"""
		String cannot represent a non string value: []

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
		String cannot represent a non string value: true

		<document>:238:24
		237 |    field112: String! = null
		238 |    field113: String! = true
		    |                        ^
		239 |    field114: String! = VALUE
	""",
	"""
		String cannot represent a non string value: VALUE

		<document>:239:24
		238 |    field113: String! = true
		239 |    field114: String! = VALUE
		    |                        ^
		240 |    field115: String! = 1
	""",
	"""
		String cannot represent a non string value: 1

		<document>:240:24
		239 |    field114: String! = VALUE
		240 |    field115: String! = 1
		    |                        ^
		241 |    field116: String! = 1.0
	""",
	"""
		String cannot represent a non string value: 1.0

		<document>:241:24
		240 |    field115: String! = 1
		241 |    field116: String! = 1.0
		    |                        ^
		242 |    field117: String! = {}
	""",
	"""
		String cannot represent a non string value: {}

		<document>:242:24
		241 |    field116: String! = 1.0
		242 |    field117: String! = {}
		    |                        ^
		243 |    field118: String! = []
	""",
	"""
		String cannot represent a non string value: []

		<document>:243:24
		242 |    field117: String! = {}
		243 |    field118: String! = []
		    |                        ^
		244 | }
	""",
)
