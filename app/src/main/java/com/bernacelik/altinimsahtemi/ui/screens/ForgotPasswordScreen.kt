package com.bernacelik.altinimsahtemi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLinkSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        // Geri Butonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clickable { onNavigateBack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", color = White, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Geri", color = TextGray, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Yuvarlak Zarf İkonu
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✉", color = GoldPrimary, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Şifremi unuttum", color = White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sıfırlama bağlantısı göndereceğimiz e-postayı girin.",
            color = TextGray,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // E-posta Giriş Alanı
        Text("E-posta", color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("can@ornek.com", color = TextGray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = White,
                unfocusedTextColor = White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // UX Geliştirmesi: Bağlantı gönderildiğinde yeşil bilgilendirme metni çıkar
        if (isLinkSent) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✓ Sıfırlama bağlantısı başarıyla gönderildi! Lütfen gelen kutunuzu kontrol edin.",
                color = Color(0xFF4CAF50),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Gönder Butonu
        Button(
            onClick = {
                if (email.trim().isNotEmpty() && email.contains("@")) {
                    isLoading = true
                    isLinkSent = false
                    errorMessage = null
                    auth.sendPasswordResetEmail(email.trim())
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                isLinkSent = true
                                Toast.makeText(context, "Bağlantı e-postanıza gönderildi!", Toast.LENGTH_SHORT).show()
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Bağlantı gönderme başarısız."
                            }
                        }
                } else {
                    Toast.makeText(context, "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Sıfırlama bağlantısı gönder",
                    color = DarkBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}