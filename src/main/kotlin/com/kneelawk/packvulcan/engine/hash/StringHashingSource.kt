package com.kneelawk.packvulcan.engine.hash

import okio.ForwardingSource
import okio.Source

abstract class StringHashingSource(source: Source) : ForwardingSource(source) {
    abstract fun hashString(): String
}