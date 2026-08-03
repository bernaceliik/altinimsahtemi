package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Kütüphane Veri Modeli - Tamamen korundu
data class RecordItem(
    val id: Int,
    val material: String,
    val objectType: String,
    val time: String,
    val details: String,
    val color: Color,
    val dateGroup: String // "BUGÜN" veya "DÜN"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldLibraryScreen( // Projedeki diğer kopyalarla çakışmaması için benzersiz isim verildi!
    records: List<RecordItem>,
    onNavigateToRecord: () -> Unit,
    onNavigateToTab: (route: String) -> Unit, // Ana menü geçişleri sapasağlam bağlandı
    onNavigateToProfile: () -> Unit // Profil butonu geçişi sapasağlam bağlandı
) {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tümü") }
    val selectedTab = "Kütüphane"

    // Detay penceresi için durum yönetimleri (states)
    var selectedRecordForDetail by remember { mutableStateOf<RecordItem?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // Firebase Entegrasyonu
    val user = remember { FirebaseAuth.getInstance().currentUser }
    val userId = user?.uid ?: "anonymous"
    val db = remember { FirebaseFirestore.getInstance() }
    var firestoreRecords by remember { mutableStateOf<List<RecordItem>>(emptyList()) }
    var isFetching by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != "anonymous") {
            isFetching = true
            db.collection("user_captures")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    isFetching = false
                    val list = mutableListOf<RecordItem>()
                    var counter = 1
                    for (doc in querySnapshot) {
                        val mat = doc.getString("material") ?: "24K"
                        val obj = doc.getString("objectType") ?: "Gram"
                        val surf = doc.getString("surface") ?: "Mermer"
                        val isReal = doc.getBoolean("isGenuineGold") ?: true
                        val timestamp = doc.getLong("createdAt") ?: 0L
                        
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                        val isToday = android.text.format.DateUtils.isToday(timestamp)
                        val dateGroup = if (isToday) "BUGÜN" else "GEÇMİŞ"
                        
                        list.add(
                            RecordItem(
                                id = counter++,
                                material = if (isReal) mat else "$mat (Sahte)",
                                objectType = obj,
                                time = timeStr,
                                details = "$surf zemin",
                                color = if (isReal) GoldPrimary else Color.Red,
                                dateGroup = dateGroup
                            )
                        )
                    }
                    firestoreRecords = list
                }
                .addOnFailureListener {
                    isFetching = false
                }
        }
    }

    // İstediğin gibi 24 Ayar filtresi listeye dahil edildi!
    val filters = listOf("Tümü", "24 Ayar", "22 Ayar", "14 Ayar", "Sahte")

    // Arama ve Akıllı Filtreleme İşlemi (Çift yönlü kontrol yapısı)
    val filteredRecords = firestoreRecords.filter { record ->
        val matchesSearch = record.material.contains(searchText, ignoreCase = true) || record.objectType.contains(searchText, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Tümü" -> true
            "24 Ayar" -> record.material.contains("24K", ignoreCase = true) || record.material.contains("24 Ayar", ignoreCase = true)
            "22 Ayar" -> record.material.contains("22K", ignoreCase = true) || record.material.contains("22 Ayar", ignoreCase = true)
            "14 Ayar" -> record.material.contains("14K", ignoreCase = true) || record.material.contains("14 Ayar", ignoreCase = true)
            "Sahte" -> record.material.contains("Sahte", ignoreCase = true) ||
                    record.material.contains("Pirinç", ignoreCase = true) ||
                    record.material.contains("Tungsten", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Üst Kısım
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kütüphane", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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

        if (isFetching) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else if (firestoreRecords.isEmpty()) {
            // --- BOŞ KÜTÜPHANE GÖRÜNÜMÜ ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(DarkSurface, RoundedCornerShape(20.dp))
                        .border(1.dp, TextGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📄", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Henüz kayıt yok", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "İlk kaydınızı oluşturduğunuzda burada listelenecek.",
                    color = TextGray, fontSize = 14.sp, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToRecord,
                    modifier = Modifier.width(180.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎤", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kayıt Başlat", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // --- DOLU KÜTÜPHANE GÖRÜNÜMÜ ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Arama Çubuğu
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Kayıtlarda ara...", color = TextGray) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 8.dp)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Yan Tarafa Kaydırılabilen Filtre Slider Alanı (LazyRow)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) GoldPrimary else DarkSurface,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GoldPrimary else TextGray.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) DarkBackground else White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Kayıtlar Listesi
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
                ) {
                    // Bugün Grubu
                    val todayRecords = filteredRecords.filter { it.dateGroup == "BUGÜN" }
                    if (todayRecords.isNotEmpty()) {
                        item {
                            Text("BUGÜN", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        items(todayRecords) { record ->
                            GoldRecordCard(
                                record = record,
                                onClick = {
                                    selectedRecordForDetail = record
                                    showDetailDialog = true
                                }
                            )
                        }
                    }

                    // Dün Grubu
                    val yesterdayRecords = filteredRecords.filter { it.dateGroup == "DÜN" }
                    if (yesterdayRecords.isNotEmpty()) {
                        item {
                            Text("DÜN", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        items(yesterdayRecords) { record ->
                            GoldRecordCard(
                                record = record,
                                onClick = {
                                    selectedRecordForDetail = record
                                    showDetailDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Alt Gezinme Barı (Bottom Navigation) - İstediğin gibi Anasayfa ve 🏠 yapıldı!
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LibraryBottomNavItem(icon = "🏠", label = "Anasayfa", isSelected = false, onClick = { onNavigateToTab("home") })
                LibraryBottomNavItem(icon = "🎤", label = "Kayıt", isSelected = false, onClick = { onNavigateToTab("session_setup") })
                LibraryBottomNavItem(icon = "📂", label = "Kütüphane", isSelected = true, onClick = { onNavigateToTab("library") })
                LibraryBottomNavItem(icon = "⚙️", label = "Ayarlar", isSelected = false, onClick = { onNavigateToTab("settings") })
            }
        }
    }

    // ==================== AKUSTİK RAPOR DİYALOG PENCERESİ ====================
    if (showDetailDialog && selectedRecordForDetail != null) {
        val record = selectedRecordForDetail!!
        val isFake = record.color == Color(0xFFF44336)

        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            containerColor = DarkSurface,
            titleContentColor = White,
            textContentColor = TextGray,
            title = {
                Text(text = "Akustik Analiz Detayı", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mikrofon tını darbe frekans genliği sonuçları aşağıda listelenmiştir.", fontSize = 13.sp)

                    // Derleme hatasını önlemek için standart ve güvenli Divider yapıldı
                    Divider(color = DarkBackground, thickness = 1.dp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Materyal:", color = TextGray, fontSize = 13.sp)
                        Text(record.material, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cisim Tipi:", color = TextGray, fontSize = 13.sp)
                        Text(record.objectType, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Detaylar:", color = TextGray, fontSize = 13.sp)
                        Text(record.details, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Güven Endeksi:", color = TextGray, fontSize = 13.sp)
                        Text(if (isFake) "%24.5 (Riskli)" else "%99.1 (Kararlı)", color = if (isFake) Color(0xFFF44336) else Color(0xFF4CAF50), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text("Kapat", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Çakışmayı önlemek için ismi tamamen benzersiz yapıldı!
@Composable
fun GoldRecordCard(
    record: RecordItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .border(1.dp, TextGray.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() } // Tüm kart ve ok işareti artık tetikleniyor!
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(record.color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, record.color, RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(record.material, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(" • ", color = TextGray)
                    Text(record.objectType, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${record.time} • ${record.details}",
                    color = TextGray, fontSize = 11.sp
                )
            }
        }
        Text("➔", color = TextGray, fontSize = 18.sp)
    }
}

// Çakışmayı önlemek için ismi tamamen benzersiz yapıldı!
@Composable
fun LibraryBottomNavItem(icon: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
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