package com.bernacelik.altinimsahtemi.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// Altınım Sahte mi? için Figma tabanlı Koyu Renk Şeması
private val CustomDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onBackground = White,
    onSurface = White
)

@Composable
fun AltinimSahtemiTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CustomDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Bildirim çubuğunun rengini de arka planımızla uyumlu koyu lacivert yapıyoruz
            window.statusBarColor = DarkBackground.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Type.kt dosyasındaki varsayılan fontlar kullanılacak
        content = content
    )
}