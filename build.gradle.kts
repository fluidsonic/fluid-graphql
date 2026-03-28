import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "3.0.0"
}

fluidLibrary(name = "graphql", version = "0.16.0") {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.graphql.InternalGraphqlApi")
		}
	}
}

fluidLibraryModule(description = "A Kotlin/JVM GraphQL library") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-dsl"))
				api(project(":fluid-graphql-execution"))
			}
		}

		jvm()
	}
}
