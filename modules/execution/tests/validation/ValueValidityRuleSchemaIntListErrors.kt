package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `[Int]` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `[Int]`, in the order the rule reports them. */
internal val valueValidityRuleSchemaIntListArgumentErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:79:27
		78 |       argument76: Int! = ""
		79 |       argument77: [Int] = true
		   |                           ^
		80 |       argument78: [Int] = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:80:27
		79 |       argument77: [Int] = true
		80 |       argument78: [Int] = VALUE
		   |                           ^
		81 |       argument79: [Int] = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:81:27
		80 |       argument78: [Int] = VALUE
		81 |       argument79: [Int] = 1.0
		   |                           ^
		82 |       argument80: [Int] = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:82:27
		81 |       argument79: [Int] = 1.0
		82 |       argument80: [Int] = {}
		   |                           ^
		83 |       argument81: [Int] = ""
	""",
	"""
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

		<document>:85:28
		84 |       argument82: [Int]! = null
		85 |       argument83: [Int]! = true
		   |                            ^
		86 |       argument84: [Int]! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:86:28
		85 |       argument83: [Int]! = true
		86 |       argument84: [Int]! = VALUE
		   |                            ^
		87 |       argument85: [Int]! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:87:28
		86 |       argument84: [Int]! = VALUE
		87 |       argument85: [Int]! = 1.0
		   |                            ^
		88 |       argument86: [Int]! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:88:28
		87 |       argument85: [Int]! = 1.0
		88 |       argument86: [Int]! = {}
		   |                            ^
		89 |       argument87: [Int]! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:89:28
		88 |       argument86: [Int]! = {}
		89 |       argument87: [Int]! = ""
		   |                            ^
		90 |       argument88: [Int!] = true
	""",
)

/** Errors reported for input field definition default values of type `[Int]`, in the order the rule reports them. */
internal val valueValidityRuleSchemaIntListInputFieldErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:202:21
		201 |    field76: Int! = ""
		202 |    field77: [Int] = true
		    |                     ^
		203 |    field78: [Int] = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:203:21
		202 |    field77: [Int] = true
		203 |    field78: [Int] = VALUE
		    |                     ^
		204 |    field79: [Int] = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:204:21
		203 |    field78: [Int] = VALUE
		204 |    field79: [Int] = 1.0
		    |                     ^
		205 |    field80: [Int] = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:205:21
		204 |    field79: [Int] = 1.0
		205 |    field80: [Int] = {}
		    |                     ^
		206 |    field81: [Int] = ""
	""",
	"""
		Int cannot represent non-integer value: ""

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
		Int cannot represent non-integer value: true

		<document>:208:22
		207 |    field82: [Int]! = null
		208 |    field83: [Int]! = true
		    |                      ^
		209 |    field84: [Int]! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:209:22
		208 |    field83: [Int]! = true
		209 |    field84: [Int]! = VALUE
		    |                      ^
		210 |    field85: [Int]! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:210:22
		209 |    field84: [Int]! = VALUE
		210 |    field85: [Int]! = 1.0
		    |                      ^
		211 |    field86: [Int]! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:211:22
		210 |    field85: [Int]! = 1.0
		211 |    field86: [Int]! = {}
		    |                      ^
		212 |    field87: [Int]! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:212:22
		211 |    field86: [Int]! = {}
		212 |    field87: [Int]! = ""
		    |                      ^
		213 |    field88: [Int!] = true
	""",
)
