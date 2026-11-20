package com.plexus.iberiamusic.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorSchemeSelectionTest {

  private val dynamicLight = lightColorScheme(primary = Color.Red)
  private val dynamicDark = darkColorScheme(primary = Color.Green)

  private fun choose(darkTheme: Boolean, dynamicColor: Boolean, sdkInt: Int): ColorScheme =
      chooseColorScheme(
          darkTheme = darkTheme,
          dynamicColor = dynamicColor,
          sdkInt = sdkInt,
          dynamicLight = { dynamicLight },
          dynamicDark = { dynamicDark },
      )

  /** [ColorScheme] has no structural equality, so compare the roles that tell the schemes apart. */
  private fun assertSameScheme(expected: ColorScheme, actual: ColorScheme) {
    assertEquals(expected.primary, actual.primary)
    assertEquals(expected.surface, actual.surface)
  }

  @Test
  fun `uses the wallpaper colors on Android 12 and later`() {
    assertSameScheme(dynamicLight, choose(darkTheme = false, dynamicColor = true, sdkInt = 31))
    assertSameScheme(dynamicDark, choose(darkTheme = true, dynamicColor = true, sdkInt = 36))
  }

  @Test
  fun `falls back to the static palette before Android 12`() {
    assertSameScheme(
        lightColorScheme(),
        choose(darkTheme = false, dynamicColor = true, sdkInt = 30),
    )
    assertSameScheme(darkColorScheme(), choose(darkTheme = true, dynamicColor = true, sdkInt = 26))
  }

  @Test
  fun `uses the static palette when dynamic color is turned off`() {
    assertSameScheme(
        lightColorScheme(),
        choose(darkTheme = false, dynamicColor = false, sdkInt = 36),
    )
    assertSameScheme(darkColorScheme(), choose(darkTheme = true, dynamicColor = false, sdkInt = 36))
  }
}
