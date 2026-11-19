package com.plexus.iberiamusic.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Material 3 theme following the system light/dark setting, using the default accessible palette.
 */
@Composable
fun IberiaMusicTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  MaterialTheme(
      colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
      content = content,
  )
}
