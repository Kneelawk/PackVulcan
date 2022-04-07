package com.kneelawk.packvulcan.util

sealed class Either<out L, out R> {
    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)
        fun <L, R> right(value: R): Either<L, R> = Right(value)
    }

    inline fun <T> switch(ifLeft: (L) -> T, ifRight: (R) -> T): T {
        return when (this) {
            is Left -> ifLeft(this.value)
            is Right -> ifRight(this.value)
        }
    }

    inline fun ifLeft(ifLeft: (L) -> Unit) {
        switch(ifLeft) {}
    }

    inline fun ifRight(ifRight: (R) -> Unit) {
        switch({}, ifRight)
    }

    inline fun escapeIfRight(ifRight: (R) -> Nothing): L {
        return switch({ it }, ifRight)
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

    data class Left<L, R>(val value: L) : Either<L, R>()

    data class Right<L, R>(val value: R) : Either<L, R>()
}

fun <L, R> left(value: L) = Either.left<L, R>(value)
fun <L, R> right(value: R) = Either.right<L, R>(value)

fun <L, R> leftOr(value: L?, right: R): Either<L, R> {
    return value?.let { left(it) } ?: right(right)
}
