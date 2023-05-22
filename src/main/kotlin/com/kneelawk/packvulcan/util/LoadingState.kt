package com.kneelawk.packvulcan.util

sealed class LoadingState<out T> {
    object Loading : LoadingState<Nothing>()
    object Error : LoadingState<Nothing>()
    data class Loaded<T>(val data: T) : LoadingState<T>()

    inline fun <R> map(mapper: (T) -> R): LoadingState<R> {
        return when (this) {
            Error -> Error
            is Loaded -> Loaded(mapper(data))
            Loading -> Loading
        }
    }

    inline fun <R> flatMap(mapper: (T) -> LoadingState<R>): LoadingState<R> {
        return when (this) {
            Error -> Error
            is Loaded -> when (val mapped = mapper(data)) {
                Error -> Error
                is Loaded -> Loaded(mapped.data)
                Loading -> Loading
            }
            Loading -> Loading
        }
    }
}
