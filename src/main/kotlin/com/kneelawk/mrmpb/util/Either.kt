package com.kneelawk.mrmpb.util

sealed class Either<out L, out R> {
    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)
        fun <L, R> right(value: R): Either<L, R> = Right(value)

        fun <L, R> leftOr(value: L?, right: R): Either<L, R> {
            return value?.let { left(it) } ?: right(right)
        }
    }

    fun <T> switch(ifLeft: (L) -> T, ifRight: (R) -> T): T {
        return when (this) {
            is Left -> ifLeft(this.value)
            is Right -> ifRight(this.value)
        }
    }

    fun ifLeft(ifLeft: (L) -> Unit) {
        switch(ifLeft) {}
    }

    fun ifRight(ifRight: (R) -> Unit) {
        switch({}, ifRight)
    }

    fun isLeft(): Boolean {
        return when (this) {
            is Left -> true
            is Right -> false
        }
    }

    fun isRight(): Boolean {
        return when (this) {
            is Left -> false
            is Right -> true
        }
    }

    fun leftOrNull(): L? {
        return when (this) {
            is Left -> value
            is Right -> null
        }
    }

    private data class Left<L, R>(val value: L) : Either<L, R>()

    private data class Right<L, R>(val value: R) : Either<L, R>()
}
