package com.plexus.iberiamusic.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.assertTrue
import org.junit.Test

/** WCAG contrast ratio between two colors: from 1:1 (equal) to 21:1 (black on white). */
private fun contrast(a: Color, b: Color): Double {
  val lighter = maxOf(a.luminance(), b.luminance()).toDouble()
  val darker = minOf(a.luminance(), b.luminance()).toDouble()
  return (lighter + 0.05) / (darker + 0.05)
}

class SpotifyPaletteTest {

  private val scheme = SpotifyColorScheme

  private fun assertText(foreground: Color, background: Color, name: String) {
    val ratio = contrast(foreground, background)
    assertTrue(ratio >= 4.5, "$name has contrast $ratio, below 4.5:1")
  }

  @Test
  fun `main text is readable on the background and the surfaces`() {
    assertText(scheme.onBackground, scheme.background, "title on background")
    assertText(scheme.onSurface, scheme.surface, "title on surface")
    assertText(scheme.onSurfaceVariant, scheme.background, "subtitle on background")
    assertText(scheme.onSurfaceVariant, scheme.surfaceVariant, "subtitle on elevated surface")
  }

  @Test
  fun `the green accent and the error color are readable as text on the background`() {
    assertText(scheme.primary, scheme.background, "green on background")
    assertText(scheme.error, scheme.background, "error on background")
  }

  @Test
  fun `content on the accent colors is readable`() {
    assertText(scheme.onPrimary, scheme.primary, "text on green")
    assertText(scheme.onError, scheme.error, "text on error")
  }

  @Test
  fun `the search field text and label are readable on the white pill`() {
    assertText(SearchFieldText, SearchFieldContainer, "search text")
    assertText(SearchFieldLabel, SearchFieldContainer, "search label")
  }

  @Test
  fun `outlines and the white pill stand out from the background by at least 3 to 1`() {
    assertTrue(contrast(scheme.outline, scheme.background) >= 3.0)
    assertTrue(contrast(SearchFieldContainer, scheme.background) >= 3.0)
    assertTrue(contrast(scheme.primary, scheme.background) >= 3.0)
  }
}
