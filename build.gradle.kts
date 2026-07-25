import dev.detekt.gradle.extensions.DetektExtension
import io.fluidsonic.gradle.fluidLibrary
import io.fluidsonic.gradle.fluidLibraryModule

plugins {
	id("io.fluidsonic.gradle") version "4.0.0"
	id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
	id("dev.detekt") version "2.0.0-alpha.5" apply false
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

allprojects {
	apply(plugin = "org.jlleitschuh.gradle.ktlint")
	apply(plugin = "dev.detekt")

	configure<DetektExtension> {
		buildUponDefaultConfig = true
		config.setFrom(rootProject.files("config/detekt/detekt.yml"))
		source.setFrom(files("sources", "tests"))
	}
}
