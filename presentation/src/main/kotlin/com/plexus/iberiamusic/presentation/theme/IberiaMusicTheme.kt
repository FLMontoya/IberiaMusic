package com.plexus.iberiamusic.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Dark palette. Every text/background pair is checked in IberiaPaletteTest.
internal val IberiaBackground = Color(0xFF121212)
internal val IberiaSurface = Color(0xFF181818)
internal val IberiaElevated = Color(0xFF282828)
internal val IberiaGreen = Color(0xFF1DB954)
internal val IberiaWhite = Color(0xFFFFFFFF)
internal val IberiaGray = Color(0xFFB3B3B3)
internal val IberiaOutline = Color(0xFF727272)
internal val IberiaError = Color(0xFFF3727F)

// The search field is a white pill with dark text.
internal val SearchFieldContainer = Color(0xFFFFFFFF)
internal val SearchFieldText = Color(0xFF000000)
internal val SearchFieldLabel = Color(0xFF535353)

internal val IberiaColorScheme =
    darkColorScheme(
        primary = IberiaGreen,
        onPrimary = Color.Black,
        background = IberiaBackground,
        onBackground = IberiaWhite,
        surface = IberiaSurface,
        onSurface = IberiaWhite,
        surfaceVariant = IberiaElevated,
        onSurfaceVariant = IberiaGray,
        outline = IberiaOutline,
        error = IberiaError,
        onError = Color.Black,
    )

private val baseTypography = Typography()

/** Bold headings and semi-bold titles for screen and track names. */
internal val IberiaTypography =
    baseTypography.copy(
        headlineMedium = baseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )

/** Square-ish corners (4 dp) on covers and cards. */
internal val IberiaShapes =
    Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
    )

/** Material 3 theme with a dark look: always dark, green accent, bold type. */
@Composable
fun IberiaMusicTheme(content: @Composable () -> Unit) {
  MaterialTheme(
      colorScheme = IberiaColorScheme,
      typography = IberiaTypography,
      shapes = IberiaShapes,
      content = content,
  )
}
