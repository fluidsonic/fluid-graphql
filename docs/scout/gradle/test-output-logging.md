# Test output is configured by the fluid-gradle plugin, not by this repository

Where test console logging comes from, and why a local `testLogging` override must not come back.

The root `build.gradle.kts` contains no test-task configuration at all. fluid-gradle 4.1.0 supplies it: the lifecycle console view reports failed tests only, with the full exception format, and each test task prints a single `N tests, all passed` summary line. A successful `./gradlew clean check --console=plain` therefore stays around 200 lines, while a failing test still reports its name, assertion message, expected versus actual, and stack trace.

Commit b82431e added a local override for this while the project was on fluid-gradle 4.0.0, which logged every passed test (~1950 lines on success). The override was removed in the same change that moved the project to 4.1.0, once the plugin handled it upstream. Re-adding one would override the plugin's lifecycle configuration and silently drop `exceptionFormat` and `showCauses`.

Two traps the removed workaround had documented, should such configuration ever be needed again: the block form `testLogging { events("failed") }` was a silent no-op there and `testLogging.setEvents(...)` was required; and the configuration had to run inside `gradle.projectsEvaluated`, because the plugin configures test tasks while each module build file is evaluated — after the root `allprojects` block and after any `afterEvaluate` registered from it.

Related: fluid-gradle-plugin.md.
