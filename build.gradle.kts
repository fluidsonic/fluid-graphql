import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.3"
}

fluidLibrary(name = "graphql", version = "0.9.0")

fluidLibraryVariant {
	description = "GraphQL stuff"

	jvm(JvmTarget.jdk8) {
		dependencies {
			// FIXME
			// Only used for KClass.isInstance. Remove once Kotlin 1.3.70 is released.
			// See https://youtrack.jetbrains.com/issue/KT-14720
			implementation(kotlin("reflect"))
		}
	}
}
