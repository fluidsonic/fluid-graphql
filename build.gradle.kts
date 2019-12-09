import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.3"
}

fluidLibrary(name = "graphql", version = "0.9.0")

fluidLibraryVariant {
	description = "GraphQL stuff"

	common {
		dependencies {
			implementation(kotlinx("serialization-runtime", "0.14.0"))
		}
	}

	jvm(JvmTarget.jdk8)
}
