import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

fluidLibraryModule(description = "FIXME") {
	targets {
		darwin()
		js(compiler = KotlinJsCompilerType.LEGACY) // FIXME https://youtrack.jetbrains.com/issue/KT-39088
		jvm()
	}
}
