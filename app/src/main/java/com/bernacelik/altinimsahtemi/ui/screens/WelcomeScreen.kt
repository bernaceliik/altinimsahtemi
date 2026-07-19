package com.bernacelik.altinimsahtemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.ui.theme.DarkBackground
import com.bernacelik.altinimsahtemi.ui.theme.GoldPrimary
import com.bernacelik.altinimsahtemi.ui.theme.White
import com.bernacelik.altinimsahtemi.ui.theme.TextGray

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Üst Kısım: Logo ve Uygulama Adı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GoldPrimary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("💎", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AudioCollect",
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Orta Kısım: Başlık ve Açıklama
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Hoş Geldiniz",
                color = White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Altın ses analizine başlamak için giriş yapın veya hesap oluşturun.",
                color = TextGray,
                fontSize = 16.sp
            )
        }

        // Alt Kısım: Butonlar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Giriş yap", color = DarkBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, TextGray, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hesap oluştur", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}