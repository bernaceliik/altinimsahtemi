package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun EnvironmentControlScreen(
    material: String,
    objectType: String,
    surface: String,
    onNavigateBack: () -> Unit,
    onNavigateToNext: () -> Unit,
    onNavigateToProfile: () -> Unit // YENİ: Profil yönlendirme parametresi eklendi!
) {
    // Canlı desibel simülasyonu (30-33 dB arasında tatlı tatlı oynayacak)
    var currentDb by remember { mutableStateOf(31) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            currentDb = (30..33).random()
        }
    }

    // Osiloskop dalgasının akması için sonsuz animasyon zamanlayıcısı
    val infiniteTransition = rememberInfiniteTransition(label = "oscilloscope")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
    ) {
        // Üst Bar: Geri oku, Başlık ve Kullanıcı Logosu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "←",
                    color = White,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(end = 16.dp)
                )
                Text(
                    text = "AudioCollect",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // MODİFİKASYON: Artık bu dairesel profil logosuna tıklanınca profil sayfana uçacak!
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

        // Adım Çizgisi ve Bilgisi (Adım 2/3)
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(TextGray.copy(alpha = 0.2f))
            ) {
                // Adım 2 çizgisi (Ekranın 3'te 2'sini kaplayacak şekilde altın rengi)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .fillMaxHeight()
                        .background(GoldPrimary)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adım 2/3 • Ortam Kontrolü",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ortam Kontrolü",
            color = White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 1. Desibel Ölçüm Çemberi (Yeşil Halkalı Büyük Alan)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(2.dp, Color(0xFF4CAF50), CircleShape), // Dengeli durumu belirten yeşil halka
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentDb",
                        color = White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "dB",
                        color = TextGray,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ORTAM SEVİYESİ • DENGELİ",
                color = Color(0xFF4CAF50),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Gerçek Zamanlı Osiloskop Alanı
        Text(
            text = "GERÇEK ZAMANLI OSİLOSKOP • 44.1 kHz",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tasarımdaki şık dalgalı kutu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(DarkSurface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val path = Path()

                // Canlı hareket eden sinüs dalgası çizimi
                path.moveTo(0f, height / 2)
                for (x in 0..width.toInt() step 5) {
                    val y = (height / 2) + sin((x.toFloat() / width * 4 * Math.PI.toFloat()) + phaseShift) * 15f
                    path.lineTo(x.toFloat(), y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFF4CAF50), // Tasarımdaki yeşil ses izi dalgası
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Ortam sessiz" Onay Kutusu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF132A1F), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Ortam sessiz - ses için hazır", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Arka plan gürültü tabanı: -84dBFS",
            color = TextGray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Kayda Geç Butonu
        Button(
            onClick = onNavigateToNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Kayda Geç",
                color = DarkBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tasarımdaki Alt Bar Boşluğu
        Spacer(modifier = Modifier.height(60.dp))
    }
}