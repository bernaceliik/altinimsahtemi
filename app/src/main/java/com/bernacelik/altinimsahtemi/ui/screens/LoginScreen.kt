package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: (role: String, isApproved: Boolean, businessName: String, taxNumber: String) -> Unit,
    onNavigateToGoogleSignIn: () -> Unit,
    onNavigateToForgotPassword: () -> Unit // Yeni yönlendirme callback'imiz
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = remember { FirebaseAuth.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
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

        Spacer(modifier = Modifier.height(40.dp))

        Text("Giriş yap", color = White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Hesabınıza erişmek için bilgilerinizi girin.", color = TextGray, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

        Text("Şifre", color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
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

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "Şifremi unuttum",
                color = GoldPrimary,
                modifier = Modifier.clickable { onNavigateToForgotPassword() } // Burayı yeni parametreyle bağladık!
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Lütfen tüm alanları doldurun."
                    return@Button
                }
                isLoading = true
                errorMessage = null
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid
                            if (uid != null) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener { document ->
                                        isLoading = false
                                        if (document != null && document.exists()) {
                                            val role = document.getString("role") ?: "individual"
                                            val isApproved = document.getBoolean("isApproved") ?: true
                                            val businessName = document.getString("businessName") ?: ""
                                            val taxNumber = document.getString("taxNumber") ?: ""
                                            onLoginSuccess(role, isApproved, businessName, taxNumber)
                                        } else {
                                            onLoginSuccess("individual", true, "", "")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Kullanıcı verisi alınamadı: ${e.localizedMessage}"
                                    }
                            } else {
                                isLoading = false
                                onLoginSuccess("individual", true, "", "")
                            }
                        } else {
                            isLoading = false
                            errorMessage = task.exception?.localizedMessage ?: "Giriş başarısız."
                        }
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
                Text("Giriş yap", color = DarkBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = DarkSurface)
            Text("veya", color = TextGray, modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = DarkSurface)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("G Google ile devam et", color = White, fontSize = 16.sp)
        }
    }
}