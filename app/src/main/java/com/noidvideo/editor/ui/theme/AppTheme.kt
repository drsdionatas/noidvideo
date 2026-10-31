package com.noidvideo.editor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NoidGreen = Color(0xFF00E5A0)
val NoidBlack = Color(0xFF000000)
val NoidSurface = Color(0xFF121212)
val NoidSurfaceHigh = Color(0xFF1E1E1E)
val NoidText = Color(0xFFEDEDED)
val NoidTextDim = Color(0xFF8A8A8A)

private val NoidColorScheme = darkColorScheme(
    primary = NoidGreen,
    onPrimary = NoidBlack,
    secondary = NoidGreen,
    onSecondary = NoidBlack,
    background = NoidBlack,
    onBackground = NoidText,
    surface = NoidSurface,
    onSurface = NoidText,
    surfaceVariant = NoidSurfaceHigh,
    onSurfaceVariant = NoidTextDim
)

@Composable
fun NoidVideoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NoidColorScheme,
        content = content
    )
}
