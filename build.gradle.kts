import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.3"
}

fluidLibrary(name = "graphql", version = "0.9.0")

fluidLibraryVariant {
	description = "GraphQL stuff"

	jvm(JvmTarget.jdk8)
}
