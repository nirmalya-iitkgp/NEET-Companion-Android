package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF13101C),
    surface = Color(0xFF1C182E),
    onPrimary = Color.Black,
    onBackground = Color(0xFFEADDFF),
    onSurface = Color(0xFFEADDFF),
    primaryContainer = Color(0xFF4527A0),
    secondaryContainer = Color(0xFF21005D),
    surfaceVariant = Color(0xFF2D2544)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ArtisticPrimary,
    secondary = ArtisticSecondaryVal,
    tertiary = ArtisticTertiary,
    background = ArtisticBackground,
    surface = ArtisticSurface,
    onPrimary = ArtisticOnPrimary,
    onBackground = ArtisticOnBackground,
    onSurface = ArtisticOnBackground,
    secondaryContainer = ArtisticLavender,
    primaryContainer = ArtisticLightPurple,
    surfaceVariant = ArtisticLightPurple
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor by default to highlight handcrafted Artistic Flair
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
