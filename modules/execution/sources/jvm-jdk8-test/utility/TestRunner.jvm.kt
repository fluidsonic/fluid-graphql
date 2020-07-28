package testing

import kotlinx.coroutines.*


actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) {
	kotlinx.coroutines.test.runBlockingTest { action() }
}
