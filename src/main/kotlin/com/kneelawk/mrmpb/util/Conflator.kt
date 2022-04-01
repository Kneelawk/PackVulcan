package com.kneelawk.mrmpb.util

import io.ktor.utils.io.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
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
    companion object {
        private val log = KotlinLogging.logger { }
    }

    private val channel = Channel<T>(Channel.CONFLATED)

    init {
        scope.launch(context, start) {
            while (isActive) {
                try {
                    val cur = channel.receive()
                    // update detected, applying
                    block(cur)
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    log.error("Error encountered in Conflator.", e)
                }
            }
        }
    }

    fun send(data: T) {
        channel.trySend(data)
    }
}