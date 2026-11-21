package com.plexus.iberiamusic.presentation.detail

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

/** Formats [date] in the long style of [locale], for example "October 31, 1975". */
internal fun formatReleaseDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
