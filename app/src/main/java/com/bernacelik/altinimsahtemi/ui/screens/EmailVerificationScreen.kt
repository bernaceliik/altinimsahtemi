package com.bernacelik.altinimsahtemi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    email: String = "can@ornek.com",
    onVerifySuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val codeLength = 6
    val codeValues = remember { mutableStateListOf(*Array(codeLength) { "" }) }
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }

    // Tekrar gönder sayacı (60 saniyeden geri sayım)
    var secondsLeft by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        } else {
            canResend = true
        }
    }

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

        // Yeşil Kalkan/Güvenlik İkonu
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF1B3D2F), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🛡️", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("E-postanı doğrula", color = White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("$email ", color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("adresine 6 haneli kod gönderdik.", color = TextGray, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6 Haneli Kod Giriş Kutucukları (OTP)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0 until codeLength) {
                OutlinedTextField(
                    value = codeValues[i],
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            codeValues[i] = value
                            // Karakter girildiyse otomatik sonraki kutuya odaklan
                            if (value.isNotEmpty() && i < codeLength - 1) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.85f)
                        .focusRequester(focusRequesters[i])
                        .border(
                            width = 1.dp,
                            color = if (codeValues[i].isNotEmpty()) GoldPrimary else TextGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        // İlk kutuya otomatik odaklanma
        LaunchedEffect(Unit) {
            focusRequesters[0].requestFocus()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kod Tekrar Gönderme Alanı
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kod gelmedi mi? ", color = TextGray, fontSize = 14.sp)
            Text(
                text = if (canResend) "Tekrar gönder" else "Tekrar gönder ($secondsLeft s)",
                color = if (canResend) GoldPrimary else TextGray.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(enabled = canResend) {
                    // Tekrar gönderme tetikleyicisi
                    secondsLeft = 60
                    canResend = false
                    Toast.makeText(context, "Doğrulama kodu yeniden gönderildi!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Doğrula Butonu
        Button(
            onClick = {
                val enteredCode = codeValues.joinToString("")
                if (enteredCode.length == codeLength) {
                    onVerifySuccess()
                } else {
                    Toast.makeText(context, "Lütfen 6 haneli kodun tamamını girin.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Doğrula",
                color = DarkBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}