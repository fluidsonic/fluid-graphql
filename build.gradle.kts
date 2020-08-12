import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.4"
}

fluidLibrary(name = "graphql", version = "0.9.3") {
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

		js()
		jvm()
		nativeDarwin()
	}
}
