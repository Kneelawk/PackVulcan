package com.kneelawk.mrmpb.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

object ApplicationScope : CoroutineScope {
    override val coroutineContext = Dispatchers.Main + SupervisorJob()

    fun shutdown() {
        coroutineContext.cancel()
    }
}