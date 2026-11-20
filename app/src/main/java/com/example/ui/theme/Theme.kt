package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = Color(0xFF020617),
    primaryContainer = DarkEmeraldDeep,
    onPrimaryContainer = DarkEmeraldPrimary,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    tertiary = DarkAmber,
    onTertiary = Color(0xFF020617),
    background = DarkBg,
    onBackground = DarkSlate100,
    surface = DarkCard,
    onSurface = DarkSlate100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSlate300,
    outline = DarkSlate700,
    outlineVariant = DarkSlate800,
    error = DarkRed,
    onError = Color.White,
    errorContainer = DarkRedBg,
    onErrorContainer = Color(0xFFFCA5A5),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = EmeraldPrimary,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    tertiary = AmberTimer,
    onTertiary = Color.White,
    background = ParchmentBg,
    onBackground = Slate800,
    surface = ParchmentCard,
    onSurface = Slate800,
    surfaceVariant = ParchmentSoft,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Color(0xFFE5E0D5),
    error = RedMistake,
    onError = Color.White,
    errorContainer = RedSoft,
    onErrorContainer = RedMistake,
  )

@Composable
fun MuhasabaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
