package ru.radiationx.data.entity.mapper

import java.util.*

fun String.appendBaseUrl(baseUrl: String): String {
    return "$baseUrl$this"
}

fun Int.secToDate(): Date = Date(secToMillis())

fun Int.secToMillis(): Long = this * 1000L