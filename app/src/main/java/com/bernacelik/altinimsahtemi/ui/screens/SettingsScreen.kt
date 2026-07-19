package com.bernacelik.altinimsahtemi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToTab: (route: String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- DURUM YÖNETİMLERİ (STATES) ---
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var isAutoSyncEnabled by remember { mutableStateOf(true) }
    var currentLanguage by remember { mutableStateOf("Türkçe") } // "Türkçe" veya "English"
    var currentGain by remember { mutableStateOf(12) } // Varsayılan +12dB

    // Bottom Sheet ve Diyalog Kontrolleri
    var showCalibrationSheet by remember { mutableStateOf(false) }
    var showGainSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val selectedTab = "Ayarlar"

    // --- DİL ÇEVİRİ SÖZLÜĞÜ (Uygulama İçi Yerelleştirme) ---
    val isTr = currentLanguage == "Türkçe"
    val tSettings = if (isTr) "Ayarlar" else "Settings"
    val tHardware = if (isTr) "DONANIM" else "HARDWARE"
    val tPreferences = if (isTr) "TERCİHLER" else "PREFERENCES"
    val tPrivacySection = if (isTr) "GİZLİLİK" else "PRIVACY"
    val tMicCalib = if (isTr) "Mikrofon Kalibrasyonu" else "Microphone Calibration"
    val tGain = if (isTr) "Kazanç varsayılanı" else "Default Gain"
    val tNotifications = if (isTr) "Bildirimler" else "Notifications"
    val tAutoSync = if (isTr) "Otomatik senkronizasyon" else "Auto-Sync"
    val tLanguage = if (isTr) "Dil" else "Language"
    val tPrivacyPolicy = if (isTr) "Gizlilik politikası" else "Privacy Policy"
    val tDeleteAccount = if (isTr) "Hesabı sil" else "Delete Account"
    val tLogout = if (isTr) "Çıkış Yap" else "Sign Out"
    val tUpdated = if (isTr) "Güncel" else "Calibrated"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // İçerik Alanı
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Başlık
            Text(
                text = tSettings,
                color = White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 20.dp)
            )

            // Kullanıcı Profil Kartı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, TextGray.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .clickable { onNavigateToProfile() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("C", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Can Yılmaz", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("can@ornek.com", color = TextGray, fontSize = 12.sp)
                }
                Text("➔", color = TextGray, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DONANIM BÖLÜMÜ
            SettingsSectionHeader(tHardware)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = "🎙️",
                        title = tMicCalib,
                        modifier = Modifier.clickable { showCalibrationSheet = true },
                        rightContent = {
                            Text(tUpdated, color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    )
                    HorizontalDivider(color = DarkBackground, thickness = 1.dp)
                    SettingsRow(
                        icon = "🎚️",
                        title = tGain,
                        modifier = Modifier.clickable { showGainSheet = true },
                        rightContent = {
                            Text("+$currentGain dB", color = TextGray, fontSize = 12.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TERCİHLER BÖLÜMÜ
            SettingsSectionHeader(tPreferences)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(icon = "🔔", title = tNotifications, rightContent = {
                        Switch(
                            checked = isNotificationsEnabled,
                            onCheckedChange = {
                                isNotificationsEnabled = it
                                val msg = if (it) {
                                    if (isTr) "Bildirimler açıldı" else "Notifications enabled"
                                } else {
                                    if (isTr) "Bildirimler kapatıldı" else "Notifications disabled"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = GoldPrimary,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = DarkSurface
                            )
                        )
                    })
                    HorizontalDivider(color = DarkBackground, thickness = 1.dp)
                    SettingsRow(icon = "☁️", title = tAutoSync, rightContent = {
                        Switch(
                            checked = isAutoSyncEnabled,
                            onCheckedChange = {
                                isAutoSyncEnabled = it
                                val msg = if (it) {
                                    if (isTr) "Otomatik senkronizasyon aktif" else "Auto-sync enabled"
                                } else {
                                    if (isTr) "Otomatik senkronizasyon pasif" else "Auto-sync disabled"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = GoldPrimary,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = DarkSurface
                            )
                        )
                    })
                    HorizontalDivider(color = DarkBackground, thickness = 1.dp)
                    SettingsRow(
                        icon = "🌐",
                        title = tLanguage,
                        modifier = Modifier.clickable { showLanguageSheet = true },
                        rightContent = {
                            Text(currentLanguage, color = TextGray, fontSize = 12.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GIZLİLİK BÖLÜMÜ
            SettingsSectionHeader(tPrivacySection)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRow(
                        icon = "📄",
                        title = tPrivacyPolicy,
                        modifier = Modifier.clickable { showPrivacyDialog = true },
                        rightContent = {
                            Text("➔", color = TextGray, fontSize = 14.sp)
                        }
                    )
                    HorizontalDivider(color = DarkBackground, thickness = 1.dp)
                    SettingsRow(
                        icon = "🗑️",
                        title = tDeleteAccount,
                        titleColor = Color(0xFFF44336),
                        modifier = Modifier.clickable { showDeleteAccountDialog = true },
                        rightContent = {
                            Text("➔", color = TextGray, fontSize = 14.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Çıkış Yap Butonu
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.3f))
            ) {
                Text(tLogout, color = Color(0xFFF44336), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Alt Gezinme Barı (Bottom Navigation) - İstediğin gibi senkronize edildi!
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SettingsBottomNavItem(icon = "🏠", label = if (isTr) "Anasayfa" else "Home", isSelected = selectedTab == "Panel", onClick = { onNavigateToTab("home") })
                SettingsBottomNavItem(icon = "🎤", label = if (isTr) "Kayıt" else "Record", isSelected = selectedTab == "Kayıt", onClick = { onNavigateToTab("session_setup") })
                SettingsBottomNavItem(icon = "📂", label = if (isTr) "Kütüphane" else "Library", isSelected = selectedTab == "Kütüphane", onClick = { onNavigateToTab("library") })
                SettingsBottomNavItem(icon = "⚙️", label = if (isTr) "Ayarlar" else "Settings", isSelected = selectedTab == "Ayarlar", onClick = { onNavigateToTab("settings") })
            }
        }
    }

    // ==================== BOTTOM SHEETS & ALERTS ====================

    // 1. MİKROFON KALİBRASYONU PANELİ
    if (showCalibrationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCalibrationSheet = false },
            containerColor = DarkSurface,
            contentColor = White
        ) {
            var calibProgress by remember { mutableStateOf(0f) }
            var isCalibrating by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isTr) "Mikrofon Kalibrasyonu" else "Microphone Calibration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isTr) "Ortam gürültüsünü ölçmek and frekans sönümlenme testini optimize etmek için lütfen sessiz bir ortamda kalın."
                    else "Please remain quiet to let the device analyze background frequency.",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                LinearProgressIndicator(
                    progress = { calibProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = GoldPrimary,
                    trackColor = DarkBackground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isCalibrating) "${(calibProgress * 100).toInt()}%" else "Ready",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (!isCalibrating) {
                            isCalibrating = true
                            coroutineScope.launch {
                                for (i in 1..100) {
                                    delay(20)
                                    calibProgress = i / 100f
                                }
                                showCalibrationSheet = false
                                Toast.makeText(
                                    context,
                                    if (isTr) "Mikrofon kalibrasyonu başarıyla tamamlandı!" else "Microphone calibrated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(
                        text = if (isCalibrating) (if (isTr) "Kalibre Ediliyor..." else "Calibrating...") else (if (isTr) "Kalibrasyonu Başlat" else "Start Calibration"),
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 2. KAZANÇ VARSAYILANI PANELİ
    if (showGainSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGainSheet = false },
            containerColor = DarkSurface,
            contentColor = White
        ) {
            var sliderValue by remember { mutableStateOf(currentGain.toFloat()) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tGain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isTr) "Akustik kayıtlar esnasında mikrofondan alınacak kazanç desibel değerini manuel olarak tanımlayın."
                    else "Manually adjust default hardware decibel booster for recordings.",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "+${sliderValue.toInt()} dB",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..24f,
                    steps = 24,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldPrimary,
                        inactiveTrackColor = DarkBackground
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        currentGain = sliderValue.toInt()
                        showGainSheet = false
                        Toast.makeText(
                            context,
                            if (isTr) "Kazanç değeri +${currentGain}dB olarak ayarlandı." else "Gain updated to +${currentGain}dB.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(if (isTr) "Değeri Kaydet" else "Save Value", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 3. DİL SEÇİM PANELİ
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = DarkSurface,
            contentColor = White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = tLanguage,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentLanguage = "Türkçe"
                            showLanguageSheet = false
                            Toast.makeText(context, "Dil Türkçe olarak değiştirildi.", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Türkçe (TR)", color = White, fontSize = 16.sp)
                    if (currentLanguage == "Türkçe") {
                        Text("✓", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                HorizontalDivider(color = DarkBackground)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentLanguage = "English"
                            showLanguageSheet = false
                            Toast.makeText(context, "Language set to English.", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("English (EN)", color = White, fontSize = 16.sp)
                    if (currentLanguage == "English") {
                        Text("✓", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 4. GİZLİLİK POLİTİKASI DİYALOGU
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = DarkSurface,
            titleContentColor = White,
            textContentColor = TextGray,
            title = {
                Text(text = tPrivacyPolicy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (isTr) {
                            "1. AudioCollect, altınınızın çıkardığı akustik tını rezonanslarını analiz ederek, sistem kütüphanesindeki orijinal darphane kalıpları ile eşleştirir.\n\n" +
                                    "2. Kaydedilen hiçbir ses verisi rızanız dışında üçüncü şahıslarla paylaşılmaz and bulut yedeklemelerimiz uçtan uca şifrelenir.\n\n" +
                                    "3. [ÖNEMLİ UYARI]: Bu uygulama %100 oranında bir finansal veya fiziksel kesinlik taahhüt etmez. Akustik analiz, yüzey darbe açısı, mikrofon kalitesi and ortam gürültüsünden etkilenebilir. AudioCollect tını testine dayanarak herhangi bir altın alım-satım and ticaret riski almamanız önerilir. Kesin sonuçlar yalnızca uzman ayar evleri and kuyumcu darphanelerince tescil edilebilir."
                        } else {
                            "1. AudioCollect processes acoustic resonance patterns to predict the structural density and potential purity of precious metals.\n\n" +
                                    "2. Your captured data is locally computed and completely safe with secure custom cloud database integrations.\n\n" +
                                    "3. [CRITICAL WARNING]: This app cannot guarantee 100% legal authentication of precious metals. Environmental noise, impact point variations, and mic degradation can impact analysis results. Do not make commercial decisions based solely on acoustic diagnostic software."
                        },
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(if (isTr) "Okudum, Anladım" else "Dismiss", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 5. HESABI SİL ONAY KUTUSU
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = DarkSurface,
            titleContentColor = Color(0xFFF44336),
            textContentColor = TextGray,
            title = {
                Text(text = if (isTr) "Hesabınızı Silmek İstediğinize Emin misiniz?" else "Permanently Delete Account?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    text = if (isTr) "Bu işlem geri alınamaz. Kaydedilen tüm tını analizleriniz and kütüphane geçmişiniz sunuculardan kalıcı olarak silinecektir."
                            + " Emin olmanız durumunda 'Hesabı Sil' butonuna tıklayın."
                    else "This action is irreversible. All cached resonance sessions and library databases will be permanently destroyed.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(if (isTr) "Vazgeç" else "Cancel", color = White)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        Toast.makeText(
                            context,
                            if (isTr) "Hesabınız başarıyla silindi." else "Account deleted successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                        onLogout()
                    }
                ) {
                    Text(if (isTr) "Evet, Hesabımı Sil" else "Delete Permanent", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// --- YARDIMCI BİLEŞENLER ---

@Composable
fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        color = TextGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsRow(
    icon: String,
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = White,
    rightContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        rightContent()
    }
}

// Çakışma hatalarını kökten silmek için ismi "SettingsBottomNavItem" olarak kilitlendi!
@Composable
fun SettingsBottomNavItem(icon: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
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