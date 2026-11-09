package com.swim.abrechnung.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object TimeUtils {
    /**
     * Formats a decimal number German-style without unnecessary decimals,
     * e.g. 1.5 -> "1,5", 2.0 -> "2", 1.25 -> "1,25".
     */
    fun formatNumber(value: Double): String {
        val df = DecimalFormat("0.##", DecimalFormatSymbols(Locale.GERMAN))
        return df.format(value)
    }

    /**
     * Calculates duration in minutes between two time strings in "HH:mm" format.
     */
    fun calculateDurationMinutes(start: String, end: String): Int {
        try {
            val startParts = start.split(":")
            val endParts = end.split(":")
            if (startParts.size != 2 || endParts.size != 2) return 0
            
            val startTotalMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
            val endTotalMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
            
            val diff = endTotalMinutes - startTotalMinutes
            return if (diff < 0) diff + (24 * 60) else diff // Handle midnight wrap if necessary
        } catch (e: Exception) {
            return 0
        }
    }

    /**
     * Formats minutes into "H:mm Std." string.
     */
    fun formatMinutesToHours(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) "$hours Std." else String.format("%d:%02d Std.", hours, minutes)
    }
}
