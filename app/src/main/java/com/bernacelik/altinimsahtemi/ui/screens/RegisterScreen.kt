package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: (Boolean, String) -> Unit // DÜZENLEME: Buraya seçim durumunu ve e-postayı göndermek için parametreler güncellendi
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isJewelerSelected by remember { mutableStateOf(true) }
    var isDisclaimerChecked by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var businessName by remember { mutableStateOf("") }
    var taxNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

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

        Text("Hesap oluştur", color = White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Kuyumcu veya doğrulayıcı olarak kayıt olun.", color = TextGray, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Ad Soyad
        Text("Ad Soyad", color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(16.dp))

        // E-posta
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

        Spacer(modifier = Modifier.height(16.dp))

        // Şifre
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

        Spacer(modifier = Modifier.height(20.dp))

        // Rol Seçimi
        Text("Rol", color = TextGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        if (isJewelerSelected) GoldPrimary.copy(alpha = 0.15f) else DarkSurface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (isJewelerSelected) GoldPrimary else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { isJewelerSelected = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Kuyumcu",
                    color = if (isJewelerSelected) GoldPrimary else TextGray,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        if (!isJewelerSelected) GoldPrimary.copy(alpha = 0.15f) else DarkSurface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (!isJewelerSelected) GoldPrimary else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { isJewelerSelected = false },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Bireysel",
                    color = if (!isJewelerSelected) GoldPrimary else TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isJewelerSelected) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("İşletme Adı", color = TextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = businessName,
                onValueChange = { businessName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Can Kuyumculuk", color = TextGray) },
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

            Spacer(modifier = Modifier.height(16.dp))
            Text("Vergi Numarası", color = TextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = taxNumber,
                onValueChange = { taxNumber = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("1234567890", color = TextGray) },
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hukuki Feragatname Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isDisclaimerChecked,
                onCheckedChange = { isDisclaimerChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sonuçların %100 kesinlik taşımadığını, şüphe durumunda laboratuvar testi gerektiğini kabul ediyorum.",
                color = TextGray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || name.isBlank()) {
                    errorMessage = "Lütfen tüm alanları doldurun."
                    return@Button
                }
                if (!email.contains("@")) {
                    errorMessage = "Geçersiz e-posta adresi."
                    return@Button
                }
                if (password.length < 6) {
                    errorMessage = "Şifre en az 6 karakter olmalıdır."
                    return@Button
                }
                if (isJewelerSelected && (businessName.isBlank() || taxNumber.isBlank())) {
                    errorMessage = "Lütfen işletme adı ve vergi numarasını doldurun."
                    return@Button
                }

                isLoading = true
                errorMessage = null
                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            val uid = user?.uid
                            if (uid != null) {
                                val role = if (isJewelerSelected) "jeweler" else "individual"
                                val isApproved = !isJewelerSelected
                                
                                val userData = mutableMapOf<String, Any>(
                                    "uid" to uid,
                                    "name" to name.trim(),
                                    "email" to email.trim(),
                                    "role" to role,
                                    "isApproved" to isApproved,
                                    "createdAt" to System.currentTimeMillis()
                                )
                                if (isJewelerSelected) {
                                    userData["businessName"] = businessName.trim()
                                    userData["taxNumber"] = taxNumber.trim()
                                }
                                
                                db.collection("users").document(uid).set(userData)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onRegisterSuccess(isJewelerSelected, email.trim())
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Kullanıcı kaydı oluşturulamadı: ${e.localizedMessage}"
                                        user.delete()
                                    }
                            } else {
                                isLoading = false
                                errorMessage = "Kullanıcı kimliği alınamadı."
                            }
                        } else {
                            isLoading = false
                            errorMessage = task.exception?.localizedMessage ?: "Kayıt başarısız."
                        }
                    }
            },
            enabled = isDisclaimerChecked && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                disabledContainerColor = TextGray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(24.dp))
            } else {
                Text("Hesap oluştur", color = DarkBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}