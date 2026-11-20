package com.plexus.iberiamusic.presentation.detail

import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Formats [millis] as `m:ss`, truncating (not rounding) to whole seconds. */
internal fun formatDuration(millis: Long): String {
  val totalSeconds = millis / 1000
  val seconds = (totalSeconds % 60).toString().padStart(2, '0')
  return "${totalSeconds / 60}:$seconds"
}

/** Formats the date part of an ISO timestamp for [locale], or returns [raw] if it is not one. */
internal fun formatReleaseDate(raw: String, locale: Locale = Locale.getDefault()): String =
    try {
      LocalDate.parse(raw.take(ISO_DATE_LENGTH))
          .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
    } catch (_: DateTimeException) {
      raw
    }

/**
 * iTunes serves the same cover at any size by changing the size in its URL; the search results only
 * carry the 100 px thumbnail, which would look blurry in the detail.
 */
internal fun largeArtworkUrl(url: String): String = url.replace("100x100bb", "600x600bb")

private const val ISO_DATE_LENGTH = 10
