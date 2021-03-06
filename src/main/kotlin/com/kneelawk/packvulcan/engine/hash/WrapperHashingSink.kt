package com.kneelawk.packvulcan.engine.hash

import okio.HashingSink

class WrapperHashingSink(private val hashingSink: HashingSink) : StringHashingSink(hashingSink) {
    override fun hashString(): String {
        return hashingSink.hash.hex()
    }
}