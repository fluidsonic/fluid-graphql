package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `ID` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `ID`, in the order the rule reports them. */
internal val valueValidityRuleSchemaIdArgumentErrors = listOf(
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:42:24
		41 |       argument39: Float! = ""
		42 |       argument40: ID = true
		   |                        ^
		43 |       argument41: ID = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:43:24
		42 |       argument40: ID = true
		43 |       argument41: ID = 1.0
		   |                        ^
		44 |       argument44: ID = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:44:24
		43 |       argument41: ID = 1.0
		44 |       argument44: ID = VALUE
		   |                        ^
		45 |       argument42: ID = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:45:24
		44 |       argument44: ID = VALUE
		45 |       argument42: ID = []
		   |                        ^
		46 |       argument43: ID = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

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
		ID cannot represent a non-string and non-integer value: true

		<document>:48:25
		47 |       argument45: ID! = null
		48 |       argument46: ID! = true
		   |                         ^
		49 |       argument47: ID! = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:49:25
		48 |       argument46: ID! = true
		49 |       argument47: ID! = 1.0
		   |                         ^
		50 |       argument48: ID! = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:50:25
		49 |       argument47: ID! = 1.0
		50 |       argument48: ID! = VALUE
		   |                         ^
		51 |       argument49: ID! = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:51:25
		50 |       argument48: ID! = VALUE
		51 |       argument49: ID! = []
		   |                         ^
		52 |       argument50: ID! = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:52:25
		51 |       argument49: ID! = []
		52 |       argument50: ID! = {}
		   |                         ^
		53 |       argument51: Input = true
	""",
)

/** Errors reported for input field definition default values of type `ID`, in the order the rule reports them. */
internal val valueValidityRuleSchemaIdInputFieldErrors = listOf(
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:165:18
		164 |    field39: Float! = ""
		165 |    field40: ID = true
		    |                  ^
		166 |    field41: ID = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:166:18
		165 |    field40: ID = true
		166 |    field41: ID = 1.0
		    |                  ^
		167 |    field44: ID = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:167:18
		166 |    field41: ID = 1.0
		167 |    field44: ID = VALUE
		    |                  ^
		168 |    field42: ID = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:168:18
		167 |    field44: ID = VALUE
		168 |    field42: ID = []
		    |                  ^
		169 |    field43: ID = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

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
		ID cannot represent a non-string and non-integer value: true

		<document>:171:19
		170 |    field45: ID! = null
		171 |    field46: ID! = true
		    |                   ^
		172 |    field47: ID! = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:172:19
		171 |    field46: ID! = true
		172 |    field47: ID! = 1.0
		    |                   ^
		173 |    field48: ID! = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:173:19
		172 |    field47: ID! = 1.0
		173 |    field48: ID! = VALUE
		    |                   ^
		174 |    field49: ID! = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:174:19
		173 |    field48: ID! = VALUE
		174 |    field49: ID! = []
		    |                   ^
		175 |    field50: ID! = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:175:19
		174 |    field49: ID! = []
		175 |    field50: ID! = {}
		    |                   ^
		176 |    field51: Input = true
	""",
)
