package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `[Int!]` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `[Int!]`, in the order the rule reports them. */
internal val valueValidityRuleSchemaNonNullIntListArgumentErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:90:28
		89 |       argument87: [Int]! = ""
		90 |       argument88: [Int!] = true
		   |                            ^
		91 |       argument89: [Int!] = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:91:28
		90 |       argument88: [Int!] = true
		91 |       argument89: [Int!] = VALUE
		   |                            ^
		92 |       argument90: [Int!] = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:92:28
		91 |       argument89: [Int!] = VALUE
		92 |       argument90: [Int!] = 1.0
		   |                            ^
		93 |       argument91: [Int!] = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:93:28
		92 |       argument90: [Int!] = 1.0
		93 |       argument91: [Int!] = {}
		   |                            ^
		94 |       argument92: [Int!] = ""
	""",
	"""
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

		<document>:96:29
		95 |       argument93: [Int!]! = null
		96 |       argument94: [Int!]! = true
		   |                             ^
		97 |       argument95: [Int!]! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:97:29
		96 |       argument94: [Int!]! = true
		97 |       argument95: [Int!]! = VALUE
		   |                             ^
		98 |       argument96: [Int!]! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:98:29
		97 |       argument95: [Int!]! = VALUE
		98 |       argument96: [Int!]! = 1.0
		   |                             ^
		99 |       argument97: [Int!]! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:99:29
		 98 |       argument96: [Int!]! = 1.0
		 99 |       argument97: [Int!]! = {}
		    |                             ^
		100 |       argument98: [Int!]! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:100:29
		 99 |       argument97: [Int!]! = {}
		100 |       argument98: [Int!]! = ""
		    |                             ^
		101 |       argument99: Scalar = VALUE
	""",
)

/** Errors reported for input field definition default values of type `[Int!]`, in the order the rule reports them. */
internal val valueValidityRuleSchemaNonNullIntListInputFieldErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:213:22
		212 |    field87: [Int]! = ""
		213 |    field88: [Int!] = true
		    |                      ^
		214 |    field89: [Int!] = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:214:22
		213 |    field88: [Int!] = true
		214 |    field89: [Int!] = VALUE
		    |                      ^
		215 |    field90: [Int!] = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:215:22
		214 |    field89: [Int!] = VALUE
		215 |    field90: [Int!] = 1.0
		    |                      ^
		216 |    field91: [Int!] = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:216:22
		215 |    field90: [Int!] = 1.0
		216 |    field91: [Int!] = {}
		    |                      ^
		217 |    field92: [Int!] = ""
	""",
	"""
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

		<document>:219:23
		218 |    field93: [Int!]! = null
		219 |    field94: [Int!]! = true
		    |                       ^
		220 |    field95: [Int!]! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:220:23
		219 |    field94: [Int!]! = true
		220 |    field95: [Int!]! = VALUE
		    |                       ^
		221 |    field96: [Int!]! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:221:23
		220 |    field95: [Int!]! = VALUE
		221 |    field96: [Int!]! = 1.0
		    |                       ^
		222 |    field97: [Int!]! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:222:23
		221 |    field96: [Int!]! = 1.0
		222 |    field97: [Int!]! = {}
		    |                       ^
		223 |    field98: [Int!]! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:223:23
		222 |    field97: [Int!]! = {}
		223 |    field98: [Int!]! = ""
		    |                       ^
		224 |    field99: Scalar = VALUE
	""",
)
