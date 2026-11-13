package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val StudyLightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = PureWhite,
    primaryContainer = SkyTint,
    onPrimaryContainer = DeepIndigo,
    secondary = VividPurple,
    onSecondary = PureWhite,
    secondaryContainer = SoftLavender,
    onSecondaryContainer = ElectricViolet,
    tertiary = CyanAccent,
    onTertiary = PureWhite,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF0E7490),
    background = SoftCloud,
    onBackground = Color(0xFF0F172A),
    surface = PureWhite,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE8EEFF),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = RoseAlert,
    onError = PureWhite
)

private val StudyDarkColorScheme = darkColorScheme(
    primary = LightBlueAccent,
    onPrimary = Color(0xFF0A1938),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = LightPurpleAccent,
    onSecondary = Color(0xFF230C52),
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = SoftLavender,
    tertiary = Color(0xFF22D3EE),
    onTertiary = Color(0xFF083344),
    tertiaryContainer = Color(0xFF164E63),
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = NightNavy,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkBorder,
    outlineVariant = Color(0xFF242E57),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun StudyTimeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (isDark) StudyDarkColorScheme else StudyLightColorScheme,
        typography = StudyTypography,
        content = content
    )
}
