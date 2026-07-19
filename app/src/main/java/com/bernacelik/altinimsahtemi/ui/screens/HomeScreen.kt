package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun HomeScreen(
    records: List<RecordItem>, // Kütüphanedeki gerçek kayıt listesi
    onNavigateToSetup: () -> Unit,
    onNavigateToTab: (route: String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val selectedTab = "Anasayfa"

    // --- ANALİZLERİ GÖR PENCERESİ İÇİN DURUM YÖNETİMİ ---
    var showGlobalAnalysisDialog by remember { mutableStateOf(false) }

    // --- GERÇEK ZAMANLI İSTATİSTİK HESAPLAMALARI ---
    val totalRecordsCount = records.size

    // Akıllı Filtreleme: Hem kısaltmaları hem de uzun isimleri yakalar
    val count24K = records.count {
        it.material.contains("24K", ignoreCase = true) || it.material.contains("24 Ayar", ignoreCase = true)
    }
    val count22K = records.count {
        it.material.contains("22K", ignoreCase = true) || it.material.contains("22 Ayar", ignoreCase = true)
    }
    val count14K = records.count {
        it.material.contains("14K", ignoreCase = true) || it.material.contains("14 Ayar", ignoreCase = true)
    }
    val countFake = records.count {
        it.material.contains("Sahte", ignoreCase = true) ||
                it.material.contains("Pirinç", ignoreCase = true) ||
                it.material.contains("Tungsten", ignoreCase = true)
    }

    // Başarı yüzdesi hesaplama (Sahte olmayanların toplam kayda oranı)
    val successRate = if (totalRecordsCount > 0) {
        ((totalRecordsCount - countFake).toFloat() / totalRecordsCount * 100).toInt()
    } else {
        100
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Üst Bar (Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoldPrimary, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AudioCollect",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Kullanıcı Baş Harf Logosu (C)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GoldPrimary, CircleShape)
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Text("C", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Ana İçerik (Kaydırılabilir Alan)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Karşılama Alanı
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Merhaba, Can", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Oturum ilerlemeniz güncellendi.", color = TextGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // İstatistik Kartları Satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bugünkü toplam kayıt.", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("$totalRecordsCount", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            if (totalRecordsCount > 0) {
                                Text("+${totalRecordsCount * 100}%", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bekleyen senk.", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("0", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔄", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tüm veriler güvende", color = TextGray, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Koleksiyon Kategorileri Alanı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Koleksiyon kategorileri", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    // Modifikasyon: Artık tıklanabilir ve diyalog penceresini açıyor!
                    Text(
                        text = "Analizleri gör",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showGlobalAnalysisDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kategori Listesi
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeCategoryRow(icon = "🌟", title = "24 Ayar Altın", count = "$count24K Kayıt")
                    HomeCategoryRow(icon = "💎", title = "22 Ayar Altın", count = "$count22K Kayıt")
                    HomeCategoryRow(icon = "💵", title = "14 Ayar Altın", count = "$count14K Kayıt", subtitle = "Doğrulanmış kalite")
                    HomeCategoryRow(icon = "⚠️", title = "Sahte / Şüpheli", count = "$countFake Kayıt", subtitle = "Yüksek öncelik", isAlert = true)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToSetup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎤", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yeni Kayıt Oturumu Başlat", color = DarkBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Donanım: Dahili Mikrofonlar (Aktif)",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Alt Gezinme Barı (Bottom Navigation)
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HomeBottomNavItem(icon = "🏠", label = "Anasayfa", isSelected = true, onClick = { onNavigateToTab("home") })
                HomeBottomNavItem(icon = "🎤", label = "Kayıt", isSelected = false, onClick = { onNavigateToTab("session_setup") })
                HomeBottomNavItem(icon = "📂", label = "Kütüphane", isSelected = false, onClick = { onNavigateToTab("library") })
                HomeBottomNavItem(icon = "⚙️", label = "Ayarlar", isSelected = false, onClick = { onNavigateToTab("settings") })
            }
        }
    }

    // ==================== GENEL AKUSTİK ANALİZ RAPORU DİYALOGU ====================
    if (showGlobalAnalysisDialog) {
        AlertDialog(
            onDismissRequest = { showGlobalAnalysisDialog = false },
            containerColor = DarkSurface,
            titleContentColor = White,
            textContentColor = TextGray,
            title = {
                Text(text = "Genel Akustik Analiz Raporu", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Kütüphanenizde biriken tüm akustik tarama oturumlarının istatistiksel dağılımı aşağıda özetlenmiştir.",
                        fontSize = 13.sp
                    )
                    Divider(color = DarkBackground, thickness = 1.dp)

                    HomeScreenAnalysisRow(label = "Toplam Taranan:", value = "$totalRecordsCount Cisim")
                    HomeScreenAnalysisRow(label = "24 Ayar Dağılımı:", value = "%${if (totalRecordsCount > 0) (count24K * 100 / totalRecordsCount) else 0}")
                    HomeScreenAnalysisRow(label = "22 Ayar Dağılımı:", value = "%${if (totalRecordsCount > 0) (count22K * 100 / totalRecordsCount) else 0}")
                    HomeScreenAnalysisRow(label = "14 Ayar Dağılımı:", value = "%${if (totalRecordsCount > 0) (count14K * 100 / totalRecordsCount) else 0}")
                    HomeScreenAnalysisRow(
                        label = "Saptanan Sahte/Riskli:",
                        value = "$countFake Adet",
                        valueColor = if (countFake > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                    )

                    Divider(color = DarkBackground, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Genel Güvenlik Skoru:", color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "%$successRate",
                            color = if (successRate < 70) Color(0xFFF44336) else Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGlobalAnalysisDialog = false }) {
                    Text("Kapat", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun HomeCategoryRow(icon: String, title: String, count: String, subtitle: String? = null, isAlert: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isAlert) Color(0xFF3A1F1F) else DarkBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(subtitle, color = if (isAlert) Color.Red else TextGray, fontSize = 11.sp)
                }
            }
        }
        Text(count, color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HomeBottomNavItem(icon: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(icon, fontSize = 20.sp, color = if (isSelected) GoldPrimary else TextGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = if (isSelected) GoldPrimary else TextGray, fontSize = 11.sp)
    }
}

// Analiz satırları için çakışma yaratmayacak özel ve yeni alt bileşen
@Composable
fun HomeScreenAnalysisRow(label: String, value: String, valueColor: Color = White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextGray, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}