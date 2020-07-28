package testing

import kotlinx.coroutines.*


actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit): dynamic =
	GlobalScope.promise { action() }
