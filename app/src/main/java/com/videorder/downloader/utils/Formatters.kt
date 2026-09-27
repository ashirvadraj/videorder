package com.videorder.downloader.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return if (index == 0) {
            String.format(Locale.US, "%d B", bytes)
        } else {
            String.format(Locale.US, "%.1f %s", value, units[index])
        }
    }

    fun formatSpeed(bytesPerSec: Long): String {
        return if (bytesPerSec <= 0) {
            "0 KB/s"
        } else {
            "${formatBytes(bytesPerSec)}/s"
        }
    }

    fun formatEta(seconds: Long): String {
        if (seconds <= 0) return "--"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hours > 0 -> String.format(Locale.US, "%dh %02dm", hours, minutes)
            minutes > 0 -> String.format(Locale.US, "%dm %02ds", minutes, secs)
            else -> String.format(Locale.US, "%ds", secs)
        }
    }

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, secs)
        }
    }

    fun formatDate(epochMs: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun getDateBucket(epochMs: Long): String {
        val itemCal = Calendar.getInstance().apply { timeInMillis = epochMs }
        val nowCal = Calendar.getInstance()

        val isSameDay = itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                itemCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        if (isSameDay) return "Today"

        nowCal.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                itemCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        if (isYesterday) return "Yesterday"

        nowCal.add(Calendar.DAY_OF_YEAR, -6)
        if (epochMs >= nowCal.timeInMillis) return "Earlier this week"

        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }
}
