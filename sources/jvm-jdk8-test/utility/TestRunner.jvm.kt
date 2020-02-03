package tests

import kotlinx.coroutines.*


actual fun runBlockingTest(block: suspend CoroutineScope.() -> Unit) {
	kotlinx.coroutines.test.runBlockingTest { block() }
}
