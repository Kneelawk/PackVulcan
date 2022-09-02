package com.kneelawk.packvulcan.util

typealias ProgressListener = (Float, String) -> Unit

val EMPTY_PROGRESS_LISTENER: ProgressListener = { _, _ -> }
