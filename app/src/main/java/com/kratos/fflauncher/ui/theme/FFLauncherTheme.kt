package com.kratos.fflauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF58E6C5),
    secondary = Color(0xFF83A9FF),
    background = Color(0xFF080D16),
    surface = Color(0xFF111A29),
    onPrimary = Color(0xFF05130F),
    onBackground = Color(0xFFEAF1FF),
    onSurface = Color(0xFFEAF1FF)
)

@Composable
fun FFLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
