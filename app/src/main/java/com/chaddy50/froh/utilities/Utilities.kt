package com.chaddy50.froh.utilities

import java.text.Normalizer
import java.util.Locale


fun stripArticles(name: String): String {
    val prefixes = listOf("The ", "A ", "An ")
    for (prefix in prefixes) {
        if (name.startsWith(prefix, ignoreCase = true)) {
            return name.substring(prefix.length).trim()
        }
    }
    return name.trim()
}

fun stripDiacritics(input: String): String {
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}

fun normalizeYear(year: String?): String {
    if (year.isNullOrBlank() || year == "0") {
        return "Unknown Year"
    }
    return year.take(4)
}

fun parseTrackNumber(trackNumberAsString: String?): Int {
    if (trackNumberAsString == null) return -1
    return if (trackNumberAsString.contains("/")) {
        trackNumberAsString.substringBefore("/").toIntOrNull() ?: -1
    } else {
        trackNumberAsString.toIntOrNull() ?: -1
    }
}

fun chooseAlbumLabel(isClassical: Boolean): String =
    if (isClassical) "works" else "albums"

fun chooseArtistLabel(isClassical: Boolean): String =
    if (isClassical) "composers" else "artists"

fun formatMillisecondsIntoMinutesAndSeconds(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}