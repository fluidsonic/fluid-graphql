package io.fluidsonic.graphql


@GGraphDsl.Mark
val graphql = GGraphDsl


object GGraphDsl {

	@DslMarker
	@Retention(AnnotationRetention.BINARY)
	@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
	annotation class Mark
}
