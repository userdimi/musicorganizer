package de.colognecode.musicorganizer.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun MusicOrganizerTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content,
        colors = if (isDarkTheme) DarkColors else LightColors,
        typography = MusicOrganizerTypographie
    )
}

private val LightColors = lightColors(
    primary = Purple200,
    primaryVariant = Purple_700,
    onPrimary = White,
    secondary = Teal_200,
    secondaryVariant = Teal_700,
    onSecondary = Black,
    onSurface = Black
)

private val DarkColors = darkColors(
    primary = Purple500,
    primaryVariant = Purple_700,
    onPrimary = Black,
    secondary = Teal_200,
    secondaryVariant = Teal_200,
    onSecondary = Black,
    onSurface = White
)
