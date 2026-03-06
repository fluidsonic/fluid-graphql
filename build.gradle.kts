import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "2.0.2"
}

fluidLibrary(name = "graphql", version = "0.15.0") {
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

		jvm()
	}
}
