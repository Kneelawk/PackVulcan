package com.kneelawk.packvulcan.util

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.supervisorScope

suspend fun <K, V> AsyncCache<K, V>.suspendGet(key: K, mappingFunction: suspend (K) -> V): V = supervisorScope {
    get(key) { keyInternal, _ ->
        future {
            mappingFunction(keyInternal)
        }
    }.await()
}

@OptIn(DelicateCoroutinesApi::class)
fun <K, V> Caffeine<K, V>.buildSuspend(
    scope: CoroutineScope = GlobalScope, mappingFunction: suspend (K) -> V
): AsyncLoadingCache<K, V> {
    return buildAsync { key, _ ->
        scope.future {
            mappingFunction(key)
        }
    }
}
