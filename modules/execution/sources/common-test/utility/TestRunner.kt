package testing

import kotlinx.coroutines.*


expect fun runBlockingTest(block: suspend CoroutineScope.() -> Unit)
