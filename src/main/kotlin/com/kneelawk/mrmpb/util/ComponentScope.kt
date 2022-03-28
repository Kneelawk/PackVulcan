package com.kneelawk.mrmpb.util

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ComponentScope(lifecycle: Lifecycle, context: CoroutineContext = Dispatchers.Main + SupervisorJob()) :
    CoroutineScope {
    override val coroutineContext = if (context[Job] != null) context else context + SupervisorJob()

    init {
        lifecycle.subscribe(object : Lifecycle.Callbacks {
            override fun onStop() {
                coroutineContext.cancelChildren()
            }

            override fun onDestroy() {
                coroutineContext.cancel()
            }
        })
    }
}