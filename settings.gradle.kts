rootProject.name = "fluid-graphql"

// `listFiles()` returns hidden entries too, so a tool that drops a dot-directory under `modules/`
// (e.g. `modules/.claude`) would otherwise silently become an included project and break the build.
file("modules")
	.listFiles()!!
	.filter { it.isDirectory && !it.name.startsWith(".") }
	.forEach { directory ->
		val name = directory.name

		include(name)

		project(":$name").apply {
			this.name = "${rootProject.name}-$name"
			this.projectDir = directory
		}
	}
