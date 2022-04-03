package com.kneelawk.mrmpb.util

sealed class LoadingState<out T> {
    object Loading : LoadingState<Nothing>()
    object Error : LoadingState<Nothing>()
    data class Loaded<T>(val data: T) : LoadingState<T>()
}
