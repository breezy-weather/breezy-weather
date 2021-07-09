package wangdaye.com.geometricweather.common.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

suspend inline fun <T> suspendCoroutineWithTimeout(
        timeoutMillis: Long,
        crossinline block: (CancellableContinuation<T>) -> Unit
) : T? {
    var value : T? = null
    withTimeoutOrNull(timeoutMillis) {
        value = suspendCancellableCoroutine(block)
    }
    return value
}

fun <T> CancellableContinuation<T>.resumeSafely(value: T) {
    if (isActive) {
        resume(value)
    }
}