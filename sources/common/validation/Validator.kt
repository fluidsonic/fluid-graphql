package io.fluidsonic.graphql


internal class Validator(
	private val document: GDocument,
	private val rules: List<ValidationRule>,
	schema: GSchema
) {

	private val context = ValidationContext(
		document = document,
		schema = schema
	)

	private val visitor = rules
		.map(::ValidationRuleVisitor)
		.parallelize()
		.contextualize(context)


	fun validate() {
		document.accept(visitor, data = context)

		for (rule in rules)
			rule.beforeTraversal(context)
	}


	// FIXME make rules
//import io.fluidsonic.graphql.*
//
//internal fun requireImplementationOfInterface(implementingTypeName: String, iface: GInterfaceType, fields: List<GFieldDefinition>) {
//	for (expectedField in iface.fields.values) {
//		val implementedField = fields.firstOrNull { it.name == expectedField.name }
//		requireNotNull(implementedField) {
//			"'fields' in object '$implementingTypeName' must contain an element named '${expectedField.name}' as required by interface '${iface.name}': $fields"
//		}
//		require(implementedField.type.isSubtypeOf(expectedField.type)) {
//			"'fields' element '${implementedField.name}' in object '$implementingTypeName' must be a subtype of '${expectedField.type}' " +
//				"as required by interface '${iface.name}': $fields"
//		}
//
//		for (expectedArg in expectedField.arguments.values) {
//			val implementedArg = implementedField.arguments.values.firstOrNull { it.name == expectedArg.name }
//			requireNotNull(implementedArg) {
//				"'fields' element '${implementedField.name}' in object '$implementingTypeName' must accept an argument named '${expectedArg.name}' " +
//					"as required by interface '${iface.name}': $fields"
//			}
//			require(implementedArg.type == expectedArg.type) {
//				"'fields' element '${implementedField.name}' argument '${implementedArg.name}' in object '$implementingTypeName' must be of type '${expectedArg.type}' " +
//					"as required by interface '${iface.name}': $fields"
//			}
//		}
//
//		for (implementedArg in implementedField.arguments.values) {
//			if (expectedField.arguments.values.none { it.name == implementedArg.name }) {
//				require(implementedArg.type !is GNonNullType) {
//					"'fields' element '${implementedField.name}' argument '${implementedArg.name}' in object '$implementingTypeName' must be of a nullable type " +
//						"as interface '${iface.name}' doesn't require it: $fields"
//				}
//			}
//		}
//	}
//}
}
