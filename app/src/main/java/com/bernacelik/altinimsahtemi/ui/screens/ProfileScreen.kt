package com.bernacelik.altinimsahtemi.ui.screens

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    recordCount: Int,
    isKuyumcu: Boolean,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val userRole = if (isKuyumcu) "Kuyumcu Üye" else "Bireysel Kullanıcı"
    val groupLabel = if (isKuyumcu) "Kuyumcu" else "Bireysel"

    var name by remember { mutableStateOf("Can Yılmaz") }
    var email by remember { mutableStateOf("can@ornek.com") }
    var phone by remember { mutableStateOf("+90 555 123 4567") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isTwoFactorEnabled by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) profileImageUri = uri }
    )

    val bitmap = remember(profileImageUri) {
        profileImageUri?.let { uri ->
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) { null }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("←", color = White, fontSize = 24.sp, modifier = Modifier.clickable { onNavigateBack() }.padding(end = 16.dp))
            Text("Profil Bilgileri", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                if (bitmap != null) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(96.dp).clip(CircleShape).border(2.dp, GoldPrimary, CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.size(96.dp).background(GoldPrimary, CircleShape), contentAlignment = Alignment.Center) {
                        Text(name.first().uppercase(), color = DarkBackground, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(name, color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(userRole, color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStatCard(modifier = Modifier.weight(1f), count = "$recordCount", label = "Toplam Tarama")
                ProfileStatCard(modifier = Modifier.weight(1f), count = groupLabel, label = "Üye Grubu")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("KİŞİSEL BİLGİLER", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EditableProfileInfoRow("Ad Soyad", name) { /* Düzenleme mantığı eklenebilir */ }
                HorizontalDivider(color = DarkBackground)
                EditableProfileInfoRow("E-posta", email) { /* Düzenleme mantığı eklenebilir */ }
                HorizontalDivider(color = DarkBackground)
                EditableProfileInfoRow("Telefon", phone) { /* Düzenleme mantığı eklenebilir */ }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("HESAP GÜVENLİĞİ", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                ProfileSettingsRow("🔒", "Şifre Değiştir") { /* Şifre değiştirme mantığı eklenebilir */ }
                HorizontalDivider(color = DarkBackground)
                ProfileSettingsRow("🛡️", "İki Adımlı Doğrulama", isTwoFactorEnabled) { isTwoFactorEnabled = !isTwoFactorEnabled }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DarkSurface), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.4f))) {
            Text("Oturumu Kapat", color = Color(0xFFF44336), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun ProfileStatCard(modifier: Modifier = Modifier, count: String, label: String) {
    Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(12.dp), modifier = modifier.border(1.dp, TextGray.copy(alpha = 0.05f), RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count, color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = TextGray, fontSize = 11.sp)
        }
    }
}

@Composable
fun EditableProfileInfoRow(label: String, value: String, onEditClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(label, color = TextGray, fontSize = 11.sp)
            Text(value, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("✏️", modifier = Modifier.clickable { onEditClick() })
    }
}

@Composable
fun ProfileSettingsRow(icon: String, title: String, isActive: Boolean? = null, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = White, fontSize = 15.sp)
        }
        if (isActive != null) Text(if (isActive) "Aktif" else "Devre Dışı", color = if (isActive) Color(0xFF4CAF50) else TextGray, fontSize = 12.sp)
        else Text("➔", color = TextGray)
    }
}