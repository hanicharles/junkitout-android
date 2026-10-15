package com.junkitout.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object JColors {
    val DeepGreen = Color(0xFF07301A)
    val ForestGreen = Color(0xFF0B4A24)
    val Primary = Color(0xFF14793A)
    val Mid = Color(0xFF2E9A50)
    val Light = Color(0xFFE8F5E9)
    val Mint = Color(0xFFD5EDD8)
    val Text = Color(0xFF0F2A18)
    val Subtle = Color(0xFF6B7C70)
    val Border = Color(0xFFDCE7DD)
    val Red = Color(0xFFE5484D)
    val Bg = Color(0xFFF6FAF6)
}

@Composable
fun JunkItOutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = JColors.Primary,
            onPrimary = Color.White,
            background = Color.White,
            surface = Color.White,
        ),
        content = content
    )
}
