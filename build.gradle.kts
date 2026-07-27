import dev.detekt.gradle.extensions.DetektExtension
import io.fluidsonic.gradle.fluidLibrary
import io.fluidsonic.gradle.fluidLibraryModule
import kotlinx.validation.ApiValidationExtension

buildscript {
	repositories {
		mavenCentral()
	}

	dependencies {
		// Resolved through `buildscript` rather than the `plugins {}` block. The original rationale — that the
		// Gradle Plugin Portal is unreachable from this environment — does not hold: the portal serves the three
		// plugins below, and it does publish a marker for this one, so this could move into `plugins {}`.
		classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.18.1")
	}
}

plugins {
	id("io.fluidsonic.gradle") version "4.1.0"
	id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
	id("dev.detekt") version "2.0.0-alpha.5" apply false
}

apply(plugin = "binary-compatibility-validator")

// The tracked API surface is the *published* one. `@InternalGraphqlApi` is `@RequiresOptIn(ERROR)` and carries
// no compatibility promise, so its declarations are excluded — otherwise the whole `visitors` package lands in
// the dump and every later release's diff drowns in churn from a surface nobody may depend on.
configure<ApiValidationExtension> {
	nonPublicMarkers += "io.fluidsonic.graphql.InternalGraphqlApi"
}

// `nonPublicMarkers` matches per class file and does NOT propagate from an outer class to its nested classes, so
// marking `Visitor` alone leaves `Visitor$Hierarchical`, `Visitor$Companion`, … in the dump. Freezing that surface
// would defeat the whole point of tracking only the published API, so guard against it regressing silently.
tasks.register("checkApiDumpExcludesInternalApi") {
	group = "verification"
	description = "Fails if declarations marked @InternalGraphqlApi leaked into the committed API dumps."

	val dumps = fileTree(rootDir) {
		include("api/*.api", "modules/*/api/*.api")
	}
	val sources = fileTree(rootDir) {
		include("sources/**/*.kt", "modules/*/sources/**/*.kt")
	}

	inputs.files(dumps, sources)
	outputs.upToDateWhen { true }

	doLast {
		// Discover the marked declarations instead of naming them: a hardcoded list silently stops covering
		// whatever is added next. `@InternalGraphqlApi` is followed by modifiers and then the declaration, so
		// take the first identifier after the declaration keyword on the next non-annotation line.
		val declaration = Regex("""^\s*(?:@\w+\s+)*(?:public |internal |abstract |sealed |open |fun |value |data )*(?:class|interface|object)\s+(\w+)""")
		val marked = sources.files.flatMap { source ->
			val lines = source.readLines()

			lines.indices
				.filter { lines[it].trimStart().startsWith("@InternalGraphqlApi") }
				.mapNotNull { index ->
					lines.drop(index + 1)
						.firstOrNull { it.isNotBlank() && !it.trimStart().startsWith("@") }
						?.let { declaration.find(it)?.groupValues?.get(1) }
				}
		}.toSet()

		require(marked.isNotEmpty()) {
			"Found no @InternalGraphqlApi declarations in the sources — the discovery regex has stopped matching, " +
				"so this check would pass vacuously."
		}

		// A nested class does not inherit the marker in the bytecode, so compare on the *outermost* segment:
		// that is what catches an unmarked `Marked$New` sneaking into the dump.
		val leaks = dumps.files.sorted().flatMap { dump ->
			dump.readLines().withIndex().mapNotNull { (index, line) ->
				val name = Regex("""io/fluidsonic/graphql/([\w$]+)""").find(line)
					?.groupValues?.get(1)
					?.substringBefore('$')
					?.takeIf { line.startsWith("public ") && it in marked }

				name?.let { "${dump.relativeTo(rootDir)}:${index + 1}: $line" }
			}
		}

		require(leaks.isEmpty()) {
			"@InternalGraphqlApi declarations leaked into the API dump:\n" + leaks.joinToString("\n")
		}
	}
}

// `check` is contributed by the Kotlin plugin after this block runs, so bind lazily by name.
tasks.matching { it.name == "check" }.configureEach {
	dependsOn("checkApiDumpExcludesInternalApi")
}

fluidLibrary(name = "graphql", version = "0.19.0") {
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
