package com.olgag.qrcreator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorPalette = darkColors(
    primary = Black,
    onPrimary = White,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = DarkGray,
    surface = White
)

private val LightColorPalette = lightColors(
    primary = DarkGray,
    onPrimary = LightGray,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = Background,
    surface = DarkGray
//
//    onSecondary = Black,
//    onBackground = Black,
//    onSurface =Black,

)

@Composable
fun QRCreatorTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        val view = LocalView.current
        val window = (view.context as Activity).window
        window.statusBarColor = Black.toArgb()
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}