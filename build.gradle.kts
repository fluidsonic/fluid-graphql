import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.25"
}

fluidLibrary(name = "graphql", version = "0.12.0") {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.graphql.InternalGraphqlApi")
		}
	}
}

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-dsl"))
				api(project(":fluid-graphql-execution"))
			}
		}

		darwin()
		js()
		jvm()
	}
}
