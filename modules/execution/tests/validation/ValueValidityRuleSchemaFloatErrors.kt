package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `Float` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `Float`, in the order the rule reports them. */
internal val valueValidityRuleSchemaFloatArgumentErrors = listOf(
	"""
		Float cannot represent non numeric value: true

		<document>:31:27
		30 |       argument28: Enum! = ""
		31 |       argument29: Float = true
		   |                           ^
		32 |       argument30: Float = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:32:27
		31 |       argument29: Float = true
		32 |       argument30: Float = VALUE
		   |                           ^
		33 |       argument31: Float = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:33:27
		32 |       argument30: Float = VALUE
		33 |       argument31: Float = []
		   |                           ^
		34 |       argument32: Float = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:34:27
		33 |       argument31: Float = []
		34 |       argument32: Float = {}
		   |                           ^
		35 |       argument33: Float = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:35:27
		34 |       argument32: Float = {}
		35 |       argument33: Float = ""
		   |                           ^
		36 |       argument34: Float! = null
	""",
	"""
		Type 'Float!' does not allow value 'null'.

		<document>:36:28
		35 |       argument33: Float = ""
		36 |       argument34: Float! = null
		   |                            ^
		37 |       argument35: Float! = true
	""",
	"""
		Float cannot represent non numeric value: true

		<document>:37:28
		36 |       argument34: Float! = null
		37 |       argument35: Float! = true
		   |                            ^
		38 |       argument36: Float! = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:38:28
		37 |       argument35: Float! = true
		38 |       argument36: Float! = VALUE
		   |                            ^
		39 |       argument37: Float! = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:39:28
		38 |       argument36: Float! = VALUE
		39 |       argument37: Float! = []
		   |                            ^
		40 |       argument38: Float! = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:40:28
		39 |       argument37: Float! = []
		40 |       argument38: Float! = {}
		   |                            ^
		41 |       argument39: Float! = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:41:28
		40 |       argument38: Float! = {}
		41 |       argument39: Float! = ""
		   |                            ^
		42 |       argument40: ID = true
	""",
)

/** Errors reported for input field definition default values of type `Float`, in the order the rule reports them. */
internal val valueValidityRuleSchemaFloatInputFieldErrors = listOf(
	"""
		Float cannot represent non numeric value: true

		<document>:154:21
		153 |    field28: Enum! = ""
		154 |    field29: Float = true
		    |                     ^
		155 |    field30: Float = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:155:21
		154 |    field29: Float = true
		155 |    field30: Float = VALUE
		    |                     ^
		156 |    field31: Float = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:156:21
		155 |    field30: Float = VALUE
		156 |    field31: Float = []
		    |                     ^
		157 |    field32: Float = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:157:21
		156 |    field31: Float = []
		157 |    field32: Float = {}
		    |                     ^
		158 |    field33: Float = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:158:21
		157 |    field32: Float = {}
		158 |    field33: Float = ""
		    |                     ^
		159 |    field34: Float! = null
	""",
	"""
		Type 'Float!' does not allow value 'null'.

		<document>:159:22
		158 |    field33: Float = ""
		159 |    field34: Float! = null
		    |                      ^
		160 |    field35: Float! = true
	""",
	"""
		Float cannot represent non numeric value: true

		<document>:160:22
		159 |    field34: Float! = null
		160 |    field35: Float! = true
		    |                      ^
		161 |    field36: Float! = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:161:22
		160 |    field35: Float! = true
		161 |    field36: Float! = VALUE
		    |                      ^
		162 |    field37: Float! = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:162:22
		161 |    field36: Float! = VALUE
		162 |    field37: Float! = []
		    |                      ^
		163 |    field38: Float! = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:163:22
		162 |    field37: Float! = []
		163 |    field38: Float! = {}
		    |                      ^
		164 |    field39: Float! = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:164:22
		163 |    field38: Float! = {}
		164 |    field39: Float! = ""
		    |                      ^
		165 |    field40: ID = true
	""",
)
