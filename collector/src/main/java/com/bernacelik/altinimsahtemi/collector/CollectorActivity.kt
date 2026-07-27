package com.bernacelik.altinimsahtemi.collector

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bernacelik.altinimsahtemi.collector.ui.CollectorNavigation
import com.bernacelik.altinimsahtemi.collector.ui.theme.CollectorTheme

class CollectorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Saha oturumları uzun sürer ve operatörün elleri numuneyle meşguldür;
        // ekranın kararması dinlemeyi yarıda keserdi.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            val container = (application as CollectorApp).container
            var hasPermission by remember { mutableStateOf(container.hasRecordPermission()) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted -> hasPermission = granted }

            CollectorTheme {
                CollectorNavigation(
                    hasPermission = hasPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                )
            }
        }
    }
}
