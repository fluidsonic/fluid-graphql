package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


class VisitorTest {

	@Test
	fun `visits a document`() {
		val visitor = StackCollectingVisitor()

		val document = GDocument.parse("""
			|query query(${'$'}variable: Int = 1 @foo) @foo(argument: 1) {
			|   field(argument: 1) @foo {
			|      ...fragment @foo
			|      ... on Foo @foo {
			|         field(argument: [{ id: ${'$'}variable }, true, 1, 2.0, "", VALUE, null])
			|      }
			|   }
			|}
		""".trimMargin())

		val schema = GSchema.parse("""
			|${'"'}""description${'"'}""
			|enum Enum @foo { VALUE @foo }
			|
			|extend enum Enum @foo { VALUE2 @foo }
			|
			|${'"'}""description${'"'}""
			|input Input @foo {
			|   field: String @foo
			|}
			|
			|extend input Input @foo {
			|   field: String @foo
			|}
			|
			|${'"'}""description${'"'}""
			|interface Interface implements Something @foo {
			|   field(argument: Int = 2 @foo): [Int!] @foo
			|}
			|
			|extend interface Interface implements Something @foo {
			|   field(argument: Int = 2 @foo): [Int!] @foo
			|}
			|
			|${'"'}""description${'"'}""
			|type Type implements Something @foo {
			|   field(argument: Int = 2 @foo): [Int!] @foo
			|}
			|
			|extend type Type implements Something @foo {
			|   field(argument: Int = 2 @foo): [Int!] @foo
			|}
			|
			|${'"'}""description${'"'}""
			|scalar Scalar @foo
			|extend scalar Scalar @foo
			|
			|${'"'}""description${'"'}""
			|union Union @foo = Type | OtherType
			|extend union Union @foo = ThirdType
			|
			|schema @foo {
			|  query: Query
			|  mutation: Mutation
			|}
			|extend schema @foo {
			|  subscription: Subscription
			|}
			|
			|directive @foo on ARGUMENT_DEFINITION
		""".trimMargin())!!

		document.accept(visitor, data = StackCollectingVisitor.Data())
		schema.document.accept(visitor, data = StackCollectingVisitor.Data())
		GListType(GBooleanType).accept(visitor, data = StackCollectingVisitor.Data())
		GNonNullType(GBooleanType).accept(visitor, data = StackCollectingVisitor.Data())
		GFloatType.accept(visitor, data = StackCollectingVisitor.Data())
		GIdType.accept(visitor, data = StackCollectingVisitor.Data())
		GIntType.accept(visitor, data = StackCollectingVisitor.Data())
		GStringType.accept(visitor, data = StackCollectingVisitor.Data())

		val expectedStacks: List<List<String>> = listOf(

			listOf("Document(0)"),
			listOf("Document(0)", "OperationDefinition(1)"),
			listOf("Document(0)", "OperationDefinition(1)", "Name(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "Name(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "NamedTypeRef(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "NamedTypeRef(3)", "Name(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "IntValue(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "Directive(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "Directive(2)", "Argument(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "Directive(2)", "Argument(3)", "Name(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "Directive(2)", "Argument(3)", "IntValue(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "Name(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "Argument(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "Argument(4)", "Name(5)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "Argument(4)", "IntValue(5)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "Directive(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "Directive(4)", "Name(5)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "FragmentSelection(5)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "FragmentSelection(5)", "Name(6)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "FragmentSelection(5)", "Directive(6)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "FragmentSelection(5)", "Directive(6)", "Name(7)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "NamedTypeRef(6)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "NamedTypeRef(6)", "Name(7)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "Directive(6)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "Directive(6)", "Name(7)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Name(8)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "Name(9)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "ObjectValue(10)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "ObjectValue(10)", "ObjectValueField(11)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "ObjectValue(10)", "ObjectValueField(11)", "Name(12)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "ObjectValue(10)", "ObjectValueField(11)", "VariableRef(12)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "ObjectValue(10)", "ObjectValueField(11)", "VariableRef(12)", "Name(13)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "BooleanValue(10)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "IntValue(10)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "FloatValue(10)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "StringValue(10)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "EnumValue(10)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)", "FieldSelection(3)", "SelectionSet(4)", "InlineFragmentSelection(5)", "SelectionSet(6)", "FieldSelection(7)", "Argument(8)", "ListValue(9)", "NullValue(10)"),
			listOf("Document(0)"),
			listOf("Document(0)", "EnumType(1)"),
			listOf("Document(0)", "EnumType(1)", "StringValue(2)"),
			listOf("Document(0)", "EnumType(1)", "Name(2)"),
			listOf("Document(0)", "EnumType(1)", "Directive(2)"),
			listOf("Document(0)", "EnumType(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "EnumType(1)", "EnumValueDefinition(2)"),
			listOf("Document(0)", "EnumType(1)", "EnumValueDefinition(2)", "Name(3)"),
			listOf("Document(0)", "EnumType(1)", "EnumValueDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "EnumType(1)", "EnumValueDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "EnumTypeExtension(1)"),
			listOf("Document(0)", "EnumTypeExtension(1)", "Name(2)"),
			listOf("Document(0)", "EnumTypeExtension(1)", "Directive(2)"),
			listOf("Document(0)", "EnumTypeExtension(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "EnumTypeExtension(1)", "EnumValueDefinition(2)"),
			listOf("Document(0)", "EnumTypeExtension(1)", "EnumValueDefinition(2)", "Name(3)"),
			listOf("Document(0)", "EnumTypeExtension(1)", "EnumValueDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "EnumTypeExtension(1)", "EnumValueDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "InputObjectType(1)"),
			listOf("Document(0)", "InputObjectType(1)", "StringValue(2)"),
			listOf("Document(0)", "InputObjectType(1)", "Name(2)"),
			listOf("Document(0)", "InputObjectType(1)", "Directive(2)"),
			listOf("Document(0)", "InputObjectType(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "InputObjectType(1)", "ArgumentDefinition(2)"),
			listOf("Document(0)", "InputObjectType(1)", "ArgumentDefinition(2)", "Name(3)"),
			listOf("Document(0)", "InputObjectType(1)", "ArgumentDefinition(2)", "NamedTypeRef(3)"),
			listOf("Document(0)", "InputObjectType(1)", "ArgumentDefinition(2)", "NamedTypeRef(3)", "Name(4)"),
			listOf("Document(0)", "InputObjectType(1)", "ArgumentDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "InputObjectType(1)", "ArgumentDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "Name(2)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "Directive(2)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "ArgumentDefinition(2)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "ArgumentDefinition(2)", "Name(3)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "ArgumentDefinition(2)", "NamedTypeRef(3)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "ArgumentDefinition(2)", "NamedTypeRef(3)", "Name(4)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "ArgumentDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "InputObjectTypeExtension(1)", "ArgumentDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "InterfaceType(1)"),
			listOf("Document(0)", "InterfaceType(1)", "StringValue(2)"),
			listOf("Document(0)", "InterfaceType(1)", "Name(2)"),
			listOf("Document(0)", "InterfaceType(1)", "NamedTypeRef(2)"),
			listOf("Document(0)", "InterfaceType(1)", "NamedTypeRef(2)", "Name(3)"),
			listOf("Document(0)", "InterfaceType(1)", "Directive(2)"),
			listOf("Document(0)", "InterfaceType(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "Name(3)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Name(4)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)", "Name(5)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "IntValue(4)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)", "Name(5)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ListTypeRef(3)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)", "Name(6)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "InterfaceType(1)", "FieldDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "Name(2)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "NamedTypeRef(2)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "NamedTypeRef(2)", "Name(3)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "Directive(2)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "Name(3)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Name(4)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)", "Name(5)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "IntValue(4)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)", "Name(5)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)", "Name(6)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "InterfaceTypeExtension(1)", "FieldDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "ObjectType(1)"),
			listOf("Document(0)", "ObjectType(1)", "StringValue(2)"),
			listOf("Document(0)", "ObjectType(1)", "Name(2)"),
			listOf("Document(0)", "ObjectType(1)", "NamedTypeRef(2)"),
			listOf("Document(0)", "ObjectType(1)", "NamedTypeRef(2)", "Name(3)"),
			listOf("Document(0)", "ObjectType(1)", "Directive(2)"),
			listOf("Document(0)", "ObjectType(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "Name(3)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Name(4)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)", "Name(5)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "IntValue(4)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)", "Name(5)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ListTypeRef(3)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)", "Name(6)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "ObjectType(1)", "FieldDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "ObjectTypeExtension(1)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "Name(2)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "NamedTypeRef(2)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "NamedTypeRef(2)", "Name(3)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "Directive(2)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "Name(3)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Name(4)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "NamedTypeRef(4)", "Name(5)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "IntValue(4)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ArgumentDefinition(3)", "Directive(4)", "Name(5)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "ListTypeRef(3)", "NonNullTypeRef(4)", "NamedTypeRef(5)", "Name(6)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "Directive(3)"),
			listOf("Document(0)", "ObjectTypeExtension(1)", "FieldDefinition(2)", "Directive(3)", "Name(4)"),
			listOf("Document(0)", "ScalarType(1)"),
			listOf("Document(0)", "ScalarType(1)", "StringValue(2)"),
			listOf("Document(0)", "ScalarType(1)", "Name(2)"),
			listOf("Document(0)", "ScalarType(1)", "Directive(2)"),
			listOf("Document(0)", "ScalarType(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "ScalarTypeExtension(1)"),
			listOf("Document(0)", "ScalarTypeExtension(1)", "Name(2)"),
			listOf("Document(0)", "ScalarTypeExtension(1)", "Directive(2)"),
			listOf("Document(0)", "ScalarTypeExtension(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "UnionType(1)"),
			listOf("Document(0)", "UnionType(1)", "StringValue(2)"),
			listOf("Document(0)", "UnionType(1)", "Name(2)"),
			listOf("Document(0)", "UnionType(1)", "Directive(2)"),
			listOf("Document(0)", "UnionType(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "UnionType(1)", "NamedTypeRef(2)"),
			listOf("Document(0)", "UnionType(1)", "NamedTypeRef(2)", "Name(3)"),
			listOf("Document(0)", "UnionType(1)", "NamedTypeRef(2)"),
			listOf("Document(0)", "UnionType(1)", "NamedTypeRef(2)", "Name(3)"),
			listOf("Document(0)", "UnionTypeExtension(1)"),
			listOf("Document(0)", "UnionTypeExtension(1)", "Name(2)"),
			listOf("Document(0)", "UnionTypeExtension(1)", "Directive(2)"),
			listOf("Document(0)", "UnionTypeExtension(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "UnionTypeExtension(1)", "NamedTypeRef(2)"),
			listOf("Document(0)", "UnionTypeExtension(1)", "NamedTypeRef(2)", "Name(3)"),
			listOf("Document(0)", "SchemaDefinition(1)"),
			listOf("Document(0)", "SchemaDefinition(1)", "Directive(2)"),
			listOf("Document(0)", "SchemaDefinition(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "SchemaDefinition(1)", "OperationTypeDefinition(2)"),
			listOf("Document(0)", "SchemaDefinition(1)", "OperationTypeDefinition(2)", "NamedTypeRef(3)"),
			listOf("Document(0)", "SchemaDefinition(1)", "OperationTypeDefinition(2)", "NamedTypeRef(3)", "Name(4)"),
			listOf("Document(0)", "SchemaDefinition(1)", "OperationTypeDefinition(2)"),
			listOf("Document(0)", "SchemaDefinition(1)", "OperationTypeDefinition(2)", "NamedTypeRef(3)"),
			listOf("Document(0)", "SchemaDefinition(1)", "OperationTypeDefinition(2)", "NamedTypeRef(3)", "Name(4)"),
			listOf("Document(0)", "SchemaExtensionDefinition(1)"),
			listOf("Document(0)", "SchemaExtensionDefinition(1)", "Directive(2)"),
			listOf("Document(0)", "SchemaExtensionDefinition(1)", "Directive(2)", "Name(3)"),
			listOf("Document(0)", "SchemaExtensionDefinition(1)", "OperationTypeDefinition(2)"),
			listOf("Document(0)", "SchemaExtensionDefinition(1)", "OperationTypeDefinition(2)", "NamedTypeRef(3)"),
			listOf("Document(0)", "SchemaExtensionDefinition(1)", "OperationTypeDefinition(2)", "NamedTypeRef(3)", "Name(4)"),
			listOf("Document(0)", "DirectiveDefinition(1)"),
			listOf("Document(0)", "DirectiveDefinition(1)", "Name(2)"),
			listOf("Document(0)", "DirectiveDefinition(1)", "Name(2)"),
			listOf("SyntheticNode(0)"),
			listOf("SyntheticNode(0)", "ScalarType(1)"),
			listOf("SyntheticNode(0)", "ScalarType(1)", "Name(2)"),
			listOf("SyntheticNode(0)"),
			listOf("SyntheticNode(0)", "ScalarType(1)"),
			listOf("SyntheticNode(0)", "ScalarType(1)", "Name(2)"),
			listOf("ScalarType(0)"),
			listOf("ScalarType(0)", "Name(1)"),
			listOf("ScalarType(0)"),
			listOf("ScalarType(0)", "Name(1)"),
			listOf("ScalarType(0)"),
			listOf("ScalarType(0)", "Name(1)"),
			listOf("ScalarType(0)"),
			listOf("ScalarType(0)", "Name(1)")
		)

		val actualStacksString = makePrettyStacks(visitor.target.stacks)
		val expectedStacksString = makePrettyStacks(expectedStacks)

		assertEquals(expected = expectedStacksString, actual = actualStacksString)
	}


	@Test
	fun `skips children`() {
		val visitor = StackCollectingVisitor(
			skipsChildrenInNode = { it is GSelectionSet || it is GVariableDefinition || it is GDirective }
		)

		val document = GDocument.parse("""
			|query query(${'$'}variable: Int = 1 @foo) @foo(argument: 1) {
			|   field(argument: 1) @foo {
			|      ...fragment @foo
			|      ... on Foo @foo {
			|         field(argument: [{ id: ${'$'}variable }, true, 1, 2.0, "", VALUE, null])
			|      }
			|   }
			|}
		""".trimMargin())

		document.accept(visitor, data = StackCollectingVisitor.Data())

		val expectedStacks: List<List<String>> = listOf(
			listOf("Document(0)"),
			listOf("Document(0)", "OperationDefinition(1)"),
			listOf("Document(0)", "OperationDefinition(1)", "Name(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "Directive(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "SelectionSet(2)")
		)

		val actualStacksString = makePrettyStacks(visitor.target.stacks)
		val expectedStacksString = makePrettyStacks(expectedStacks)

		assertEquals(expected = expectedStacksString, actual = actualStacksString)
	}


	@Test
	fun aborts() {
		val visitor = StackCollectingVisitor(
			abortsInNode = { it is GDirective }
		)

		val document = GDocument.parse("""
			|query query(${'$'}variable: Int = 1 @foo) @foo(argument: 1) {
			|   field(argument: 1) @foo {
			|      ...fragment @foo
			|      ... on Foo @foo {
			|         field(argument: [{ id: ${'$'}variable }, true, 1, 2.0, "", VALUE, null])
			|      }
			|   }
			|}
		""".trimMargin())

		document.accept(visitor, data = StackCollectingVisitor.Data())

		val expectedStacks: List<List<String>> = listOf(
			listOf("Document(0)"),
			listOf("Document(0)", "OperationDefinition(1)"),
			listOf("Document(0)", "OperationDefinition(1)", "Name(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "Name(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "NamedTypeRef(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "NamedTypeRef(3)", "Name(4)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "IntValue(3)"),
			listOf("Document(0)", "OperationDefinition(1)", "VariableDefinition(2)", "Directive(3)")
		)

		val actualStacksString = makePrettyStacks(visitor.target.stacks)
		val expectedStacksString = makePrettyStacks(expectedStacks)

		assertEquals(expected = expectedStacksString, actual = actualStacksString)
	}


	private fun makePrettyStacks(stacks: List<List<String>>) =
		"\n" + stacks.joinToString(",\n") { stack ->
			"listOf(" + stack.joinToString(", ") { "\"$it\"" } + ")"
		} + "\n"
}
