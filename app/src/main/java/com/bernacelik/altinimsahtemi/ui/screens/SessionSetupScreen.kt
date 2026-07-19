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

@Composable
fun SessionSetupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEnvironmentControl: (material: String, objectType: String, surface: String) -> Unit,
    onNavigateToProfile: () -> Unit // YENİ: Profil yönlendirme parametresi eklendi!
) {
    // Tasarımdaki varsayılan seçimleri önceden aktif ediyoruz (24K, Gram, Mermer)
    var selectedMaterial by remember { mutableStateOf("24K") }
    var selectedObjectType by remember { mutableStateOf("Gram") }
    var selectedSurface by remember { mutableStateOf("Mermer") }

    val materials = listOf("24K", "22K", "14K", "Pirinç", "Tungsten")
    val objectTypes = listOf("Çeyrek", "Gram", "Bilezik", "Yüzük")
    val surfaces = listOf("Mermer", "Ahşap", "Cam")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
    ) {
        // Üst Kısım: Kapat simgesi, Başlık ve Sağdaki Kullanıcı Logosu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✕",
                    color = White,
                    fontSize = 22.sp,
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

            // MODİFİKASYON: Artık bu profil dairesine tıklanınca profil ekranına gidecek!
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GoldPrimary, CircleShape)
                    .clickable { onNavigateToProfile() }, // Tıklama aksiyonu eklendi
                contentAlignment = Alignment.Center
            ) {
                Text("C", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Adım Çizgisi ve Bilgisi (Adım 1/3)
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(TextGray.copy(alpha = 0.2f))
            ) {
                // Adım 1 çizgisi (Ekranın 3'te 1'ini kaplayacak şekilde altın rengi)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.33f)
                        .fillMaxHeight()
                        .background(GoldPrimary)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adım 1/3 • Ortam Kurulumu",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Başlıklar ve Açıklama
        Text(
            text = "Oturum Kurulumu",
            color = White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Yüksek sadakatli kayıt için yakalama parametrelerini tanımlayın.",
            color = TextGray,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 1. MATERYAL SEÇİM ALANI
        Text(
            text = "MATERYAL",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            materials.take(4).forEach { material ->
                SelectableChip(
                    text = material,
                    isSelected = selectedMaterial == material,
                    onClick = { selectedMaterial = material }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SelectableChip(
                text = materials[4], // Tungsten
                isSelected = selectedMaterial == materials[4],
                onClick = { selectedMaterial = materials[4] }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. NESNE TÜRÜ SEÇİM ALANI
        Text(
            text = "NESNE TÜRÜ",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            objectTypes.forEach { type ->
                SelectableChip(
                    text = type,
                    isSelected = selectedObjectType == type,
                    onClick = { selectedObjectType = type }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3. DARBE YÜZEYİ SEÇİM ALANI (En Kritik Mühendislik Seçimi)
        Text(
            text = "DARBE YÜZEYİ",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            surfaces.forEach { surface ->
                SelectableChip(
                    text = surface,
                    isSelected = selectedSurface == surface,
                    onClick = { selectedSurface = surface }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // İleri Butonu
        Button(
            onClick = {
                onNavigateToEnvironmentControl(selectedMaterial, selectedObjectType, selectedSurface)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "İleri: Çevre Kontrolü",
                color = DarkBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tasarımdaki Alt Bar Simülasyonu (Sabit yer kaplaması için)
        Spacer(modifier = Modifier.height(60.dp))
    }
}

// Seçilebilir Özel Filtre Buton Bileşeni (Chip)
@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) GoldPrimary else TextGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) GoldPrimary else TextGray,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}