package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `[Int]` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `[Int]`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIntListVariableErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:78:25
		77 |    ${'$'}variable76: Int! = ""
		78 |    ${'$'}variable77: [Int] = true
		   |                         ^
		79 |    ${'$'}variable78: [Int] = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:79:25
		78 |    ${'$'}variable77: [Int] = true
		79 |    ${'$'}variable78: [Int] = VALUE
		   |                         ^
		80 |    ${'$'}variable79: [Int] = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:80:25
		79 |    ${'$'}variable78: [Int] = VALUE
		80 |    ${'$'}variable79: [Int] = 1.0
		   |                         ^
		81 |    ${'$'}variable80: [Int] = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:81:25
		80 |    ${'$'}variable79: [Int] = 1.0
		81 |    ${'$'}variable80: [Int] = {}
		   |                         ^
		82 |    ${'$'}variable81: [Int] = ""
	""",
	"""
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

		<document>:84:26
		83 |    ${'$'}variable82: [Int]! = null
		84 |    ${'$'}variable83: [Int]! = true
		   |                          ^
		85 |    ${'$'}variable84: [Int]! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:85:26
		84 |    ${'$'}variable83: [Int]! = true
		85 |    ${'$'}variable84: [Int]! = VALUE
		   |                          ^
		86 |    ${'$'}variable85: [Int]! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:86:26
		85 |    ${'$'}variable84: [Int]! = VALUE
		86 |    ${'$'}variable85: [Int]! = 1.0
		   |                          ^
		87 |    ${'$'}variable86: [Int]! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:87:26
		86 |    ${'$'}variable85: [Int]! = 1.0
		87 |    ${'$'}variable86: [Int]! = {}
		   |                          ^
		88 |    ${'$'}variable87: [Int]! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:88:26
		87 |    ${'$'}variable86: [Int]! = {}
		88 |    ${'$'}variable87: [Int]! = ""
		   |                          ^
		89 |    ${'$'}variable88: [Int!] = true
	""",
)

/** Errors reported for field argument values of type `[Int]`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIntListArgumentErrors = listOf(
	"""
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
)

/** Errors reported for input object field values of type `[Int]`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIntListInputFieldErrors = listOf(
	"""
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
)
