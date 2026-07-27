package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `[Int!]` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `[Int!]`, in the order the rule reports them. */
internal val valueValidityRuleDocumentNonNullIntListVariableErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:89:26
		88 |    ${'$'}variable87: [Int]! = ""
		89 |    ${'$'}variable88: [Int!] = true
		   |                          ^
		90 |    ${'$'}variable89: [Int!] = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:90:26
		89 |    ${'$'}variable88: [Int!] = true
		90 |    ${'$'}variable89: [Int!] = VALUE
		   |                          ^
		91 |    ${'$'}variable90: [Int!] = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:91:26
		90 |    ${'$'}variable89: [Int!] = VALUE
		91 |    ${'$'}variable90: [Int!] = 1.0
		   |                          ^
		92 |    ${'$'}variable91: [Int!] = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:92:26
		91 |    ${'$'}variable90: [Int!] = 1.0
		92 |    ${'$'}variable91: [Int!] = {}
		   |                          ^
		93 |    ${'$'}variable92: [Int!] = ""
	""",
	"""
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

		<document>:95:27
		94 |    ${'$'}variable93: [Int!]! = null
		95 |    ${'$'}variable94: [Int!]! = true
		   |                           ^
		96 |    ${'$'}variable95: [Int!]! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:96:27
		95 |    ${'$'}variable94: [Int!]! = true
		96 |    ${'$'}variable95: [Int!]! = VALUE
		   |                           ^
		97 |    ${'$'}variable96: [Int!]! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:97:27
		96 |    ${'$'}variable95: [Int!]! = VALUE
		97 |    ${'$'}variable96: [Int!]! = 1.0
		   |                           ^
		98 |    ${'$'}variable97: [Int!]! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:98:27
		97 |    ${'$'}variable96: [Int!]! = 1.0
		98 |    ${'$'}variable97: [Int!]! = {}
		   |                           ^
		99 |    ${'$'}variable98: [Int!]! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:99:27
		 98 |    ${'$'}variable97: [Int!]! = {}
		 99 |    ${'$'}variable98: [Int!]! = ""
		    |                           ^
		100 |    ${'$'}variable99: Scalar = VALUE
	""",
)

/** Errors reported for field argument values of type `[Int!]`, in the order the rule reports them. */
internal val valueValidityRuleDocumentNonNullIntListArgumentErrors = listOf(
	"""
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
)

/** Errors reported for input object field values of type `[Int!]`, in the order the rule reports them. */
internal val valueValidityRuleDocumentNonNullIntListInputFieldErrors = listOf(
	"""
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

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
		Int cannot represent non-integer value: VALUE

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
		Int cannot represent non-integer value: 1.0

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
		Int cannot represent non-integer value: {}

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
		Int cannot represent non-integer value: ""

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
)
