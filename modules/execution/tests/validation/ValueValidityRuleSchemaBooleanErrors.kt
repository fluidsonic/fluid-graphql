package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `Boolean` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `Boolean`, in the order the rule reports them. */
internal val valueValidityRuleSchemaBooleanArgumentErrors = listOf(
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:3:28
		2 |    fun(
		3 |       argument1: Boolean = VALUE
		  |                            ^
		4 |       argument2: Boolean = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:4:28
		3 |       argument1: Boolean = VALUE
		4 |       argument2: Boolean = 1.0
		  |                            ^
		5 |       argument3: Boolean = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:5:28
		4 |       argument2: Boolean = 1.0
		5 |       argument3: Boolean = 1
		  |                            ^
		6 |       argument4: Boolean = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:6:28
		5 |       argument3: Boolean = 1
		6 |       argument4: Boolean = []
		  |                            ^
		7 |       argument5: Boolean = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:7:28
		6 |       argument4: Boolean = []
		7 |       argument5: Boolean = {}
		  |                            ^
		8 |       argument6: Boolean = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:8:28
		7 |       argument5: Boolean = {}
		8 |       argument6: Boolean = ""
		  |                            ^
		9 |       argument7: Boolean! = null
	""",
	"""
		Type 'Boolean!' does not allow value 'null'.

		<document>:9:29
		 8 |       argument6: Boolean = ""
		 9 |       argument7: Boolean! = null
		   |                             ^
		10 |       argument8: Boolean! = VALUE
	""",
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:10:29
		 9 |       argument7: Boolean! = null
		10 |       argument8: Boolean! = VALUE
		   |                             ^
		11 |       argument9: Boolean! = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:11:29
		10 |       argument8: Boolean! = VALUE
		11 |       argument9: Boolean! = 1.0
		   |                             ^
		12 |       argument10: Boolean! = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:12:30
		11 |       argument9: Boolean! = 1.0
		12 |       argument10: Boolean! = 1
		   |                              ^
		13 |       argument11: Boolean! = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:13:30
		12 |       argument10: Boolean! = 1
		13 |       argument11: Boolean! = []
		   |                              ^
		14 |       argument12: Boolean! = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:14:30
		13 |       argument11: Boolean! = []
		14 |       argument12: Boolean! = {}
		   |                              ^
		15 |       argument13: Boolean! = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:15:30
		14 |       argument12: Boolean! = {}
		15 |       argument13: Boolean! = ""
		   |                              ^
		16 |       argument14: Enum = true
	""",
)

/** Errors reported for input field definition default values of type `Boolean`, in the order the rule reports them. */
internal val valueValidityRuleSchemaBooleanInputFieldErrors = listOf(
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:126:22
		125 | input Input {
		126 |    field1: Boolean = VALUE
		    |                      ^
		127 |    field2: Boolean = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:127:22
		126 |    field1: Boolean = VALUE
		127 |    field2: Boolean = 1.0
		    |                      ^
		128 |    field3: Boolean = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:128:22
		127 |    field2: Boolean = 1.0
		128 |    field3: Boolean = 1
		    |                      ^
		129 |    field4: Boolean = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:129:22
		128 |    field3: Boolean = 1
		129 |    field4: Boolean = []
		    |                      ^
		130 |    field5: Boolean = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:130:22
		129 |    field4: Boolean = []
		130 |    field5: Boolean = {}
		    |                      ^
		131 |    field6: Boolean = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:131:22
		130 |    field5: Boolean = {}
		131 |    field6: Boolean = ""
		    |                      ^
		132 |    field7: Boolean! = null
	""",
	"""
		Type 'Boolean!' does not allow value 'null'.

		<document>:132:23
		131 |    field6: Boolean = ""
		132 |    field7: Boolean! = null
		    |                       ^
		133 |    field8: Boolean! = VALUE
	""",
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:133:23
		132 |    field7: Boolean! = null
		133 |    field8: Boolean! = VALUE
		    |                       ^
		134 |    field9: Boolean! = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:134:23
		133 |    field8: Boolean! = VALUE
		134 |    field9: Boolean! = 1.0
		    |                       ^
		135 |    field10: Boolean! = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:135:24
		134 |    field9: Boolean! = 1.0
		135 |    field10: Boolean! = 1
		    |                        ^
		136 |    field11: Boolean! = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:136:24
		135 |    field10: Boolean! = 1
		136 |    field11: Boolean! = []
		    |                        ^
		137 |    field12: Boolean! = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:137:24
		136 |    field11: Boolean! = []
		137 |    field12: Boolean! = {}
		    |                        ^
		138 |    field13: Boolean! = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:138:24
		137 |    field12: Boolean! = {}
		138 |    field13: Boolean! = ""
		    |                        ^
		139 |    field14: Enum = true
	""",
)
