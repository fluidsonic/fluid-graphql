import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.25"
	id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

fluidLibrary(name = "graphql", version = "0.11.1") {
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

kover {
	coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO)
}

tasks.koverMergedXmlReport {
	xmlReportFile.set(layout.buildDirectory.file("kover/result.xml"))
	includes = listOf("io.fluidsonic.*")
}