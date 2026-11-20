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

// Dark, Spotify-like palette. Every text/background pair is checked in SpotifyPaletteTest.
internal val SpotifyBackground = Color(0xFF121212)
internal val SpotifySurface = Color(0xFF181818)
internal val SpotifyElevated = Color(0xFF282828)
internal val SpotifyGreen = Color(0xFF1DB954)
internal val SpotifyWhite = Color(0xFFFFFFFF)
internal val SpotifyGray = Color(0xFFB3B3B3)
internal val SpotifyOutline = Color(0xFF727272)
internal val SpotifyError = Color(0xFFF3727F)

// The search field is a white pill with dark text, as in Spotify's search.
internal val SearchFieldContainer = Color(0xFFFFFFFF)
internal val SearchFieldText = Color(0xFF000000)
internal val SearchFieldLabel = Color(0xFF535353)

internal val SpotifyColorScheme =
    darkColorScheme(
        primary = SpotifyGreen,
        onPrimary = Color.Black,
        background = SpotifyBackground,
        onBackground = SpotifyWhite,
        surface = SpotifySurface,
        onSurface = SpotifyWhite,
        surfaceVariant = SpotifyElevated,
        onSurfaceVariant = SpotifyGray,
        outline = SpotifyOutline,
        error = SpotifyError,
        onError = Color.Black,
    )

private val baseTypography = Typography()

/** Bold headings and semi-bold titles, the way Spotify sets its screen and track names. */
internal val SpotifyTypography =
    baseTypography.copy(
        headlineMedium = baseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )

/** Spotify keeps square-ish corners (4 dp) on covers and cards. */
internal val SpotifyShapes =
    Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
    )

/** Material 3 theme with a Spotify-like look: always dark, green accent, bold type. */
@Composable
fun IberiaMusicTheme(content: @Composable () -> Unit) {
  MaterialTheme(
      colorScheme = SpotifyColorScheme,
      typography = SpotifyTypography,
      shapes = SpotifyShapes,
      content = content,
  )
}
