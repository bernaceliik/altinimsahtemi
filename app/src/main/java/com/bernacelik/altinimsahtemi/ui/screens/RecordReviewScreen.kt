package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.bernacelik.altinimsahtemi.ui.theme.*

@Composable
fun RecordReviewScreen(
    duration: String,
    onSaveAndUpload: () -> Unit,
    onDiscardAndRepeat: () -> Unit,
    onNavigateToProfile: () -> Unit // YENİ: Profil yönlendirme parametresi eklendi!
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
    ) {
        // Üst Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "←", color = White, fontSize = 24.sp,
                    modifier = Modifier.clickable { onDiscardAndRepeat() }.padding(end = 16.dp)
                )
                Text("Kaydı İncele", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // MODİFİKASYON: Profil dairesine tıklanınca profil sayfasına gitmesi sağlandı!
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GoldPrimary, CircleShape)
                    .clickable { onNavigateToProfile() }, // Tıklama aksiyonu bağlandı
                contentAlignment = Alignment.Center
            ) {
                Text("C", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Büyük Dalga Önizleme Kutusu (Placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DarkSurface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(40.dp).background(GoldPrimary, RoundedCornerShape(8.dp)))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Teknik Bilgiler Başlığı
        Text(
            text = "TEKNİK BİLGİLER",
            color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Teknik Bilgiler Kartı
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TechnicalInfoRow(label = "Dosya boyutu", value = "2.4MB")
                TechnicalInfoRow(label = "Süre", value = duration)
                TechnicalInfoRow(label = "Örnekleme hızı", value = "44.1kHz")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Başarılı / Optimum Sinyal Kartı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF132A1F), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Optimum sinyal tespit edildi", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "-1.2dB'de ölçülen etki tepe noktası\nve pürüzsüz sönümlene eğrisi.\nVeri akışında kırpılma yok.",
                    color = TextGray, fontSize = 12.sp, lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Butonlar
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSaveAndUpload,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet ve Yükle", color = DarkBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDiscardAndRepeat,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TextGray.copy(alpha = 0.2f))
            ) {
                Text("Sil ve Tekrarla", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun TechnicalInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Text(value, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}