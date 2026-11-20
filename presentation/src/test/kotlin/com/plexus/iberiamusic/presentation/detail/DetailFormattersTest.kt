package com.plexus.iberiamusic.presentation.detail

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
  fun `formats an ISO release date for the given locale`() {
    assertEquals("October 31, 1975", formatReleaseDate("1975-10-31T07:00:00Z", Locale.US))
  }

  @Test
  fun `falls back to the original text when the date cannot be parsed`() {
    assertEquals("someday", formatReleaseDate("someday", Locale.US))
  }

  @Test
  fun `asks iTunes for a larger artwork than the thumbnail`() {
    assertEquals(
        "https://is1.mzstatic.com/image/thumb/Music/a/600x600bb.jpg",
        largeArtworkUrl("https://is1.mzstatic.com/image/thumb/Music/a/100x100bb.jpg"),
    )
  }

  @Test
  fun `keeps an artwork url that has no size to replace`() {
    assertEquals("https://example.com/cover.jpg", largeArtworkUrl("https://example.com/cover.jpg"))
  }
}
