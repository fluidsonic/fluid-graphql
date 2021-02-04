package testing

import kotlinx.coroutines.*


@OptIn(ExperimentalCoroutinesApi::class)
actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) {
	kotlinx.coroutines.test.runBlockingTest { action() }
}
