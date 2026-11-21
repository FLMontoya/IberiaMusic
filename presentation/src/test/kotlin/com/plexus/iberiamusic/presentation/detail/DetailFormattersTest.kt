package com.plexus.iberiamusic.presentation.detail

import java.time.LocalDate
import java.util.Locale
import kotlin.test.assertEquals
import org.junit.Test

class DetailFormattersTest {

  @Test
  fun `formats a duration as minutes and padded seconds`() {
    assertEquals("5:54", formatDuration(354_000L))
  }

  @Test
  fun `pads seconds below ten`() {
    assertEquals("3:05", formatDuration(185_000L))
  }

  @Test
  fun `formats durations shorter than a minute`() {
    assertEquals("0:59", formatDuration(59_000L))
  }

  @Test
  fun `drops the milliseconds instead of rounding up`() {
    assertEquals("0:01", formatDuration(1_999L))
  }

  @Test
  fun `keeps counting minutes past one hour`() {
    assertEquals("61:01", formatDuration(3_661_000L))
  }

  @Test
  fun `formats a release date for the given locale`() {
    assertEquals("October 31, 1975", formatReleaseDate(LocalDate.of(1975, 10, 31), Locale.US))
  }

  @Test
  fun `formats the same release date differently in another locale`() {
    assertEquals(
        "31 de octubre de 1975",
        formatReleaseDate(LocalDate.of(1975, 10, 31), Locale("es", "ES")),
    )
  }
}
