package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `Input` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `Input`, in the order the rule reports them. */
internal val valueValidityRuleSchemaInputObjectArgumentErrors = listOf(
	"""
		Type 'Input' does not allow value 'true'.

		<document>:53:27
		52 |       argument50: ID! = {}
		53 |       argument51: Input = true
		   |                           ^
		54 |       argument52: Input = VALUE
	""",
	"""
		Type 'Input' does not allow value 'VALUE'.

		<document>:54:27
		53 |       argument51: Input = true
		54 |       argument52: Input = VALUE
		   |                           ^
		55 |       argument53: Input = 1.0
	""",
	"""
		Type 'Input' does not allow value '1.0'.

		<document>:55:27
		54 |       argument52: Input = VALUE
		55 |       argument53: Input = 1.0
		   |                           ^
		56 |       argument54: Input = 1
	""",
	"""
		Type 'Input' does not allow value '1'.

		<document>:56:27
		55 |       argument53: Input = 1.0
		56 |       argument54: Input = 1
		   |                           ^
		57 |       argument55: Input = []
	""",
	"""
		Type 'Input' does not allow a list value.

		<document>:57:27
		56 |       argument54: Input = 1
		57 |       argument55: Input = []
		   |                           ^
		58 |       argument56: Input = ""
	""",
	"""
		Type 'Input' does not allow value '""'.

		<document>:58:27
		57 |       argument55: Input = []
		58 |       argument56: Input = ""
		   |                           ^
		59 |       argument57: Input! = null
	""",
	"""
		Type 'Input!' does not allow value 'null'.

		<document>:59:28
		58 |       argument56: Input = ""
		59 |       argument57: Input! = null
		   |                            ^
		60 |       argument58: Input! = true
	""",
	"""
		Type 'Input!' does not allow value 'true'.

		<document>:60:28
		59 |       argument57: Input! = null
		60 |       argument58: Input! = true
		   |                            ^
		61 |       argument59: Input! = VALUE
	""",
	"""
		Type 'Input!' does not allow value 'VALUE'.

		<document>:61:28
		60 |       argument58: Input! = true
		61 |       argument59: Input! = VALUE
		   |                            ^
		62 |       argument60: Input! = 1.0
	""",
	"""
		Type 'Input!' does not allow value '1.0'.

		<document>:62:28
		61 |       argument59: Input! = VALUE
		62 |       argument60: Input! = 1.0
		   |                            ^
		63 |       argument61: Input! = 1
	""",
	"""
		Type 'Input!' does not allow value '1'.

		<document>:63:28
		62 |       argument60: Input! = 1.0
		63 |       argument61: Input! = 1
		   |                            ^
		64 |       argument62: Input! = []
	""",
	"""
		Type 'Input!' does not allow a list value.

		<document>:64:28
		63 |       argument61: Input! = 1
		64 |       argument62: Input! = []
		   |                            ^
		65 |       argument63: Input! = ""
	""",
	"""
		Type 'Input!' does not allow value '""'.

		<document>:65:28
		64 |       argument62: Input! = []
		65 |       argument63: Input! = ""
		   |                            ^
		66 |       argument64: Int = true
	""",
)

/** Errors reported for input field definition default values of type `Input`, in the order the rule reports them. */
internal val valueValidityRuleSchemaInputObjectInputFieldErrors = listOf(
	"""
		Type 'Input' does not allow value 'true'.

		<document>:176:21
		175 |    field50: ID! = {}
		176 |    field51: Input = true
		    |                     ^
		177 |    field52: Input = VALUE
	""",
	"""
		Type 'Input' does not allow value 'VALUE'.

		<document>:177:21
		176 |    field51: Input = true
		177 |    field52: Input = VALUE
		    |                     ^
		178 |    field53: Input = 1.0
	""",
	"""
		Type 'Input' does not allow value '1.0'.

		<document>:178:21
		177 |    field52: Input = VALUE
		178 |    field53: Input = 1.0
		    |                     ^
		179 |    field54: Input = 1
	""",
	"""
		Type 'Input' does not allow value '1'.

		<document>:179:21
		178 |    field53: Input = 1.0
		179 |    field54: Input = 1
		    |                     ^
		180 |    field55: Input = []
	""",
	"""
		Type 'Input' does not allow a list value.

		<document>:180:21
		179 |    field54: Input = 1
		180 |    field55: Input = []
		    |                     ^
		181 |    field56: Input = ""
	""",
	"""
		Type 'Input' does not allow value '""'.

		<document>:181:21
		180 |    field55: Input = []
		181 |    field56: Input = ""
		    |                     ^
		182 |    field57: Input! = null
	""",
	"""
		Type 'Input!' does not allow value 'null'.

		<document>:182:22
		181 |    field56: Input = ""
		182 |    field57: Input! = null
		    |                      ^
		183 |    field58: Input! = true
	""",
	"""
		Type 'Input!' does not allow value 'true'.

		<document>:183:22
		182 |    field57: Input! = null
		183 |    field58: Input! = true
		    |                      ^
		184 |    field59: Input! = VALUE
	""",
	"""
		Type 'Input!' does not allow value 'VALUE'.

		<document>:184:22
		183 |    field58: Input! = true
		184 |    field59: Input! = VALUE
		    |                      ^
		185 |    field60: Input! = 1.0
	""",
	"""
		Type 'Input!' does not allow value '1.0'.

		<document>:185:22
		184 |    field59: Input! = VALUE
		185 |    field60: Input! = 1.0
		    |                      ^
		186 |    field61: Input! = 1
	""",
	"""
		Type 'Input!' does not allow value '1'.

		<document>:186:22
		185 |    field60: Input! = 1.0
		186 |    field61: Input! = 1
		    |                      ^
		187 |    field62: Input! = []
	""",
	"""
		Type 'Input!' does not allow a list value.

		<document>:187:22
		186 |    field61: Input! = 1
		187 |    field62: Input! = []
		    |                      ^
		188 |    field63: Input! = ""
	""",
	"""
		Type 'Input!' does not allow value '""'.

		<document>:188:22
		187 |    field62: Input! = []
		188 |    field63: Input! = ""
		    |                      ^
		189 |    field64: Int = true
	""",
)
