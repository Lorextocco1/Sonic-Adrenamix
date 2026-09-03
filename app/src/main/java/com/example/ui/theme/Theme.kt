package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = SonicGold,
    secondary = NeonOrange,
    tertiary = PlatinumSilver,
    background = SegaBlue,
    surface = SegaBlueDark,
    onPrimary = SegaBlue,
    onSecondary = SegaBlue,
    onBackground = Color.White,
    onSurface = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for Sonic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
