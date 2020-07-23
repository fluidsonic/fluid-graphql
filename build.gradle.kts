import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.13"
}

fluidLibrary(name = "graphql", version = "0.9.1")

fluidLibraryVariant {
	description = "GraphQL stuff"

	common {
		dependencies {
			api(kotlinx("coroutines-core-common", "1.3.8"))
		}
	}

	jvm(JvmTarget.jdk8) {
		dependencies {
			implementation(kotlinx("coroutines-core", "1.3.8"))
		}

		testDependencies {
			implementation(kotlinx("coroutines-test", "1.3.8"))
		}
	}
}
