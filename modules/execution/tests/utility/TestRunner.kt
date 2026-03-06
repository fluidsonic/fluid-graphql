package testing

import kotlinx.coroutines.*


@OptIn(ExperimentalCoroutinesApi::class)
fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) {
	kotlinx.coroutines.test.runBlockingTest { action() }
}
