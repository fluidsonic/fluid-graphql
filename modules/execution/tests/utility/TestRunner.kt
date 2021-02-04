package testing

import kotlinx.coroutines.*


expect fun runBlockingTest(action: suspend CoroutineScope.() -> Unit)
