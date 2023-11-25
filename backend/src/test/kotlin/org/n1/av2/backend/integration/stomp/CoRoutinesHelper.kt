package org.n1.av2.backend.integration.stomp

import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> CompletableFuture<T>.await(): T = suspendCancellableCoroutine { continuation ->
    whenComplete { result, exception ->
        if (exception == null) {
            continuation.resume(result)
        } else {
            continuation.resumeWithException(exception)
        }
    }
}