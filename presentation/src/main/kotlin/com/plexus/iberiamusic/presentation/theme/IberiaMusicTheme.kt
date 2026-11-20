package com.plexus.iberiamusic.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 theme following the system light/dark setting. On Android 12 and later it uses the
 * colors derived from the user's wallpaper ([dynamicColor]); before that, or when it is turned off,
 * it uses the default accessible palette.
 */
@Composable
fun IberiaMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  MaterialTheme(
      colorScheme =
          chooseColorScheme(
              darkTheme = darkTheme,
              dynamicColor = dynamicColor,
              sdkInt = Build.VERSION.SDK_INT,
              dynamicLight = { dynamicLightColorScheme(context) },
              dynamicDark = { dynamicDarkColorScheme(context) },
          ),
      content = content,
  )
}

internal fun chooseColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    sdkInt: Int,
    dynamicLight: () -> ColorScheme,
    dynamicDark: () -> ColorScheme,
): ColorScheme =
    when {
      dynamicColor && sdkInt >= Build.VERSION_CODES.S ->
          if (darkTheme) dynamicDark() else dynamicLight()
      darkTheme -> darkColorScheme()
      else -> lightColorScheme()
    }
