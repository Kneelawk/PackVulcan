package com.kneelawk.packvulcan.engine.hash

import okio.ForwardingSink
import okio.Sink

abstract class StringHashingSink(sink: Sink) : ForwardingSink(sink) {
    abstract fun hashString(): String
}
