package testing

import kotlinx.coroutines.*


actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) {
	runBlocking { action() }
}
