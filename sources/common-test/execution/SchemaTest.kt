package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/execution/__tests__/schema-test.js
class SchemaTest {

	@Test
	fun `executes with a complex schema`() {
		val document = GDocument.parse("""
			|{
			|  feed {
			|    id,
			|    title
			|  },
			|  article(id: "1") {
			|    ...articleFields,
			|    author {
			|      id,
			|      name,
			|      pic(width: 640, height: 480) {
			|        url,
			|        width,
			|        height
			|      },
			|      recentArticle {
			|        ...articleFields,
			|        keywords
			|      }
			|    }
			|  }
			|}
			|
			|fragment articleFields on Article {
			|  id,
			|  isPublished,
			|  title,
			|  body
			|}
		""".trimMargin())

		val result = document.execute(
			schema = schema,
			rootValue = Unit
		)

		assertEquals(
			expected = mapOf(
				"data" to mapOf(
					"feed" to listOf(
						mapOf("id" to "1", "title" to "My Article 1"),
						mapOf("id" to "2", "title" to "My Article 2"),
						mapOf("id" to "3", "title" to "My Article 3"),
						mapOf("id" to "4", "title" to "My Article 4"),
						mapOf("id" to "5", "title" to "My Article 5"),
						mapOf("id" to "6", "title" to "My Article 6"),
						mapOf("id" to "7", "title" to "My Article 7"),
						mapOf("id" to "8", "title" to "My Article 8"),
						mapOf("id" to "9", "title" to "My Article 9"),
						mapOf("id" to "10", "title" to "My Article 10")
					),
					"article" to mapOf(
						"id" to "1",
						"isPublished" to true,
						"title" to "My Article 1",
						"body" to "This is a post",
						"author" to mapOf(
							"id" to "123",
							"name" to "John Smith",
							"pic" to mapOf(
								"url" to "cdn://123",
								"width" to 640,
								"height" to 480
							),
							"recentArticle" to mapOf(
								"id" to "1",
								"isPublished" to true,
								"title" to "My Article 1",
								"body" to "This is a post",
								"keywords" to listOf("foo", "bar", 1, true, null)
							)
						)
					)
				)
			),
			actual = result
		)
	}


	companion object {

		private val schema = schema {
			val Article by type
			val Author by type
			val Image by type

			Query {
				field("article" of Article) {
					argument("id" of ID)
					resolve {
						article(it.stringArgument("id"))
					}
				}
				field("feed" of List(Article)) {
					resolve {
						List(10) { article("${it + 1}") }
					}
				}
			}

			Object<Author>(Author) {
				field("id" of String) {
					resolve { id }
				}
				field("name" of String) {
					resolve { name }
				}
				field("pic" of Image) {
					argument("width" of Int)
					argument("height" of Int)

					resolve { pic(it.intArgument("width"), it.intArgument("height")) }
				}
				field("recentArticle" of Article) {
					resolve { recentArticle }
				}
			}

			Object<Article>(Article) {
				field("id" of !String) {
					resolve { id }
				}
				field("isPublished" of Boolean) {
					resolve { isPublished }
				}
				field("author" of Author) {
					resolve { author }
				}
				field("title" of String) {
					resolve { title }
				}
				field("body" of String) {
					resolve { body }
				}
				field("keywords" of List(String)) {
					resolve { keywords }
				}
			}

			Object<Image>(Image) {
				field("url" of String) {
					resolve { url }
				}
				field("width" of Int) {
					resolve { width }
				}
				field("height" of Int) {
					resolve { height }
				}
			}
		}


		private val johnSmith = Author(
			id = "123",
			name = "John Smith",
			recentArticleProvider = { article("1") }
		)


		private fun article(id: String): Article =
			Article(
				id = id,
				isPublished = true,
				author = johnSmith,
				title = "My Article $id",
				body = "This is a post",
				hidden = "This data is not exposed in the schema",
				keywords = listOf("foo", "bar", 1, true, null)
			)
	}


	private data class Article(
		val author: Author,
		val id: String,
		val isPublished: Boolean,
		val title: String,
		val body: String,
		val hidden: String,
		val keywords: List<Any?>
	)


	private data class Author(
		val id: String,
		val name: String,
		private val recentArticleProvider: () -> Article
	) {

		val recentArticle
			get() = recentArticleProvider()


		fun pic(width: Int, height: Int) =
			Image(
				url = "cdn://$id",
				width = width,
				height = height
			)
	}


	private data class Image(
		val url: String,
		val width: Int,
		val height: Int
	)
}
