package com.kneelawk.packvulcan.util

import java.time.Duration
import java.time.Period
import java.time.ZonedDateTime
import java.util.*

inline fun <R : Collection<*>?, T : R> T?.ifNullOrEmpty(default: () -> R): R {
    return if (this.isNullOrEmpty()) {
        default()
    } else {
        this
    }
}

fun <K, V> treeMapOf(vararg pairs: Pair<K, V>): TreeMap<K, V> {
    val map = TreeMap<K, V>()

    for (pair in pairs) {
        map[pair.first] = pair.second
    }

    return map
}

private val suffixes = treeMapOf(1_000 to "k", 1_000_000 to "M", 1_000_000_000 to "G")
fun Int.formatHumanReadable(): String {
    if (this == Int.MIN_VALUE) return (this + 1).formatHumanReadable()
    if (this < 0) return "-" + (-this).formatHumanReadable()
    if (this < 1000) return toString()

    val entry = suffixes.floorEntry(this)
    val divideBy = entry.key
    val suffix = entry.value

    val truncated = this / (divideBy / 10)
    val hasDecimal = truncated < 100 && truncated % 10 != 0
    return (if (hasDecimal) truncated / 10.0 else truncated / 10).toString() + suffix
}

fun ZonedDateTime.formatRelative(): String {
    val now = ZonedDateTime.now()
    val period = Period.between(toLocalDate(), now.toLocalDate())
    val duration = Duration.between(this, now)

    val hours = duration.toHours()
    val minutes = duration.toMinutes()
    val seconds = duration.toSeconds()

    val sb = StringBuilder()

    if (period.years > 0) {
        sb.append(period.years)
        if (period.years > 1) {
            sb.append(" years")
        } else {
            sb.append(" year")
        }
    } else if (period.months > 0) {
        sb.append(period.months)
        if (period.months > 1) {
            sb.append(" months")
        } else {
            sb.append(" month")
        }
    } else if (period.days > 0) {
        sb.append(period.days)
        if (period.days > 1) {
            sb.append(" days")
        } else {
            sb.append(" day")
        }
    } else if (hours > 0) {
        sb.append(hours)
        if (hours > 1) {
            sb.append(" hours")
        } else {
            sb.append(" hour")
        }
    } else if (minutes > 0) {
        sb.append(minutes)
        if (minutes > 1) {
            sb.append(" minutes")
        } else {
            sb.append(" minute")
        }
    } else {
        sb.append(seconds)
        if (seconds == 1L) {
            sb.append(" second")
        } else {
            sb.append(" seconds")
        }
    }

    sb.append(" ago")

    return sb.toString()
}
