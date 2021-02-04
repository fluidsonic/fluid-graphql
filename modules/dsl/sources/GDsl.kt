package io.fluidsonic.graphql


@GGraphDsl.Mark
public val graphql: GGraphDsl = GGraphDsl


public object GGraphDsl {

	@DslMarker
	@Retention(AnnotationRetention.BINARY)
	@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
	public annotation class Mark
}
