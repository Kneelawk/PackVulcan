package com.kneelawk.mrmpb.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Used to take a whole bunch of requests that would execute a blocking operation and make sure the
 * blocking operation is only executed on the latest request value.
 */
class Conflator<T>(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.(T) -> Unit
) {
    private val channel = Channel<T>(Channel.CONFLATED)

    init {
        scope.launch(context, start) {
            while (isActive) {
                val cur = channel.receive()
                // update detected, applying
                block(cur)
            }
        }
    }

    fun send(data: T) {
        channel.trySend(data)
    }
}