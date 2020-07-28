import io.fluidsonic.gradle.*

// FIXME Rework MPP in fluid-gradle with Kotlin 1.4.
fluidLibraryVariant {
	description = "FIXME"

	common {
		dependencies {
			api(project(":fluid-graphql-language"))

			implementation(project(":fluid-graphql-dsl"))
		}

		testDependencies {
			implementation(kotlinx("coroutines-core-common", "1.3.8"))
		}
	}

	js {
		browser()
		nodejs()
	}

	jvm(JvmTarget.jdk8) {
		testDependencies {
			implementation(kotlinx("coroutines-test", "1.3.8"))
		}
	}

	listOf(ObjcTarget.iosArm64, ObjcTarget.iosX64, ObjcTarget.macosX64).forEach { target ->
		val suffix = when (target) {
			ObjcTarget.iosArm64 -> "iosarm64"
			ObjcTarget.iosX64 -> "iosx64"
			ObjcTarget.macosX64 -> "macosx64"
		}

		objc(target) {
			testDependencies {
				implementation(kotlinx("coroutines-core-$suffix", "1.3.8"))
			}
		}
	}
}

kotlin {
	sourceSets {
		val jsTest by getting {
			dependencies {
				implementation(kotlinx("coroutines-core-js", "1.3.8"))
			}
		}
	}
}

kotlin {
	sourceSets.all {
		languageSettings.useExperimentalAnnotation("io.fluidsonic.graphql.InternalGraphqlApi")
	}
}
