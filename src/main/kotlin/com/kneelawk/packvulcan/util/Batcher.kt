package com.kneelawk.packvulcan.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import java.time.Duration as JDuration

class Batcher<T, R>(
    scope: CoroutineScope, cycleDuration: Duration, context: CoroutineContext = EmptyCoroutineContext,
    handler: suspend (List<Message<T, R>>) -> Unit
) : AutoCloseable, Closeable {
    private val messageChannel = Channel<Message<T, R>>(64)
    private val running = AtomicBoolean(true)

    constructor(
        scope: CoroutineScope, cycleDuration: JDuration, context: CoroutineContext = EmptyCoroutineContext,
        handler: suspend (List<Message<T, R>>) -> Unit
    ) : this(scope, cycleDuration.toKotlinDuration(), context, handler)

    init {
        scope.launch(context) {
            while (isActive && running.get()) {
                delay(cycleDuration)

                var result = messageChannel.tryReceive()

                if (result.isSuccess) {
                    val messages = mutableListOf<Message<T, R>>()
                    do {
                        messages.add(result.getOrThrow())
                        result = messageChannel.tryReceive()
                    } while (result.isSuccess)

                    handler(messages)
                }
            }
        }
    }

    suspend fun request(request: T): R {
        val responseChannel = Channel<R>(1)
        messageChannel.send(Message(request, responseChannel))

        return responseChannel.receive()
    }

    override fun close() {
        running.set(false)
    }

    data class Message<T, R>(val request: T, val responseChannel: Channel<R>)
}
