import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.7"
}

fluidLibrary(name = "graphql", version = "0.9.0")

fluidLibraryVariant {
	description = "GraphQL stuff"

	common {
		dependencies {
			api(kotlinx("coroutines-core-common", "1.3.3"))
		}
	}

	jvm(JvmTarget.jdk8) {
		dependencies {
			// FIXME
			// Only used for KClass.isInstance. Remove once Kotlin 1.3.70 is released.
			// See https://youtrack.jetbrains.com/issue/KT-14720
			implementation(kotlin("reflect"))
			implementation(kotlinx("coroutines-core", "1.3.3"))
		}

		testDependencies {
			implementation(kotlinx("coroutines-test", "1.3.3"))
		}
	}
}
