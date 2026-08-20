package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun GoogleSignInScreen(
    onSignInSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val gso = remember {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val webClientId = if (resId != 0) context.getString(resId) else "948401168902-mockwebclientid.apps.googleusercontent.com"
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            selectedAccount = account.displayName ?: (account.email ?: "Google Kullanıcısı")
            isLoading = true
            coroutineScope.launch {
                try {
                    val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                    val user = authResult.user
                    if (user != null) {
                        val db = FirebaseFirestore.getInstance()
                        val userDoc = db.collection("users").document(user.uid).get().await()
                        if (!userDoc.exists()) {
                            val profile = mapOf(
                                "uid" to user.uid,
                                "email" to (user.email ?: ""),
                                "role" to "User",
                                "isApproved" to true
                            )
                            db.collection("users").document(user.uid).set(profile).await()
                        }
                    }
                    isLoading = false
                    onSignInSuccess()
                } catch (e: Exception) {
                    isLoading = false
                    Toast.makeText(context, "Firebase Hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: ApiException) {
            isLoading = false
            Toast.makeText(context, "Google Hatası: ${e.localizedMessage} (Kod: ${e.statusCode})", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Arka Plandaki Karartı Bölümü
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Google ile Güvenli Giriş",
                color = TextGray,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Google Giriş Paneli (Bottom Sheet Tasarımı)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Panel Üst Çizgisi
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(TextGray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!isLoading) {
                    // Başlık ve Google Logosu Simgesi
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Bir hesap seçin",
                            color = White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "altinimsahtemi.com uygulamasına devam etmek için",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Kullanıcının Seçebileceği Hesaplar Listesi
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AccountRow(
                            name = "Google Hesabı Seç",
                            email = "Oturum açmak için dokunun",
                            initial = "G",
                            color = GoldPrimary,
                            onClick = {
                                launcher.launch(googleSignInClient.signInIntent)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // İptal Et Butonu
                    TextButton(onClick = onCancel) {
                        Text("İptal Et", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                } else {
                    // Giriş Yapılıyor Yüklenme Ekranı
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 40.dp)
                    ) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "$selectedAccount olarak giriş yapılıyor...",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Hesap Satırı Bileşeni
@Composable
fun AccountRow(
    name: String,
    email: String,
    initial: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profil Dairesi (Profil fotoğrafı yerine harf simgesi)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(name, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(email, color = TextGray, fontSize = 13.sp)
        }
    }
}