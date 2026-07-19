package com.bernacelik.altinimsahtemi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bernacelik.altinimsahtemi.ui.theme.AltinimSahtemiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AltinimSahtemiTheme {
                // MainNavigation.kt dosyasında tanımladığımız fonksiyon
                AppNavigation()
            }
        }
    }
}