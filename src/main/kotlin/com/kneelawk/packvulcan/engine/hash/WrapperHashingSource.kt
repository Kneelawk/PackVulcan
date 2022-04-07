package com.kneelawk.packvulcan.engine.hash

import okio.HashingSource

class WrapperHashingSource(private val hashingSource: HashingSource) : StringHashingSource(hashingSource) {
    override fun hashString(): String {
        return hashingSource.hash.hex()
    }
}