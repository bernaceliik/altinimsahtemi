package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AudioResultScreen(
    material: String,
    objectType: String,
    onNavigateToReview: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // 1. Analiz durumu
    var isAnalyzing by remember { mutableStateOf(true) }

    // 2. Analiz simülasyonu
    LaunchedEffect(Unit) {
        delay(2500)
        isAnalyzing = false
    }

    // 3. Yükleme Ekranı
    if (isAnalyzing) {
        Column(
            modifier = Modifier.fillMaxSize().background(DarkBackground),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Akustik sinyaller işleniyor...", color = White, fontSize = 16.sp)
            Text("Lütfen bekleyiniz...", color = TextGray, fontSize = 14.sp)
        }
    } else {
        // 4. Gerçek Sonuç Ekranı
        val isReal = !material.contains("Sahte", ignoreCase = true) &&
                !material.contains("Pirinç", ignoreCase = true) &&
                !material.contains("Tungsten", ignoreCase = true)

        Column(
            modifier = Modifier.fillMaxSize().background(DarkBackground).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Akustik Analiz Tamamlandı", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$material - $objectType", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(100.dp)
                        .background(color = if (isReal) GoldPrimary.copy(alpha = 0.15f) else Color(0xFF2C1A1A), shape = CircleShape)
                        .border(width = 2.dp, color = if (isReal) GoldPrimary else Color(0xFFF44336), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isReal) "✓" else "⚠️", color = if (isReal) GoldPrimary else Color(0xFFF44336), fontSize = 42.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isReal) "CİSİM GERÇEK" else "ŞÜPHELİ / SAHTE",
                    color = if (isReal) GoldPrimary else Color(0xFFF44336),
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isReal) "Nesne tınısı, veri tabanındaki orijinal rezonans değerleri ile %98.4 oranında eşleşti."
                    else "Frekans sönümlenme eğrisinde düzensizlik tespit edildi. Materyal yoğunluğu orijinalinden farklı olabilir.",
                    color = TextGray, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNavigateToReview,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Teknik Detayları Gör", color = DarkBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToHome,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.2f))
                ) {
                    Text("Kapat ve Panele Dön", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}