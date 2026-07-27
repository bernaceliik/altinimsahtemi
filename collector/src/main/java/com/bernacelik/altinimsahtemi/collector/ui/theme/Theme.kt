package com.bernacelik.altinimsahtemi.collector.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// Tüketici uygulamasıyla aynı palet — saha aracı da aynı markanın parçası.
val DarkBackground = Color(0xFF0F1424)
val DarkSurface = Color(0xFF1E2235)
val GoldPrimary = Color(0xFFF2A33A)
val TextGray = Color(0xFF8E93A8)
val White = Color(0xFFFFFFFF)

// Ölçüm durumlarını anlatan semantik renkler
val SignalGreen = Color(0xFF4CAF50)
val SignalRed = Color(0xFFF44336)
val SignalAmber = Color(0xFFFFC107)

private val CollectorColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBackground,
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    error = SignalRed,
)

@Composable
fun CollectorTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.statusBarColor = DarkBackground.toArgb()
        }
    }
    MaterialTheme(
        colorScheme = CollectorColorScheme,
        typography = Typography(),
        content = content,
    )
}
