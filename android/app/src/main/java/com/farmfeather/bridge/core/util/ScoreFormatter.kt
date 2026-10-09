package com.farmfeather.bridge.core.util

import java.util.Locale

object ScoreFormatter {

    fun score(value: Int): String {
        val digits = value.toString()
        if (digits.length <= 3) return digits
        val builder = StringBuilder()
        var counter = 0
        for (index in digits.lastIndex downTo 0) {
            builder.append(digits[index])
            counter++
            if (counter % 3 == 0 && index > 0) builder.append(GROUP_SEPARATOR)
        }
        return builder.reverse().toString()
    }

    fun clock(millis: Long): String {
        val safe = if (millis < 0) 0L else millis
        val totalSeconds = safe / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun seconds(value: Int): String = clock(value * 1000L)

    fun ratio(current: Int, total: Int): String = "$current/$total"

    fun cartPace(periodMs: Long): String =
        String.format(Locale.US, "%.1fs", periodMs / 1000f)

    private const val GROUP_SEPARATOR = ','
}
